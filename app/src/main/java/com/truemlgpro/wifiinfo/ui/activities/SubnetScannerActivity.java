package com.truemlgpro.wifiinfo.ui.activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
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
import com.stealthcopter.networktools.SubnetDevices;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.adapters.SubnetScannerAdapter;
import com.truemlgpro.wifiinfo.models.DeviceInfoParcel;
import com.truemlgpro.wifiinfo.models.SubnetDevice;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.db.OUIDatabaseHelper;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubnetScannerActivity extends AppCompatActivity {
	private MaterialToolbar toolbar;
	private MaterialTextView textview_nonetworkconn;
	private LinearProgressIndicator subnet_scanner_progress_bar;
	private MaterialTextView local_ip_text;
	private MaterialTextView devices_found_text;
	private MaterialButton subnet_scan_button;
	private MaterialButton subnet_scan_cancel_button;
	private TextInputLayout text_input_layout_timeout;
	private TextInputLayout text_input_layout_threads;
	private EditText edittext_timeout;
	private EditText edittext_threads;
	private RecyclerView recyclerview_subnet_devices;

	private ArrayList<SubnetDevice> devicesArrayList;
	private static SubnetScannerAdapter recyclerAdapter;

	private SubnetDevices.DiscoverySession subnetScanner;
	private OUIDatabaseHelper ouiDbHelper;
	private final ConcurrentHashMap<String, SubnetDevices.NetworkDeviceInfo> infoByIp = new ConcurrentHashMap<>();
	private volatile boolean isScanning = false;

	private int stageIndex = 1;
	private int stageCount = 1;
	private String stageName = "";
	private int lastDone = 0;
	private int lastTotal = 0;

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private Handler mainHandler;
	private final Runnable doCheck = this::checkConnectivity;

	private static Boolean wifi_connected;
	private static Boolean cellular_connected;
	private String localWifiIp;
	private String localCellIp;

	private int threads = 256;
	private int timeout = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.subnet_scanner_activity);

		stageName = getString(R.string.subnet_scanner_scan_stage);

		toolbar = findViewById(R.id.toolbar);
		textview_nonetworkconn = findViewById(R.id.textview_nonetworkconn);
		subnet_scanner_progress_bar = findViewById(R.id.subnet_scanner_progress_bar);
		local_ip_text = findViewById(R.id.local_ip_text);
		devices_found_text = findViewById(R.id.devices_found_text);
		subnet_scan_button = findViewById(R.id.subnet_scan_button);
		subnet_scan_cancel_button = findViewById(R.id.subnet_scan_cancel_button);
		text_input_layout_timeout = findViewById(R.id.input_layout_timeout_subnet_scanner);
		text_input_layout_threads = findViewById(R.id.input_layout_threads_subnet_scanner);
		edittext_timeout = findViewById(R.id.edittext_timeout_subnet_scanner);
		edittext_threads = findViewById(R.id.edittext_threads_subnet_scanner);
		recyclerview_subnet_devices = findViewById(R.id.recyclerview_subnet_devices);

		devicesArrayList = new ArrayList<>();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerview_subnet_devices.getContext(), linearLayoutManager.getOrientation());
		recyclerview_subnet_devices.addItemDecoration(dividerItemDecoration);
		recyclerview_subnet_devices.setLayoutManager(linearLayoutManager);
		recyclerAdapter = new SubnetScannerAdapter(devicesArrayList, this);
		recyclerview_subnet_devices.setAdapter(recyclerAdapter);

		recyclerAdapter.setOnItemClickListener(item -> openDeviceDetails(item.getIP()));
		ouiDbHelper = new OUIDatabaseHelper(this);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		toolbar.setNavigationOnClickListener(v -> finish());

		subnet_scan_button.setOnClickListener(v -> {
			startSubnetScanner();
			devices_found_text.setText(getString(R.string.devices_found_na));
			recyclerAdapter.clear();
		});

		subnet_scan_cancel_button.setOnClickListener(v -> {
			if (subnetScanner != null) subnetScanner.cancel();
			isScanning = false;
			toolbar.setSubtitle(null);
			setEnabled(subnet_scan_button, true);
			setEnabled(subnet_scan_cancel_button, false);
		});

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mainHandler = new Handler(Looper.getMainLooper());

		checkConnectivity();
		initInsets();
	}

	private void openDeviceDetails(String ip) {
		SubnetDevices.NetworkDeviceInfo full = infoByIp.get(ip);

		// Fallback to minimal item if not yet in full map
		SubnetDevice minimal = null;
		for (SubnetDevice sd : devicesArrayList) {
			if (sd.getIP().equals(ip)) { minimal = sd; break; }
		}

		DeviceInfoParcel parcel = DeviceInfoParcel.from(full, minimal);
		if (parcel == null) return;

		if (parcel.deviceName == null || parcel.deviceName.trim().isEmpty()) {
			if (full != null) {
				String best = bestNameFor(full);
				if (best != null && !best.trim().isEmpty()) {
					parcel.deviceName = best.trim();
				}
			}
		}

		Intent intent = new Intent(this, DeviceDetailsActivity.class);
		intent.putExtra(DeviceDetailsActivity.EXTRA_DETAILS, parcel);
		startActivity(intent);
	}

	@NonNull
	private static Long convertDiscoveredIPToLong(String ip) {
		try {
			InetAddress addr = InetAddress.getByName(ip);
			if (addr instanceof Inet4Address) {
				byte[] b = addr.getAddress();
				return ((b[0] & 0xffL) << 24)
						| ((b[1] & 0xffL) << 16)
						| ((b[2] & 0xffL) << 8)
						|  (b[3] & 0xffL);
			} else {
				return Long.MAX_VALUE; // push IPv6 to end
			}
		} catch (Exception ex) {
			return Long.MAX_VALUE;
		}
	}

	private void sortListByIP() {
		Collections.sort(devicesArrayList, (itemOne, itemNext) ->
				convertDiscoveredIPToLong(itemOne.getIP()).compareTo(convertDiscoveredIPToLong(itemNext.getIP())));
	}

	private void upsertDeviceFromInfo(SubnetDevices.NetworkDeviceInfo info) {
		String ip = info.getIp();
		String mac = info.getMac() != null ? info.getMac() : getString(R.string.na);
		String vendor = info.getVendor() != null ? info.getVendor() : "";
		String name = bestNameFor(info);
		String type = "";
		String pingStr = (info.getTimeMs() != null ? info.getTimeMs() : 0f) + "ms";

		localWifiIp = NetworkUtils.getIPv4Address("wlan0");
		localCellIp = NetworkUtils.getIPv4Address("rmnet_data0");
		String gatewayIp = NetworkUtils.getGatewayIP(this);

		if (wifi_connected != null && wifi_connected) {
			if (ip.equals(localWifiIp) && !ip.equals(gatewayIp)) {
				type = getString(R.string.your_device);
				if (Build.VERSION.SDK_INT <= 29) {
					String localMac = NetworkUtils.getMacAddress();
					mac = localMac;
					vendor = ouiDbHelper.getVendorFromMac(localMac);
				}
				if (!notEmpty(name)) name = Build.MANUFACTURER + " " + Build.MODEL;
			} else if (ip.equals(gatewayIp)) {
				type = getString(R.string.gateway);
			}
		} else if (cellular_connected != null && cellular_connected) {
			if (ip.equals(localCellIp)) {
				type = getString(R.string.your_device);
				mac = getString(R.string.na);
				if (!notEmpty(name)) name = Build.MANUFACTURER + " " + Build.MODEL;
			}
		}

		infoByIp.put(ip, info);
		SubnetDevice updated = new SubnetDevice(ip, mac, vendor, name, type, pingStr);

		runOnUiThread(() -> {
			int idx = -1;
			for (int i = 0; i < devicesArrayList.size(); i++) {
				if (devicesArrayList.get(i).getIP().equals(ip)) { idx = i; break; }
			}
			if (idx >= 0) {
				devicesArrayList.set(idx, updated);
				recyclerAdapter.notifyItemChanged(idx);
			} else {
				Comparator<SubnetDevice> ipComparator = (a, b) ->
						convertDiscoveredIPToLong(a.getIP()).compareTo(convertDiscoveredIPToLong(b.getIP()));
				int index = Collections.binarySearch(devicesArrayList, updated, ipComparator);
				int insertPos = (index < 0) ? (-index - 1) : index;
				devicesArrayList.add(insertPos, updated);
				recyclerAdapter.notifyItemInserted(insertPos);
			}
			devices_found_text.setText(String.format(getString(R.string.devices_found), devicesArrayList.size()));
		});
	}

	private void startSubnetScanner() {
		String threads_string = edittext_threads.getText().toString();
		String timeout_string = edittext_timeout.getText().toString();

		disableViews();

		if (TextUtils.isEmpty(threads_string) || !isStringInt(threads_string) || threads <= 0) {
			threads_string = "256";
			edittext_threads.setText(threads_string);
		}
		threads = Integer.parseInt(threads_string);

		if (TextUtils.isEmpty(timeout_string) || !isStringInt(timeout_string) || timeout <= 0) {
			timeout_string = "3000";
			edittext_timeout.setText(timeout_string);
		}
		timeout = Integer.parseInt(timeout_string);

		isScanning = true;

		runOnUiThread(() -> {
			subnet_scanner_progress_bar.setVisibility(View.VISIBLE);
			subnet_scanner_progress_bar.setIndeterminate(false);
			subnet_scanner_progress_bar.setProgressCompat(0, false);
			devices_found_text.setText(getString(R.string.devices_found_na));
			recyclerAdapter.clear();
			infoByIp.clear();

			stageIndex = 1;
			stageCount = 1;
			stageName = "Scan";
			lastDone = 0;
			lastTotal = 0;
			updateStageSubtitle();
		});

		// Prefer Wi‑Fi if both present
		SubnetDevices.Companion.fromIPAddress(wifi_connected ? localWifiIp : localCellIp);
		subnetScanner = SubnetDevices
				.discovery()
				.setNoThreads(threads)
				.setTimeOutMillis(timeout)
				.setDisableProcNetMethod(Build.VERSION.SDK_INT >= 29)
				.enableNetBios(true)
				.enableUpnp(true)
				.enableNsd(getApplicationContext(), true)
				.setNsdServiceTypes(Arrays.asList(
						"_workstation._tcp.",
						"_smb._tcp.",
						"_afpovertcp._tcp.",
						"_ipp._tcp.",
						"_printer._tcp.",
						"_http._tcp.",
						"_airplay._tcp.",
						"_googlecast._tcp.",
						"_ssh._tcp.",
						"_ftp._tcp.",
						"_device-info._tcp.",
						"_adb-tls-connect._tcp."
				))
				.setVendorResolver(mac -> mac == null ? null : ouiDbHelper.getVendorFromMac(mac))
				.findDevices(new SubnetDevices.DiscoveryListener() {
					@Override
					public void onStageChanged(int index, int count, @NonNull String name) {
						runOnUiThread(() -> {
							stageIndex = index;
							stageCount = count;
							stageName = name;
							boolean determinate = "scan".equalsIgnoreCase(name);
							setDeterminateProgress(determinate);
							updateStageSubtitle();
							if (isScanning && subnet_scanner_progress_bar.getVisibility() != View.VISIBLE) {
								subnet_scanner_progress_bar.setVisibility(View.VISIBLE);
							}
						});
					}

					@Override
					public void onProgress(int done, int total) {
						lastDone = done;
						lastTotal = total;
						runOnUiThread(() -> {
							if (!subnet_scanner_progress_bar.isIndeterminate()) {
								int progress = (total > 0)
										? Math.min(100, Math.max(0, (int) Math.round(done * 100.0 / total)))
										: 0;
								subnet_scanner_progress_bar.setProgressCompat(progress, true);
							}
							updateStageSubtitle();
							if (isScanning && subnet_scanner_progress_bar.getVisibility() != View.VISIBLE) {
								subnet_scanner_progress_bar.setVisibility(View.VISIBLE);
							}
						});
					}

					@Override
					public void onDeviceFound(@NonNull SubnetDevices.NetworkDeviceInfo info) {
						upsertDeviceFromInfo(info);
						runOnUiThread(() ->
								devices_found_text.setText(String.format(getString(R.string.devices_found), devicesArrayList.size()))
						);
					}

					@Override
					public void onDeviceUpdated(@NonNull SubnetDevices.NetworkDeviceInfo info) {
						upsertDeviceFromInfo(info);
					}

					@Override
					public void onFinished(@NonNull List<SubnetDevices.NetworkDeviceInfo> devices) {
						enableViews();
						isScanning = false;
						runOnUiThread(() -> {
							devices_found_text.setText(String.format(getString(R.string.devices_found), devices.size()));
							sortListByIP();
							recyclerAdapter.notifyDataSetChanged();
							subnet_scanner_progress_bar.setVisibility(View.INVISIBLE);
							toolbar.setSubtitle(null);
						});
					}
				});
	}

	private void updateStageSubtitle() {
		String suffix = lastTotal > 0 ? " — " + lastDone + "/" + lastTotal : "";
		String text = stageIndex + "/" + stageCount + " " + stageName + suffix;
		toolbar.setSubtitle(text);
	}

	private void setDeterminateProgress(boolean determinate) {
		subnet_scanner_progress_bar.setIndeterminate(!determinate);
		if (determinate) {
			int pct = (lastTotal > 0)
					? Math.min(100, Math.max(0, (int) Math.round(lastDone * 100.0 / lastTotal)))
					: 0;
			subnet_scanner_progress_bar.setProgressCompat(pct, true);
		}
	}

	private boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }

	private boolean isAdbType(String type) {
		if (type == null) return false;
		String t = type.toLowerCase(Locale.US);
		if (t.endsWith(".")) t = t.substring(0, t.length() - 1);
		return t.equals("._adb-tls-connect._tcp");
	}

	private String bestNameFor(SubnetDevices.NetworkDeviceInfo info) {
		// 1) NetBIOS
		SubnetDevices.NetBiosInfo nb = info.getNetbios();
		if (nb != null && notEmpty(nb.getPrimaryName())) return nb.getPrimaryName().trim();

		// 2) UPnP (SSDP)
		SubnetDevices.UpnpInfo upnp = info.getUpnp();
		if (upnp != null) {
			if (notEmpty(upnp.getFriendlyName()) && notEmpty(upnp.getManufacturer())) {
				return upnp.getManufacturer().trim() + " " + upnp.getFriendlyName().trim();
			}
			if (notEmpty(upnp.getFriendlyName())) return upnp.getFriendlyName().trim();
			if (notEmpty(upnp.getManufacturer()) && notEmpty(upnp.getModelName())) {
				return (upnp.getManufacturer() + " " + upnp.getModelName()).trim();
			}
			if (notEmpty(upnp.getModelName())) return upnp.getModelName().trim();
			if (notEmpty(upnp.getServer())) return upnp.getServer().trim();
			if (notEmpty(upnp.getDeviceType())) {
				String dt = upnp.getDeviceType().trim();
				int lastColon = dt.lastIndexOf(':'); // urn:...:device:Router:1 -> Router
				if (lastColon > 0 && lastColon < dt.length() - 1) dt = dt.substring(lastColon + 1);
				return dt;
			}
		}

		// 3) NSD services
		List<SubnetDevices.NsdService> svcs = info.getNsdServices();
		if (svcs != null && !svcs.isEmpty()) {
			for (SubnetDevices.NsdService s : svcs) {
				if (isAdbType(s.getType())) {
					Map<String, String> attrs = s.getAttributes();
					if (attrs != null && !attrs.isEmpty()) {
						String adbName = attrs.get("name");
						if (notEmpty(adbName)) {
							String apiVer = attrs.get("api");
							String androidVer;
							try {
								androidVer = wirelessDebugApiToAndroid(Integer.parseInt(apiVer));
							} catch (NumberFormatException e) {
								androidVer = getString(R.string.na);
							}
							return adbName + " ("+ androidVer + " / API " + apiVer + ")";
						}
					}
					if (notEmpty(s.getName())) return s.getName().trim();
				}
			}
			for (SubnetDevices.NsdService s : svcs) {
				if (notEmpty(s.getName())) return s.getName().trim();
			}
			SubnetDevices.NsdService s0 = svcs.get(0);
			if (notEmpty(s0.getName())) return s0.getName().trim();
			if (notEmpty(s0.getType())) return s0.getType().trim();
		}

		return "";
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_subnet);
		LinearLayout linear_layout_root = findViewById(R.id.linear_layout_subnet_root);

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
		if (subnetScanner != null) subnetScanner.cancel();
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

	private void checkConnectivity() {
		boolean wifi = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.WIFI, false);
		boolean cell = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.CELLULAR, false);

		localWifiIp = NetworkUtils.getIPv4Address("wlan0");
		localCellIp = NetworkUtils.getIPv4Address("rmnet_data0");

		if (wifi || cell) {
			showWidgets();
			if (wifi) {
				local_ip_text.setText(String.format(getString(R.string.your_ip), localWifiIp));
			} else {
				local_ip_text.setText(String.format(getString(R.string.your_ip), localCellIp));
			}
			wifi_connected = wifi;
			cellular_connected = !wifi && cell;
		} else {
			local_ip_text.setText(getString(R.string.your_ip_na));
			devices_found_text.setText(getString(R.string.devices_found_na));
			recyclerAdapter.clear();
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	private static String wirelessDebugApiToAndroid(int api) {
		return switch (api) {
			case 36 -> "Android 16";
			case 35 -> "Android 15";
			case 34 -> "Android 14";
			case 33 -> "Android 13";
			case 32 -> "Android 12L";
			case 31 -> "Android 12";
			case 30 -> "Android 11";
			default -> "API " + api;
		};
	}

	private boolean isStringInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void showWidgets() {
		local_ip_text.setVisibility(View.VISIBLE);
		devices_found_text.setVisibility(View.VISIBLE);
		subnet_scan_button.setVisibility(View.VISIBLE);
		subnet_scan_cancel_button.setVisibility(View.VISIBLE);
		text_input_layout_timeout.setVisibility(View.VISIBLE);
		text_input_layout_threads.setVisibility(View.VISIBLE);
		recyclerview_subnet_devices.setVisibility(View.VISIBLE);
		subnet_scanner_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	private void hideWidgets() {
		local_ip_text.setVisibility(View.GONE);
		devices_found_text.setVisibility(View.GONE);
		subnet_scan_button.setVisibility(View.GONE);
		subnet_scan_cancel_button.setVisibility(View.GONE);
		text_input_layout_timeout.setVisibility(View.GONE);
		text_input_layout_threads.setVisibility(View.GONE);
		recyclerview_subnet_devices.setVisibility(View.GONE);
		subnet_scanner_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> {
			if (view != null) {
				view.setEnabled(enabled);
			}
		});
	}

	private void enableViews() {
		setEnabled(subnet_scan_button, true);
		setEnabled(subnet_scan_cancel_button, false);
	}

	private void disableViews() {
		setEnabled(subnet_scan_button, false);
		setEnabled(subnet_scan_cancel_button, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recyclerview_subnet_devices.setAdapter(null);
	}
}
