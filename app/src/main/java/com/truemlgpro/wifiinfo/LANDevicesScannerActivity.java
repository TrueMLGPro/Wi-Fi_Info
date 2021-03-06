package com.truemlgpro.wifiinfo;

import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.stealthcopter.networktools.*;
import com.stealthcopter.networktools.subnet.*;
import java.net.*;
import java.util.*;
import me.anwarshahriar.calligrapher.*;

import android.support.v7.widget.Toolbar;

public class LANDevicesScannerActivity extends AppCompatActivity
{
	
	private Toolbar toolbar;
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
	
	public static Scanner sc;
	
	public static Boolean wifi_connected;
	public static Boolean cellular_connected;
	
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
		setContentView(R.layout.lan_devices_scanner_activity);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		local_ip_text = (TextView) findViewById(R.id.local_ip_text);
		devices_found_text = (TextView) findViewById(R.id.devices_found_text);
		lan_scan_button = (Button) findViewById(R.id.lan_scan_button);
		listview_lan_devices = (ListView) findViewById(R.id.listview_lan_devices);
		
		devices_arrayList = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices_arrayList);
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

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Back button pressed
					finish();
				}
			});
			
		lan_scan_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					findSubnetDevices();
					devices_found_text.setText("Devices Found: -");
					adapter.clear();
				}
		});
		
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
	
	public static String getWiFiLocalIPv4Address() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Wi-Fi Info", ex.toString());
		} 
		return null;
	}
	
	public static String getMACAddress() {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase("wlan0"))
					continue;

				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					return "";
				}

				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) {  
					res1.append(String.format("%02X:", b));
				}

				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}

				return res1.toString();
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
        runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (view != null) {
					view.setEnabled(enabled);
				}
			}
		});
    }
	
	private void addDevicesToList(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.add(text);
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	public static Long formatDiscoveredIP(String ip) {
		if (wifi_connected) {
			sc = new Scanner(ip).useDelimiter("\\.|\\W\\|(|<=\\|).*$");
		} else if (cellular_connected) {
			sc = new Scanner(ip).useDelimiter("\\.|\\W\\(.*$");
		}
		return (sc.nextLong() << 24) + (sc.nextLong() << 16) + 
			(sc.nextLong() << 8) + (sc.nextLong());
    }
    
	public void sortListByIP() {
        Comparator<String> ipComparator = new Comparator<String>() {
            @Override
            public int compare(String ip1, String ip2) {
                return formatDiscoveredIP(ip1).compareTo(formatDiscoveredIP(ip2));
            }       
        };
		Collections.sort(devices_arrayList, ipComparator);
	}
	
	private void findSubnetDevices() {
        setEnabled(lan_scan_button, false);
		
        SubnetDevices subnetDevices = SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
				@Override
				public void onDeviceFound(Device device) {
					if (wifi_connected) {
						if (device.ip.equalsIgnoreCase(getWiFiLocalIPv4Address()) && !device.ip.equalsIgnoreCase(getGateway())) {
							if (getMACAddress() == null) {
								addDevicesToList(device.ip + " | " + "N/A" + " (Your Device)");
							} else {
								addDevicesToList(device.ip + " | " + getMACAddress() + " (Your Device)");
							}
						} else if (!device.ip.equalsIgnoreCase(getWiFiLocalIPv4Address()) && !device.ip.equalsIgnoreCase(getGateway())) {
							if (device.mac == null) {
								addDevicesToList(device.ip + " | " + "N/A");
							} else {
								addDevicesToList(device.ip + " | " + device.mac.toUpperCase());
							}
						} else if (device.ip.equalsIgnoreCase(getGateway())) {
							if (device.mac == null) {
								addDevicesToList(device.ip + " | " + "N/A");
							} else {
								addDevicesToList(device.ip + " | " + device.mac.toUpperCase() + " (Gateway)");
							}
						}
					}
					
					if (cellular_connected) {
						if (device.ip.equalsIgnoreCase(getCellularLocalIPv4Address())) {
							addDevicesToList(device.ip + " (Your Device)");
						} else {
							addDevicesToList(device.ip);
						}
					}
				}

				@Override
				public void onFinished(final ArrayList<Device> devicesFound) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							devices_found_text.setText("Devices Found: " + devicesFound.size());
							sortListByIP();
							adapter.notifyDataSetChanged();
						}
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
