package com.truemlgpro.wifiinfo.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.adapters.SubnetScannerAdapter;
import com.truemlgpro.wifiinfo.models.SubnetDevice;
import com.truemlgpro.wifiinfo.utils.FontManager;
import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.utils.OUIDatabaseHelper;
import com.truemlgpro.wifiinfo.utils.ThemeManager;

import java.lang.ref.WeakReference;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.NetbiosAddress;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;

public class SubnetScannerActivity extends AppCompatActivity {
	private TextView textview_nonetworkconn;
	private ProgressBar subnet_scanner_progress_bar;
	private TextView local_ip_text;
	private TextView devices_found_text;
	private Button subnet_scan_button;
	private Button subnet_scan_cancel_button;
	private TextInputLayout text_input_layout_timeout;
	private TextInputLayout text_input_layout_threads;
	private EditText edittext_timeout;
	private EditText edittext_threads;
	private RecyclerView recyclerview_subnet_devices;

	private ArrayList<SubnetDevice> devicesArrayList;
	private static SubnetScannerAdapter recyclerAdapter;

	private SubnetDevices subnetScanner;
	private OUIDatabaseHelper ouiDbHelper;

	private BroadcastReceiver NetworkConnectivityReceiver;
	private ConnectivityManager connectivityManager;
	private NetworkInfo wifiCheck;
	private NetworkInfo cellularCheck;

	private static Boolean wifi_connected;
	private static Boolean cellular_connected;

	private int threads = 256;
	private int timeout = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.subnet_scanner_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		subnet_scanner_progress_bar = (ProgressBar) findViewById(R.id.subnet_scanner_progress_bar);
		local_ip_text = (TextView) findViewById(R.id.local_ip_text);
		devices_found_text = (TextView) findViewById(R.id.devices_found_text);
		subnet_scan_button = (Button) findViewById(R.id.subnet_scan_button);
		subnet_scan_cancel_button = (Button) findViewById(R.id.subnet_scan_cancel_button);
		text_input_layout_timeout = (TextInputLayout) findViewById(R.id.input_layout_timeout_subnet_scanner);
		text_input_layout_threads = (TextInputLayout) findViewById(R.id.input_layout_threads_subnet_scanner);
		edittext_timeout = (EditText) findViewById(R.id.edittext_timeout_subnet_scanner);
		edittext_threads = (EditText) findViewById(R.id.edittext_threads_subnet_scanner);
		recyclerview_subnet_devices = (RecyclerView) findViewById(R.id.recyclerview_subnet_devices);

