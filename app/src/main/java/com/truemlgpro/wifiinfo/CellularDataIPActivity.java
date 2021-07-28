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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

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

public class CellularDataIPActivity extends AppCompatActivity
{
	
	private Toolbar toolbar;
	private CardView cardview_ip;
	private CardView cardview_local_ip;
	private TextView textview_ip_cell;
	private TextView textview_nocellconn;
	private TextView textview_local_ipv4_cell;
	private FloatingActionButton fab_update_ip;
	private ConnectivityManager CM;
	private NetworkInfo CellularCheck;
	private String publicIPFetched;
	private boolean siteReachable = false;
	private Scanner scanner;
	private BroadcastReceiver CellularDataConnectivityReceiver;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Boolean keyTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, MainActivity.darkMode);
		Boolean keyAmoledTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, MainActivity.amoledMode);

		if (keyTheme) {
			setTheme(R.style.DarkTheme);
		}

		if (keyAmoledTheme) {
			if (keyTheme) {
				setTheme(R.style.AmoledDarkTheme);
			}
		}

		if (!keyTheme) {
			setTheme(R.style.LightTheme);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cellular_data_ip_activity);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		cardview_ip = (CardView) findViewById(R.id.cardview_ip);
		cardview_local_ip = (CardView) findViewById(R.id.cardview_local_ip);
		textview_ip_cell = (TextView) findViewById(R.id.textview_ip_cell);
		textview_local_ipv4_cell = (TextView) findViewById(R.id.textview_local_ipv4_cell);
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

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Back button pressed
				finish();
			}
		});
		
		fab_update_ip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fab_update_ip.setEnabled(false);
				PublicIPRunnable runnableIP = new PublicIPRunnable();
				new Thread(runnableIP).start();
			}
		});
			
		checkCellularConnectivity();
	}
	
	public boolean isReachable(String url) throws IOException {
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
			scanner = new Scanner(new URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A");
			publicIP = scanner.next();
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return publicIP;
	}

	class PublicIPRunnable implements Runnable {
		@Override
		public void run() {
			new AsyncTask<String, Void, Void>() {
				@Override
				protected Void doInBackground(String[] voids) {
					publicIPFetched = getPublicIPAddress();
					String url_ip = "https://api.ipify.org";
					try {
						if (isReachable(url_ip)) {
							siteReachable = true;
						} else {
							siteReachable = false;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					if (siteReachable == true) {
						textview_ip_cell.setText("Your IP: " + publicIPFetched);
					}

					if (siteReachable == false) {
						textview_ip_cell.setText("Your IP: N/A");
					}
				}
			}.execute();

			Handler handlerEnableFAB = new Handler(Looper.getMainLooper());
			handlerEnableFAB.postDelayed(new Runnable() {
					@Override
					public void run() {
						fab_update_ip.setEnabled(true);
					}
				}, 5000);
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
		CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		CellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (CellularCheck.isConnected()) {
			showWidgets();
			textview_local_ipv4_cell.setText("IPv4: " + getCellularLocalIPv4Address());
		} else {
			textview_local_ipv4_cell.setText("IPv4: N/A");
			textview_ip_cell.setText("Your IP: N/A");
			hideWidgets();
		}
	}
	
	public static String getCellularLocalIPv4Address() {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
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

	@Override
	protected void onStart()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		CellularDataConnectivityReceiver = new CellularDataConnectivityReceiver();
		registerReceiver(CellularDataConnectivityReceiver, filter);
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		unregisterReceiver(CellularDataConnectivityReceiver);
		super.onStop();
	}
	
}
