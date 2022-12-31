package com.truemlgpro.wifiinfo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.preference.PreferenceManager;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import me.anwarshahriar.calligrapher.Calligrapher;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
	private Toolbar toolbar;
	private Menu toolbarMenu;
	private TextView textview_public_ip;
	private TextView textview_ssid;
	private TextView textview_hidden_ssid;
	private TextView textview_bssid;
	private TextView textview_ipv4;
	private TextView textview_ipv6;
	private TextView textview_gateway_ip;
	private TextView textview_hostname;
	private TextView textview_dns1;
	private TextView textview_dns2;
	private TextView textview_subnet_mask;
	private TextView textview_network_id;
	private TextView textview_mac_address;
	private TextView textview_network_interface;
	private TextView textview_loopback_address;
	private TextView textview_localhost;
	private TextView textview_frequency;
	private TextView textview_network_channel;
	private TextView textview_rssi;
	private TextView textview_lease_duration;
	private TextView textview_transmit_link_speed;
	private TextView textview_receive_link_speed;
	private TextView textview_network_speed;
	private TextView textview_transmitted_data;
	private TextView textview_received_data;
	private TextView textview_supplicant_state;
	private TextView textview_5ghz_support;
	private TextView textview_wifi_direct_support;
	private TextView textview_tdls_support;
	private TextView textview_wpa3_sae_support;
	private TextView textview_wpa3_suite_b_support;
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

	// Strings for onInfoGet()
	String info_1 = "";
	String info_2 = "";
	String info_3 = "";
	String info_4 = "";
	String info_5 = "";
	String info_6 = "";
	String info_7 = "";
	String info_8 = "";
	String info_9 = "";
	String info_10 = "";
	String info_11 = "";
	String info_12 = "";
	String info_13 = "";
	String info_14 = "";
	String info_15 = "";
	String info_16 = "";
	String info_17 = "";
	String info_18 = "";
	String info_19 = "";
	String info_20 = "";
	String info_21 = "";
	String info_22 = "";
	String info_23 = "";
	String info_24 = "";
	String info_25 = "";
	String info_26 = "";
	String info_27 = "";
	String info_28 = "";
	String info_29 = "";
	String info_30 = "";

	private final int LocationPermissionCode = 123;

	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private DhcpInfo dhcp;
	private WifiInfo wInfo;
	private WifiManager mainWifiManager;
	private BroadcastReceiver WiFiConnectivityReceiver;
	public static Boolean isServiceRunning = false;
	public static Boolean darkMode = true;
	public static Boolean amoledMode = false;
	public static Boolean startOnBoot = false;
	public static Boolean showNtfc = true;
	public static Boolean visualizeSigStrg = false;
	public static Boolean startStopSrvcScrnState = false;
	public static Boolean colorizeNtfc = false;
	private boolean neverShowGeoDialog = false;
	private boolean neverShowPermissionReqDialog = false;
	private final String keyNeverShowGeoDialog = "NeverShowGeoDialog";
	private final String keyNeverShowPermissionReqDialog = "NeverShowPermissionDialog";
	private boolean isHandlerRunning = false;
	public static String ntfcUpdateInterval = "1000";
	private String cardUpdateInterval = "1000";
	public static String appFont = "fonts/Gilroy-Semibold.ttf";
	private String publicIPFetched;
	private boolean siteReachable = false;
	private String version;
	private final double megabyte = 1024 * 1024;
	private final double gigabyte = 1024 * 1024 * 1024;

	private HandlerThread infoHandlerThread;
	private Handler infoHandler;

	private SharedPreferences.OnSharedPreferenceChangeListener listener;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		/// Shared Preferences ///
		new ThemeManager().initializeThemes(this, getApplicationContext());
		initSharedPrefs();

		super.onCreate(savedInstanceState);
	    /// Splash Screen API ///
		SplashScreen.installSplashScreen(this);
		setContentView(R.layout.main);

		/// Initialize ///
		initViews();
		initOnClickListeners();
		initCopyableText();

		/// Request permissions ///
		if (Build.VERSION.SDK_INT >= 26) {
			if (!isLocationPermissionGranted()) {
				// If user didn't choose to hide the permission request dialog
				if (!new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(keyNeverShowPermissionReqDialog, neverShowPermissionReqDialog)) {
					requestPermissionsOnStart();
				}
			} else {
				/// Notify if GPS is disabled ///
				// If user didn't choose to hide the GPS request dialog
				if (!new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(keyNeverShowGeoDialog, neverShowGeoDialog)) {
					requestGPSFeature();
				}
			}
		}

		/// Services ///
		initForegroundServices();

		/// Create dynamic shortcuts ///
		if (Build.VERSION.SDK_INT >= 26) {
			createShortcuts();
		}

		/// Keep screen on ///
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		/// Get app version ///
		try {
			PackageInfo pi = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		/// Initialize font and ActionBar ///
		initFontAndActionbar();

	    /// Set up FloatingActionMenu ///
		fam.setClosedOnTouchOutside(true);

		/// Set default preferences ///
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		/// Check Wi-Fi Connectivity ///
		initConnectivityCheck();
    }

	private void initFontAndActionbar() {
		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, appFont);
		calligrapher.setFont(this, font, true);

		setSupportActionBar(toolbar);
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(false);
		actionbar.setSubtitle("Release v" + version);
		actionbar.setElevation(20);
	}

	private void getAllNetworkInformation() {
		mainWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wInfo = mainWifiManager.getConnectionInfo();
		dhcp = mainWifiManager.getDhcpInfo();
		String ssid = wInfo.getSSID();
		String bssid;
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		} else {
			bssid = "N/A";
		}
		String ipv4 = getIPv4Address();
		String ipv6 = getIPv6Address();
		String gatewayIP = getGatewayIP();
		String hostname = getHostname();
		String dns1 = intToIp(dhcp.dns1);
		String dns2 = intToIp(dhcp.dns2);
		String subnetMask = intToIp(dhcp.netmask);
		int network_id = wInfo.getNetworkId();
		// Apps cannot access MAC Address on Android 11
		String macAdd;
		if (Build.VERSION.SDK_INT > 29) {
			macAdd = "N/A";
		} else {
			macAdd = getMACAddress();
		}
		String network_interface = getNetworkInterface();
		InetAddress loopbackAddr = InetAddress.getLoopbackAddress();
		String localhostAddr = getLocalhostAddress();
		int leaseTime = dhcp.leaseDuration;
		int leaseTimeHours = dhcp.leaseDuration / 3600;
		int leaseTimeMinutes = dhcp.leaseDuration / 60;
		int freq = wInfo.getFrequency();
		int channel = convertFrequencyToChannel(freq);
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		int networkSpeed = wInfo.getLinkSpeed();
		int TXLinkSpd = 0;
		int RXLinkSpd = 0;
		if (Build.VERSION.SDK_INT >= 29) {
			TXLinkSpd = wInfo.getTxLinkSpeedMbps();
			RXLinkSpd = wInfo.getRxLinkSpeedMbps();
		}
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
		SupplicantState supState = wInfo.getSupplicantState();

		if (ssid.equals("<unknown ssid>")) {
			info_1 = "SSID: N/A";
		} else {
			info_1 = "SSID: " + ssid;
		}

		if (wInfo.getHiddenSSID()) {
			info_2 = "Hidden SSID: Yes";
		} else {
			info_2 = "Hidden SSID: No";
		}

		if (bssid.contains("02:00:00:00:00:00")) {
			info_3 = "BSSID: N/A";
		} else {
			info_3 = "BSSID: " + bssid.toUpperCase();
		}

		info_4 = "IPv4: " + ipv4;
		info_5 = "IPv6: " + ipv6;
		info_6 = "Gateway IP: " + gatewayIP;
		info_7 = "Hostname: " + hostname;
		info_8 = "DNS (1): " + dns1;
		info_9 = "DNS (2): " + dns2;

		if (subnetMask.contains("0.0.0.0")) {
			info_10 = "Subnet Mask: N/A";
		} else {
			info_10 = "Subnet Mask: " + subnetMask;
		}

		if (network_id == -1) {
			info_11 = "Network ID: N/A";
		} else {
			info_11 = "Network ID: " + network_id;
		}

		info_12 = "MAC Address: " + macAdd;
		info_13 = "Network Interface: " + network_interface;
		info_14 = "Loopback Address: " + loopbackAddr;
		info_15 = "Localhost: " + localhostAddr;
		info_16 = "Frequency: " + freq + "MHz";
		info_17 = "Network Channel: " + channel;
		info_18 = "RSSI (Signal Strength): " + RSSIconv + "%" + " (" + rssi + "dBm" + ")";

		if (leaseTime == 0) {
			info_19 = "Lease Duration: N/A";
		} else if (leaseTime >= 3600) {
			info_19 = "Lease Duration: " + leaseTime + "s " + "(" + leaseTimeHours + "h)";
		} else if (leaseTime < 3600) {
			info_19 = "Lease Duration: " + leaseTime + "s " + "(" + leaseTimeMinutes + "m)";
		}

		info_20 = "Transmit Link Speed: " + TXLinkSpd + "MB/s";
		info_21 = "Receive Link Speed: " + RXLinkSpd + "MB/s";
		info_22 = "Network Speed: " + networkSpeed + "MB/s";
		info_23 = "Transmitted MBs/GBs: " + wifiTXMegabytesStr + "MB " + "(" + wifiTXGigabytesStr + "GB" + ")";
		info_24 = "Received MBs/GBs: " + wifiRXMegabytesStr + "MB "  + "(" + wifiRXGigabytesStr + "GB" + ")";
		info_25 = "Supplicant State: " + supState;

		if (mainWifiManager.is5GHzBandSupported()) {
			info_26 = "5GHz Band Support: Yes";
		} else {
			info_26 = "5GHz Band Support: No";
		}

		if (mainWifiManager.isP2pSupported()) {
			info_27 = "Wi-Fi Direct Support: Yes";
		} else {
			info_27 = "Wi-Fi Direct Support: No";
		}

		if (mainWifiManager.isTdlsSupported()) {
			info_28 = "TDLS Support: Yes";
		} else {
			info_28 = "TDLS Support: No";
		}

		if (Build.VERSION.SDK_INT >= 29) {
			if (mainWifiManager.isWpa3SaeSupported()) {
				info_29 = "WPA3 SAE Support: Yes";
			} else {
				info_29 = "WPA3 SAE Support: No";
			}

			if (mainWifiManager.isWpa3SuiteBSupported()) {
				info_30 = "WPA3 Suite B Support: Yes";
			} else {
				info_30 = "WPA3 Suite B Support: No";
			}
		}
	}

	private void updateTextviews() {
		textview_ssid.setText(info_1);
		textview_hidden_ssid.setText(info_2);
		textview_bssid.setText(info_3);
		textview_ipv4.setText(info_4);
		textview_ipv6.setText(info_5);
		textview_gateway_ip.setText(info_6);
		textview_hostname.setText(info_7);
		textview_dns1.setText(info_8);
		textview_dns2.setText(info_9);
		textview_subnet_mask.setText(info_10);
		textview_network_id.setText(info_11);
		textview_mac_address.setText(info_12);
		textview_network_interface.setText(info_13);
		textview_loopback_address.setText(info_14);
		textview_localhost.setText(info_15);
		textview_frequency.setText(info_16);
		textview_network_channel.setText(info_17);
		textview_rssi.setText(info_18);
		textview_lease_duration.setText(info_19);
		textview_network_speed.setText(info_22);
		textview_transmitted_data.setText(info_23);
		textview_received_data.setText(info_24);
		textview_supplicant_state.setText(info_25);
		textview_5ghz_support.setText(info_26);
		textview_wifi_direct_support.setText(info_27);
		textview_tdls_support.setText(info_28);

		if (Build.VERSION.SDK_INT >= 29) {
			textview_transmit_link_speed.setText(info_20);
			textview_receive_link_speed.setText(info_21);
			textview_wpa3_sae_support.setText(info_29);
			textview_wpa3_suite_b_support.setText(info_30);
		} else {
			if (textview_transmit_link_speed.getVisibility() != View.GONE && textview_receive_link_speed.getVisibility() != View.GONE
				&& textview_wpa3_sae_support.getVisibility() != View.GONE && textview_wpa3_suite_b_support.getVisibility() != View.GONE) {
				textview_transmit_link_speed.setVisibility(View.GONE);
				textview_receive_link_speed.setVisibility(View.GONE);
				textview_wpa3_sae_support.setVisibility(View.GONE);
				textview_wpa3_suite_b_support.setVisibility(View.GONE);
			}
		}
	}

	private void startInfoHandlerThread() {
		infoHandlerThread = new HandlerThread("BackgroundInfoHandlerThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		infoHandlerThread.start();
		infoHandler = new Handler(infoHandlerThread.getLooper());
		infoHandler.post(infoRunnable);
	}

	private final Runnable infoRunnable = new Runnable() {
		@Override
		public void run() {
			String keyCardFreq = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval);
			int keyCardFreqFormatted = Integer.parseInt(keyCardFreq);
			getAllNetworkInformation();
			runOnUiThread(() -> updateTextviews());
			infoHandler.postDelayed(infoRunnable, keyCardFreqFormatted);
		}
	};

	private void stopInfoHandlerThread() {
		if (infoHandlerThread != null) {
			infoHandlerThread.quit();
			infoHandlerThread = null;
		}
	}
	
	class WiFiConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			checkWiFiConnectivity();
		}
	}

	private void checkWiFiConnectivity() {
		CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (!WiFiCheck.isConnected()) {
			textview_noconn.setVisibility(View.VISIBLE);
			hideWidgets(); // Hides CardViews and TextViews
			textview_public_ip.setText("Your IP: N/A");
			if (toolbarMenu != null) {
				if (toolbarMenu.findItem(R.id.copy_all).isEnabled()) {
					setToolbarItemEnabled(R.id.copy_all, false);
				}
			}
			if (isHandlerRunning) {
				stopInfoHandlerThread();
				isHandlerRunning = false;
			}
		} else {
			textview_noconn.setVisibility(View.GONE);
			showWidgets(); // Makes CardViews and TextViews visible
			if (toolbarMenu != null) {
				if (!toolbarMenu.findItem(R.id.copy_all).isEnabled()) {
					setToolbarItemEnabled(R.id.copy_all, true);
				}
			}
			if (!isHandlerRunning) {
				startInfoHandlerThread();
				isHandlerRunning = true;
			}
		}
	}

	private void showWidgets() {
		textview_public_ip.setVisibility(View.VISIBLE);
		textview_ssid.setVisibility(View.VISIBLE);
		textview_hidden_ssid.setVisibility(View.VISIBLE);
		textview_bssid.setVisibility(View.VISIBLE);
		textview_ipv4.setVisibility(View.VISIBLE);
		textview_ipv6.setVisibility(View.VISIBLE);
		textview_gateway_ip.setVisibility(View.VISIBLE);
		textview_hostname.setVisibility(View.VISIBLE);
		textview_dns1.setVisibility(View.VISIBLE);
		textview_dns2.setVisibility(View.VISIBLE);
		textview_subnet_mask.setVisibility(View.VISIBLE);
		textview_network_id.setVisibility(View.VISIBLE);
		textview_mac_address.setVisibility(View.VISIBLE);
		textview_network_interface.setVisibility(View.VISIBLE);
		textview_loopback_address.setVisibility(View.VISIBLE);
		textview_localhost.setVisibility(View.VISIBLE);
		textview_frequency.setVisibility(View.VISIBLE);
		textview_network_channel.setVisibility(View.VISIBLE);
		textview_rssi.setVisibility(View.VISIBLE);
		textview_lease_duration.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT < 29 && textview_transmit_link_speed.getVisibility() == View.VISIBLE && textview_receive_link_speed.getVisibility() == View.VISIBLE) {
			textview_transmit_link_speed.setVisibility(View.GONE);
			textview_receive_link_speed.setVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 29 && textview_transmit_link_speed.getVisibility() == View.GONE && textview_receive_link_speed.getVisibility() == View.GONE) {
			textview_transmit_link_speed.setVisibility(View.VISIBLE);
			textview_receive_link_speed.setVisibility(View.VISIBLE);
		}
		textview_network_speed.setVisibility(View.VISIBLE);
		textview_transmitted_data.setVisibility(View.VISIBLE);
		textview_received_data.setVisibility(View.VISIBLE);
		textview_supplicant_state.setVisibility(View.VISIBLE);
		textview_5ghz_support.setVisibility(View.VISIBLE);
		textview_wifi_direct_support.setVisibility(View.VISIBLE);
		textview_tdls_support.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT < 29 && textview_wpa3_sae_support.getVisibility() == View.VISIBLE && textview_wpa3_suite_b_support.getVisibility() == View.VISIBLE) {
			textview_wpa3_sae_support.setVisibility(View.GONE);
			textview_wpa3_suite_b_support.setVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 29 && textview_wpa3_sae_support.getVisibility() == View.GONE && textview_wpa3_suite_b_support.getVisibility() == View.GONE) {
			textview_wpa3_sae_support.setVisibility(View.VISIBLE);
			textview_wpa3_suite_b_support.setVisibility(View.VISIBLE);
		}
		cardview_1.setVisibility(View.VISIBLE);
		cardview_2.setVisibility(View.VISIBLE);
		cardview_3.setVisibility(View.VISIBLE);
		cardview_4.setVisibility(View.VISIBLE);
		cardview_5.setVisibility(View.VISIBLE);
		cardview_6.setVisibility(View.VISIBLE);
		fab_update.setVisibility(View.VISIBLE);
	}
	
	private void hideWidgets() {
		textview_public_ip.setVisibility(View.GONE);
		textview_ssid.setVisibility(View.GONE);
		textview_hidden_ssid.setVisibility(View.GONE);
		textview_bssid.setVisibility(View.GONE);
		textview_ipv4.setVisibility(View.GONE);
		textview_ipv6.setVisibility(View.GONE);
		textview_gateway_ip.setVisibility(View.GONE);
		textview_hostname.setVisibility(View.GONE);
		textview_dns1.setVisibility(View.GONE);
		textview_dns2.setVisibility(View.GONE);
		textview_subnet_mask.setVisibility(View.GONE);
		textview_network_id.setVisibility(View.GONE);
		textview_mac_address.setVisibility(View.GONE);
		textview_network_interface.setVisibility(View.GONE);
		textview_loopback_address.setVisibility(View.GONE);
		textview_localhost.setVisibility(View.GONE);
		textview_frequency.setVisibility(View.GONE);
		textview_network_channel.setVisibility(View.GONE);
		textview_rssi.setVisibility(View.GONE);
		textview_lease_duration.setVisibility(View.GONE);
		textview_transmit_link_speed.setVisibility(View.GONE);
		textview_receive_link_speed.setVisibility(View.GONE);
		textview_network_speed.setVisibility(View.GONE);
		textview_transmitted_data.setVisibility(View.GONE);
		textview_received_data.setVisibility(View.GONE);
		textview_supplicant_state.setVisibility(View.GONE);
		textview_5ghz_support.setVisibility(View.GONE);
		textview_wifi_direct_support.setVisibility(View.GONE);
		textview_tdls_support.setVisibility(View.GONE);
		textview_wpa3_sae_support.setVisibility(View.GONE);
		textview_wpa3_suite_b_support.setVisibility(View.GONE);
		cardview_1.setVisibility(View.GONE);
		cardview_2.setVisibility(View.GONE);
		cardview_3.setVisibility(View.GONE);
		cardview_4.setVisibility(View.GONE);
		cardview_5.setVisibility(View.GONE);
		cardview_6.setVisibility(View.GONE);
		fab_update.setVisibility(View.GONE);
	}
	
	private boolean isLocationEnabled() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	private boolean hasPermissions(Context context, String... permissions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			for (String permission: permissions) {
				if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

	private String getPublicIPAddress() {
		String publicIP = "";
		try {
			Scanner scanner = new Scanner(new URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A");
			publicIP = scanner.next();
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return publicIP;
	}

	private boolean isReachable(String url) {
		boolean reachable;
		int response_code;

		try {
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(3000);
			connection.connect();
			response_code = connection.getResponseCode();
			connection.disconnect();
			reachable = response_code == 200;
		} catch (Exception e) {
			reachable = false;
		}
		return reachable;
	}

	@SuppressWarnings("deprecation")
	class PublicIPRunnable implements Runnable {
		@Override
		public void run() {
			new AsyncTask<String, Void, Void>() {
				@Override
				protected Void doInBackground(String[] voids) {
					String url = "https://api.ipify.org";
					siteReachable = isReachable(url);
					if (siteReachable) {
						publicIPFetched = getPublicIPAddress();
					} else {
						publicIPFetched = "N/A";
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					textview_public_ip.setText("Your IP: " + publicIPFetched);
				}
			}.execute();

			Handler handlerEnableFAB = new Handler(Looper.getMainLooper());
			handlerEnableFAB.postDelayed(() -> fab_update.setEnabled(true), 5000);
		}
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

	private String getGatewayIP() {
		if (!WiFiCheck.isConnected()) {
			return "0.0.0.0";
		}
		mainWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifiManager.getDhcpInfo();
		int gatewayIP = dhcp.gateway;
		return intToIp(gatewayIP);
	}
	
	private String getIPv4Address() {
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
			Log.e("getIPv4Address()", ex.toString());
		}
		return null;
	}

	private String getIPv6Address() {
		try {
			for (Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces(); enumNetworkInterfaces.hasMoreElements();) {
				NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("getIPv6Address()", ex.toString());
		}
		return null;
	}

	private String getHostname() {
		String hostname = null;
		try {
			InetAddress hostnameAddr = InetAddress.getByName(getGatewayIP());
			hostname = hostnameAddr.getHostName();
			return hostname;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return hostname;
	}

	private String getNetworkInterface() {
		String interfc = null;
		for (Network network : CM.getAllNetworks()) {
			LinkProperties linkProp = CM.getLinkProperties(network);
			interfc = linkProp.getInterfaceName();
		}
		return interfc;
	}

	private String getLocalhostAddress() {
		String localHost_converted = null;
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			localHost_converted = localHost.toString();
			return localHost_converted;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return localHost_converted;
	}

	private String intToIp(int ipInt) {
		return ((ipInt & 0xFF) + "."
		+ ((ipInt >> 8) & 0xFF) + "."
		+ ((ipInt >> 16) & 0xFF) + "."
		+ ((ipInt >> 24) & 0xFF));
	}

	private int convertFrequencyToChannel(int freq) {
		if (freq >= 2412 && freq <= 2484) {
			return (freq - 2412) / 5 + 1;
		} else if (freq >= 5170 && freq <= 5825) {
			return (freq - 5170) / 5 + 34;
		} else {
			return -1;
		}
	}

	private void requestGPSFeature() {
		// Notify User if GPS is disabled
		if (!isLocationEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Location is Disabled")
				.setPositiveButton("Enable", (dialog, id) -> {
					if (Build.VERSION.SDK_INT == 26) {
						Toast.makeText(this, "Enable Location to show SSID and BSSID of current network", Toast.LENGTH_LONG).show();
					} else if (Build.VERSION.SDK_INT >= 27) {
						Toast.makeText(this, "Enable Location to show SSID, BSSID and Network ID of current network", Toast.LENGTH_LONG).show();
					}
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				})
				.setNegativeButton("Cancel", (dialog, id) -> {
					if (Build.VERSION.SDK_INT == 26) {
						Toast.makeText(this, "SSID and BSSID of current network won't be shown", Toast.LENGTH_LONG).show();
					} else if (Build.VERSION.SDK_INT >= 27) {
						Toast.makeText(this, "SSID, BSSID and Network ID of current network won't be shown", Toast.LENGTH_LONG).show();
					}
					dialog.cancel();
				})
				.setNeutralButton("Don't Show Again", (dialog, id) -> new SharedPreferencesManager(getApplicationContext()).storeBoolean(keyNeverShowGeoDialog, neverShowGeoDialog = true));
			if (Build.VERSION.SDK_INT == 26) {
				builder.setMessage("Wi-Fi Info needs Location to show SSID (network name) and BSSID (network MAC address) " +
						"on Android 8+\n\nClick \"Enable\" to grant Wi-Fi Info permission to show SSID and BSSID");
			} else if (Build.VERSION.SDK_INT >= 27) {
				builder.setMessage("Wi-Fi Info needs Location to show SSID (network name) and BSSID (network MAC address) and Network ID " +
						"on Android 8.1\n\nClick \"Enable\" to grant Wi-Fi Info permission to show SSID, BSSID and Network ID");
			}
			builder.setCancelable(false);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}

	private void requestPermissionsOnStart() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Permission required!")
			.setMessage("Location permission is necessary to show SSID, BSSID and Network ID on devices running Android 8.1 and higher")
			.setPositiveButton("Ok", (dialog, id) -> {
				// Android 8.1 - Android 10
				String[] ForegroundCoarseLocationPermission_API27 = {Manifest.permission.ACCESS_COARSE_LOCATION};
				// Android 11+
				String[] ForegroundFineLocationPermission_API30 = {Manifest.permission.ACCESS_FINE_LOCATION};
				if (Build.VERSION.SDK_INT >= 27 && Build.VERSION.SDK_INT < 30) {
					ActivityCompat.requestPermissions(MainActivity.this, ForegroundCoarseLocationPermission_API27, LocationPermissionCode);
				} else if (Build.VERSION.SDK_INT >= 30) {
					ActivityCompat.requestPermissions(MainActivity.this, ForegroundFineLocationPermission_API30, LocationPermissionCode);
				}
			})
			.setNegativeButton("No Thanks", null)
			.setNeutralButton("Don't Show Again", (dialog, id) -> new SharedPreferencesManager(getApplicationContext()).storeBoolean(keyNeverShowPermissionReqDialog, neverShowPermissionReqDialog = true))
			.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}

	private boolean isLocationPermissionGranted() {
		// In Android 8.1 (API 27) - 11 (API 30) ACCESS_COARSE_LOCATION needs to be granted to access network information
		// Android 12+ (API 31) needs ACCESS_FINE_LOCATION to be granted though
		boolean permissionGranted = false;
		if (Build.VERSION.SDK_INT >= 27 && Build.VERSION.SDK_INT < 31) {
			// Android 8.1 - Android 11
			permissionGranted = hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION);
		} else if (Build.VERSION.SDK_INT >= 31) {
			// Android 12+
			permissionGranted = hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION);
		}
		return permissionGranted;
	}

	@RequiresApi(api = Build.VERSION_CODES.N_MR1)
	private void createShortcuts() {
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

	private void initSharedPrefs() {
		listener = (prefs, key) -> {
			if (key.equals(SettingsActivity.KEY_PREF_SWITCH)) {
				if (prefs.getBoolean(SettingsActivity.KEY_PREF_SWITCH, true)) {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_SWITCH, darkMode = true);
				} else {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_SWITCH, darkMode = false);
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_AMOLED_CHECK)) {
				if (prefs.getBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, false)) {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, amoledMode = true);
				} else {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, amoledMode = false);
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_BOOT_SWITCH)) {
				if (prefs.getBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, false)) {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, startOnBoot = true);
				} else {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, startOnBoot = false);
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_NTFC_SWITCH)) {
				if (prefs.getBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, true)) {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, showNtfc = true);
				} else {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, showNtfc = false);
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK)) {
				if (prefs.getBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, false)) {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, visualizeSigStrg = true);
				} else {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, visualizeSigStrg = false);
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK)) {
				if (prefs.getBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, false)) {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, startStopSrvcScrnState = true);
				} else {
					new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, startStopSrvcScrnState = false);
				}
				Intent restartConnectionStateService = new Intent(MainActivity.this, ConnectionStateService.class);
				Intent restartNotificationService = new Intent(MainActivity.this, NotificationService.class);
				if (ConnectionStateService.isNotificationServiceRunning) {
					stopService(restartNotificationService);
				}
				if (ConnectionStateService.isConnectionStateServiceRunning) {
					stopService(restartConnectionStateService);
					if (Build.VERSION.SDK_INT < 26) {
						startService(restartConnectionStateService);
					} else {
						startForegroundService(restartConnectionStateService);
					}
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_CLR_CHECK)) {
				if (prefs.getBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, false)) {
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
				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Gilroy-Semibold.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Gilroy-Semibold.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/CircularStd-Bold.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/CircularStd-Bold.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Comfortaa-Regular.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Comfortaa-Regular.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/CondellBio-Medium.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/CondellBio-Medium.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/FilsonPro-Regular.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/FilsonPro-Regular.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Hellix-Medium.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Hellix-Medium.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Moderat-Regular.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Moderat-Regular.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Newson-Medium.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Newson-Medium.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/NoirText-Bold.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/NoirText-Bold.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Poligon-Regular.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Poligon-Regular.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/ProximaSoft-Medium.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/ProximaSoft-Medium.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Squalo-Regular.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Squalo-Regular.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Tomkin-Regular.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Tomkin-Regular.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Urbani-Regular.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, appFont = "fonts/Urbani-Regular.ttf");
				}
			}
		};

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}
	
	private void initViews() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_public_ip = (TextView) findViewById(R.id.textview_public_ip);
		textview_ssid = (TextView) findViewById(R.id.textview_ssid);
		textview_hidden_ssid = (TextView) findViewById(R.id.textview_hidden_ssid);
		textview_bssid = (TextView) findViewById(R.id.textview_bssid);
		textview_ipv4 = (TextView) findViewById(R.id.textview_ipv4);
		textview_ipv6 = (TextView) findViewById(R.id.textview_ipv6);
		textview_gateway_ip = (TextView) findViewById(R.id.textview_gateway_ip);
		textview_hostname = (TextView) findViewById(R.id.textview_hostname);
		textview_dns1 = (TextView) findViewById(R.id.textview_dns1);
		textview_dns2 = (TextView) findViewById(R.id.textview_dns2);
		textview_subnet_mask = (TextView) findViewById(R.id.textview_subnet_mask);
		textview_network_id = (TextView) findViewById(R.id.textview_network_id);
		textview_mac_address = (TextView) findViewById(R.id.textview_mac_address);
		textview_network_interface = (TextView) findViewById(R.id.textview_network_interface);
		textview_loopback_address = (TextView) findViewById(R.id.textview_loopback_address);
		textview_localhost = (TextView) findViewById(R.id.textview_localhost);
		textview_frequency = (TextView) findViewById(R.id.textview_frequency);
		textview_network_channel = (TextView) findViewById(R.id.textview_network_channel);
		textview_rssi = (TextView) findViewById(R.id.textview_rssi);
		textview_lease_duration = (TextView) findViewById(R.id.textview_lease_duration);
		textview_transmit_link_speed = (TextView) findViewById(R.id.textview_transmit_link_speed);
		textview_receive_link_speed = (TextView) findViewById(R.id.textview_receive_link_speed);
		textview_network_speed = (TextView) findViewById(R.id.textview_network_speed);
		textview_transmitted_data = (TextView) findViewById(R.id.textview_transmitted_data);
		textview_received_data = (TextView) findViewById(R.id.textview_received_data);
		textview_supplicant_state = (TextView) findViewById(R.id.textview_supplicant_state);
		textview_5ghz_support = (TextView) findViewById(R.id.textview_5ghz_support);
		textview_wifi_direct_support = (TextView) findViewById(R.id.textview_wifi_direct_support);
		textview_tdls_support = (TextView) findViewById(R.id.textview_tdls_support);
		textview_wpa3_sae_support = (TextView) findViewById(R.id.textview_wpa3_sae_support);
		textview_wpa3_suite_b_support = (TextView) findViewById(R.id.textview_wpa3_suite_b_support);
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

	private void initForegroundServices() {
		boolean keyNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, showNtfc);
		if (keyNtfc) {
			if (!isServiceRunning) {
				Intent ConnectionStateServiceIntent = new Intent(MainActivity.this, ConnectionStateService.class);
				if (Build.VERSION.SDK_INT < 26) {
					startService(ConnectionStateServiceIntent);
				} else {
					startForegroundService(ConnectionStateServiceIntent);
				}
				isServiceRunning = true;
			}
		} else {
			if (isServiceRunning) {
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
	}

	private void initConnectivityCheck() {
		CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (!WiFiCheck.isConnected()) {
			textview_noconn.setVisibility(View.VISIBLE);
			hideWidgets(); // Hides CardViews and TextViews
			textview_public_ip.setText("Your IP: N/A");
		} else {
			textview_noconn.setVisibility(View.GONE);
			showWidgets(); // Makes CardViews and TextViews visible
		}
	}

	private void initOnClickListeners() {
		fab_info.setOnClickListener(v -> {
			Intent intent_info = new Intent(MainActivity.this, DevInfoActivity.class);
			startActivity(intent_info);
			fam.close(true);
		});

		fab_discord.setOnClickListener(v -> {
			Intent intent_discord = new Intent(MainActivity.this, DiscordServersActivity.class);
			startActivity(intent_discord);
			fam.close(true);
		});

		fab_supporters.setOnClickListener(v -> {
			Intent intent_supporters = new Intent(MainActivity.this, SupportersActivity.class);
			startActivity(intent_supporters);
			fam.close(true);
		});
		
		fab_tools.setOnClickListener(v -> {
			Intent intent_url_to_ip = new Intent(MainActivity.this, ToolsActivity.class);
			startActivity(intent_url_to_ip);
			fam.close(true);
		});

		fab_settings.setOnClickListener(v -> {
			finish();
			overridePendingTransition(0, 0);
			Intent intent_settings = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(intent_settings);
			fam.close(true);
		});

		fab_update.setOnClickListener(v -> {
			fab_update.setEnabled(false);
			PublicIPRunnable runnableIP = new PublicIPRunnable();
			new Thread(runnableIP).start();
		});
	}
	
	private void initCopyableText() {
		textview_public_ip.setOnLongClickListener(v -> {
			if (textview_public_ip.getText().equals("Your IP: N/A")) {
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
		});

		textview_ssid.setOnLongClickListener(v -> {
			if (textview_ssid.getText().equals("SSID: N/A")) {
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
		});

		textview_hidden_ssid.setOnLongClickListener(v -> {
			if (textview_hidden_ssid.getText().equals("Hidden SSID: Yes")) {
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
		});

		textview_bssid.setOnLongClickListener(v -> {
			if (textview_bssid.getText().equals("BSSID: N/A")) {
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
		});

		textview_ipv4.setOnLongClickListener(v -> {
			String ipv4 = getIPv4Address();
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("IPv4", ipv4);
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + ipv4, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_ipv6.setOnLongClickListener(v -> {
			String ipv6 = getIPv6Address();
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("IPv6", ipv6);
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + ipv6, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_gateway_ip.setOnLongClickListener(v -> {
			String gatewayIP = getGatewayIP();
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Gateway IP", gatewayIP);
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + gatewayIP, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_hostname.setOnLongClickListener(v -> {
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
		});

		textview_dns1.setOnLongClickListener(v -> {
			dhcp = mainWifiManager.getDhcpInfo();
			String dns1 = intToIp(dhcp.dns1);
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("DNS (1)", dns1);
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + dns1, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_dns2.setOnLongClickListener(v -> {
			dhcp = mainWifiManager.getDhcpInfo();
			String dns2 = intToIp(dhcp.dns2);
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("DNS (2)", dns2);
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + dns2, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_subnet_mask.setOnLongClickListener(v -> {
			if (textview_subnet_mask.getText().equals("Subnet Mask: N/A")) {
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Subnet Mask", "N/A");
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "N/A", Toast.LENGTH_SHORT).show();
			} else {
				dhcp = mainWifiManager.getDhcpInfo();
				String subnetMask = intToIp(dhcp.netmask);
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Subnet Mask", subnetMask);
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + subnetMask, Toast.LENGTH_SHORT).show();
			}
			return true;
		});

		textview_network_id.setOnLongClickListener(v -> {
			if (textview_network_id.getText().equals("Network ID: N/A")) {
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
		});

		textview_mac_address.setOnLongClickListener(v -> {
			String macAddress = getMACAddress();
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("MAC Address", macAddress);
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + macAddress, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_network_interface.setOnLongClickListener(v -> {
			for (Network network : CM.getAllNetworks()) {
				LinkProperties linkProp = CM.getLinkProperties(network);
				String interfc = linkProp.getInterfaceName();
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Network Interface", interfc);
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + interfc, Toast.LENGTH_SHORT).show();
			}
			return true;
		});

		textview_loopback_address.setOnLongClickListener(v -> {
			InetAddress loopbackAddr = InetAddress.getLoopbackAddress();
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Loopback Address", loopbackAddr.toString());
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + loopbackAddr, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_localhost.setOnLongClickListener(v -> {
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
		});

		textview_frequency.setOnLongClickListener(v -> {
			int freq = wInfo.getFrequency();
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Frequency", freq + "MHz");
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + freq + "MHz", Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_network_channel.setOnLongClickListener(v -> {
			int freq = wInfo.getFrequency();
			int channel = convertFrequencyToChannel(freq);
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Network Channel", String.valueOf(channel));
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + channel, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_rssi.setOnLongClickListener(v -> {
			int rssi = wInfo.getRssi();
			int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("RSSI", RSSIconv + "%" + " (" + rssi + "dBm" + ")");
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")", Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_lease_duration.setOnLongClickListener(v -> {
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
		});

		if (Build.VERSION.SDK_INT >= 29) {
			textview_transmit_link_speed.setOnLongClickListener(v -> {
				int TXLinkSpd = wInfo.getTxLinkSpeedMbps();
				String transmitLinkSpeed = TXLinkSpd + "MB/s";
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Transmit Link Speed", transmitLinkSpeed);
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + transmitLinkSpeed, Toast.LENGTH_SHORT).show();
				return true;
			});

			textview_receive_link_speed.setOnLongClickListener(v -> {
				int RXLinkSpd = wInfo.getRxLinkSpeedMbps();
				String receiveLinkSpeed = RXLinkSpd + "MB/s";
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Receive Link Speed", receiveLinkSpeed);
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + receiveLinkSpeed, Toast.LENGTH_SHORT).show();
				return true;
			});
		}

		textview_network_speed.setOnLongClickListener(v -> {
			int networkSpeed = wInfo.getLinkSpeed();
			String networkSpd = networkSpeed + "MB/s";
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Network Speed", networkSpd);
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + networkSpd, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_transmitted_data.setOnLongClickListener(v -> {
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
		});

		textview_received_data.setOnLongClickListener(v -> {
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
		});

		textview_supplicant_state.setOnLongClickListener(v -> {
			SupplicantState supState = wInfo.getSupplicantState();
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Supplicant State", String.valueOf(supState));
			cbm.setPrimaryClip(clip);
			Toast.makeText(getBaseContext(), "Copied to Clipboard: " + supState, Toast.LENGTH_SHORT).show();
			return true;
		});

		textview_5ghz_support.setOnLongClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (mainWifiManager.is5GHzBandSupported()) {
				ClipData clip = ClipData.newPlainText("5GHz Band Support", "Yes");
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
			} else {
				ClipData clip = ClipData.newPlainText("5GHz Band Support", "No");
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
			}
			return true;
		});

		textview_wifi_direct_support.setOnLongClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (mainWifiManager.isP2pSupported()) {
				ClipData clip = ClipData.newPlainText("Wi-Fi Direct Support", "Yes");
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
			} else {
				ClipData clip = ClipData.newPlainText("Wi-Fi Direct Support", "No");
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
			}
			return true;
		});

		textview_tdls_support.setOnLongClickListener(v -> {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (mainWifiManager.isTdlsSupported()) {
				ClipData clip = ClipData.newPlainText("TDLS Support", "Yes");
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
			} else {
				ClipData clip = ClipData.newPlainText("TDLS Support", "No");
				cbm.setPrimaryClip(clip);
				Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
			}
			return true;
		});

		if (Build.VERSION.SDK_INT >= 29) {
			textview_wpa3_sae_support.setOnLongClickListener(v -> {
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				if (mainWifiManager.isWpa3SaeSupported()) {
					ClipData clip = ClipData.newPlainText("WPA3 SAE Support", "Yes");
					cbm.setPrimaryClip(clip);
					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
				} else {
					ClipData clip = ClipData.newPlainText("WPA3 SAE Support", "No");
					cbm.setPrimaryClip(clip);
					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
				}
				return true;
			});

			textview_wpa3_suite_b_support.setOnLongClickListener(v -> {
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				if (mainWifiManager.isWpa3SuiteBSupported()) {
					ClipData clip = ClipData.newPlainText("WPA3 Suite B Support", "Yes");
					cbm.setPrimaryClip(clip);
					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "Yes", Toast.LENGTH_SHORT).show();
				} else {
					ClipData clip = ClipData.newPlainText("WPA3 Suite B Support", "No");
					cbm.setPrimaryClip(clip);
					Toast.makeText(getBaseContext(), "Copied to Clipboard: " + "No", Toast.LENGTH_SHORT).show();
				}
				return true;
			});
		}
	}

	private void copyAllTextviews() {
		ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		StringBuilder strB = new StringBuilder();
		strB.append(info_1).append("\n").append(info_2).append("\n").append(info_3).append("\n").append(info_4).append("\n")
			.append(info_5).append("\n").append(info_6).append("\n").append(info_7).append("\n").append(info_8).append("\n")
			.append(info_9).append("\n").append(info_10).append("\n").append(info_11).append("\n").append(info_12).append("\n")
			.append(info_13).append("\n").append(info_14).append("\n").append(info_15).append("\n").append(info_16).append("\n")
			.append(info_17).append("\n").append(info_18).append("\n").append(info_19).append("\n");
		if (Build.VERSION.SDK_INT >= 29) {
			strB.append(info_20).append("\n").append(info_21).append("\n");
		}
		strB.append(info_22).append("\n").append(info_23).append("\n").append(info_24).append("\n").append(info_25).append("\n")
			.append(info_26).append("\n").append(info_27).append("\n").append(info_28).append("\n");
		if (Build.VERSION.SDK_INT >= 29) {
			strB.append(info_29).append("\n").append(info_30);
		}

		ClipData clipData = ClipData.newPlainText("all_info_text", strB);
		cbm.setPrimaryClip(clipData);
		Toast.makeText(getBaseContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (isHandlerRunning) {
			stopInfoHandlerThread();
			isHandlerRunning = false;
		}
		unregisterReceiver(WiFiConnectivityReceiver);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (!isHandlerRunning) {
			startInfoHandlerThread();
			isHandlerRunning = true;
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		WiFiConnectivityReceiver = new WiFiConnectivityReceiver();
		registerReceiver(WiFiConnectivityReceiver, filter);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (Build.VERSION.SDK_INT >= 30) {
			if (requestCode == LocationPermissionCode) {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Background Location Permission (Optional)")
							.setMessage("Due to the changes in Android 11+ you need to go to Settings to enable it" + "\n" + "Once Background Location permission is granted you'll be able to see the SSID in notification even if you close the app")
							.setPositiveButton("Ok", (dialog, id) -> {
								Toast.makeText(MainActivity.this, "Go to Permissions -> Location", Toast.LENGTH_LONG).show();
								Toast.makeText(MainActivity.this, "Select \"Allow all the time\"", Toast.LENGTH_LONG).show();
								Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								Uri uri = Uri.fromParts("package", getPackageName(), null);
								intent.setData(uri);
								startActivity(intent);
							})
							.setNegativeButton("No Thanks", null)
							.setCancelable(false);
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Are you sure?")
				.setMessage("Do you want to exit?")
				.setPositiveButton("Exit", (dialog, id) -> finish())
				.setNegativeButton("Cancel", null);
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setToolbarItemEnabled(int item, Boolean enabled) {
		if (toolbarMenu != null) {
			toolbarMenu.findItem(item).setEnabled(enabled);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_action_bar_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toolbarMenu = menu;
		checkWiFiConnectivity();
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.github) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TrueMLGPro/Wi-Fi_Info"));
			startActivity(intent);
		} else if (id == R.id.copy_all) {
			copyAllTextviews();
		}
		return true;
    }
}
