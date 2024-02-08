package com.truemlgpro.wifiinfo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.clans.fab.FloatingActionButton;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.AppClipboardManager;
import com.truemlgpro.wifiinfo.utils.FontManager;
import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CellularDataIPActivity extends AppCompatActivity {
	private TextView textview_nocellconn;
	private CardView cardview_ip;
	private CardView cardview_local_ip;
	private TextView textview_public_ip_cell;
	private TextView textview_local_ipv4_cell;
	private TextView textview_local_ipv6_cell;
	private FloatingActionButton fab_update_ip;

	private BroadcastReceiver CellularDataConnectivityReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.cellular_data_ip_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		cardview_ip = (CardView) findViewById(R.id.cardview_ip);
		cardview_local_ip = (CardView) findViewById(R.id.cardview_local_ip);
		textview_public_ip_cell = (TextView) findViewById(R.id.textview_public_ip_cell);
		textview_local_ipv4_cell = (TextView) findViewById(R.id.textview_local_ipv4_cell_value);
		textview_local_ipv6_cell = (TextView) findViewById(R.id.textview_local_ipv6_cell_value);
		textview_nocellconn = (TextView) findViewById(R.id.textview_noconn);
		fab_update_ip = (FloatingActionButton) findViewById(R.id.fab_update_ip);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		FontManager.init(this, getApplicationContext(), true);
		initCopyableText();

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		actionbar.setTitle(getResources().getString(R.string.cellular_data_ip));

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		fab_update_ip.setOnClickListener(v -> new PublicIPAsyncTask().execute());

		checkCellularConnectivity();
	}

	private class PublicIPAsyncTask extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			fab_update_ip.setEnabled(false);
		}

		@Override
		protected String doInBackground(Void... params) {
			HttpURLConnection urlConnection = null;
			String ipAddress = null;
			try {
				URL url = new URL("https://public-ip-api.vercel.app/api/ip/");
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");

				int responseCode = urlConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					StringBuilder responseBuilder = new StringBuilder();
					InputStream inputStream = urlConnection.getInputStream();
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
						String line;
						while ((line = reader.readLine()) != null) {
							responseBuilder.append(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					ipAddress = responseBuilder.toString();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return ipAddress;
		}

		@Override
		protected void onPostExecute(String ipAddress) {
			if (ipAddress == null)
				ipAddress = getString(R.string.na);
			textview_public_ip_cell.setText(String.format(getString(R.string.your_ip), ipAddress));
			new Handler(Looper.getMainLooper()).postDelayed(() -> fab_update_ip.setEnabled(true), 5000);
		}
	}

	private class CellularDataConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkCellularConnectivity();
		}
	}

	private boolean isSimCardPresent(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
	}

	private void checkCellularConnectivity() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo cellularCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (isSimCardPresent(this) && Objects.nonNull(cellularCheck) && cellularCheck.isConnected()) {
			showWidgets();
			textview_local_ipv4_cell.setText(getCellularIPv4Address());
			textview_local_ipv6_cell.setText(getCellularIPv6Address());
		} else {
			textview_public_ip_cell.setText(getString(R.string.your_ip_na));
			textview_local_ipv4_cell.setText(getString(R.string.na));
			textview_local_ipv6_cell.setText(getString(R.string.na));
			hideWidgets();
		}
	}

	private String getCellularIPv4Address() {
		try {
			List<NetworkInterface> listNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : listNetworkInterfaces) {
				List<InetAddress> addrs = Collections.list(networkInterface.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
						return addr.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getString(R.string.na);
	}

	private String getCellularIPv6Address() {
		try {
			List<NetworkInterface> listNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : listNetworkInterfaces) {
				List<InetAddress> addrs = Collections.list(networkInterface.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress() && addr instanceof Inet6Address) {
						int index = String.valueOf(addr).indexOf("%");
						if (index != -1) {
							return Objects.requireNonNull(addr.getHostAddress()).substring(0, index-1);
						}
						return addr.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getString(R.string.na);
	}

	private void initCopyableText() {
		textview_public_ip_cell.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(this, getString(R.string.public_ip_address), textview_public_ip_cell.getText().toString());
			return true;
		});

		textview_local_ipv4_cell.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(this, getString(R.string.ipv4), textview_local_ipv4_cell.getText().toString());
			return true;
		});

		textview_local_ipv6_cell.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(this, getString(R.string.ipv6), textview_local_ipv6_cell.getText().toString());
			return true;
		});
	}

	private void showWidgets() {
		cardview_ip.setVisibility(View.VISIBLE);
		cardview_local_ip.setVisibility(View.VISIBLE);
		textview_nocellconn.setVisibility(View.GONE);
	}

	private void hideWidgets() {
		cardview_ip.setVisibility(View.GONE);
		cardview_local_ip.setVisibility(View.GONE);
		textview_nocellconn.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		CellularDataConnectivityReceiver = new CellularDataConnectivityReceiver();
		registerReceiver(CellularDataConnectivityReceiver, filter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(CellularDataConnectivityReceiver);
	}
}
