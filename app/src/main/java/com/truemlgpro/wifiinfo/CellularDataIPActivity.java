package com.truemlgpro.wifiinfo;

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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import me.anwarshahriar.calligrapher.Calligrapher;

public class CellularDataIPActivity extends AppCompatActivity {
	private TextView textview_nocellconn;
	private CardView cardview_ip;
	private CardView cardview_local_ip;
	private TextView textview_ip_cell;
	private TextView textview_header_local_ip;
	private TextView textview_local_ip_cell;
	private FloatingActionButton fab_update_ip;
	private String publicIPFetched;
	private boolean siteReachable = false;

	private BroadcastReceiver CellularDataConnectivityReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		new ThemeManager().initializeThemes(this, getApplicationContext());
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cellular_data_ip_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		cardview_ip = (CardView) findViewById(R.id.cardview_ip);
		cardview_local_ip = (CardView) findViewById(R.id.cardview_local_ip);
		textview_ip_cell = (TextView) findViewById(R.id.textview_ip_cell);
		textview_header_local_ip = (TextView) findViewById(R.id.textview_header_local_ip);
		textview_local_ip_cell = (TextView) findViewById(R.id.textview_local_ip_cell);
		textview_nocellconn = (TextView) findViewById(R.id.textview_noconn);
		fab_update_ip = (FloatingActionButton) findViewById(R.id.fab_update_ip);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
		boolean reachable = false;
		int code;

		try {
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(3000);
			connection.connect();
			code = connection.getResponseCode();
			connection.disconnect();
			if (code == 200) {
				reachable = true;
			} else {
				reachable = false;
			}
		} catch (Exception e) {
			reachable = false;
		}
		return reachable;
	}
	
	public String getPublicIPAddress() {
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
						publicIPFetched = "N/A";
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					textview_ip_cell.setText("Your IP: " + publicIPFetched);
				}
			}.execute();

			Handler handlerEnableFAB = new Handler(Looper.getMainLooper());
			handlerEnableFAB.postDelayed(() -> fab_update_ip.setEnabled(true), 5000);
		}
	}
	
	class CellularDataConnectivityReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			checkCellularConnectivity();
		}
	}
	
	public void showWidgets() {
		cardview_ip.setVisibility(View.VISIBLE);
		cardview_local_ip.setVisibility(View.VISIBLE);
		textview_nocellconn.setVisibility(View.GONE);
	}
	
	public void hideWidgets() {
		cardview_ip.setVisibility(View.GONE);
		cardview_local_ip.setVisibility(View.GONE);
		textview_nocellconn.setVisibility(View.VISIBLE);
	}
	
	public void checkCellularConnectivity() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo CellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (CellularCheck.isConnected()) {
			showWidgets();
			if (isAnIPv4Addr(getCellularLocalIPAddress())) {
				textview_header_local_ip.setText("IPv4");
				textview_local_ip_cell.setText("IPv4: " + getCellularLocalIPAddress());
			} else {
				textview_header_local_ip.setText("IPv6");
				textview_local_ip_cell.setText("IPv6: " + getCellularLocalIPAddress());
			}
		} else {
			textview_local_ip_cell.setText("IP: N/A");
			textview_ip_cell.setText("Your IP: N/A");
			hideWidgets();
		}
	}
	
	public static String getCellularLocalIPAddress() {
		try {
			List<NetworkInterface> listNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : listNetworkInterfaces) {
				List<InetAddress> addrs = Collections.list(networkInterface.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						return addr.getHostAddress();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	private boolean isAnIPv4Addr(String ipAddr) {
		boolean isIPv4 = false;
		String IPV4_REGEX = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
		/* The following types of addresses match this RegEx pattern:
			IPv6 addresses
			zero compressed IPv6 addresses (section 2.2 of RFC5952)
			link-local IPv6 addresses with zone index (section 11 of RFC4007)
			IPv4-Embedded IPv6 Address (section 2 of RFC6052)
			IPv4-mapped IPv6 addresses (section 2.1 of RFC2765)
			IPv4-translated addresses (section 2.1 of RFC2765)
		*/
		String IPV6_REGEX = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
				"^::(?:[0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +
				"^[0-9a-fA-F]{1,4}::(?:[0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}$|" +
				"^[0-9a-fA-F]{1,4}:[0-9a-fA-F]{1,4}::(?:[0-9a-fA-F]{1,4}:){0,4}[0-9a-fA-F]{1,4}$|" +
				"^(?:[0-9a-fA-F]{1,4}:){0,2}[0-9a-fA-F]{1,4}::(?:[0-9a-fA-F]{1,4}:){0,3}[0-9a-fA-F]{1,4}$|" +
				"^(?:[0-9a-fA-F]{1,4}:){0,3}[0-9a-fA-F]{1,4}::(?:[0-9a-fA-F]{1,4}:){0,2}[0-9a-fA-F]{1,4}$|" +
				"^(?:[0-9a-fA-F]{1,4}:){0,4}[0-9a-fA-F]{1,4}::(?:[0-9a-fA-F]{1,4}:)?[0-9a-fA-F]{1,4}$|" +
				"^(?:[0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}::[0-9a-fA-F]{1,4}$|" +
				"^(?:[0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}::$";

		if (ipAddr.matches(IPV4_REGEX)) {
			isIPv4 = true;
		} else if (ipAddr.matches(IPV6_REGEX)) {
			isIPv4 = false;
		}
		return isIPv4;
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		CellularDataConnectivityReceiver = new CellularDataConnectivityReceiver();
		registerReceiver(CellularDataConnectivityReceiver, filter);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		unregisterReceiver(CellularDataConnectivityReceiver);
	}
}
