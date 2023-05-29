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
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.stealthcopter.networktools.PortScan;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.anwarshahriar.calligrapher.Calligrapher;

public class PortScannerActivity extends AppCompatActivity {
	private TextView textview_nonetworkconn;
	private ProgressBar port_scanner_progress_bar;
	private TextInputLayout text_input_layout_ip;
	private TextInputLayout text_input_layout_threads;
	private TextInputLayout text_input_layout_ports;
	private EditText edittext_ip;
	private EditText edittext_threads;
	private EditText edittext_ports;
	private Spinner spinner_packet_types;
	private TextView ports_open_text;
	private TextView ports_closed_text;
	private Button port_scan_button;
	private Button port_scan_stop_button;
	private ListView listview_open_ports;

	private PortScan portScanner;

	private ArrayList<String> portsArrayList;
	private ArrayAdapter<String> adapter;

	private BroadcastReceiver NetworkConnectivityReceiver;

	private HandlerThread portScannerHandlerThread;
	private Handler portScannerHandler;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	private String url_ip = "";
	private String ports = "";
	private int threads = 8;
	private int closedPorts = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.port_scanner_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		port_scanner_progress_bar = (ProgressBar) findViewById(R.id.port_scanner_progress_bar);
		text_input_layout_ip = (TextInputLayout) findViewById(R.id.input_layout_ip);
		text_input_layout_threads = (TextInputLayout) findViewById(R.id.input_layout_threads);
		text_input_layout_ports = (TextInputLayout) findViewById(R.id.input_layout_ports);
		edittext_ip = (EditText) findViewById(R.id.edittext_ip);
		edittext_threads = (EditText) findViewById(R.id.edittext_threads);
		edittext_ports = (EditText) findViewById(R.id.edittext_ports);
		spinner_packet_types = (Spinner) findViewById(R.id.spinner_packet_types);
		ports_open_text = (TextView) findViewById(R.id.ports_open_text);
		ports_closed_text = (TextView) findViewById(R.id.ports_closed_text);
		port_scan_button = (Button) findViewById(R.id.port_scan_button);
		port_scan_stop_button = (Button) findViewById(R.id.port_scan_stop_button);
		listview_open_ports = (ListView) findViewById(R.id.listview_open_ports);

