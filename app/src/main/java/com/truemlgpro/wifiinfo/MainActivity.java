package com.truemlgpro.wifiinfo;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.location.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.*;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.support.v7.preference.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.github.clans.fab.*;
import java.io.*;
import java.net.*;
import java.util.*;
import me.anwarshahriar.calligrapher.*;

import android.app.AlertDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity 
{
	
	private Toolbar toolbar;
	private ActionBar actionbar;
	private ScrollView scrollview;
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
	private TextView textview21;
	private TextView textview22;
	private TextView textview23;
	private TextView textview24;
	private TextView textview25;
	private TextView textview26;
	private TextView textview27;
	private TextView textview28;
	private TextView textview29;
	private TextView textview30;
	private TextView textview_noconn;
	private CardView cardview_1;
	private CardView cardview_2;
	private CardView cardview_3;
	private CardView cardview_4;
	private CardView cardview_5;
	private CardView cardview_6;
	private FloatingActionMenu fam;
	private FloatingActionButton fab_info;
	private FloatingActionButton fab_discord;
	private FloatingActionButton fab_supporters;
	private FloatingActionButton fab_tools;
	private FloatingActionButton fab_settings;
	private FloatingActionButton fab_update;
	
	private LocationManager locationManager;
	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private DhcpInfo dhcp;
	private WifiInfo wInfo;
	private WifiManager mainWifi;
	private BroadcastReceiver WiFiConnectivityReceiver;
	public static Boolean isServiceRunning = false;
	public static Boolean darkMode = true;
	public static Boolean amoledMode = false;
	public static Boolean startOnBoot = false;
	public static Boolean showNtfc = true;
	public static Boolean visualizeSigStrg = false;
	public static Boolean startStopSrvcScrnState = false;
	public static Boolean colorizeNtfc = false;
	public static Boolean neverShow = false;
	private static Boolean isTapTargetSeen_NoConnection = false;
	public static Boolean isHandlerRunning = false;
	public static String ntfcUpdateInterval = "1000";
	public static String cardUpdateInterval = "1000";
	public static String appFont = "fonts/GoogleSans-Medium.ttf";
	public static Activity main;
	public static AlertDialog alertAPI25;
	public static AlertDialog alertAPI29;
	private Handler IPFetchHandler = new Handler();
	private String publicIPFetched;
	private boolean siteReachable = false;
	private Scanner scanner;
	private String version;
	private double megabyte = 1024 * 1024;
	private double gigabyte = 1024 * 1024 * 1024;
	
	private SharedPreferences.OnSharedPreferenceChangeListener listener;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		/// Shared Preferences ///
		
		Boolean keyTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, darkMode);
		Boolean keyAmoledTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, amoledMode);
		
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
		
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
			{
				if (key.equals(SettingsActivity.KEY_PREF_SWITCH)) {
					if (prefs.getBoolean(SettingsActivity.KEY_PREF_SWITCH, true) == true) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_SWITCH, darkMode = true);
					} else {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_SWITCH, darkMode = false);
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_AMOLED_CHECK)) {
					if (prefs.getBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, false) == true) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, amoledMode = true);
					} else {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, amoledMode = false);
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_BOOT_SWITCH)) {
					if (prefs.getBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, false) == true) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, startOnBoot = true);
					} else {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, startOnBoot = false);
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_NTFC_SWITCH)) {
					if (prefs.getBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, true) == true) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, showNtfc = true);
					} else {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, showNtfc = false);
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK)) {
					if (prefs.getBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, false) == true) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, visualizeSigStrg = true);
					} else {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, visualizeSigStrg = false);
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK)) {
					if (prefs.getBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, false) == true) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, startStopSrvcScrnState = true);
						Intent restartConnectionStateService = new Intent(MainActivity.this, ConnectionStateService.class);
						Intent restartNotificationService = new Intent(MainActivity.this, NotificationService.class);
						if (ConnectionStateService.isNotificationServiceRunning) {
							stopService(restartNotificationService);
						}
						if (ConnectionStateService.isConnectionStateServiceRunning) {
							stopService(restartConnectionStateService);
							if (android.os.Build.VERSION.SDK_INT < 26) {
								startService(restartConnectionStateService);
							} else {
								startForegroundService(restartConnectionStateService);
							}
						}
					} else {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, startStopSrvcScrnState = false);
						Intent restartConnectionStateService = new Intent(MainActivity.this, ConnectionStateService.class);
						Intent restartNotificationService = new Intent(MainActivity.this, NotificationService.class);
						if (ConnectionStateService.isNotificationServiceRunning) {
							stopService(restartNotificationService);
						}
						if (ConnectionStateService.isConnectionStateServiceRunning) {
							stopService(restartConnectionStateService);
							if (android.os.Build.VERSION.SDK_INT < 26) {
								startService(restartConnectionStateService);
							} else {
								startForegroundService(restartConnectionStateService);
							}
						}
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_CLR_CHECK)) {
					if (prefs.getBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, false) == true) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, colorizeNtfc = true);
					} else {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, colorizeNtfc = false);
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_NTFC_FREQ)) {
					if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("500")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, ntfcUpdateInterval = "500");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("1000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, ntfcUpdateInterval = "1000");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("2000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, ntfcUpdateInterval = "2000");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("3000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, ntfcUpdateInterval = "3000");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("4000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, ntfcUpdateInterval = "4000");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("5000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, ntfcUpdateInterval = "5000");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("10000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, ntfcUpdateInterval = "10000");
					}
				}
				
				if (key.equals(SettingsActivity.KEY_PREF_CARD_FREQ)) {
					if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("500")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval = "500");
					}

					if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("1000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval = "1000");
					}

					if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("2000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval = "2000");
					}

					if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("3000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval = "3000");
					}

					if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("4000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval = "4000");
					}

					if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("5000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval = "5000");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("10000")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval = "10000");
					}
				}
					
				if (key.equals(SettingsActivity.KEY_PREF_APP_FONT)) {
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/GoogleSans-Medium.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/GoogleSans-Medium.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/CircularStd-Bold.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/CircularStd-Bold.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Comfortaa-Regular.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Comfortaa-Regular.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/CondellBio-Medium.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/CondellBio-Medium.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/FilsonPro-Regular.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/FilsonPro-Regular.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Hellix-Medium.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Hellix-Medium.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Moderat-Regular.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Moderat-Regular.ttf");
					}

					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Newson-Medium.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Newson-Medium.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/NoirText-Bold.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/NoirText-Bold.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Poligon-Regular.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Poligon-Regular.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/ProximaSoft-Medium.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/ProximaSoft-Medium.ttf");
					}

					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Squalo-Regular.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Squalo-Regular.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Tomkin-Regular.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Tomkin-Regular.ttf");
					}
					
					if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/GoogleSans-Medium.ttf").equals("fonts/Urbani-Regular.ttf")) {
						new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Urbani-Regular.ttf");
					}
				}
			}
		};
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(listener);
		
		/// END ///
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		/// Initialize ///
		
		main = this;
		
		initializeViews();
		initializeOnClickListeners();
		initializeCopyableText();
		
		/// END ///
		
		/// POLICY SETTINGS ///
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		/// END ///
		
		/// Request permissions ///
		
		if (android.os.Build.VERSION.SDK_INT > 25) {
			requestPermissionsOnStart();
		}
		
		/// END ///
		
		/// Notify if GPS is disabled ///
		
		if (hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			if (android.os.Build.VERSION.SDK_INT > 25 && android.os.Build.VERSION.SDK_INT < 29) {
				Boolean keyNeverShow25 = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean("dialogNeverShowAPI25", neverShow);
				if (!keyNeverShow25) {
					requestGPS_API25();
				}
			} else if (android.os.Build.VERSION.SDK_INT == 29) {
				Boolean keyNeverShow29 = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean("dialogNeverShowAPI29", neverShow);
				if (!keyNeverShow29) {
					requestGPS_API29();
				}
			}
		}

		/// END ///
		
		/// Service startup ///
		
		Boolean keyNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, showNtfc);
		
		if (keyNtfc == true) {
			if (isServiceRunning == false) {
				Intent ConnectionStateServiceIntent = new Intent(MainActivity.this, ConnectionStateService.class);
				if (android.os.Build.VERSION.SDK_INT < 26) {
					startService(ConnectionStateServiceIntent);
				} else {
					startForegroundService(ConnectionStateServiceIntent);
				}
				isServiceRunning = true;
			}
		} else {
			if (isServiceRunning == true) {
				ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				
				Intent ConnectionStateServiceIntent = new Intent(MainActivity.this, ConnectionStateService.class);
				Intent NotificationServiceIntent = new Intent(MainActivity.this, NotificationService.class);
				
				stopService(ConnectionStateServiceIntent);
				isServiceRunning = false;
				if (WiFiCheck.isConnected()) {
					stopService(NotificationServiceIntent);
				}
			}
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
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, appFont);
		calligrapher.setFont(this, font, true);
		
		setSupportActionBar(toolbar);
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(false);
		actionbar.setSubtitle("Release v" + version);
		actionbar.setElevation(20);
		
		/// END ///
		
		/// Set up FloatingActionMenu options ///
		
		fam.setClosedOnTouchOutside(true);
		
		/// END ///
		
		/// Set default preferences ///
		
		android.support.v7.preference.PreferenceManager
			.setDefaultValues(this, R.xml.preferences, false);
		
		/// END ///
		
		CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (!WiFiCheck.isConnected()) {
			textview_noconn.setVisibility(View.VISIBLE);
			hideWidgets(); // Hides CardViews and TextViews
			textview_ip.setText("Your IP: N/A");
		} else {
			textview_noconn.setVisibility(View.GONE);
			showWidgets(); // Makes CardViews and TextViews visible
		}
	}
	
	public void onInfoGet() {
		mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wInfo = mainWifi.getConnectionInfo();
		dhcp = mainWifi.getDhcpInfo();
		String ssid = wInfo.getSSID();
		String macAdd = getMACAddress();
		String bssid;
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		} else {
			bssid = "N/A";
		}
		int rssi = wInfo.getRssi();
		int freq = wInfo.getFrequency();
		int networkSpeed = wInfo.getLinkSpeed();
		int network_id = wInfo.getNetworkId();
		String gatewayIP = getGatewayIP();
		String ipv4 = getIPv4Address();
		String ipv6 = getIPv6Address();
		String dns1 = intToIp(dhcp.dns1);
		String dns2 = intToIp(dhcp.dns2);
		int leaseTime = dhcp.leaseDuration;
		int leaseTimeHours = dhcp.leaseDuration / 3600;
		int leaseTimeMinutes = dhcp.leaseDuration / 60;
		int RSSIconv = mainWifi.calculateSignalLevel(rssi, 101);
		String subnetMask = intToIp(dhcp.netmask);
		int channel = convertFrequencyToChannel(freq);
		SupplicantState supState = wInfo.getSupplicantState();
		InetAddress loopbackAddr = InetAddress.getLoopbackAddress();
		double totalRXBytes = TrafficStats.getTotalRxBytes();
		double totalTXBytes = TrafficStats.getTotalTxBytes();
		double mobileRXBytes = TrafficStats.getMobileRxBytes();
		double mobileTXBytes = TrafficStats.getMobileTxBytes();
		double wifiRXBytes = totalRXBytes - mobileRXBytes;
		double wifiTXBytes = totalTXBytes - mobileTXBytes;
		double wifiRXMegabytes = wifiRXBytes / megabyte;
		double wifiTXMegabytes = wifiTXBytes / megabyte;
		double wifiRXGigabytes = wifiRXBytes / gigabyte;
		double wifiTXGigabytes = wifiTXBytes / gigabyte;
		String wifiRXMegabytesStr = String.format(Locale.US, "%.2f", wifiRXMegabytes);
		String wifiTXMegabytesStr = String.format(Locale.US, "%.2f", wifiTXMegabytes);
		String wifiRXGigabytesStr = String.format(Locale.US, "%.2f", wifiRXGigabytes);
		String wifiTXGigabytesStr = String.format(Locale.US, "%.2f", wifiTXGigabytes);
			
		String info_1 = "SSID: " + ssid;
		String info_3 = "BSSID: " + bssid.toUpperCase();
		String info_4 = "IPv4: " + ipv4;
		String info_5 = "IPv6: " + ipv6;
		String info_6 = "Gateway IP: " + gatewayIP;
		String info_8 = "DNS (1): " + dns1;
		String info_9 = "DNS (2): " + dns2;
		String info_10 = "Subnet Mask: " + subnetMask;
		String info_11 = "Network ID: " + network_id;
		String info_12 = "MAC Address: " + macAdd;
		String info_14 = "Loopback Address: " + loopbackAddr;
		String info_16 = "Frequency: " + freq + "MHz";
		String info_17 = "Network Channel: " + channel;
		String info_18 = "RSSI (Signal Strength): " + RSSIconv + "%" + " (" + rssi + "dBm" + ")";
		String info_19 = "Lease Duration: " + leaseTime + "s " + "(" + leaseTimeHours + "h)";
		String info_19_1 = "Lease Duration: " + leaseTime + "s " + "(" + leaseTimeMinutes + "m)";
		String info_22 = "Network Speed: " + networkSpeed + "MB/s";
		String info_23 = "Transmitted MBs/GBs: " + wifiTXMegabytesStr + "MB " + "(" + wifiTXGigabytesStr + "GB" + ")";
		String info_24 = "Received MBs/GBs: " + wifiRXMegabytesStr + "MB "  + "(" + wifiRXGigabytesStr + "GB" + ")";
		String info_25 = "Supplicant State: " + supState;
			
		if (ssid.equals("<unknown ssid>")) {
			textview1.setText("SSID: N/A");
		} else {
			textview1.setText(info_1);
		}
			
		if (wInfo.getHiddenSSID() == true) {
			textview2.setText("Hidden SSID: Yes");
		} else {
			textview2.setText("Hidden SSID: No");
		}
			
		if (bssid.contains("02:00:00:00:00:00")) {
			textview3.setText("BSSID: N/A");
		} else {
			textview3.setText(info_3);
		}
			
		textview4.setText(info_4);
		textview5.setText(info_5);
		textview6.setText(info_6);
			
		try {
			InetAddress hostnameAddr = InetAddress.getByName(gatewayIP);
			String info_7 = "Hostname: " + hostnameAddr.getHostName();
			textview7.setText(info_7);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
			
		textview8.setText(info_8);
		textview9.setText(info_9);
			
		if (subnetMask.contains("0.0.0.0")) {
			textview10.setText("Subnet Mask: N/A");
		} else {
			textview10.setText(info_10);
		}
			
		if (network_id == -1) {
			textview11.setText("Network ID: N/A");
		} else {
			textview11.setText(info_11);
		}
			
		textview12.setText(info_12);
			
		for (Network network : CM.getAllNetworks()) {
			LinkProperties linkProp = CM.getLinkProperties(network);
			String interfc = linkProp.getInterfaceName();
			String info_13 = "Network Interface: " + interfc;
			textview13.setText(info_13);
		}
			
		textview14.setText(info_14);
			
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			String info_15 = "Localhost: " + localHost;
			textview15.setText(info_15);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
			
		textview16.setText(info_16);
		textview17.setText(info_17);
		textview18.setText(info_18);
			
		if (leaseTime == 0) {
			textview19.setText("Lease Duration: N/A");
		} else if (leaseTime >= 3600) {
			textview19.setText(info_19);
		} else if (leaseTime < 3600) {
			textview19.setText(info_19_1);
		}
			
		if (Build.VERSION.SDK_INT >= 29) {
			int TXLinkSpd = wInfo.getTxLinkSpeedMbps();
			int RXLinkSpd = wInfo.getRxLinkSpeedMbps();
			String info_20 = "Transmit Link Speed: " + TXLinkSpd + "MB/s";
			String info_21 = "Receive Link Speed: " + RXLinkSpd + "MB/s";
			textview20.setText(info_20);
			textview21.setText(info_21);
		} else {
			textview20.setVisibility(View.GONE);
			textview21.setVisibility(View.GONE);
		}
			
		textview22.setText(info_22);
		textview23.setText(info_23);
		textview24.setText(info_24);
		textview25.setText(info_25);
			
		if (mainWifi.is5GHzBandSupported()) {
			String info_26 = "5GHz Band Support: Yes";
			textview26.setText(info_26);
		} else {
			String info_26 = "5GHz Band Support: No";
			textview26.setText(info_26);
		}
			
		if (mainWifi.isP2pSupported()) {
			String info_27 = "Wi-Fi Direct Support: Yes";
			textview27.setText(info_27);
		} else {
			String info_27 = "Wi-Fi Direct Support: No";
			textview27.setText(info_27);
		}
			
		if (mainWifi.isTdlsSupported()) {
			String info_28 = "TDLS Support: Yes";
			textview28.setText(info_28);
		} else {
			String info_28 = "TDLS Support: No";
			textview28.setText(info_28);
		}
			
		if (Build.VERSION.SDK_INT >= 29) {
			if (mainWifi.isWpa3SaeSupported()) {
				String info_29 = "WPA3 SAE Support: Yes";
				textview29.setText(info_29);
			} else {
				String info_29 = "WPA3 SAE Support: No";
				textview29.setText(info_29);
			}
			
			if (mainWifi.isWpa3SuiteBSupported()) {
				String info_30 = "WPA3 Suite B Support: Yes";
				textview30.setText(info_30);
			} else {
				String info_30 = "WPA3 Suite B Support: No";
				textview30.setText(info_30);
			}
		} else {
			textview29.setVisibility(View.GONE);
			textview30.setVisibility(View.GONE);
		}
	}
	
	class WiFiConnectivityReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (!WiFiCheck.isConnected()) {
				textview_noconn.setVisibility(View.VISIBLE);
				hideWidgets(); // Hides CardViews and TextViews
				textview_ip.setText("Your IP: N/A");
				if (isHandlerRunning) {
					handler.removeCallbacks(runnable);
					isHandlerRunning = false;
				}
			} else {
				textview_noconn.setVisibility(View.GONE);
				showWidgets(); // Makes CardViews and TextViews visible
				if (!isHandlerRunning) {
					handler.post(runnable);
					isHandlerRunning = true;
				}
			}
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
		textview21.setVisibility(View.GONE);
		textview22.setVisibility(View.GONE);
		textview23.setVisibility(View.GONE);
		textview24.setVisibility(View.GONE);
		textview25.setVisibility(View.GONE);
		textview26.setVisibility(View.GONE);
		textview27.setVisibility(View.GONE);
		textview28.setVisibility(View.GONE);
		textview29.setVisibility(View.GONE);
		textview30.setVisibility(View.GONE);
		cardview_1.setVisibility(View.GONE);
		cardview_2.setVisibility(View.GONE);
		cardview_3.setVisibility(View.GONE);
		cardview_4.setVisibility(View.GONE);
		cardview_5.setVisibility(View.GONE);
		cardview_6.setVisibility(View.GONE);
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
		textview17.setVisibility(View.VISIBLE);
		textview18.setVisibility(View.VISIBLE);
		textview19.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT < 29 && textview20.getVisibility() == View.VISIBLE && textview21.getVisibility() == View.VISIBLE) {
			textview20.setVisibility(View.GONE);
			textview21.setVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 29 && textview20.getVisibility() == View.GONE && textview21.getVisibility() == View.GONE) {
			textview20.setVisibility(View.VISIBLE);
			textview21.setVisibility(View.VISIBLE);
		}
		textview22.setVisibility(View.VISIBLE);
		textview23.setVisibility(View.VISIBLE);
		textview24.setVisibility(View.VISIBLE);
		textview25.setVisibility(View.VISIBLE);
		textview26.setVisibility(View.VISIBLE);
		textview27.setVisibility(View.VISIBLE);
		textview28.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT < 29 && textview29.getVisibility() == View.VISIBLE && textview30.getVisibility() == View.VISIBLE) {
			textview29.setVisibility(View.GONE);
			textview30.setVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 29 && textview29.getVisibility() == View.GONE && textview30.getVisibility() == View.GONE) {
			textview29.setVisibility(View.VISIBLE);
			textview30.setVisibility(View.VISIBLE);
		}
		cardview_1.setVisibility(View.VISIBLE);
		cardview_2.setVisibility(View.VISIBLE);
		cardview_3.setVisibility(View.VISIBLE);
		cardview_4.setVisibility(View.VISIBLE);
		cardview_5.setVisibility(View.VISIBLE);
		cardview_6.setVisibility(View.VISIBLE);
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
			String keyCardFreq = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval);
			int keyCardFreqFormatted = Integer.parseInt(keyCardFreq);
			onInfoGet();
			handler.postDelayed(runnable, keyCardFreqFormatted);
		}
	};
	
	public void requestGPS_API25() {
		// Notify User if GPS is disabled
		isLocationEnabled();
		if (!isLocationEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Location is Disabled")
				.setMessage("Wi-Fi Info needs Location to show SSID (network name) and BSSID (network MAC address) on Android 8+\n\nClick Enable to grant Wi-Fi Info permission to show SSID and BSSID")
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
				})
				.setNeutralButton("Don't show again", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean("dialogNeverShowAPI25", neverShow = true);
					}
				});
			builder.setCancelable(false);
			alertAPI25 = builder.create();
			alertAPI25.show();
		}
	}
	
	public void requestGPS_API29() {
		// Notify User if GPS is disabled
		isLocationEnabled();
		if (!isLocationEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Location is Disabled")
				.setMessage("Wi-Fi Info needs Location to show SSID (network name) and BSSID (network MAC address) and Network ID on Android 10\n\nClick Enable to grant Wi-Fi Info permission to show SSID, BSSID and Network ID")
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
				})
				.setNeutralButton("Don't show again", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new SharedPreferencesManager(getApplicationContext()).storeBoolean("dialogNeverShowAPI29", neverShow = true);
					}
				});
			builder.setCancelable(false);
			alertAPI29 = builder.create();
			alertAPI29.show();
		}
	}
	
	public void requestPermissionsOnStart() {
		if (!hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			/* Toast toast = Toast.makeText(this, "Location permission is needed to show SSID, BSSID and Network ID on Android 8+, grant it to get full info", Toast.LENGTH_LONG);
			 toast.setGravity(Gravity.CENTER|Gravity.FILL_HORIZONTAL, 0, 50);
			 toast.show(); */
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Permission required!")
				.setMessage("Location permission is needed to show SSID, BSSID and Network ID on Android 8+, grant it to get full info")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						int Permission_All = 1;
						String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
						/*Toast toast = Toast.makeText(MainActivity.this, "Click \"Grant\" button to grant required permissions\n(Background location is optional)", Toast.LENGTH_LONG);
						toast.show();*/
						ActivityCompat.requestPermissions(MainActivity.this, Permissions, Permission_All);
					}
				})
				.setCancelable(false);
			AlertDialog alert = builder.create();
			alert.show();
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
		scrollview = (ScrollView) findViewById(R.id.scroll_view);
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
		textview21 = (TextView) findViewById(R.id.textview21);
		textview22 = (TextView) findViewById(R.id.textview22);
		textview23 = (TextView) findViewById(R.id.textview23);
		textview24 = (TextView) findViewById(R.id.textview24);
		textview25 = (TextView) findViewById(R.id.textview25);
		textview26 = (TextView) findViewById(R.id.textview26);
		textview27 = (TextView) findViewById(R.id.textview27);
		textview28 = (TextView) findViewById(R.id.textview28);
		textview29 = (TextView) findViewById(R.id.textview29);
		textview30 = (TextView) findViewById(R.id.textview30);
		textview_noconn = (TextView) findViewById(R.id.textview_noconn);
		cardview_1 = (CardView) findViewById(R.id.cardview_1);
		cardview_2 = (CardView) findViewById(R.id.cardview_2);
		cardview_3 = (CardView) findViewById(R.id.cardview_3);
		cardview_4 = (CardView) findViewById(R.id.cardview_4);
		cardview_5 = (CardView) findViewById(R.id.cardview_5);
		cardview_6 = (CardView) findViewById(R.id.cardview_6);
		fam = (FloatingActionMenu) findViewById(R.id.fam);
		fab_info = (FloatingActionButton) findViewById(R.id.menu_item_1);
		fab_discord = (FloatingActionButton) findViewById(R.id.menu_item_2);
		fab_supporters = (FloatingActionButton) findViewById(R.id.menu_item_3);
		fab_tools = (FloatingActionButton) findViewById(R.id.menu_item_4);
		fab_settings = (FloatingActionButton) findViewById(R.id.menu_item_5);
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
		
		fab_tools.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent_url_to_ip = new Intent(MainActivity.this, ToolsActivity.class);
					startActivity(intent_url_to_ip);
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
	
	public void initializeCopyableText() {
		textview_ip.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					if (textview_ip.getText().equals("Your IP: N/A")) {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Public IP", "N/A");
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					} else {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Public IP", publicIPFetched);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + publicIPFetched, Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview1.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					if (textview1.getText().equals("SSID: N/A")) {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("SSID", "N/A");
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					} else {
						String ssid = wInfo.getSSID();
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("SSID", ssid);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + ssid, Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview2.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					if (textview2.getText().equals("Hidden SSID: Yes")) {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Hidden SSID", "Yes");
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
					} else {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Hidden SSID", "No");
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview3.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					if (textview3.getText().equals("BSSID: N/A")) {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("BSSID", "N/A");
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					} else {
						String bssid = wInfo.getBSSID().toUpperCase();
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("BSSID", bssid);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + bssid, Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview4.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					String ipv4 = getIPv4Address();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("IPv4", ipv4);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + ipv4, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview5.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					String ipv6 = getIPv6Address();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("IPv6", ipv6);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + ipv6, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview6.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					String gatewayIP = getGatewayIP();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Gateway IP", gatewayIP);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + gatewayIP, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview7.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					try {
						String gatewayIP = getGatewayIP();
						InetAddress hostnameAddr = InetAddress.getByName(gatewayIP);
						String hostName = hostnameAddr.getHostName();
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Hostname", hostName);
						cbm.setPrimaryClip(clip);
						
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + hostName, Toast.LENGTH_SHORT).show();
					} catch (UnknownHostException e) {
						e.printStackTrace();
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview8.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					dhcp = mainWifi.getDhcpInfo();
					String dns1 = intToIp(dhcp.dns1);
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("DNS (1)", dns1);
					cbm.setPrimaryClip(clip);
					
					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + dns1, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview9.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					dhcp = mainWifi.getDhcpInfo();
					String dns2 = intToIp(dhcp.dns2);
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("DNS (2)", dns2);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + dns2, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview10.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					if (textview10.getText().equals("Subnet Mask: N/A")) {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Subnet Mask", "N/A");
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					} else {
						dhcp = mainWifi.getDhcpInfo();
						String subnetMask = intToIp(dhcp.netmask);
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Subnet Mask", subnetMask);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + subnetMask, Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview11.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					if (textview11.getText().equals("Network ID: N/A")) {
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Network ID", "N/A");
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					} else {
						String network_id = String.valueOf(wInfo.getNetworkId());
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Network ID", network_id);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + network_id, Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview12.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					String macAddress = getMACAddress();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("MAC Address", macAddress);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + macAddress, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview13.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					for (Network network : CM.getAllNetworks()) {
						LinkProperties linkProp = CM.getLinkProperties(network);
						String interfc = linkProp.getInterfaceName();
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Network Interface", interfc);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + interfc, Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview14.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					InetAddress loopbackAddr = InetAddress.getLoopbackAddress();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Loopback Address", loopbackAddr.toString());
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + loopbackAddr, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview15.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					try {
						InetAddress localHost = InetAddress.getLocalHost();
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Localhost", localHost.toString());
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + localHost, Toast.LENGTH_SHORT).show();
					} catch (UnknownHostException e) {
						e.printStackTrace();
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview16.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					int freq = wInfo.getFrequency();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Frequency", freq + "MHz");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + freq + "MHz", Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview17.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					int freq = wInfo.getFrequency();
					int channel = convertFrequencyToChannel(freq);
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Network Channel", String.valueOf(channel));
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + channel, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview18.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					int rssi = wInfo.getRssi();
					int RSSIconv = mainWifi.calculateSignalLevel(rssi, 101);
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("RSSI", RSSIconv + "%" + " (" + rssi + "dBm" + ")");
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")", Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview19.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					int leaseTime = dhcp.leaseDuration;
					int leaseTimeHours = dhcp.leaseDuration / 3600;
					int leaseTimeMinutes = dhcp.leaseDuration / 60;
					String leaseTimeHoursFormat = leaseTime + "s " + "(" + leaseTimeHours + "h)";
					String leaseTimeMinutesFormat = leaseTime + "s " + "(" + leaseTimeMinutes + "m)";
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

					if (leaseTime == 0) {
						ClipData clip = ClipData.newPlainText("Lease Duration", "N/A");
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
					} else if (leaseTime >= 3600) {
						ClipData clip = ClipData.newPlainText("Lease Duration", leaseTimeHoursFormat);
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + leaseTimeHoursFormat, Toast.LENGTH_SHORT).show();
					} else if (leaseTime < 3600) {
						ClipData clip = ClipData.newPlainText("Lease Duration", leaseTimeMinutesFormat);
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + leaseTimeMinutesFormat, Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		if (Build.VERSION.SDK_INT >= 29) {
			textview20.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v)
					{
						int TXLinkSpd = wInfo.getTxLinkSpeedMbps();
						String transmitLinkSpeed = TXLinkSpd + "MB/s";
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Transmit Link Speed", transmitLinkSpeed);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + transmitLinkSpeed, Toast.LENGTH_SHORT).show();
						return true;
					}
				});

			textview21.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v)
					{
						int RXLinkSpd = wInfo.getRxLinkSpeedMbps();
						String receiveLinkSpeed = RXLinkSpd + "MB/s";
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("Receive Link Speed", receiveLinkSpeed);
						cbm.setPrimaryClip(clip);

						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + receiveLinkSpeed, Toast.LENGTH_SHORT).show();
						return true;
					}
				});
		}

		textview22.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					int networkSpeed = wInfo.getLinkSpeed();
					String networkSpd = networkSpeed + "MB/s";
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Network Speed", networkSpd);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + networkSpd, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview23.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					double totalTXBytes = TrafficStats.getTotalTxBytes();
					double mobileTXBytes = TrafficStats.getMobileTxBytes();
					double wifiTXBytes = totalTXBytes - mobileTXBytes;
					double wifiTXMegabytes = wifiTXBytes / megabyte;
					double wifiTXGigabytes = wifiTXBytes / gigabyte;
					String wifiTXMegabytesStr = String.format(Locale.US, "%.2f", wifiTXMegabytes);
					String wifiTXGigabytesStr = String.format(Locale.US, "%.2f", wifiTXGigabytes);
					String TX_Formatted = wifiTXMegabytesStr + "MB " + "(" + wifiTXGigabytesStr + "GB" + ")";
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Transmitted MBs/GBs", TX_Formatted);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + TX_Formatted, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview24.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					double totalRXBytes = TrafficStats.getTotalRxBytes();
					double mobileRXBytes = TrafficStats.getMobileRxBytes();
					double wifiRXBytes = totalRXBytes - mobileRXBytes;
					double wifiRXMegabytes = wifiRXBytes / megabyte;
					double wifiRXGigabytes = wifiRXBytes / gigabyte;
					String wifiRXMegabytesStr = String.format(Locale.US, "%.2f", wifiRXMegabytes);
					String wifiRXGigabytesStr = String.format(Locale.US, "%.2f", wifiRXGigabytes);
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					String RX_Formatted = wifiRXMegabytesStr + "MB " + "(" + wifiRXGigabytesStr + "GB" + ")";
					ClipData clip = ClipData.newPlainText("Received MBs/GBs", RX_Formatted);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + RX_Formatted, Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview25.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					SupplicantState supState = wInfo.getSupplicantState();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Supplicant State", String.valueOf(supState));
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + String.valueOf(supState), Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		textview26.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					if (mainWifi.is5GHzBandSupported()) {
						ClipData clip = ClipData.newPlainText("5GHz Band Support", "Yes");
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
					} else {
						ClipData clip = ClipData.newPlainText("5GHz Band Support", "No");
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview27.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					if (mainWifi.isP2pSupported()) {
						ClipData clip = ClipData.newPlainText("Wi-Fi Direct Support", "Yes");
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
					} else {
						ClipData clip = ClipData.newPlainText("Wi-Fi Direct Support", "No");
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		textview28.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v)
				{
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					if (mainWifi.isTdlsSupported()) {
						ClipData clip = ClipData.newPlainText("TDLS Support", "Yes");
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
					} else {
						ClipData clip = ClipData.newPlainText("TDLS Support", "No");
						cbm.setPrimaryClip(clip);
						Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
					}
					return true;
				}
			});

		if (Build.VERSION.SDK_INT >= 29) {
			textview29.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v)
					{
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						if (mainWifi.isWpa3SaeSupported()) {
							ClipData clip = ClipData.newPlainText("WPA3 SAE Support", "Yes");
							cbm.setPrimaryClip(clip);
							Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
						} else {
							ClipData clip = ClipData.newPlainText("WPA3 SAE Support", "No");
							cbm.setPrimaryClip(clip);
							Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
						}
						return true;
					}
				});

			textview30.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v)
					{
						ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						if (mainWifi.isWpa3SuiteBSupported()) {
							ClipData clip = ClipData.newPlainText("WPA3 Suite B Support", "Yes");
							cbm.setPrimaryClip(clip);
							Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
						} else {
							ClipData clip = ClipData.newPlainText("WPA3 Suite B Support", "No");
							cbm.setPrimaryClip(clip);
							Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
						}
						return true;
					}
				});
		}
	}

	@Override
	protected void onPause()
	{
		if (isHandlerRunning) {
			handler.removeCallbacks(runnable);
			isHandlerRunning = false;
		}
		unregisterReceiver(WiFiConnectivityReceiver);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		if (!isHandlerRunning) {
			handler.post(runnable);
			isHandlerRunning = true;
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		WiFiConnectivityReceiver = new WiFiConnectivityReceiver();
		registerReceiver(WiFiConnectivityReceiver, filter);
		super.onResume();
	}

	@Override
	protected void onDestroy()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
		super.onDestroy();
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
						textview_ip.setText("Your IP: " + publicIPFetched);
					}

					if (siteReachable == false) {
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
