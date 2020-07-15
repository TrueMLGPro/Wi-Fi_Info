package com.truemlgpro.wifiinfo;

import android.*;
import android.app.*;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.location.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.*;
import android.support.v4.app.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.lang.String;
import java.util.Scanner;

import com.github.clans.fab.*;
import me.anwarshahriar.calligrapher.*;

public class MainActivity extends AppCompatActivity 
{
	
	private Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private LinearLayout linear_layout_cards;
	private TextView textview_ip;
	private TextView textview1;
	private TextView textview2;
	private TextView textview3;
	private TextView textview4;
	private TextView textview5;
	private TextView textview6;
	private TextView textview7;
	private TextView textview8;
	private TextView textview9;
	private TextView textview10;
	private TextView textview11;
	private TextView textview12;
	private TextView textview13;
	private TextView textview14;
	private TextView textview15;
	private TextView textview16;
	private TextView textview17;
	private TextView textview18;
	private TextView textview19;
	private TextView textview20;
	private TextView textview_noconn;
	private CardView cardview_1;
	private CardView cardview_2;
	private CardView cardview_3;
	private CardView cardview_4;
	private CardView cardview_5;
	private FloatingActionMenu fam;
	private FloatingActionButton fab_info;
	private FloatingActionButton fab_discord;
	private FloatingActionButton fab_supporters;
	private FloatingActionButton fab_settings;
	private FloatingActionButton fab_update;
	
	private LocationManager locationManager;
	private NetworkInfo WiFiCheck;
	private DhcpInfo dhcp;
	private WifiManager mainWifi;
	private Handler IPFetchHandler = new Handler();
	private String publicIPFetched;
	private boolean siteReachable = false;
	private Scanner scanner;
	private String version;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		initializeViews();
		initializeOnClickListeners();
		
		/// POLICY SETTINGS ///
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		/// END ///
		
		/// Request permissions ///
		
		if (android.os.Build.VERSION.SDK_INT > 25) {
			requestPermissionsOnStart();
		}
		
		/// END ///
		
		/// Service startup code goes here ///
		
		Intent ConnectionStateServiceIntent = new Intent(MainActivity.this, ConnectionStateService.class);
		if (android.os.Build.VERSION.SDK_INT < 26) {
			startService(ConnectionStateServiceIntent);
		} else {
			startForegroundService(ConnectionStateServiceIntent);
		}
		
		/// END ///
		
		/// Create dynamic shortcuts ///
		
		if (android.os.Build.VERSION.SDK_INT > 25) {
			createShortcuts();
		}
		
		/// END ///
		
		/// Keep screen on ///
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		/// END ///
		
		/// Notify if GPS is disabled ///
		
		if (android.os.Build.VERSION.SDK_INT > 25 && android.os.Build.VERSION.SDK_INT < 29) {
			requestGPS_API25();
		} else if (android.os.Build.VERSION.SDK_INT == 29) {
			requestGPS_API29();
		}
		
		/// END ///
		
		/// Get app version ///
		
		try {
			PackageInfo pi = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		
		/// END ///
		
		/// Initialize font and ActionBar
		
		Calligrapher calligrapher = new Calligrapher(this);
		calligrapher.setFont(this, "fonts/GoogleSans-Medium.ttf", true);
		
		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(false);
		actionbar.setSubtitle("Release v" + version);
		actionbar.setElevation(20);
		
		/// END ///
	}
	
