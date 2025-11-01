package com.truemlgpro.wifiinfo.ui.activities;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.stealthcopter.networktools.PortScan;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.adapters.PortScannerAdapter;
import com.truemlgpro.wifiinfo.models.DiscoveredPort;
import com.truemlgpro.wifiinfo.utils.db.IANADatabaseHelper;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortScannerActivity extends AppCompatActivity {
	private MaterialTextView textview_nonetworkconn;
	private LinearProgressIndicator port_scanner_progress_bar;
	private TextInputLayout text_input_layout_ip;
	private TextInputLayout text_input_layout_threads;
	private TextInputLayout text_input_layout_ports;
	private EditText edittext_ip;
	private EditText edittext_threads;
	private EditText edittext_ports;
	private Spinner spinner_packet_types;
	private MaterialTextView ports_open_text;
	private MaterialTextView ports_closed_text;
	private MaterialButton port_scan_button;
	private MaterialButton port_scan_stop_button;
	private MaterialButton btn_advanced;
	private View group_advanced;
	private RecyclerView recyclerview_open_ports;

	private PortScan portScanner;
	private IANADatabaseHelper ianaDbHelper;

	private ArrayList<DiscoveredPort> portsArrayList;
	private PortScannerAdapter recyclerAdapter;

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private HandlerThread portScannerHandlerThread;
	private Handler portScannerHandler;

	private Handler mainHandler;
	private final Runnable doCheck = this::checkNetworkConnectivity;

	private Boolean wifi_connected = false;
	private Boolean cellular_connected = false;
	private Boolean advancedOpen = false;

	private String url_ip = "";
	private String ports = "";
	private int threads = 64;
	private int closedPorts = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.port_scanner_activity);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		textview_nonetworkconn = findViewById(R.id.textview_nonetworkconn);
		port_scanner_progress_bar = findViewById(R.id.port_scanner_progress_bar);
		text_input_layout_ip = findViewById(R.id.input_layout_ip);
		text_input_layout_threads = findViewById(R.id.input_layout_threads);
		text_input_layout_ports = findViewById(R.id.input_layout_ports);
		edittext_ip = findViewById(R.id.edittext_ip);
		edittext_threads = findViewById(R.id.edittext_threads);
		edittext_ports = findViewById(R.id.edittext_ports);
		spinner_packet_types = findViewById(R.id.spinner_packet_types);
		ports_open_text = findViewById(R.id.ports_open_text);
		ports_closed_text = findViewById(R.id.ports_closed_text);
		port_scan_button = findViewById(R.id.port_scan_button);
		port_scan_stop_button = findViewById(R.id.port_scan_stop_button);
		recyclerview_open_ports = findViewById(R.id.recyclerview_open_ports);
		btn_advanced = findViewById(R.id.btn_advanced);
		group_advanced = findViewById(R.id.group_advanced);

		portsArrayList = new ArrayList<>();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerview_open_ports.getContext(), linearLayoutManager.getOrientation());
		recyclerview_open_ports.addItemDecoration(dividerItemDecoration);
		recyclerview_open_ports.setLayoutManager(linearLayoutManager);
		recyclerAdapter = new PortScannerAdapter(portsArrayList);
		recyclerview_open_ports.setAdapter(recyclerAdapter);

		ianaDbHelper = new IANADatabaseHelper(this);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		toolbar.setNavigationOnClickListener(v -> finish());

		port_scan_button.setOnClickListener(v -> {
			startPortScanner();
			ports_open_text.setText(getString(R.string.ports_open_none));
			recyclerAdapter.clear();
		});

		port_scan_stop_button.setOnClickListener(v -> {
			if (portScanner != null) {
				portScanner.cancel();
			}
			setEnabled(port_scan_button, true);
			setEnabled(port_scan_stop_button, false);
		});

		btn_advanced.setOnClickListener(v -> {
			advancedOpen = !advancedOpen;
			group_advanced.setVisibility(advancedOpen ? View.VISIBLE : View.GONE);
			btn_advanced.setIconResource(advancedOpen ? R.drawable.arrow_drop_up_24px : R.drawable.arrow_drop_down_24px);
		});

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mainHandler = new Handler(Looper.getMainLooper());

		checkNetworkConnectivity();
		initInsets();
	}

	private void addPortsToList(final String port, final String portServiceName, final String portServiceDescription, final String portServiceProtocol) {
		DiscoveredPort discoveredPort = new DiscoveredPort(port, portServiceName, portServiceDescription, portServiceProtocol);
		Comparator<DiscoveredPort> portComparator = (itemOne, itemNext) -> Integer.parseInt(itemOne.openPort()) - Integer.parseInt(itemNext.openPort());
		int index = Collections.binarySearch(portsArrayList, discoveredPort, portComparator);
		int insertedItemPosition = (index < 0) ? (-index - 1) : index;

		runOnUiThread(() -> {
			if (!portsArrayList.contains(port)) {
				portsArrayList.add(insertedItemPosition, discoveredPort);
				recyclerAdapter.notifyItemInserted(insertedItemPosition);
				recyclerview_open_ports.smoothScrollToPosition(portsArrayList.size() - 1);
			}
		});
	}

	private void sortListByPort() {
		Collections.sort(portsArrayList, (itemOne, itemNext) -> Integer.parseInt(itemOne.openPort()) - Integer.parseInt(itemNext.openPort()));
	}

	private boolean isPortRangeValid(String portRange) {
		String regex = "^(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5})(?:-(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5}))?(?:,(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5})(?:-(?:6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[0-5]\\d{4}|\\d{1,5}))?|\\d{1,5}-\\d{1,5})*(?:,\\s*\\d{1,5}(?:-\\d{1,5})?)*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(portRange);

		if (matcher.matches()) {
			String[] ranges = portRange.split(",");

			for (String range : ranges) {
				if (range.contains("-")) {
					String[] numbers = range.split("-");
					for (int i = 0; i < numbers.length; i++) {
						numbers[i] = numbers[i].trim();
					}
					int firstPort = Integer.parseInt(numbers[0]);
					int secondPort = Integer.parseInt(numbers[1]);

					if (firstPort >= secondPort) {
						return false;
					}
				} else {
					int singlePort = Integer.parseInt(range);
					if (singlePort > 65535) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	private void startPortScanner() {
		String threads_string = edittext_threads.getText().toString();

		disableViews();

		url_ip = NetworkUtils.extractDomain(edittext_ip.getText().toString());
		if (TextUtils.isEmpty(url_ip)) {
			if (wifi_connected) {
				url_ip = NetworkUtils.getGatewayIP(PortScannerActivity.this);
			} else if (cellular_connected) {
				url_ip = "google.com";
			}
		}
		edittext_ip.setText(url_ip);

		if (TextUtils.isEmpty(threads_string) || !isStringInt(threads_string) || threads <= 0) {
			threads_string = "64";
			edittext_threads.setText(threads_string);
		}
		threads = Integer.parseInt(threads_string);

		ports = edittext_ports.getText().toString().replaceAll("[a-zA-Z]", "");
		if (TextUtils.isEmpty(ports) || !isPortRangeValid(ports)) {
			ports = "1-65535";
			edittext_ports.setText(ports);
		}

		runOnUiThread(() -> port_scanner_progress_bar.setVisibility(View.VISIBLE));

		portScannerHandlerThread = new HandlerThread("PortScannerBackgroundHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
		portScannerHandlerThread.start();
		portScannerHandler = new Handler(portScannerHandlerThread.getLooper());

		portScannerHandler.post(() -> {
			int scanMethodIndex = spinner_packet_types.getSelectedItemPosition();
			String scanProtocol = spinner_packet_types.getSelectedItem().toString();
			try { // setMethod -> 0 (TCP), 1 (UDP)
				portScanner = PortScan.Companion.onAddress(url_ip)
						.setTimeOutMillis(1000)
						.setPorts(ports)
						.setNoThreads(threads)
						.setMethod(scanMethodIndex)
						.doScan(new PortScan.PortListener() {
							@Override
							public void onResult(int portNo, boolean open) {
								String portNoString = String.valueOf(portNo);
								if (open) {
									addPortsToList(
											portNoString,
											ianaDbHelper.getServiceName(portNo, scanProtocol.toLowerCase()),
											ianaDbHelper.getServiceDescription(portNo, scanProtocol.toLowerCase()),
											scanProtocol);
									runOnUiThread(() -> ports_open_text.setText(String.format(getString(R.string.ports_open_amount), portsArrayList.size())));
								} else {
									closedPorts++;
									runOnUiThread(() -> ports_closed_text.setText(String.format(getString(R.string.ports_closed_amount), closedPorts)));
								}
							}

							@SuppressLint("NotifyDataSetChanged")
							@Override
							public void onFinished(ArrayList<Integer> openPorts) {
								enableViews();
								runOnUiThread(() -> {
									ports_open_text.setText(String.format(getString(R.string.ports_open_amount), portsArrayList.size()));
									ports_closed_text.setText(String.format(getString(R.string.ports_closed_amount), closedPorts));
									sortListByPort();
									recyclerAdapter.notifyDataSetChanged();
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
				enableViews();
			}
		});
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_portscan);
		LinearLayout linear_layout_root = findViewById(R.id.linear_layout_root_ports);

		InsetsController.setInsets(
				app_bar_layout,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_TOP | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);

		InsetsController.setInsets(
				linear_layout_root,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_BOTTOM | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerNetworkCallback();
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
		unregisterNetworkCallback();
	}

	private void registerNetworkCallback() {
		if (connectivityManager == null) return;

		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override public void onAvailable(Network network) { scheduleCheck(); }
			@Override public void onLost(Network network) { scheduleCheck(); }
			@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
		};

		NetworkRequest request = new NetworkRequest.Builder()
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
				.build();

		try {
			if (Build.VERSION.SDK_INT >= 26) {
				connectivityManager.registerNetworkCallback(request, networkCallback, mainHandler);
			} else {
				connectivityManager.registerNetworkCallback(request, networkCallback);
			}
		} catch (Exception ignored) { }
	}

	private void unregisterNetworkCallback() {
		if (connectivityManager != null && networkCallback != null) {
			try {
				connectivityManager.unregisterNetworkCallback(networkCallback);
			} catch (Exception ignored) { }
			networkCallback = null;
		}
		if (mainHandler != null) mainHandler.removeCallbacks(doCheck);
	}

	private void scheduleCheck() {
		if (mainHandler == null) return;
		mainHandler.removeCallbacks(doCheck);
		mainHandler.postDelayed(doCheck, 100);
	}

	private void checkNetworkConnectivity() {
		boolean wifi = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.WIFI, false);
		boolean cell = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.CELLULAR, false);

		if (wifi || cell) {
			showWidgets();
			wifi_connected = wifi;
			cellular_connected = !wifi && cell;
		} else {
			ports_open_text.setText(getString(R.string.ports_open_none));
			recyclerAdapter.clear();
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

	private void showWidgets() {
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
		recyclerview_open_ports.setVisibility(View.VISIBLE);
		port_scanner_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	private void hideWidgets() {
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
		recyclerview_open_ports.setVisibility(View.GONE);
		port_scanner_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> { if (view != null) view.setEnabled(enabled); });
	}

	private void enableViews() {
		setEnabled(port_scan_button, true);
		setEnabled(port_scan_stop_button, false);
	}

	private void disableViews() {
		setEnabled(port_scan_button, false);
		setEnabled(port_scan_stop_button, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recyclerview_open_ports.setAdapter(null);
	}
}
