package com.truemlgpro.wifiinfo;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.clans.fab.FloatingActionButton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import me.anwarshahriar.calligrapher.Calligrapher;

public class CellularDataIPActivity extends AppCompatActivity {
	private TextView textview_nocellconn;
	private CardView cardview_ip;
	private CardView cardview_local_ip;
	private TextView textview_public_ip_cell;
	private TextView textview_local_ipv4_cell;
	private TextView textview_local_ipv6_cell;
	private FloatingActionButton fab_update_ip;

	private String publicIPFetched;
	private boolean siteReachable = false;

	private BroadcastReceiver CellularDataConnectivityReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());

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

		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, MainActivity.appFont);
		calligrapher.setFont(this, font, true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		fab_update_ip.setOnClickListener(v -> {
			fab_update_ip.setEnabled(false);
			PublicIPRunnable runnableIP = new PublicIPRunnable();
			new Thread(runnableIP).start();
		});

		checkCellularConnectivity();
	}

	private boolean isReachable(String url) {
		boolean reachable;
		int code;

		try {
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(3000);
			connection.connect();
			code = connection.getResponseCode();
			connection.disconnect();
			reachable = code == 200;
		} catch (Exception e) {
			reachable = false;
		}
		return reachable;
	}

	private String getPublicIPAddress() {
		String publicIP = "";
		try {
			Scanner scanner = new Scanner(new URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A");
			publicIP = scanner.next();
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return publicIP;
	}

	@SuppressWarnings("deprecation")
	class PublicIPRunnable implements Runnable {
		@Override
		public void run() {
			new AsyncTask<String, Void, Void>() {
				@Override
				protected Void doInBackground(String[] voids) {
					String url = "https://api.ipify.org";
					siteReachable = isReachable(url);
					if (siteReachable) {
						publicIPFetched = getPublicIPAddress();
					} else {
						publicIPFetched = getString(R.string.na);
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					textview_public_ip_cell.setText(String.format(getString(R.string.your_ip), publicIPFetched));
				}
			}.execute();

			Handler handlerEnableFAB = new Handler(Looper.getMainLooper());
			handlerEnableFAB.postDelayed(() -> fab_update_ip.setEnabled(true), 5000);
		}
	}

	private class CellularDataConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkCellularConnectivity();
		}
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

	@SuppressLint("SetTextI18n")
	private void checkCellularConnectivity() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo cellularCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (cellularCheck.isConnected()) {
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
		} catch (Exception ex) {
			ex.printStackTrace();
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return getString(R.string.na);
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