	public void onInfoGet() {
        ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!WiFiCheck.isConnected()) {
			textview_noconn.setVisibility(View.VISIBLE);
			hideWidgets(); // Hides CardViews and TextViews
			textview_ip.setText("Your IP: N/A");
        } else {
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			dhcp = mainWifi.getDhcpInfo();
			String ssid = wInfo.getSSID();
			String macAdd = getMACAddress();
			String bssid = wInfo.getBSSID().toUpperCase();
			int rssi = wInfo.getRssi();
			int freq = wInfo.getFrequency();
			int networkSpeed = wInfo.getLinkSpeed();
			int network_id = wInfo.getNetworkId();
			String gatewayIP = getGatewayIP();
			String ipv4 = getIPv4Address();
			String ipv6 = getIPv6Address();
			String dns1 = intToIp(dhcp.dns1);
			String dns2 = intToIp(dhcp.dns2);
			String leaseTime = String.valueOf(dhcp.leaseDuration);
			int RSSIconv = mainWifi.calculateSignalLevel(rssi, 101);
			String subnetMask = intToIp(dhcp.netmask);
			int channel = convertFrequencyToChannel(freq);
			SupplicantState supState = wInfo.getSupplicantState();
			InetAddress loopbackAddr = InetAddress.getLoopbackAddress();
			
			String info_1 = "SSID: " + ssid;
			String info_2 = "BSSID: " + bssid;
			String info_3 = "IPv4: " + ipv4;
			String info_4 = "IPv6: " + ipv6;
			String info_5 = "Gateway IP: " + gatewayIP;
			String info_6 = "DNS (1): " + dns1;
			String info_7 = "DNS (2): " + dns2;
			String info_8 = "Subnet Mask: " + subnetMask;
			String info_9 = "Network ID: " + network_id;
			String info_10 = "MAC Address: " + macAdd;
			String info_12 = "Loopback Address: " + loopbackAddr;
			String info_13 = "Frequency: " + freq + "MHz";
			String info_14 = "Network Channel: " + channel;
			String info_15 = "RSSI (Signal Strength): " + RSSIconv + "%" + " (" + rssi + "dBm" + ")";
			String info_16 = "Network Speed: " + networkSpeed + "MB/s";
			String info_19 = "Lease Duration: " + leaseTime;
			String info_20 = "Supplicant State: " + supState;
			
			if (ssid.equals("<unknown ssid>")) {
				textview1.setText("SSID: N/A");
			} else {
				textview1.setText(info_1);
			}
			
			if (bssid.contains("02:00:00:00:00:00")) {
				textview2.setText("BSSID: N/A");
			} else {
				textview2.setText(info_2);
			}
			
			textview3.setText(info_3);
			textview4.setText(info_4);
			textview5.setText(info_5);
			textview6.setText(info_6);
			textview7.setText(info_7);
			
			if (subnetMask.contains("0.0.0.0")) {
				textview8.setText("Subnet Mask: N/A");
			} else {
				textview8.setText(info_8);
			}
			
			if (network_id == -1) {
				textview9.setText("Network ID: N/A");
			} else {
				textview9.setText(info_9);
			}
			
			textview10.setText(info_10);
			
			for (Network network : CM.getAllNetworks()) {
				LinkProperties linkProp = CM.getLinkProperties(network);
				String interfc = linkProp.getInterfaceName();
				String info_11 = "Network Interface: " + interfc;
				textview11.setText(info_11);
			}
			
			textview12.setText(info_12);
			textview13.setText(info_13);
			textview14.setText(info_14);
			textview15.setText(info_15);
			textview16.setText(info_16);
			
			if (Build.VERSION.SDK_INT >= 29) {
				int TXLinkSpd = wInfo.getTxLinkSpeedMbps();
				int RXLinkSpd = wInfo.getRxLinkSpeedMbps();
				String info_17 = "Transmit Link Speed: " + TXLinkSpd + "MB/s";
				String info_18 = "Receive Link Speed: " + RXLinkSpd + "MB/s";
				textview17.setText(info_17);
				textview18.setText(info_18);
			} else {
				textview17.setVisibility(View.GONE);
				textview18.setVisibility(View.GONE);
			}
			
			textview19.setText(info_19);
			textview20.setText(info_20);
			
			textview_noconn.setVisibility(View.GONE);
			showWidgets(); // Makes CardViews and TextViews visible
		}

	}
	
	public void showToastOnEnable() {
		Toast toast = Toast.makeText(this, "Enable Location to show SSID and BSSID of current network", Toast.LENGTH_LONG);
		toast.show();
	}
	
	public void showToastOnCancel() {
		Toast toast = Toast.makeText(this, "SSID and BSSID of current network won't be shown", Toast.LENGTH_LONG);
		toast.show();
	}
	
	public void showToastOnEnableAPI29() {
		Toast toast = Toast.makeText(this, "Enable Location to show SSID, BSSID and Network ID of current network", Toast.LENGTH_LONG);
		toast.show();
	}
	
	public void showToastOnCancelAPI29() {
		Toast toast = Toast.makeText(this, "SSID, BSSID and Network ID of current network won't be shown", Toast.LENGTH_LONG);
		toast.show();
	}
	
	public void hideWidgets() {
		textview_ip.setVisibility(View.GONE);
		textview1.setVisibility(View.GONE);
		textview2.setVisibility(View.GONE);
		textview3.setVisibility(View.GONE);
		textview4.setVisibility(View.GONE);
		textview5.setVisibility(View.GONE);
		textview6.setVisibility(View.GONE);
		textview7.setVisibility(View.GONE);
		textview8.setVisibility(View.GONE);
		textview9.setVisibility(View.GONE);
		textview10.setVisibility(View.GONE);
		textview11.setVisibility(View.GONE);
		textview12.setVisibility(View.GONE);
		textview13.setVisibility(View.GONE);
		textview14.setVisibility(View.GONE);
		textview15.setVisibility(View.GONE);
		textview16.setVisibility(View.GONE);
		textview17.setVisibility(View.GONE);
		textview18.setVisibility(View.GONE);
		textview19.setVisibility(View.GONE);
		textview20.setVisibility(View.GONE);
		cardview_1.setVisibility(View.GONE);
		cardview_2.setVisibility(View.GONE);
		cardview_3.setVisibility(View.GONE);
		cardview_4.setVisibility(View.GONE);
		cardview_5.setVisibility(View.GONE);
		fab_update.setVisibility(View.GONE);
	}
	
	public void showWidgets() {
		textview_ip.setVisibility(View.VISIBLE);
		textview1.setVisibility(View.VISIBLE);
		textview2.setVisibility(View.VISIBLE);
		textview3.setVisibility(View.VISIBLE);
		textview4.setVisibility(View.VISIBLE);
		textview5.setVisibility(View.VISIBLE);
		textview6.setVisibility(View.VISIBLE);
		textview7.setVisibility(View.VISIBLE);
		textview8.setVisibility(View.VISIBLE);
		textview9.setVisibility(View.VISIBLE);
		textview10.setVisibility(View.VISIBLE);
		textview11.setVisibility(View.VISIBLE);
		textview12.setVisibility(View.VISIBLE);
		textview13.setVisibility(View.VISIBLE);
		textview14.setVisibility(View.VISIBLE);
		textview15.setVisibility(View.VISIBLE);
		textview16.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT < 29 && textview17.getVisibility() == View.VISIBLE && textview18.getVisibility() == View.VISIBLE) {
			textview17.setVisibility(View.GONE);
			textview18.setVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 29 && textview17.getVisibility() == View.GONE && textview18.getVisibility() == View.GONE) {
			textview17.setVisibility(View.VISIBLE);
			textview18.setVisibility(View.VISIBLE);
		}
		textview19.setVisibility(View.VISIBLE);
		textview20.setVisibility(View.VISIBLE);
		cardview_1.setVisibility(View.VISIBLE);
		cardview_2.setVisibility(View.VISIBLE);
		cardview_3.setVisibility(View.VISIBLE);
		cardview_4.setVisibility(View.VISIBLE);
		cardview_5.setVisibility(View.VISIBLE);
		fab_update.setVisibility(View.VISIBLE);
	}
	
	protected boolean isLocationEnabled() {
		String ls = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(ls);
		if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean hasPermissions(Context context, String... permissions) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			for(String permission: permissions) {
				if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
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
	
	private String getGatewayIP() {
		if (!WiFiCheck.isConnected()) {
			return "0.0.0.0";
		}
		mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifi.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}
	
	public String getIPv4Address() {
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
	
	public String getIPv6Address() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Wi-Fi Info", ex.toString());
		} 
		return null;
	}
	
	public String intToIp(int i) {
		return ((i & 0xFF) + "."
		+ ((i >> 8) & 0xFF) + "."
		+ ((i >> 16) & 0xFF) + "."
		+ ((i >> 24) & 0xFF));
	}
	
	public static int convertFrequencyToChannel(int freq) {
		if (freq >= 2412 && freq <= 2484) {
			return (freq - 2412) / 5 + 1;
		} else if (freq >= 5170 && freq <= 5825) {
			return (freq - 5170) / 5 + 34;
		} else {
			return -1;
		}
	}
	
	private Handler handler = new Handler();

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			onInfoGet();
			handler.postDelayed(runnable, 1000);
		}
	};
	
	public String getPublicIPAddress() {
		String publicIP = "";
		try {
			scanner = new Scanner(new URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A");
			publicIP = scanner.next();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return publicIP;
	}
	
	public void requestGPS_API25() {
		// Notify User if GPS is disabled
		isLocationEnabled();
		if (!isLocationEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Location is Disabled")
				.setMessage("Wi-Fi Info needs Location to show SSID (network name) and BSSID (network MAC address) on Android 8+ \n\nClick Enable to grant Wi-Fi Info permission to show SSID and BSSID")
				.setIcon(R.drawable.location)
				.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showToastOnEnable();
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showToastOnCancel();
						dialog.cancel();
					}
				});
			builder.setCancelable(false);
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
	
	public void requestGPS_API29() {
		// Notify User if GPS is disabled
		isLocationEnabled();
		if (!isLocationEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Location is Disabled")
				.setMessage("Wi-Fi Info needs Location to show SSID (network name) and BSSID (network MAC address) and Network ID on Android 10 \n\nClick Enable to grant Wi-Fi Info permission to show SSID, BSSID and Network ID")
				.setIcon(R.drawable.location)
				.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showToastOnEnableAPI29();
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showToastOnCancelAPI29();
						dialog.cancel();
					}
				});
			builder.setCancelable(false);
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
	
	public void requestPermissionsOnStart() {
		int Permission_All = 1;
		String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
		if(!hasPermissions(this, Permissions)) {
			/* Toast toast = Toast.makeText(this, "Location permission is needed to show SSID, BSSID and Network ID on Android 8+, grant it to get full info", Toast.LENGTH_LONG);
			 toast.setGravity(Gravity.CENTER|Gravity.FILL_HORIZONTAL, 0, 50);
			 toast.show(); */

			ActivityCompat.requestPermissions(this, Permissions, Permission_All);
		}
	}
	
	public void createShortcuts() {
		ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

		ShortcutInfo githubShortcut = new ShortcutInfo.Builder(this, "shortcut_github")
			.setShortLabel("GitHub Repo")
			.setLongLabel("Open GitHub repository")
			.setIcon(Icon.createWithResource(this, R.drawable.ic_github))
			.setRank(2)
			.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TrueMLGPro/Wi-Fi_Info/")))
			.build();

		ShortcutInfo releasesShortcut = new ShortcutInfo.Builder(this, "shortcut_releases")
			.setShortLabel("Releases")
			.setLongLabel("Open GitHub releases")
			.setRank(1)
			.setIcon(Icon.createWithResource(this, R.drawable.ic_folder))
			.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TrueMLGPro/Wi-Fi_Info/releases")))
			.build();

		shortcutManager.setDynamicShortcuts(Arrays.asList(githubShortcut, releasesShortcut));
	}
	
	public void initializeViews() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		linear_layout_cards = (LinearLayout) findViewById(R.id.linear_layout_cards);
		textview_ip = (TextView) findViewById(R.id.textview_ip);
		textview1 = (TextView) findViewById(R.id.textview1);
		textview2 = (TextView) findViewById(R.id.textview2);
		textview3 = (TextView) findViewById(R.id.textview3);
		textview4 = (TextView) findViewById(R.id.textview4);
		textview5 = (TextView) findViewById(R.id.textview5);
		textview6 = (TextView) findViewById(R.id.textview6);
		textview7 = (TextView) findViewById(R.id.textview7);
		textview8 = (TextView) findViewById(R.id.textview8);
		textview9 = (TextView) findViewById(R.id.textview9);
		textview10 = (TextView) findViewById(R.id.textview10);
		textview11 = (TextView) findViewById(R.id.textview11);
		textview12 = (TextView) findViewById(R.id.textview12);
		textview13 = (TextView) findViewById(R.id.textview13);
		textview14 = (TextView) findViewById(R.id.textview14);
		textview15 = (TextView) findViewById(R.id.textview15);
		textview16 = (TextView) findViewById(R.id.textview16);
		textview17 = (TextView) findViewById(R.id.textview17);
		textview18 = (TextView) findViewById(R.id.textview18);
		textview19 = (TextView) findViewById(R.id.textview19);
		textview20 = (TextView) findViewById(R.id.textview20);
		textview_noconn = (TextView) findViewById(R.id.textview_noconn);
		cardview_1 = (CardView) findViewById(R.id.cardview_1);
		cardview_2 = (CardView) findViewById(R.id.cardview_2);
		cardview_3 = (CardView) findViewById(R.id.cardview_3);
		cardview_4 = (CardView) findViewById(R.id.cardview_4);
		cardview_5 = (CardView) findViewById(R.id.cardview_5);
		fam = (FloatingActionMenu) findViewById(R.id.fam);
		fab_info = (FloatingActionButton) findViewById(R.id.menu_item_1);
		fab_discord = (FloatingActionButton) findViewById(R.id.menu_item_2);
		fab_supporters = (FloatingActionButton) findViewById(R.id.menu_item_3);
		fab_settings = (FloatingActionButton) findViewById(R.id.menu_item_4);
		fab_update = (FloatingActionButton) findViewById(R.id.fab_update_ip);
	}
	
	public void initializeOnClickListeners() {
		fab_info.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent_info = new Intent(MainActivity.this, DevInfoActivity.class);
					startActivity(intent_info);
					fam.close(true);
				}
			});

		fab_discord.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent_discord = new Intent(MainActivity.this, DiscordServersActivity.class);
					startActivity(intent_discord);
					fam.close(true);
				}
			});

		fab_supporters.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent_supporters = new Intent(MainActivity.this, SupportersActivity.class);
					startActivity(intent_supporters);
					fam.close(true);
				}
			});

		fab_settings.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent_settings = new Intent(MainActivity.this, SettingsActivity.class);
					startActivity(intent_settings);
					fam.close(true);
				}
			});

		fab_update.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					fab_update.setEnabled(false);
					PublicIPRunnable runnableIP = new PublicIPRunnable();
					new Thread(runnableIP).start();
				}
			});
	}

	@Override
	protected void onPause()
	{
		handler.removeCallbacks(runnable);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		handler.post(runnable);
		super.onResume();
	}
	
	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Are you sure?")
			.setMessage("Do you want to exit?")
			.setIcon(R.drawable.exit)
			.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					Toast toast = Toast.makeText(MainActivity.this, "See you next time!", Toast.LENGTH_LONG);
					toast.show();
					finish();
				}
			})
			.setNegativeButton("Cancel", null);
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public boolean isReachable(String url) throws IOException {
		boolean reachable = false;
		int code = 200;

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
					if (siteReachable = true) {
						textview_ip.setText("Your IP: " + publicIPFetched);
					}

					if (siteReachable = false) {
						textview_ip.setText("Your IP: N/A");
					}
				}
			}.execute();
			
			Handler handlerEnableFAB = new Handler(Looper.getMainLooper());
			handlerEnableFAB.postDelayed(new Runnable() {
				@Override
				public void run() {
					fab_update.setEnabled(true);
				}
			}, 5000);
		}
	}
	
	/*@Override 
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.dev_info) {
            Intent intent = new Intent(this, DevInfoActivity.class);
            startActivity(intent);
        }
		
		if (item.getItemId() == R.id.discord_servers) {
			Intent intent = new Intent(this, DiscordServersActivity.class);
			startActivity(intent);
		}
		
		if (item.getItemId() == R.id.supporters) {
			Intent intent = new Intent(this, SupportersActivity.class);
			startActivity(intent);
		}
		
		if (item.getItemId() == R.id.settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
		return true;
    }*/
	
}
