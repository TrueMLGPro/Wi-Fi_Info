package com.truemlgpro.wifiinfo;

import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.stealthcopter.networktools.*;
import java.net.*;
import java.util.*;
import me.anwarshahriar.calligrapher.*;

import android.support.v7.widget.Toolbar;

public class PortScannerActivity extends AppCompatActivity
{

	private Toolbar toolbar;
	private TextView textview_nonetworkconn;
	private EditText edittext_ip;
	private TextView ports_open_text;
	private Button port_scan_button;
	private Button port_scan_stop_button;
	private ListView listview_open_ports;
	
	private PortScan portScanner;

	private ArrayList<String> ports_arrayList;
	private ArrayAdapter<String> adapter;

	private BroadcastReceiver NetworkConnectivityReceiver;
	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private NetworkInfo CellularCheck;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Boolean keyTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, MainActivity.darkMode);
		Boolean keyAmoledTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, MainActivity.amoledMode);

		if (keyTheme == true) {
			setTheme(R.style.DarkTheme);
		}

		if (keyAmoledTheme == true) {
			if (keyTheme == true) {
				setTheme(R.style.AmoledDarkTheme);
			}
		}

		if (keyTheme == false) {
			setTheme(R.style.LightTheme);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.port_scanner_activity);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		edittext_ip = (EditText) findViewById(R.id.edittext_ip);
		ports_open_text = (TextView) findViewById(R.id.ports_open_text);
		port_scan_button = (Button) findViewById(R.id.port_scan_button);
		port_scan_stop_button = (Button) findViewById(R.id.port_scan_stop_button);
		listview_open_ports = (ListView) findViewById(R.id.listview_open_ports);

		ports_arrayList = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ports_arrayList);
		listview_open_ports.setAdapter(adapter);

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

		port_scan_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setEnabled(port_scan_stop_button, true);
					try {
						findOpenPorts();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					ports_open_text.setText("Ports Open: -");
					adapter.clear();
				}
			});
		
		port_scan_stop_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					portScanner.cancel();
					setEnabled(port_scan_button, true);
					setEnabled(port_scan_stop_button, false);
				}
		});

	}

	private void setEnabled(final View view, final boolean enabled) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (view != null) {
						view.setEnabled(enabled);
					}
				}
			});
    }

	private void addPortsToList(final String text) {
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adapter.add(text);
					adapter.notifyDataSetChanged();
				}
			});
	}
	
	private String getGateway() {
		if (!WiFiCheck.isConnected()) {
			return "0.0.0.0";
		}
		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifi.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}

	private void findOpenPorts() throws UnknownHostException {
        setEnabled(port_scan_button, false);
		
		String url_ip = edittext_ip.getText().toString();
		
		if (TextUtils.isEmpty(url_ip)) {
			url_ip = "google.com";
			Toast.makeText(getBaseContext(), "No IP or URL given...\nUsing Google URL: " + url_ip, Toast.LENGTH_LONG).show();
		}
		
        portScanner = PortScan.onAddress(url_ip).setTimeOutMillis(1000).setPortsAll().setMethodTCP().doScan(new PortScan.PortListener() {
				@Override
				public void onResult(int portNo, boolean open) {
					if (open) {
						String portNoString = String.valueOf(portNo);
						addPortsToList(portNoString);
					}
				}

				@Override
				public void onFinished(ArrayList<Integer> openPorts) {
					setEnabled(port_scan_button, true);
					setEnabled(port_scan_stop_button, false);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ports_open_text.setText("Ports Open: " + ports_arrayList.size());
						}
					});
				}
			});
    }

	class NetworkConnectivityReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			checkNetworkConnectivity();
		}
	}

	public void checkNetworkConnectivity() {
		CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		CellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		// WI-FI Connectivity Check

		if (WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			showWidgets();
			wifi_connected = true;
			cellular_connected = false;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			ports_open_text.setText("Ports Open: -");
			adapter.clear();
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}

		// Cellular Connectivity Check

		if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			showWidgets();
			wifi_connected = false;
			cellular_connected = true;
		} else if (!CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			ports_open_text.setText("Ports Open: -");
			adapter.clear();
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	public void showWidgets() {
		edittext_ip.setVisibility(View.VISIBLE);
		ports_open_text.setVisibility(View.VISIBLE);
		port_scan_button.setVisibility(View.VISIBLE);
		port_scan_stop_button.setVisibility(View.VISIBLE);
		listview_open_ports.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		edittext_ip.setVisibility(View.GONE);
		ports_open_text.setVisibility(View.GONE);
		port_scan_button.setVisibility(View.GONE);
		port_scan_stop_button.setVisibility(View.GONE);
		listview_open_ports.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStart()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new NetworkConnectivityReceiver();
		registerReceiver(NetworkConnectivityReceiver, filter);
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		unregisterReceiver(NetworkConnectivityReceiver);
		super.onStop();
	}

}