		portsArrayList = new ArrayList<>();
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, portsArrayList);
		listview_open_ports.setAdapter(adapter);

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

		port_scan_button.setOnClickListener(v -> {
			startPortScanner();
			ports_open_text.setText(getString(R.string.ports_open_none));
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
		int index = Collections.binarySearch(portsArrayList, text, portComparator);

		runOnUiThread(() -> {
			if (!portsArrayList.contains(text)) {
				portsArrayList.add((index < 0) ? (-index - 1) : index, text);
			}
			adapter.notifyDataSetChanged();
		});
	}

	public void sortListByPort() {
		Collections.sort(portsArrayList, (port1, port2) -> Integer.parseInt(port1) - Integer.parseInt(port2));
	}

	public boolean isPortRangeValid(String portRange) {
		String regex = "^(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5})(?:-(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5}))?(?:,(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5})(?:-(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5}))?|\\d{1,5}-\\d{1,5})*(?:,\\s*\\d{1,5}(?:-\\d{1,5})?)*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(portRange);

		if (matcher.matches()) {
			String[] ranges = portRange.replaceAll("[a-zA-Z]", "").split(",");

			for (String range : ranges) {
				if (range.contains("-")) {
					String[] numbers = range.split("-");
					for (int i = 0; i < numbers.length; i++) {
						numbers[i] = numbers[i].trim();
					}
					int firstPort = Integer.parseInt(numbers[0]);
					int secondPort = Integer.parseInt(numbers[1]);

					// Check if the first number is lower than the second number
					if (firstPort >= secondPort) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	private void startPortScanner() {
		setEnabled(port_scan_button, false);
		setEnabled(port_scan_stop_button, true);

		url_ip = edittext_ip.getText().toString();
		if (TextUtils.isEmpty(url_ip)) {
			url_ip = "google.com";
			edittext_ip.setText(url_ip);
		}

		if (TextUtils.isEmpty(edittext_threads.getText().toString()) || !isStringInt(edittext_threads.getText().toString()) || threads <= 0) {
			edittext_threads.setText("64");
		}
		threads = Integer.parseInt(edittext_threads.getText().toString());

		ports = edittext_ports.getText().toString().replaceAll("[a-zA-Z]", "");
		edittext_ports.setText(ports);
		if (TextUtils.isEmpty(ports) || !isPortRangeValid(ports)) {
			edittext_ports.setText("1-65535");
		}
		ports = edittext_ports.getText().toString();

		runOnUiThread(() -> port_scanner_progress_bar.setVisibility(View.VISIBLE));

		portScannerHandlerThread = new HandlerThread("BackgroundPortScannerHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
		portScannerHandlerThread.start();
		portScannerHandler = new Handler(portScannerHandlerThread.getLooper());

		portScannerHandler.post(() -> {
			int scanMethodIndex = spinner_packet_types.getSelectedItemPosition();
			try { // setMethod -> 0 (TCP), 1 (UDP)
				portScanner = PortScan.Companion.onAddress(url_ip).setTimeOutMillis(1000).setPorts(ports).setNoThreads(threads).setMethod(scanMethodIndex == 0 ? 0 : 1).doScan(new PortScan.PortListener() {
					@Override
					public void onResult(int portNo, boolean open) {
						String portNoString = String.valueOf(portNo);
						if (open) {
							addPortsToList(portNoString);
						} else {
							closedPorts++;
						}

						runOnUiThread(() -> {
							ports_open_text.setText(String.format(getString(R.string.ports_open_amount), portsArrayList.size()));
							ports_closed_text.setText(String.format(getString(R.string.ports_closed_amount), closedPorts));
						});
					}

					@Override
					public void onFinished(ArrayList<Integer> openPorts) {
						setEnabled(port_scan_button, true);
						setEnabled(port_scan_stop_button, false);
						runOnUiThread(() -> {
							ports_open_text.setText(String.format(getString(R.string.ports_open_amount), portsArrayList.size()));
							ports_closed_text.setText(String.format(getString(R.string.ports_closed_amount), closedPorts));
							sortListByPort();
							adapter.notifyDataSetChanged();
							port_scanner_progress_bar.setVisibility(View.INVISIBLE);
							closedPorts = 0;
						});
					}
				});
			} catch (UnknownHostException e) {
				e.printStackTrace();
				if (portScanner != null) {
					portScanner.cancel();
				}
				runOnUiThread(() -> port_scanner_progress_bar.setVisibility(View.INVISIBLE));
			}
		});
    }

	class NetworkConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkNetworkConnectivity();
		}
	}

	public void checkNetworkConnectivity() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo cellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifiCheck.isConnected() && !cellularCheck.isConnected()) { // WI-FI Connectivity Check
			showWidgets();
			wifi_connected = true;
			cellular_connected = false;
		} else if (cellularCheck.isConnected() && !wifiCheck.isConnected()) { // Cellular Connectivity Check
			showWidgets();
			wifi_connected = false;
			cellular_connected = true;
		} else if (!wifiCheck.isConnected() && !cellularCheck.isConnected()) {
			ports_open_text.setText(getString(R.string.ports_open_none));
			adapter.clear();
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	private boolean isStringInt(String s)  {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void showWidgets() {
		text_input_layout_ip.setVisibility(View.VISIBLE);
		text_input_layout_threads.setVisibility(View.VISIBLE);
		text_input_layout_ports.setVisibility(View.VISIBLE);
		edittext_ip.setVisibility(View.VISIBLE);
		edittext_threads.setVisibility(View.VISIBLE);
		edittext_ports.setVisibility(View.VISIBLE);
		spinner_packet_types.setVisibility(View.VISIBLE);
		ports_open_text.setVisibility(View.VISIBLE);
		ports_closed_text.setVisibility(View.VISIBLE);
		port_scan_button.setVisibility(View.VISIBLE);
		port_scan_stop_button.setVisibility(View.VISIBLE);
		listview_open_ports.setVisibility(View.VISIBLE);
		port_scanner_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		text_input_layout_ip.setVisibility(View.GONE);
		text_input_layout_threads.setVisibility(View.GONE);
		text_input_layout_ports.setVisibility(View.GONE);
		edittext_ip.setVisibility(View.GONE);
		edittext_threads.setVisibility(View.GONE);
		edittext_ports.setVisibility(View.GONE);
		spinner_packet_types.setVisibility(View.GONE);
		ports_open_text.setVisibility(View.GONE);
		ports_closed_text.setVisibility(View.GONE);
		port_scan_button.setVisibility(View.GONE);
		port_scan_stop_button.setVisibility(View.GONE);
		listview_open_ports.setVisibility(View.GONE);
		port_scanner_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new NetworkConnectivityReceiver();
		registerReceiver(NetworkConnectivityReceiver, filter);
	}

	@Override
	protected void onStop() {
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
