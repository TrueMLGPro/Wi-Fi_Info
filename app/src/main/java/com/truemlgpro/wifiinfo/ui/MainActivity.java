package com.truemlgpro.wifiinfo.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.net.wifi.ScanResult;
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
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
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
import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.services.NotificationService;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
	private RelativeLayout relativelayout_wifi_standard;
	private TextView textview_wifi_standard;
	private TextView textview_frequency;
	private TextView textview_distance;
	private TextView textview_network_channel;
	private TextView textview_rssi;
	private TextView textview_lease_duration;
	private RelativeLayout relativelayout_network_speed;
	private TextView textview_network_speed;
	private RelativeLayout relativelayout_network_speed_legacy;
	private TextView textview_network_speed_legacy;
	private TextView textview_transmitted_data;
	private TextView textview_received_data;
	private TextView textview_dns1;
	private TextView textview_dns2;
	private TextView textview_subnet_mask;
	private TextView textview_broadcast_address;
	private TextView textview_network_id;
	private TextView textview_mac_address;
	private TextView textview_network_interface;
	private TextView textview_loopback_address;
	private TextView textview_localhost;
	private TextView textview_wpa_supplicant_state;
	private TextView textview_5ghz_support;
	private TextView textview_6ghz_support;
	private TextView textview_60ghz_support;
	private TextView textview_wifi_direct_support;
	private TextView textview_tdls_support;
	private TextView textview_wpa3_sae_support;
	private TextView textview_wpa3_suite_b_support;
	private TextView textview_noconn;
	private CardView cardview_1;
	private CardView cardview_ip;
	private CardView cardview_2;
	private CardView cardview_3;
	private CardView cardview_4;
	private CardView cardview_5;
	private CardView cardview_6;
	private FloatingActionMenu fam;
	private FloatingActionButton fab_tools;
	private FloatingActionButton fab_settings;
	private FloatingActionButton fab_update;

	// Strings for getAllNetworkInformation()
	String info_ssid = "";
	String info_hidden_ssid = "";
	String info_bssid = "";
	String info_ipv4 = "";
	String info_ipv6 = "";
	String info_gateway_ip = "";
	String info_hostname = "";
	String info_wifi_standard = "";
	String info_frequency = "";
	String info_network_channel = "";
	String info_rssi = "";
	String info_distance = "";
	String info_lease_time = "";
	String info_network_speed = "";
	String info_network_speed_legacy = "";
	String info_transmitted_data = "";
	String info_received_data = "";
	String info_dns1 = "";
	String info_dns2 = "";
	String info_subnet_mask = "";
	String info_broadcast_addr = "";
	String info_network_id = "";
	String info_mac_addr = "";
	String info_network_interface = "";
	String info_loopback_addr = "";
	String info_localhost_addr = "";
	String info_supplicant_state = "";
	String info_5ghz_support = "";
	String info_6ghz_support = "";
	String info_60ghz_support = "";
	String info_p2p_support = "";
	String info_tdls_support = "";
	String info_wpa3_sae_support = "";
	String info_wpa3_suite_b_support = "";

	private final int LocationPermissionCode = 123;

	private ConnectivityManager connectivityManager;
	private NetworkInfo WiFiCheck;
	private DhcpInfo dhcpInfo;
	private WifiInfo wifiInfo;
	private WifiManager wifiManager;
	private BroadcastReceiver WiFiConnectivityReceiver;
	public static Boolean isServiceRunning = false;
	public static final Boolean darkMode = true;
	public static final Boolean amoledMode = false;
	public static final Boolean keepScreenOn = true;
	public static final Boolean startOnBoot = false;
	public static final Boolean showNtfc = true;
	public static final Boolean visualizeSigStrg = false;
	public static final Boolean startStopSrvcScrnState = false;
	public static final Boolean colorizeNtfc = false;
	private final boolean neverShowGeoDialog = false;
	private final boolean neverShowPermissionReqDialog = false;
	private final String keyNeverShowGeoDialog = "NeverShowGeoDialog";
	private final String keyNeverShowPermissionReqDialog = "NeverShowPermissionDialog";
	private boolean isHandlerRunning = false;
	public static final String ntfcUpdateInterval = "1000";
	private final String cardUpdateInterval = "1000";
	public static final String appFont = "fonts/Gilroy-Semibold.ttf";
	public static final String appLang = "default_lang";
	private final double megabyte = 1024 * 1024;
	private final double gigabyte = 1024 * 1024 * 1024;

	private HandlerThread infoHandlerThread;
	private Handler infoHandler;

	private int keyCardFreqFormatted = 1000;

	private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefChangeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		/// Shared Preferences ///
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());
		initSharedPrefs();

		/// Splash Screen API ///
		SplashScreen.installSplashScreen(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/// Initialize ///
		initViews();
		initOnClickListeners();
		initCopyableText();

		/// Handler Init ///
		keyCardFreqFormatted = Integer.parseInt(new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_CARD_FREQ, cardUpdateInterval));

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
		KeepScreenOnManager.init(getWindow(), getApplicationContext());

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
		actionbar.setElevation(20);
	}

	private void initViews() {
		toolbar = findViewById(R.id.toolbar);
		textview_public_ip = findViewById(R.id.textview_public_ip);
		textview_ssid = findViewById(R.id.textview_ssid_value);
		textview_hidden_ssid = findViewById(R.id.textview_hidden_ssid_value);
		textview_bssid = findViewById(R.id.textview_bssid_value);
		textview_ipv4 = findViewById(R.id.textview_ipv4_value);
		textview_ipv6 = findViewById(R.id.textview_ipv6_value);
		textview_gateway_ip = findViewById(R.id.textview_gateway_ip_value);
		textview_hostname = findViewById(R.id.textview_hostname_value);
		relativelayout_wifi_standard = findViewById(R.id.relativelayout_wifi_standard);
		textview_wifi_standard = findViewById(R.id.textview_wifi_standard_value);
		textview_frequency = findViewById(R.id.textview_frequency_value);
		textview_network_channel = findViewById(R.id.textview_network_channel_value);
		textview_rssi = findViewById(R.id.textview_rssi_value);
		textview_distance = findViewById(R.id.textview_distance_value);
		textview_lease_duration = findViewById(R.id.textview_ip_lease_duration_value);
		relativelayout_network_speed = findViewById(R.id.relativelayout_network_speed);
		textview_network_speed = findViewById(R.id.textview_network_speed_value);
		relativelayout_network_speed_legacy = findViewById(R.id.relativelayout_network_speed_legacy);
		textview_network_speed_legacy = findViewById(R.id.textview_network_speed_legacy_value);
		textview_transmitted_data = findViewById(R.id.textview_transmitted_data_value);
		textview_received_data = findViewById(R.id.textview_received_data_value);
		textview_dns1 = findViewById(R.id.textview_dns1_value);
		textview_dns2 = findViewById(R.id.textview_dns2_value);
		textview_subnet_mask = findViewById(R.id.textview_subnet_mask_value);
		textview_broadcast_address = findViewById(R.id.textview_broadcast_address_value);
		textview_network_id = findViewById(R.id.textview_network_id_value);
		textview_mac_address = findViewById(R.id.textview_mac_address_value);
		textview_network_interface = findViewById(R.id.textview_network_interface_value);
		textview_loopback_address = findViewById(R.id.textview_loopback_address_value);
		textview_localhost = findViewById(R.id.textview_localhost_value);
		textview_wpa_supplicant_state = findViewById(R.id.textview_wpa_supplicant_state_value);
		textview_5ghz_support = findViewById(R.id.textview_5ghz_support_value);
		textview_6ghz_support = findViewById(R.id.textview_6ghz_support_value);
		textview_60ghz_support = findViewById(R.id.textview_60ghz_support_value);
		textview_wifi_direct_support = findViewById(R.id.textview_wifi_direct_support_value);
		textview_tdls_support = findViewById(R.id.textview_tdls_support_value);
		textview_wpa3_sae_support = findViewById(R.id.textview_wpa3_sae_support_value);
		textview_wpa3_suite_b_support = findViewById(R.id.textview_wpa3_suite_b_support_value);
		textview_noconn = findViewById(R.id.textview_noconn);
		cardview_1 = findViewById(R.id.cardview_1);
		cardview_ip = findViewById(R.id.cardview_ip);
		cardview_2 = findViewById(R.id.cardview_2);
		cardview_3 = findViewById(R.id.cardview_3);
		cardview_4 = findViewById(R.id.cardview_4);
		cardview_5 = findViewById(R.id.cardview_5);
		cardview_6 = findViewById(R.id.cardview_6);
		fam = findViewById(R.id.fam);
		fab_tools = findViewById(R.id.menu_item_1);
		fab_settings = findViewById(R.id.menu_item_2);
		fab_update = findViewById(R.id.fab_update_ip);
	}

	private void getAllNetworkInformation() {
		wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wifiInfo = wifiManager.getConnectionInfo();
		dhcpInfo = wifiManager.getDhcpInfo();
		String ssid = wifiInfo.getSSID();
		String bssid;
		if (wifiInfo.getBSSID() != null) {
			bssid = wifiInfo.getBSSID().toUpperCase();
		} else {
			bssid = getString(R.string.na);
		}
		String ipv4 = getIPv4Address();
		String ipv6 = getIPv6Address();
		String gatewayIp = getGatewayIP();
		String hostname = getHostname();
		String wifiStandard = "";
		if (Build.VERSION.SDK_INT >= 30) {
			wifiStandard = getWifiStandard();
		}
		int freq = wifiInfo.getFrequency();
		String networkChannel = String.valueOf(convertFrequencyToChannel(freq));
		int rssi = wifiInfo.getRssi();
		int rssiConverted = WifiManager.calculateSignalLevel(rssi, 101);
		String distanceFromRssiRounded = String.format("~%.1fm", freqRssiToDistance(freq, rssi));
		int networkSpeed = 0;
		int TXLinkSpd = 0;
		int RXLinkSpd = 0;
		if (Build.VERSION.SDK_INT >= 29) {
			TXLinkSpd = wifiInfo.getTxLinkSpeedMbps();
			RXLinkSpd = wifiInfo.getRxLinkSpeedMbps();
		} else {
			networkSpeed = wifiInfo.getLinkSpeed();
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
		String dns1 = intToIp(dhcpInfo.dns1);
		String dns2 = intToIp(dhcpInfo.dns2);
		String subnetMask = getSubnetMask();
		String broadcastAddr = getBroadcastAddr();
		String networkId = String.valueOf(wifiInfo.getNetworkId());
		// Apps cannot access MAC address on Android 11+
		String macAddr;
		if (Build.VERSION.SDK_INT > 29) {
			macAddr = getString(R.string.na);
		} else {
			macAddr = getMACAddress();
		}
		String networkInterface = getNetworkInterface();
		String loopbackAddr = String.valueOf(InetAddress.getLoopbackAddress());
		String localhostAddr = getLocalhostAddress();
		int leaseTime = dhcpInfo.leaseDuration;
		int leaseTimeHours = dhcpInfo.leaseDuration / 3600;
		int leaseTimeMinutes = dhcpInfo.leaseDuration / 60;
		String supplicantState = String.valueOf(wifiInfo.getSupplicantState());

		if (ssid.equals("<unknown ssid>")) {
			info_ssid = getString(R.string.na);
		} else {
			info_ssid = ssid.replaceAll("^\"|\"$", "");
		}

		if (wifiInfo.getHiddenSSID()) {
			info_hidden_ssid = getString(R.string.yes);
		} else {
			info_hidden_ssid = getString(R.string.no);
		}

		if (bssid.contains("02:00:00:00:00:00")) {
			info_bssid = getString(R.string.na);
		} else {
			info_bssid = bssid;
		}

		info_ipv4 = ipv4;
		info_ipv6 = ipv6;
		info_gateway_ip = gatewayIp;
		info_hostname = hostname;
		if (Build.VERSION.SDK_INT >= 30) {
			info_wifi_standard = wifiStandard;
		}
		info_dns1 = dns1;
		info_dns2 = dns2;

		if (subnetMask == null) {
			info_subnet_mask = getString(R.string.na);
		} else {
			info_subnet_mask = subnetMask;
		}

		if (broadcastAddr == null) {
			info_broadcast_addr = getString(R.string.na);
		} else {
			info_broadcast_addr = broadcastAddr;
		}

		if (networkId.contains("-1")) {
			info_network_id = getString(R.string.na);
		} else {
			info_network_id = networkId;
		}

		info_mac_addr = macAddr;
		info_network_interface = networkInterface;
		info_loopback_addr = loopbackAddr;
		info_localhost_addr = localhostAddr;
		info_frequency = freq + "MHz";
		info_network_channel = networkChannel;
		info_rssi = rssiConverted + "%" + " (" + rssi + "dBm" + ")";
		info_distance = distanceFromRssiRounded;

		if (leaseTime == 0) {
			info_lease_time = getString(R.string.na);
		} else if (leaseTime >= 3600) {
			info_lease_time = leaseTime + "s " + "(" + leaseTimeHours + "h)";
		} else {
			info_lease_time = leaseTime + "s " + "(" + leaseTimeMinutes + "m)";
		}

		if (Build.VERSION.SDK_INT >= 29) {
			info_network_speed = RXLinkSpd + " / " + TXLinkSpd + " Mbps";
		}
		info_network_speed_legacy = networkSpeed + " / " + networkSpeed + " Mbps";
		info_transmitted_data = wifiTXMegabytesStr + " " + getString(R.string.megabyte) + " (" + wifiTXGigabytesStr + " " + getString(R.string.gigabyte) + ")";
		info_received_data = wifiRXMegabytesStr + " " + getString(R.string.megabyte) + " (" + wifiRXGigabytesStr + " " + getString(R.string.gigabyte) + ")";
		info_supplicant_state = supplicantState;

		if (wifiManager.is5GHzBandSupported()) {
			info_5ghz_support = getString(R.string.yes);
		} else {
			info_5ghz_support = getString(R.string.no);
		}

		if (Build.VERSION.SDK_INT >= 30) {
			if (wifiManager.is6GHzBandSupported()) {
				info_6ghz_support = getString(R.string.yes);
			} else {
				info_6ghz_support = getString(R.string.no);
			}
		} else {
			info_6ghz_support = getString(R.string.no);
		}

		if (Build.VERSION.SDK_INT >= 31) {
			if (wifiManager.is60GHzBandSupported()) {
				info_60ghz_support = getString(R.string.yes);
			} else {
				info_60ghz_support = getString(R.string.no);
			}
		} else {
			info_60ghz_support = getString(R.string.no);
		}

		if (wifiManager.isP2pSupported()) {
			info_p2p_support = getString(R.string.yes);
		} else {
			info_p2p_support = getString(R.string.no);
		}

		if (wifiManager.isTdlsSupported()) {
			info_tdls_support = getString(R.string.yes);
		} else {
			info_tdls_support = getString(R.string.no);
		}

		if (Build.VERSION.SDK_INT >= 29) {
			if (wifiManager.isWpa3SaeSupported()) {
				info_wpa3_sae_support = getString(R.string.yes);
			} else {
				info_wpa3_sae_support = getString(R.string.no);
			}

			if (wifiManager.isWpa3SuiteBSupported()) {
				info_wpa3_suite_b_support = getString(R.string.yes);
			} else {
				info_wpa3_suite_b_support = getString(R.string.no);
			}
		} else {
			info_wpa3_sae_support = getString(R.string.no);
			info_wpa3_suite_b_support = getString(R.string.no);
		}
	}

	private void updateTextviews() {
		textview_ssid.setText(info_ssid);
		textview_hidden_ssid.setText(info_hidden_ssid);
		textview_bssid.setText(info_bssid);
		textview_ipv4.setText(info_ipv4);
		textview_ipv6.setText(info_ipv6);
		textview_gateway_ip.setText(info_gateway_ip);
		textview_hostname.setText(info_hostname);
		if (Build.VERSION.SDK_INT >= 30) {
			textview_wifi_standard.setText(info_wifi_standard);
		} else {
			if (relativelayout_wifi_standard.getVisibility() != View.GONE) {
				relativelayout_wifi_standard.setVisibility(View.GONE);
			}
		}
		textview_frequency.setText(info_frequency);
		textview_network_channel.setText(info_network_channel);
		textview_rssi.setText(info_rssi);
		textview_distance.setText(info_distance);
		textview_lease_duration.setText(info_lease_time);
		textview_transmitted_data.setText(info_transmitted_data);
		textview_received_data.setText(info_received_data);
		textview_dns1.setText(info_dns1);
		textview_dns2.setText(info_dns2);
		textview_subnet_mask.setText(info_subnet_mask);
		textview_broadcast_address.setText(info_broadcast_addr);
		textview_network_id.setText(info_network_id);
		textview_mac_address.setText(info_mac_addr);
		textview_network_interface.setText(info_network_interface);
		textview_loopback_address.setText(info_loopback_addr);
		textview_localhost.setText(info_localhost_addr);
		textview_wpa_supplicant_state.setText(info_supplicant_state);
		textview_5ghz_support.setText(info_5ghz_support);
		textview_6ghz_support.setText(info_6ghz_support);
		textview_60ghz_support.setText(info_60ghz_support);
		textview_wifi_direct_support.setText(info_p2p_support);
		textview_tdls_support.setText(info_tdls_support);
		textview_wpa3_sae_support.setText(info_wpa3_sae_support);
		textview_wpa3_suite_b_support.setText(info_wpa3_suite_b_support);
		if (Build.VERSION.SDK_INT >= 29) {
			textview_network_speed.setText(info_network_speed);
			if (relativelayout_network_speed_legacy.getVisibility() != View.GONE) {
				relativelayout_network_speed_legacy.setVisibility(View.GONE);
			}
		} else {
			textview_network_speed_legacy.setText(info_network_speed_legacy);
			if (relativelayout_network_speed.getVisibility() != View.GONE) {
				relativelayout_network_speed.setVisibility(View.GONE);
			}
		}
	}

	private void startInfoHandlerThread() {
		infoHandlerThread = new HandlerThread("BackgroundInfoHandlerThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		infoHandlerThread.start();
	}

	private void startInfoHandler() {
		infoHandler = new Handler(infoHandlerThread.getLooper());
		infoHandler.post(infoRunnable);
		isHandlerRunning = true;
	}

	private final Runnable infoRunnable = new Runnable() {
		@Override
		public void run() {
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

	private void stopInfoHandler() {
		if (infoHandler != null) {
			infoHandler.removeCallbacksAndMessages(infoRunnable);
			isHandlerRunning = false;
		}
	}

	public class WiFiConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkWiFiConnectivity(true);
		}
	}

	private void checkWiFiConnectivity(Boolean shouldStartHandlerThread) {
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (WiFiCheck.isConnected()) {
			showWidgets(); // Makes CardViews and TextViews visible
			if (toolbarMenu != null) {
				if (!toolbarMenu.findItem(R.id.copy_all).isEnabled()) {
					setToolbarItemEnabled(R.id.copy_all, true);
				}
			}
			if (shouldStartHandlerThread) {
				if (!isHandlerRunning) {
					startInfoHandlerThread();
					startInfoHandler();
				}
			}
		} else {
			hideWidgets(); // Hides CardViews and TextViews
			textview_public_ip.setText(getString(R.string.your_ip_na));
			if (toolbarMenu != null) {
				if (toolbarMenu.findItem(R.id.copy_all).isEnabled()) {
					setToolbarItemEnabled(R.id.copy_all, false);
				}
			}
			if (isHandlerRunning) {
				stopInfoHandler();
				stopInfoHandlerThread();
			}
		}
	}

	private void showWidgets() {
		textview_noconn.setVisibility(View.GONE);
		cardview_1.setVisibility(View.VISIBLE);
		cardview_ip.setVisibility(View.VISIBLE);
		cardview_2.setVisibility(View.VISIBLE);
		cardview_3.setVisibility(View.VISIBLE);
		cardview_4.setVisibility(View.VISIBLE);
		cardview_5.setVisibility(View.VISIBLE);
		cardview_6.setVisibility(View.VISIBLE);
		fab_update.setVisibility(View.VISIBLE);
	}

	private void hideWidgets() {
		textview_noconn.setVisibility(View.VISIBLE);
		cardview_1.setVisibility(View.GONE);
		cardview_ip.setVisibility(View.GONE);
		cardview_2.setVisibility(View.GONE);
		cardview_3.setVisibility(View.GONE);
		cardview_4.setVisibility(View.GONE);
		cardview_5.setVisibility(View.GONE);
		cardview_6.setVisibility(View.GONE);
		fab_update.setVisibility(View.GONE);
	}

	private class PublicIPAsyncTask extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			fab_update.setEnabled(false);
		}

		@Override
		protected String doInBackground(Void... params) {
			HttpURLConnection urlConnection = null;
			String ipAddress = null;
			try {
				URL url = new URL("https://public-ip-api.vercel.app/api/ip/");
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");

				int responseCode = urlConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					StringBuilder responseBuilder = new StringBuilder();
					InputStream inputStream = urlConnection.getInputStream();
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
						String line;
						while ((line = reader.readLine()) != null) {
							responseBuilder.append(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					ipAddress = responseBuilder.toString();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return ipAddress;
		}

		@Override
		protected void onPostExecute(String ipAddress) {
			if (ipAddress == null)
				ipAddress = getString(R.string.na);
			textview_public_ip.setText(String.format(getString(R.string.your_ip), ipAddress));
			new Handler(Looper.getMainLooper()).postDelayed(() -> fab_update.setEnabled(true), 5000);
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
		wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = wifiManager.getDhcpInfo();
		int gatewayIP = dhcp.gateway;
		return intToIp(gatewayIP);
	}

	private String getIPv4Address() {
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
		} catch (SocketException ex) {
			Log.e("getIPv4Address()", ex.toString());
		}
		return null;
	}

	private String getIPv6Address() {
		try {
			List<NetworkInterface> allNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : allNetworkInterfaces) {
				if (!networkInterface.getName().equalsIgnoreCase("wlan0"))
					continue;
				List<InetAddress> allInetAddresses = Collections.list(networkInterface.getInetAddresses());
				for (InetAddress inetAddr : allInetAddresses) {
					if (!inetAddr.isLoopbackAddress() && inetAddr instanceof Inet6Address) {
						int index = String.valueOf(inetAddr).indexOf("%");
						if (index != -1) {
							return Objects.requireNonNull(inetAddr.getHostAddress()).substring(0, index-1);
						}
						return inetAddr.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("getIPv6Address()", ex.toString());
		}
		return null;
	}

	private String getHostname() {
		try {
			InetAddress hostnameAddr = InetAddress.getByName(getGatewayIP());
			return hostnameAddr.getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	@RequiresApi(api = Build.VERSION_CODES.R)
	private String getWifiStandard() {
		int wifiStandard = wifiInfo.getWifiStandard();
		String wifiStandardHumanReadable = getString(R.string.na);
		switch (wifiStandard) {
			case ScanResult.WIFI_STANDARD_LEGACY:
				wifiStandardHumanReadable = "Wi-Fi 1/2/3 (802.11a/b/g)";
				break;
			case ScanResult.WIFI_STANDARD_11N:
				wifiStandardHumanReadable = "Wi-Fi 4 (802.11n)";
				break;
			case ScanResult.WIFI_STANDARD_11AC:
				wifiStandardHumanReadable = "Wi-Fi 5 (802.11ac)";
				break;
			case ScanResult.WIFI_STANDARD_11AX:
				wifiStandardHumanReadable = "Wi-Fi 6 (802.11ax)";
				break;
			case ScanResult.WIFI_STANDARD_11AD:
				wifiStandardHumanReadable = "WiGig (802.11ad)";
				break;
			case ScanResult.WIFI_STANDARD_11BE:
				wifiStandardHumanReadable = "Wi-Fi 7 (802.11be)";
				break;
			case ScanResult.WIFI_STANDARD_UNKNOWN:
				wifiStandardHumanReadable = getString(R.string.na);
				break;
		}
		return wifiStandardHumanReadable;
	}

	private String getNetworkInterface() {
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			for (Network network : connectivityManager.getAllNetworks()) {
				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
				if (networkInfo.toString().equals(activeNetworkInfo.toString())) {
					LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
					return linkProperties.getInterfaceName();
				}
			}
		}
		return null;
	}

	private String netPrefixLengthToSubnetMask(int netPrefixLength) {
		int shift = 0xFFFFFFFF << (32 - netPrefixLength);
		return (((shift & 0xFF000000) >> 24) & 0xFF) + "."
			+ (((shift & 0x00FF0000) >> 16) & 0xFF) + "."
			+ (((shift & 0x0000FF00) >> 8) & 0xFF) + "."
			+ ((shift & 0x000000FF) & 0xFF);
	}

	private String getSubnetMask() {
		try {
			if (getIPv4Address() == null) {
				return null;
			}
			InetAddress inetAddress = InetAddress.getByName(getIPv4Address());
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
			for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
				if (Patterns.IP_ADDRESS.matcher(String.valueOf(address.getAddress()).replaceFirst("/", "")).matches()) {
					return netPrefixLengthToSubnetMask(address.getNetworkPrefixLength());
				}
			}
		} catch (IOException ex) {
			Log.e("getSubnetMask()", ex.toString());
		}
		return null;
	}

	private String getBroadcastAddr() {
		try {
			if (getIPv4Address() == null) {
				return null;
			}
			InetAddress inetAddress = InetAddress.getByName(String.valueOf(getIPv4Address()));
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
			for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
				if (Patterns.IP_ADDRESS.matcher(String.valueOf(address.getAddress()).replaceFirst("/", "")).matches()) {
					return String.valueOf(address.getBroadcast()).replaceFirst("/", "");
				}
			}
		} catch (IOException ex) {
			Log.e("getBroadcastAddr()", ex.toString());
		}
		return null;
	}

	private String getLocalhostAddress() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			return localHost.toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	private double freqRssiToDistance(int frequency, int rssi) {
		return Math.pow(10.0D, (27.55D - 20 * Math.log10(frequency) + Math.abs(rssi)) / 20.0D);
	}

	private String intToIp(int ipInt) {
		return ((ipInt & 0xFF) + "."
		+ ((ipInt >> 8) & 0xFF) + "."
		+ ((ipInt >> 16) & 0xFF) + "."
		+ ((ipInt >> 24) & 0xFF));
	}

	private int convertFrequencyToChannel(int freq) {
		if (freq == 2484) { // 2.4GHz Channel 14
			return 14;
		} else if (freq < 2484) { // 2.4GHz (802.11b/g/n/ax)
			return (freq - 2407) / 5;
		} else if (freq >= 4910 && freq <= 4980) { // 4.9GHz (802.11j)
			return (freq - 4000) / 5;
		} else if (freq < 5925) { // 5GHz (802.11a/h/j/n/ac/ax) - 5.9 GHz (802.11p)
			return (freq - 5000) / 5;
		} else if (freq == 5935) {
			return 2;
		} else if (freq <= 45000) { // 6 GHz+ (802.11ax and 802.11be)
			return (freq - 5950) / 5;
		} else if (freq >= 58320 && freq <= 70200) { // 60GHz (802.11ad/ay)
			return (freq - 56160) / 2160;
		} else {
			return -1;
		}
	}

	private void copyToClipboard(String label, String text) {
		ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, text);
		cbm.setPrimaryClip(clip);
		Toast.makeText(getBaseContext(), getString(R.string.copied_to_clipboard) + ": " + text, Toast.LENGTH_SHORT).show();
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

	private void requestGPSFeature() {
		// Notify User if GPS is disabled
		if (!isLocationEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle(getString(R.string.location_is_disabled))
				.setPositiveButton(getString(R.string.enable), (dialog, id) -> {
					if (Build.VERSION.SDK_INT == 26) {
						Toast.makeText(this, getString(R.string.enable_location_to_show_ssid_bssid), Toast.LENGTH_LONG).show();
					} else if (Build.VERSION.SDK_INT >= 27) {
						Toast.makeText(this, getString(R.string.enable_location_to_show_ssid_bssid_net_id), Toast.LENGTH_LONG).show();
					}
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				})
				.setNegativeButton(getString(android.R.string.cancel), (dialog, id) -> {
					if (Build.VERSION.SDK_INT == 26) {
						Toast.makeText(this, getString(R.string.ssid_bssid_not_displayed), Toast.LENGTH_LONG).show();
					} else if (Build.VERSION.SDK_INT >= 27) {
						Toast.makeText(this, getString(R.string.ssid_bssid_net_id_not_displayed), Toast.LENGTH_LONG).show();
					}
					dialog.cancel();
				})
				.setNeutralButton(getString(R.string.dont_show_again), (dialog, id) -> new SharedPreferencesManager(getApplicationContext()).storeBoolean(keyNeverShowGeoDialog, true));
			if (Build.VERSION.SDK_INT == 26) {
				builder.setMessage(getString(R.string.wifi_info_needs_location_api_26));
			} else if (Build.VERSION.SDK_INT >= 27) {
				builder.setMessage(getString(R.string.wifi_info_needs_location_api_27));
			}
			builder.setCancelable(false);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}

	private void requestPermissionsOnStart() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(getString(R.string.permission_required))
			.setMessage(getString(R.string.location_permission_is_required_android_8_1_plus))
			.setPositiveButton(getString(R.string.yes), (dialog, id) -> {
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
			.setNegativeButton(getString(R.string.no_thanks), null)
			.setNeutralButton(getString(R.string.dont_show_again), (dialog, id) -> new SharedPreferencesManager(getApplicationContext()).storeBoolean(keyNeverShowPermissionReqDialog, true))
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
			.setShortLabel(getString(R.string.github_repo))
			.setLongLabel(getString(R.string.open_github_repository))
			.setIcon(Icon.createWithResource(this, R.drawable.ic_github))
			.setRank(2)
			.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TrueMLGPro/Wi-Fi_Info")))
			.build();
		ShortcutInfo releasesShortcut = new ShortcutInfo.Builder(this, "shortcut_releases")
			.setShortLabel(getString(R.string.releases))
			.setLongLabel(getString(R.string.open_github_releases))
			.setRank(1)
			.setIcon(Icon.createWithResource(this, R.drawable.ic_folder))
			.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TrueMLGPro/Wi-Fi_Info/releases")))
			.build();
		shortcutManager.setDynamicShortcuts(Arrays.asList(githubShortcut, releasesShortcut));
	}

	private void initSharedPrefs() {
		sharedPrefChangeListener = (prefs, key) -> {
			if (key.equals(SettingsActivity.KEY_PREF_DARK_MODE_SWITCH)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_DARK_MODE_SWITCH, prefs.getBoolean(SettingsActivity.KEY_PREF_DARK_MODE_SWITCH, true));
			}

			if (key.equals(SettingsActivity.KEY_PREF_AMOLED_MODE_CHECK)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_AMOLED_MODE_CHECK, prefs.getBoolean(SettingsActivity.KEY_PREF_AMOLED_MODE_CHECK, false));
			}

			if (key.equals(SettingsActivity.KEY_PREF_BOOT_SWITCH)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, prefs.getBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, false));
			}

			if (key.equals(SettingsActivity.KEY_PREF_NTFC_SWITCH)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, prefs.getBoolean(SettingsActivity.KEY_PREF_NTFC_SWITCH, true));
			}

			if (key.equals(SettingsActivity.KEY_PREF_CLR_CHECK)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, prefs.getBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, false));
			}

			if (key.equals(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, prefs.getBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, false));
			}

			if (key.equals(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, prefs.getBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, false));
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

			if (key.equals(SettingsActivity.KEY_PREF_NTFC_FREQ)) {
				if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("500")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, "500");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("1000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("2000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, "2000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("3000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, "3000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("4000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, "4000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("5000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, "5000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_NTFC_FREQ, "1000").equals("10000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_NTFC_FREQ, "10000");
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_CARD_FREQ)) {
				if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("500")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, "500");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("1000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("2000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, "2000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("3000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, "3000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("4000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, "4000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("5000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, "5000");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_CARD_FREQ, "1000").equals("10000")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_CARD_FREQ, "10000");
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_APP_FONT)) {
				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Gilroy-Semibold.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/CircularStd-Bold.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/CircularStd-Bold.ttf");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Gilroy-Semibold.ttf").equals("fonts/Nunito-Bold.ttf")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_FONT, "fonts/Nunito-Bold.ttf");
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_APP_LANGUAGE)) {
				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("default_lang")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("en")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "en");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("fr")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "fr");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("de")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "de");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("pt")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "pt");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("ru")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "ru");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("es")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "es");
				}

				if (prefs.getString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "default_lang").equals("uk")) {
					new SharedPreferencesManager(getApplicationContext()).storeString(SettingsActivity.KEY_PREF_APP_LANGUAGE, "uk");
				}
			}

			if (key.equals(SettingsActivity.KEY_PREF_KEEP_SCREEN_ON_SWITCH)) {
				new SharedPreferencesManager(getApplicationContext()).storeBoolean(SettingsActivity.KEY_PREF_KEEP_SCREEN_ON_SWITCH, prefs.getBoolean(SettingsActivity.KEY_PREF_KEEP_SCREEN_ON_SWITCH, true));
			}
		};

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener);
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
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (!WiFiCheck.isConnected()) {
			hideWidgets(); // Hides CardViews and TextViews
			textview_public_ip.setText(getString(R.string.your_ip_na));
		} else {
			showWidgets(); // Makes CardViews and TextViews visible
		}
	}

	private void initOnClickListeners() {
		fab_tools.setOnClickListener(v -> {
			Intent intent_tools = new Intent(MainActivity.this, ToolsActivity.class);
			startActivity(intent_tools);
			fam.close(true);
		});

		fab_settings.setOnClickListener(v -> {
			Intent intent_settings = new Intent(MainActivity.this, SettingsActivity.class);
			intent_settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent_settings);
			fam.close(true);
		});

		fab_update.setOnClickListener(v -> {
			new PublicIPAsyncTask().execute();
		});
	}

	private void initCopyableText() {
		textview_public_ip.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.public_ip_address), textview_public_ip.getText().toString());
			return true;
		});

		textview_ssid.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.ssid), textview_ssid.getText().toString());
			return true;
		});

		textview_hidden_ssid.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.hidden_ssid), textview_hidden_ssid.getText().toString());
			return true;
		});

		textview_bssid.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.bssid), textview_bssid.getText().toString());
			return true;
		});

		textview_ipv4.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.ipv4), textview_ipv4.getText().toString());
			return true;
		});

		textview_ipv6.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.ipv6), textview_ipv6.getText().toString());
			return true;
		});

		textview_gateway_ip.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.gateway_ip), textview_gateway_ip.getText().toString());
			return true;
		});

		textview_hostname.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.hostname), textview_hostname.getText().toString());
			return true;
		});

		textview_wifi_standard.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.wifi_standard), textview_wifi_standard.getText().toString());
			return true;
		});

		textview_frequency.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.frequency), textview_frequency.getText().toString());
			return true;
		});

		textview_network_channel.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.network_channel), textview_network_channel.getText().toString());
			return true;
		});

		textview_rssi.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.rssi_signal_strength), textview_rssi.getText().toString());
			return true;
		});

		textview_distance.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.distance), textview_distance.getText().toString());
			return true;
		});

		textview_lease_duration.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.ip_lease_duration), textview_lease_duration.getText().toString());
			return true;
		});

		if (Build.VERSION.SDK_INT >= 29) {
			textview_network_speed.setOnLongClickListener(v -> {
				copyToClipboard(getString(R.string.network_speed), textview_network_speed.getText().toString());
				return true;
			});
		} else {
			textview_network_speed_legacy.setOnLongClickListener(v -> {
				copyToClipboard(getString(R.string.network_speed), textview_network_speed_legacy.getText().toString());
				return true;
			});
		}

		textview_transmitted_data.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.transmitted_mbs_gbs), textview_transmitted_data.getText().toString());
			return true;
		});

		textview_received_data.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.received_mbs_gbs), textview_received_data.getText().toString());
			return true;
		});

		textview_dns1.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.dns_1), textview_dns1.getText().toString());
			return true;
		});

		textview_dns2.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.dns_2), textview_dns2.getText().toString());
			return true;
		});

		textview_subnet_mask.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.subnet_mask), textview_subnet_mask.getText().toString());
			return true;
		});

		textview_broadcast_address.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.broadcast_address), textview_broadcast_address.getText().toString());
			return true;
		});

		textview_network_id.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.network_id), textview_network_id.getText().toString());
			return true;
		});

		textview_mac_address.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.mac_address), textview_mac_address.getText().toString());
			return true;
		});

		textview_network_interface.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.network_interface), textview_network_interface.getText().toString());
			return true;
		});

		textview_loopback_address.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.loopback_address), textview_loopback_address.getText().toString());
			return true;
		});

		textview_localhost.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.localhost_address), textview_localhost.getText().toString());
			return true;
		});

		textview_wpa_supplicant_state.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.wpa_supplicant_state), textview_wpa_supplicant_state.getText().toString());
			return true;
		});

		textview_5ghz_support.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string._5ghz_band_support), textview_5ghz_support.getText().toString());
			return true;
		});

		textview_6ghz_support.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string._6ghz_band_support), textview_6ghz_support.getText().toString());
			return true;
		});

		textview_60ghz_support.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string._60ghz_band_support), textview_60ghz_support.getText().toString());
			return true;
		});

		textview_wifi_direct_support.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.wifi_direct_support), textview_wifi_direct_support.getText().toString());
			return true;
		});

		textview_tdls_support.setOnLongClickListener(v -> {
			copyToClipboard(getString(R.string.tdls_support), textview_tdls_support.getText().toString());
			return true;
		});

		if (Build.VERSION.SDK_INT >= 29) {
			textview_wpa3_sae_support.setOnLongClickListener(v -> {
				copyToClipboard(getString(R.string.wpa3_sae_support), textview_wpa3_sae_support.getText().toString());
				return true;
			});

			textview_wpa3_suite_b_support.setOnLongClickListener(v -> {
				copyToClipboard(getString(R.string.wpa3_suite_b_support), textview_wpa3_suite_b_support.getText().toString());
				return true;
			});
		}
	}

	private void copyAllTextviews() {
		StringBuilder strB = new StringBuilder();
		strB.append(getString(R.string.ssid) + ": " + info_ssid).append("\n").append(getString(R.string.hidden_ssid) + ": " + info_hidden_ssid).append("\n")
			.append(getString(R.string.bssid) + ": " + info_bssid).append("\n").append(getString(R.string.ipv4) + ": " + info_ipv4).append("\n")
			.append(getString(R.string.ipv6) + ": " + info_ipv6).append("\n").append(getString(R.string.gateway_ip) + ": " + info_gateway_ip).append("\n")
			.append(getString(R.string.hostname) + ": " + info_hostname).append("\n").append(getString(R.string.wifi_standard) + ": " + info_wifi_standard).append("\n")
			.append(getString(R.string.frequency) + ": " + info_frequency).append("\n").append(getString(R.string.network_channel) + ": " + info_network_channel).append("\n")
			.append(getString(R.string.rssi_signal_strength) + ": " + info_rssi).append("\n").append(getString(R.string.distance) + ": " + info_distance).append("\n")
			.append(getString(R.string.ip_lease_duration) + ": " + info_lease_time).append("\n");
		if (Build.VERSION.SDK_INT >= 29) {
			strB.append(getString(R.string.network_speed) + ": " + info_network_speed).append("\n");
		} else {
			strB.append(getString(R.string.network_speed) + ": " + info_network_speed_legacy).append("\n");
		}
		strB.append(getString(R.string.transmitted_mbs_gbs) + ": " + info_transmitted_data).append("\n")
			.append(getString(R.string.received_mbs_gbs) + ": " + info_received_data).append("\n").append(getString(R.string.dns_1) + ": " + info_dns1).append("\n")
			.append(getString(R.string.dns_2) + ": " + info_dns2).append("\n").append(getString(R.string.subnet_mask) + ": " + info_subnet_mask).append("\n")
			.append(getString(R.string.network_id) + ": " + info_network_id).append("\n").append(getString(R.string.mac_address) + ": " + info_mac_addr).append("\n")
			.append(getString(R.string.network_interface) + ": " + info_network_interface).append("\n").append(getString(R.string.loopback_address) + ": " + info_loopback_addr).append("\n")
			.append(getString(R.string.localhost_address) + ": " + info_localhost_addr).append("\n").append(getString(R.string.wpa_supplicant_state) + ": " + info_supplicant_state).append("\n")
			.append(getString(R.string._5ghz_band_support) + ": " + info_5ghz_support).append("\n").append(getString(R.string._6ghz_band_support) + ": " + info_6ghz_support).append("\n")
			.append(getString(R.string._60ghz_band_support) + ": " + info_60ghz_support).append("\n").append(getString(R.string.wifi_direct_support) + ": " + info_p2p_support).append("\n")
			.append(getString(R.string.tdls_support) + ": " + info_tdls_support).append("\n").append(getString(R.string.wpa3_sae_support) + ": " + info_wpa3_sae_support).append("\n")
			.append(getString(R.string.wpa3_suite_b_support) + ": " + info_wpa3_suite_b_support);
		copyToClipboard("all_info_text", String.valueOf(strB));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(WiFiConnectivityReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		WiFiConnectivityReceiver = new WiFiConnectivityReceiver();
		registerReceiver(WiFiConnectivityReceiver, filter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!isHandlerRunning) {
			startInfoHandlerThread();
			startInfoHandler();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isHandlerRunning) {
			stopInfoHandler();
			stopInfoHandlerThread();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(sharedPrefChangeListener);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (Build.VERSION.SDK_INT >= 30) {
			if (requestCode == LocationPermissionCode) {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle(getString(R.string.background_location_permission))
							.setMessage(getString(R.string.due_to_the_changes_in_android_11))
							.setPositiveButton(getString(android.R.string.ok), (dialog, id) -> {
								Toast.makeText(MainActivity.this, getString(R.string.go_to_permissions_and_location), Toast.LENGTH_LONG).show();
								Toast.makeText(MainActivity.this, getString(R.string.select_allow_all_the_time), Toast.LENGTH_LONG).show();
								Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								Uri uri = Uri.fromParts("package", getPackageName(), null);
								intent.setData(uri);
								startActivity(intent);
							})
							.setNegativeButton(getString(R.string.no_thanks), null)
							.setCancelable(false);
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(getString(R.string.are_you_sure))
				.setMessage(getString(R.string.do_you_want_to_exit))
				.setPositiveButton(getString(R.string.exit), (dialog, id) -> finishAffinity())
				.setNegativeButton(getString(android.R.string.cancel), null);
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
		checkWiFiConnectivity(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.copy_all) {
			copyAllTextviews();
		}
		return true;
	}
}