		devicesArrayList = new ArrayList<>();
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerview_subnet_devices.getContext(), linearLayoutManager.getOrientation());
		recyclerview_subnet_devices.addItemDecoration(dividerItemDecoration);
		recyclerview_subnet_devices.setLayoutManager(linearLayoutManager);
		recyclerAdapter = new SubnetScannerAdapter(devicesArrayList, this);
		recyclerview_subnet_devices.setAdapter(recyclerAdapter);

		ouiDbHelper = new OUIDatabaseHelper(this);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		FontManager.init(this, getApplicationContext(), true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		actionbar.setTitle(getResources().getString(R.string.subnet_scanner));

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		subnet_scan_button.setOnClickListener(v -> {
			startSubnetScanner();
			devices_found_text.setText(getString(R.string.devices_found_na));
			recyclerAdapter.clear();
		});

		subnet_scan_cancel_button.setOnClickListener(v -> {
			if (subnetScanner != null) {
				subnetScanner.cancel();
			}
			setEnabled(subnet_scan_button, true);
			setEnabled(subnet_scan_cancel_button, false);
		});
	}

	private String getWiFiLocalIPv4Address() {
		try {
			List<NetworkInterface> allNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : allNetworkInterfaces) {
				if (!networkInterface.getName().equalsIgnoreCase("wlan0"))
					continue;

				List<InetAddress> allInetAddresses = Collections.list(networkInterface.getInetAddresses());
				for (InetAddress inetAddr : allInetAddresses) {
					if (!inetAddr.isLoopbackAddress() && inetAddr instanceof Inet4Address) {
						return inetAddr.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			Log.e("getWiFiLocalIPv4", e.toString());
		}
		return null;
	}

	private String getCellularLocalIPv4Address() {
		try {
			List<NetworkInterface> allNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : allNetworkInterfaces) {
				List<InetAddress> allInetAddresses = Collections.list(networkInterface.getInetAddresses());
				for (InetAddress inetAddr : allInetAddresses) {
					if (!inetAddr.isLoopbackAddress() && inetAddr instanceof Inet4Address) {
						return inetAddr.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			Log.e("getCellLocalIPv4", e.toString());
		}
		return null;
	}

	private String getMACAddress() {
		try {
			List<NetworkInterface> allNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : allNetworkInterfaces) {
				if (!networkInterface.getName().equalsIgnoreCase("wlan0"))
					continue;

				byte[] macBytes = networkInterface.getHardwareAddress();
				if (macBytes == null) {
					return null;
				}

				StringBuilder macAddressStringBuilder = new StringBuilder();
				for (byte b : macBytes) {
					macAddressStringBuilder.append(String.format("%02X:", b));
				}

				if (macAddressStringBuilder.length() > 0) {
					macAddressStringBuilder.deleteCharAt(macAddressStringBuilder.length() - 1);
				}

				return macAddressStringBuilder.toString();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getGateway() {
		if (!wifiCheck.isConnected()) {
			return "0.0.0.0";
		}
		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifi.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}

	private static class NetBiosScanner {
		private final WeakReference<Context> contextRef;

		public NetBiosScanner(Context context) {
			this.contextRef = new WeakReference<>(context);
		}

		public void performThreadedNetBiosLookup(String ipAddress) {
			ExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.submit(() -> {
				try {
					String netBiosName = getNetBiosName(ipAddress);
					if (netBiosName != null && !netBiosName.isEmpty()) {
						Activity activity = (Activity) contextRef.get();
						if (activity != null) {
							activity.runOnUiThread(() -> recyclerAdapter.updateNetBiosName(ipAddress, netBiosName));
						}
					}
				} finally {
					scheduler.shutdownNow();
				}
			});
		}

		// Should run in a non-UI thread
		@WorkerThread
		protected String getNetBiosName(String ipAddress) {
			String deviceNamePrimary = "";
			try {
				CIFSContext cifsContext = new BaseContext(new PropertyConfiguration(System.getProperties())).withGuestCrendentials();
				NetbiosAddress[] netBiosDeviceAddresses = cifsContext.getNameServiceClient().getNbtAllByAddress(ipAddress);
				for (NetbiosAddress addr : netBiosDeviceAddresses) {
					if (!addr.isGroupAddress(cifsContext)) {
						deviceNamePrimary = String.valueOf(addr.getName())
								.replaceAll("<[0-9A-Fa-f]+>", "");
						break;
					}
				}
			} catch (UnknownHostException | CIFSException e) {
				deviceNamePrimary = "";
			}
			return deviceNamePrimary;
		}
	}

	private void addDevicesToList(
			final String ip, final String mac, final String vendor,
			final String deviceName, final String deviceType, final String devicePing) {
		SubnetDevice subnetDevice = new SubnetDevice(ip, mac, vendor, deviceName, deviceType, devicePing);
		Comparator<SubnetDevice> ipComparator = (itemOne, itemNext) -> convertDiscoveredIPToLong(itemOne.getIP()).compareTo(convertDiscoveredIPToLong(itemNext.getIP()));
		int index = Collections.binarySearch(devicesArrayList, subnetDevice, ipComparator);
		int insertedItemPosition = (index < 0) ? (-index - 1) : index;

		runOnUiThread(() -> {
			devicesArrayList.add(insertedItemPosition, subnetDevice);
			recyclerAdapter.notifyItemInserted(insertedItemPosition);
			recyclerview_subnet_devices.smoothScrollToPosition(devicesArrayList.size() - 1);
		});
	}

	@NonNull
	private static Long convertDiscoveredIPToLong(String ip) {
		String[] octets = ip.split("\\.");

		long octet1 = Long.parseLong(octets[0]);
		long octet2 = Long.parseLong(octets[1]);
		long octet3 = Long.parseLong(octets[2]);
		long octet4 = Long.parseLong(octets[3]);

		return (octet1 << 24) + (octet2 << 16) +
			(octet3 << 8) + (octet4);
	}

	private void sortListByIP() {
		Collections.sort(devicesArrayList, (itemOne, itemNext) -> convertDiscoveredIPToLong(itemOne.getIP()).compareTo(convertDiscoveredIPToLong(itemNext.getIP())));
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

		runOnUiThread(() -> subnet_scanner_progress_bar.setVisibility(View.VISIBLE));

		subnetScanner = SubnetDevices.Companion.setDisableProcNetMethod(Build.VERSION.SDK_INT > 29).fromLocalAddress().setNoThreads(threads).setTimeOutMillis(timeout).findDevices(new SubnetDevices.OnSubnetDeviceFound() {
			@Override
			public void onDeviceFound(Device device) {
				String devicePing = device.time + "ms";
				if (wifi_connected) {
					if (device.ip.equals(getWiFiLocalIPv4Address()) && !device.ip.equals(getGateway())) {
						if (getMACAddress() == null || Build.VERSION.SDK_INT > 29) {
							addDevicesToList(
									device.ip,
									getString(R.string.na),
									"",
									Build.MANUFACTURER + " " + Build.MODEL,
									getString(R.string.your_device),
									devicePing);
						} else {
							addDevicesToList(
									device.ip,
									getMACAddress(),
									ouiDbHelper.getVendorFromMac(getMACAddress()),
									Build.MANUFACTURER + " " + Build.MODEL,
									getString(R.string.your_device),
									devicePing);
						}
					} else if (!device.ip.equals(getWiFiLocalIPv4Address()) && !device.ip.equals(getGateway())) {
						if (device.mac == null) {
							addDevicesToList(
									device.ip,
									getString(R.string.na),
									"",
									"",
									"",
									devicePing);
						} else {
							addDevicesToList(
									device.ip,
									device.mac,
									ouiDbHelper.getVendorFromMac(device.mac),
									"",
									"",
									devicePing);
						}
					} else if (device.ip.equals(getGateway())) {
						if (device.mac == null) {
							addDevicesToList(
									device.ip,
									getString(R.string.na),
									"",
									"",
									getString(R.string.gateway),
									devicePing);
						} else {
							addDevicesToList(
									device.ip,
									device.mac,
									ouiDbHelper.getVendorFromMac(device.mac),
									"",
									getString(R.string.gateway),
									devicePing);
						}
					}
				}

				if (cellular_connected) {
					if (device.ip.equals(getCellularLocalIPv4Address())) {
						addDevicesToList(
								device.ip,
								getString(R.string.na),
								"",
								Build.MANUFACTURER + " " + Build.MODEL,
								getString(R.string.your_device),
								devicePing);
					} else {
						addDevicesToList(
								device.ip,
								getString(R.string.na),
								"",
								"",
								"",
								devicePing);
					}
				}

				runOnUiThread(() -> devices_found_text.setText(String.format(getString(R.string.devices_found), devicesArrayList.size())));
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			public void onFinished(final ArrayList<Device> devicesFound) {
				enableViews();

				if (wifi_connected && !cellular_connected) {
					for (Device device : devicesFound) {
						if (!device.ip.equals(getWiFiLocalIPv4Address())) {
							new NetBiosScanner(SubnetScannerActivity.this).performThreadedNetBiosLookup(device.ip);
						}
					}
				}

				runOnUiThread(() -> {
					devices_found_text.setText(String.format(getString(R.string.devices_found), devicesFound.size()));
					sortListByIP();
					recyclerAdapter.notifyDataSetChanged();
					subnet_scanner_progress_bar.setVisibility(View.INVISIBLE);
				});
			}
		});
	}

	class NetworkConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkWiFiConnectivity();
		}
	}

	private boolean isSimCardPresent(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
	}

	private void checkWiFiConnectivity() {
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		cellularCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifiCheck.isConnected()) { // Wi-Fi Connectivity Check
			showWidgets();
			local_ip_text.setText(String.format(getString(R.string.your_ip), getWiFiLocalIPv4Address()));
			wifi_connected = true;
			cellular_connected = false;
		} else if (isSimCardPresent(this) && Objects.nonNull(cellularCheck) && cellularCheck.isConnected()) { // Cellular Connectivity Check
			showWidgets();
			local_ip_text.setText(String.format(getString(R.string.your_ip), getCellularLocalIPv4Address()));
			wifi_connected = false;
			cellular_connected = true;
		} else {
			local_ip_text.setText(getString(R.string.your_ip_na));
			devices_found_text.setText(getString(R.string.devices_found_na));
			recyclerAdapter.clear();
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
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
		if (subnetScanner != null) {
			subnetScanner.cancel();
		}
		unregisterReceiver(NetworkConnectivityReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recyclerview_subnet_devices.setAdapter(null);
	}
}
