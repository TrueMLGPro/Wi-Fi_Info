package com.truemlgpro.wifiinfo;

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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import me.anwarshahriar.calligrapher.Calligrapher;

public class LANDevicesScannerActivity extends AppCompatActivity {
	private TextView textview_nonetworkconn;
	private TextView local_ip_text;
	private TextView devices_found_text;
	private Button lan_scan_button;
	private ListView listview_lan_devices;
	
	private ArrayList<String> devices_arrayList;
	private ArrayAdapter<String> adapter;
	
	private BroadcastReceiver NetworkConnectivityReceiver;
	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private NetworkInfo CellularCheck;
	
	private static Scanner sc;
	
	private static Boolean wifi_connected;
	private static Boolean cellular_connected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		new ThemeManager().initializeThemes(this, getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.lan_devices_scanner_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		local_ip_text = (TextView) findViewById(R.id.local_ip_text);
		devices_found_text = (TextView) findViewById(R.id.devices_found_text);
		lan_scan_button = (Button) findViewById(R.id.lan_scan_button);
		listview_lan_devices = (ListView) findViewById(R.id.listview_lan_devices);
		
		devices_arrayList = new ArrayList<>();
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices_arrayList);
		listview_lan_devices.setAdapter(adapter);
		
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
			
		lan_scan_button.setOnClickListener(v -> {
			findSubnetDevices();
			devices_found_text.setText("Devices Found: -");
			adapter.clear();
		});
	}
	
	private String getCellularLocalIPv4Address() {
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
	
	private String getWiFiLocalIPv4Address() {
		try {
			for (Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces(); enumNetworkInterfaces.hasMoreElements();) {
				NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("getWifiLocalIPv4Addr", ex.toString());
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
					return "";
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
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
	
	private void setEnabled(final View view, final boolean enabled) {
        runOnUiThread(() -> {
	        if (view != null) {
		        view.setEnabled(enabled);
	        }
        });
    }
	
	private void addDevicesToList(final String text) {
		Comparator<String> ipComparator = (ip1, ip2) -> convertDiscoveredIPToLong(ip1).compareTo(convertDiscoveredIPToLong(ip2));
		int index = Collections.binarySearch(devices_arrayList, text, ipComparator);

		runOnUiThread(() -> {
			devices_arrayList.add((index < 0) ? (-index - 1) : index, text);
			adapter.notifyDataSetChanged();
		});
	}

	@NonNull
	public static Long convertDiscoveredIPToLong(String ip) {
		if (wifi_connected) {
			sc = new Scanner(ip).useDelimiter("\\.|\\W\\|(|<=\\|).*$");
		} else if (cellular_connected) {
			sc = new Scanner(ip).useDelimiter("\\.|\\W\\(.*$");
		}
		return (sc.nextLong() << 24) + (sc.nextLong() << 16) + 
			(sc.nextLong() << 8) + (sc.nextLong());
    }

	public void sortListByIP() {
		Collections.sort(devices_arrayList, (ip1, ip2) -> convertDiscoveredIPToLong(ip1).compareTo(convertDiscoveredIPToLong(ip2)));
	}
	
	private void findSubnetDevices() {
		setEnabled(lan_scan_button, false);

		SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
			@Override
			public void onDeviceFound(Device device) {
				if (wifi_connected) {
					if (device.ip.equals(getWiFiLocalIPv4Address()) && !device.ip.equals(getGateway())) {
						if (getMACAddress() == null || Build.VERSION.SDK_INT > 29) {
							addDevicesToList(device.ip + " | " + "MAC: N/A" + " (Your Device)");
						} else {
							addDevicesToList(device.ip + " | " + "MAC: " + getMACAddress() + " (Your Device)");
						}
					} else if (!device.ip.equals(getWiFiLocalIPv4Address()) && !device.ip.equals(getGateway())) {
						if (device.mac == null) {
							addDevicesToList(device.ip + " | " + "MAC: N/A");
						} else {
							addDevicesToList(device.ip + " | " + "MAC: " + device.mac.toUpperCase());
						}
					} else if (device.ip.equals(getGateway())) {
						if (device.mac == null) {
							addDevicesToList(device.ip + " | " + "MAC: N/A" + " (Gateway)");
						} else {
							addDevicesToList(device.ip + " | " + "MAC: " + device.mac.toUpperCase() + " (Gateway)");
						}
					}
				}

				if (cellular_connected) {
					if (device.ip.equals(getCellularLocalIPv4Address())) {
						addDevicesToList(device.ip + " (Your Device)");
					} else {
						addDevicesToList(device.ip);
					}
				}
			}

			@Override
			public void onFinished(final ArrayList<Device> devicesFound) {
				runOnUiThread(() -> {
					devices_found_text.setText("Devices Found: " + devicesFound.size());
					sortListByIP();
					adapter.notifyDataSetChanged();
				});
				setEnabled(lan_scan_button, true);
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
			local_ip_text.setText("Your IP: " + getWiFiLocalIPv4Address());
			wifi_connected = true;
			cellular_connected = false;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			local_ip_text.setText("Your IP: N/A");
			devices_found_text.setText("Devices Found: -");
			adapter.clear();
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}

		// Cellular Connectivity Check

		if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			showWidgets();
			local_ip_text.setText("Your IP: " + getCellularLocalIPv4Address());
			wifi_connected = false;
			cellular_connected = true;
		} else if (!CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			local_ip_text.setText("Your IP: N/A");
			devices_found_text.setText("Devices Found: -");
			adapter.clear();
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}
	
	public void showWidgets() {
		local_ip_text.setVisibility(View.VISIBLE);
		devices_found_text.setVisibility(View.VISIBLE);
		lan_scan_button.setVisibility(View.VISIBLE);
		listview_lan_devices.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		local_ip_text.setVisibility(View.GONE);
		devices_found_text.setVisibility(View.GONE);
		lan_scan_button.setVisibility(View.GONE);
		listview_lan_devices.setVisibility(View.GONE);
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
		unregisterReceiver(NetworkConnectivityReceiver);
	}
}
