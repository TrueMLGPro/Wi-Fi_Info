package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.stealthcopter.networktools.PortScan;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import me.anwarshahriar.calligrapher.Calligrapher;

public class PortScannerActivity extends AppCompatActivity
{

	private TextView textview_nonetworkconn;
	private TextInputLayout text_input_layout_ip;
	private TextInputLayout text_input_layout_threads;
	private EditText edittext_ip;
	private EditText edittext_threads;
	private Spinner spinner_packet_types;
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

	private HandlerThread portScannerHandlerThread;
	private Handler portScannerHandler;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	private String url_ip = "";
	private int threads = 8;

	@Override
	protected void onCreate(Bundle savedInstanceState)
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
		setContentView(R.layout.port_scanner_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		text_input_layout_ip = (TextInputLayout) findViewById(R.id.input_layout_ip);
		text_input_layout_threads = (TextInputLayout) findViewById(R.id.input_layout_threads);
		edittext_ip = (EditText) findViewById(R.id.edittext_ip);
		edittext_threads = (EditText) findViewById(R.id.edittext_threads);
		spinner_packet_types = (Spinner) findViewById(R.id.spinner_packet_types);
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

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		port_scan_button.setOnClickListener(v -> {
			setEnabled(port_scan_stop_button, true);
			startPortScanner();
			ports_open_text.setText("Ports Open: -");
			adapter.clear();
		});
		
		port_scan_stop_button.setOnClickListener(v -> {
			if (portScanner != null) {
				portScanner.cancel();
			}
			setEnabled(port_scan_button, true);
			setEnabled(port_scan_stop_button, false);
		});

	}

	private void setEnabled(final View view, final boolean enabled) {
        runOnUiThread(() -> {
	        if (view != null) {
		        view.setEnabled(enabled);
	        }
        });
    }

	private void addPortsToList(final String text) {
		Comparator<String> portComparator = (port1, port2) -> Integer.parseInt(port1) - Integer.parseInt(port2);
		int index = Collections.binarySearch(ports_arrayList, text, portComparator);

		runOnUiThread(() -> {
			if (!ports_arrayList.contains(text)) {
				ports_arrayList.add((index < 0) ? (-index - 1) : index, text);
			}
			adapter.notifyDataSetChanged();
		});
	}

	public void sortListByPort() {
		Collections.sort(ports_arrayList, (port1, port2) -> Integer.parseInt(port1) - Integer.parseInt(port2));
	}

	private void startPortScanner() {
        setEnabled(port_scan_button, false);
		
		url_ip = edittext_ip.getText().toString();
		threads = Integer.parseInt(edittext_threads.getText().toString());
		
		if (TextUtils.isEmpty(url_ip)) {
			url_ip = "google.com";
			Toast.makeText(getBaseContext(), "No IP or URL given...\nUsing Google URL: " + url_ip, Toast.LENGTH_LONG).show();
		}

		if (TextUtils.isEmpty(edittext_threads.getText().toString()) || threads <= 0) {
			threads = 8;
		}

		portScannerHandlerThread = new HandlerThread("BackgroundPortScannerHandlerThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		portScannerHandlerThread.start();
		portScannerHandler = new Handler(portScannerHandlerThread.getLooper());

		portScannerHandler.post(() -> {
			String packet_type = spinner_packet_types.getSelectedItem().toString();
			if (packet_type.equals("TCP")) {
				try {
					portScanner = PortScan.onAddress(url_ip).setTimeOutMillis(1000).setPortsAll().setNoThreads(threads).setMethodTCP().doScan(new PortScan.PortListener() {
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
							runOnUiThread(() -> {
								ports_open_text.setText("Ports Open: " + ports_arrayList.size());
								sortListByPort();
								adapter.notifyDataSetChanged();
							});
						}
					});
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			} else if (packet_type.equals("UDP")) {
				try {
					portScanner = PortScan.onAddress(url_ip).setTimeOutMillis(1000).setPortsAll().setNoThreads(threads).setMethodUDP().doScan(new PortScan.PortListener() {
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
							runOnUiThread(() -> {
								ports_open_text.setText("Ports Open: " + ports_arrayList.size());
								sortListByPort();
								adapter.notifyDataSetChanged();
							});
						}
					});
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
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
		text_input_layout_ip.setVisibility(View.VISIBLE);
		text_input_layout_threads.setVisibility(View.VISIBLE);
		edittext_ip.setVisibility(View.VISIBLE);
		edittext_threads.setVisibility(View.VISIBLE);
		spinner_packet_types.setVisibility(View.VISIBLE);
		ports_open_text.setVisibility(View.VISIBLE);
		port_scan_button.setVisibility(View.VISIBLE);
		port_scan_stop_button.setVisibility(View.VISIBLE);
		listview_open_ports.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		text_input_layout_ip.setVisibility(View.GONE);
		text_input_layout_threads.setVisibility(View.GONE);
		edittext_ip.setVisibility(View.GONE);
		edittext_threads.setVisibility(View.GONE);
		spinner_packet_types.setVisibility(View.GONE);
		ports_open_text.setVisibility(View.GONE);
		port_scan_button.setVisibility(View.GONE);
		port_scan_stop_button.setVisibility(View.GONE);
		listview_open_ports.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new NetworkConnectivityReceiver();
		registerReceiver(NetworkConnectivityReceiver, filter);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (portScanner != null) {
			portScanner.cancel();
		}
		if (portScannerHandlerThread != null) {
			portScannerHandlerThread.quit();
			portScannerHandlerThread = null;
		}
		if (portScannerHandler != null) {
			portScannerHandler.removeCallbacks(null);
			portScannerHandler.getLooper().quit();
		}
		unregisterReceiver(NetworkConnectivityReceiver);
	}
}
