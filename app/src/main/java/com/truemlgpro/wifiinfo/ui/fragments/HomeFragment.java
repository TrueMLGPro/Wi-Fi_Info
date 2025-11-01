package com.truemlgpro.wifiinfo.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.ui.activities.MainActivity;
import com.truemlgpro.wifiinfo.utils.app.AppClipboardManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;

import java.net.InetAddress;
import java.util.Locale;

public class HomeFragment extends Fragment {
	private MaterialToolbar toolbar;
	private MaterialTextView textview_public_ip;
	private MaterialTextView textview_ssid;
	private MaterialTextView textview_bssid;
	private MaterialTextView textview_ipv4;
	private MaterialTextView textview_ipv6;
	private MaterialTextView textview_gateway_ip;
	private MaterialTextView textview_hostname;
	private RelativeLayout relativelayout_wifi_standard;
	private MaterialTextView textview_wifi_standard;
	private MaterialTextView textview_frequency;
	private MaterialTextView textview_distance;
	private MaterialTextView textview_network_channel;
	private MaterialTextView textview_rssi;
	private MaterialTextView textview_lease_duration;
	private RelativeLayout relativelayout_network_speed;
	private MaterialTextView textview_network_speed;
	private RelativeLayout relativelayout_network_speed_legacy;
	private MaterialTextView textview_network_speed_legacy;
	private MaterialTextView textview_transmitted_data;
	private MaterialTextView textview_received_data;
	private MaterialTextView textview_dns1;
	private MaterialTextView textview_dns2;
	private MaterialTextView textview_subnet_mask;
	private MaterialTextView textview_broadcast_address;
	private MaterialTextView textview_network_id;
	private MaterialTextView textview_mac_address;
	private MaterialTextView textview_network_interface;
	private MaterialTextView textview_loopback_address;
	private MaterialTextView textview_localhost;
	private MaterialTextView textview_wpa_supplicant_state;
	private MaterialTextView textview_5ghz_support;
	private MaterialTextView textview_6ghz_support;
	private MaterialTextView textview_60ghz_support;
	private MaterialTextView textview_wifi_direct_support;
	private MaterialTextView textview_tdls_support;
	private MaterialTextView textview_wpa3_sae_support;
	private MaterialTextView textview_wpa3_suite_b_support;
	private MaterialTextView textview_noconn;
	private MaterialCardView cardview_1;
	private MaterialCardView cardview_ip;
	private MaterialCardView cardview_2;
	private MaterialCardView cardview_3;
	private MaterialCardView cardview_4;
	private MaterialCardView cardview_5;
	private MaterialCardView cardview_6;
	private FloatingActionButton fab_update;

	// Strings for getAllNetworkInformation()
	private String info_ssid = "";
	private String info_bssid = "";
	private String info_ipv4 = "";
	private String info_ipv6 = "";
	private String info_gateway_ip = "";
	private String info_hostname = "";
	private String info_wifi_standard = "";
	private String info_frequency = "";
	private String info_network_channel = "";
	private String info_rssi = "";
	private String info_distance = "";
	private String info_lease_time = "";
	private String info_network_speed = "";
	private String info_network_speed_legacy = "";
	private String info_transmitted_data = "";
	private String info_received_data = "";
	private String info_dns1 = "";
	private String info_dns2 = "";
	private String info_subnet_mask = "";
	private String info_broadcast_addr = "";
	private String info_network_id = "";
	private String info_mac_addr = "";
	private String info_network_interface = "";
	private String info_loopback_addr = "";
	private String info_localhost_addr = "";
	private String info_supplicant_state = "";
	private String info_5ghz_support = "";
	private String info_6ghz_support = "";
	private String info_60ghz_support = "";
	private String info_p2p_support = "";
	private String info_tdls_support = "";
	private String info_wpa3_sae_support = "";
	private String info_wpa3_suite_b_support = "";

	private ConnectivityManager connectivityManager;
	private DhcpInfo dhcpInfo;
	private WifiInfo wifiInfo;
	private WifiManager wifiManager;

	private ConnectivityManager.NetworkCallback networkCallback;

	private boolean isHandlerRunning = false;
	private final double MEGABYTE = 1024 * 1024;
	private final double GIGABYTE = 1024 * 1024 * 1024;

	private SharedPreferencesManager sp;

	private static final int MAX_GPS_DENIALS = 3;
	private boolean isGpsDialogShowingOrPending = false;

	private HandlerThread infoHandlerThread;
	private Handler infoHandler;

	private Handler mainHandler;
	private final Runnable doCheck = () -> checkWiFiConnectivity(false);

	private int keyCardFreqFormatted = 1000;
	private boolean wifiConnected = false;

	private NetworkUtils.Cancelable publicIpRequest;

	public HomeFragment() {
		super(R.layout.fragment_home);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		initViews(view);

		mainHandler = new Handler(Looper.getMainLooper());

		fab_update.setOnClickListener(v -> fetchPublicIp());
		initCopyableText(requireContext().getApplicationContext());

		checkWiFiConnectivity(false);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		AppCompatActivity act = (AppCompatActivity) requireActivity();
		act.setSupportActionBar(toolbar);

		MenuHost menuHost = requireActivity();
		menuHost.addMenuProvider(new MenuProvider() {
			@Override
			public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
				menuInflater.inflate(R.menu.main_action_bar_menu, menu);
			}

			@Override
			public void onPrepareMenu(@NonNull Menu menu) {
				checkWiFiConnectivity(false);
				MenuItem copy = menu.findItem(R.id.copy_all);
				if (copy != null) {
					copy.setEnabled(wifiConnected);
				}
			}

			@Override
			public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
				if (menuItem.getItemId() == R.id.copy_all) {
					copyAllTextviews();
					return true;
				}
				return false;
			}
		}, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = new SharedPreferencesManager(requireContext().getApplicationContext());

		keyCardFreqFormatted = Integer.parseInt(
				sp.retrieveString(PreferenceKeys.KEY_PREF_CARD_INTERVAL, PreferenceDefaults.CARD_UPDATE_INTERVAL)
		);
	}

	private void initViews(View view) {
		AppBarLayout app_bar_layout = view.findViewById(R.id.appbarlayout_main);
		toolbar = view.findViewById(R.id.toolbar);
		NestedScrollView scroll_view = view.findViewById(R.id.scroll_view);
		textview_public_ip = view.findViewById(R.id.textview_public_ip);
		textview_ssid = view.findViewById(R.id.textview_ssid_value);
		textview_bssid = view.findViewById(R.id.textview_bssid_value);
		textview_ipv4 = view.findViewById(R.id.textview_ipv4_value);
		textview_ipv6 = view.findViewById(R.id.textview_ipv6_value);
		textview_gateway_ip = view.findViewById(R.id.textview_gateway_ip_value);
		textview_hostname = view.findViewById(R.id.textview_hostname_value);
		relativelayout_wifi_standard = view.findViewById(R.id.relativelayout_wifi_standard);
		textview_wifi_standard = view.findViewById(R.id.textview_wifi_standard_value);
		textview_frequency = view.findViewById(R.id.textview_frequency_value);
		textview_network_channel = view.findViewById(R.id.textview_network_channel_value);
		textview_rssi = view.findViewById(R.id.textview_rssi_value);
		textview_distance = view.findViewById(R.id.textview_distance_value);
		textview_lease_duration = view.findViewById(R.id.textview_ip_lease_duration_value);
		relativelayout_network_speed = view.findViewById(R.id.relativelayout_network_speed);
		textview_network_speed = view.findViewById(R.id.textview_network_speed_value);
		relativelayout_network_speed_legacy = view.findViewById(R.id.relativelayout_network_speed_legacy);
		textview_network_speed_legacy = view.findViewById(R.id.textview_network_speed_legacy_value);
		textview_transmitted_data = view.findViewById(R.id.textview_transmitted_data_value);
		textview_received_data = view.findViewById(R.id.textview_received_data_value);
		textview_dns1 = view.findViewById(R.id.textview_dns1_value);
		textview_dns2 = view.findViewById(R.id.textview_dns2_value);
		textview_subnet_mask = view.findViewById(R.id.textview_subnet_mask_value);
		textview_broadcast_address = view.findViewById(R.id.textview_broadcast_address_value);
		textview_network_id = view.findViewById(R.id.textview_network_id_value);
		textview_mac_address = view.findViewById(R.id.textview_mac_address_value);
		textview_network_interface = view.findViewById(R.id.textview_network_interface_value);
		textview_loopback_address = view.findViewById(R.id.textview_loopback_address_value);
		textview_localhost = view.findViewById(R.id.textview_localhost_value);
		textview_wpa_supplicant_state = view.findViewById(R.id.textview_wpa_supplicant_state_value);
		textview_5ghz_support = view.findViewById(R.id.textview_5ghz_support_value);
		textview_6ghz_support = view.findViewById(R.id.textview_6ghz_support_value);
		textview_60ghz_support = view.findViewById(R.id.textview_60ghz_support_value);
		textview_wifi_direct_support = view.findViewById(R.id.textview_wifi_direct_support_value);
		textview_tdls_support = view.findViewById(R.id.textview_tdls_support_value);
		textview_wpa3_sae_support = view.findViewById(R.id.textview_wpa3_sae_support_value);
		textview_wpa3_suite_b_support = view.findViewById(R.id.textview_wpa3_suite_b_support_value);
		textview_noconn = view.findViewById(R.id.textview_noconn);
		cardview_1 = view.findViewById(R.id.cardview_1);
		cardview_ip = view.findViewById(R.id.cardview_ip);
		cardview_2 = view.findViewById(R.id.cardview_2);
		cardview_3 = view.findViewById(R.id.cardview_3);
		cardview_4 = view.findViewById(R.id.cardview_4);
		cardview_5 = view.findViewById(R.id.cardview_5);
		cardview_6 = view.findViewById(R.id.cardview_6);
		fab_update = view.findViewById(R.id.fab_update_ip);

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
				scroll_view,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	private boolean hasPermissions(String... permissions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
			final Activity activity = getActivity();
			for (String permission : permissions) {
				if (activity != null && ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isLocationPermissionGranted() {
		// In Android 8.1 (API 27) - 11 (API 30) ACCESS_COARSE_LOCATION needs to be granted to access network information
		// Android 12+ (API 31) needs ACCESS_FINE_LOCATION to be granted though
		boolean permissionGranted = false;
		if (Build.VERSION.SDK_INT >= 27 && Build.VERSION.SDK_INT < 31) {
			// Android 8.1 - Android 11
			permissionGranted = hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
		} else if (Build.VERSION.SDK_INT >= 31) {
			// Android 12+
			permissionGranted = hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
		}
		return permissionGranted;
	}

	private void requestGPSFeature() {
		final Context context = requireContext();

		// 1. Get the LocationManager and check if GPS is already enabled
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			return; // GPS is already on
		}

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
		builder.setTitle(context.getString(R.string.location_is_disabled))
				.setPositiveButton(context.getString(R.string.enable), (dialog, id) -> {
					sp.storeInt(PreferenceKeys.KEY_GPS_DENIAL_COUNT, 0);
					if (Build.VERSION.SDK_INT == 26) {
						Toast.makeText(context, context.getString(R.string.enable_location_to_show_ssid_bssid), Toast.LENGTH_LONG).show();
					} else if (Build.VERSION.SDK_INT >= 27) {
						Toast.makeText(context, context.getString(R.string.enable_location_to_show_ssid_bssid_net_id), Toast.LENGTH_LONG).show();
					}
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				})
				.setNegativeButton(context.getString(android.R.string.cancel), (dialog, id) -> {
					int currentDenials = sp.retrieveInt(PreferenceKeys.KEY_GPS_DENIAL_COUNT, 0);
					sp.storeInt(PreferenceKeys.KEY_GPS_DENIAL_COUNT, currentDenials + 1);
					if (Build.VERSION.SDK_INT == 26) {
						Toast.makeText(context, context.getString(R.string.ssid_bssid_not_displayed), Toast.LENGTH_LONG).show();
					} else if (Build.VERSION.SDK_INT >= 27) {
						Toast.makeText(context, context.getString(R.string.ssid_bssid_net_id_not_displayed), Toast.LENGTH_LONG).show();
					}
					dialog.cancel();
				})
				.setNeutralButton(context.getString(R.string.dont_show_again), (dialog, id) ->
						sp.storeBoolean(PreferenceKeys.KEY_NEVER_SHOW_GEO_DIALOG, true));
		if (Build.VERSION.SDK_INT == 26) {
			builder.setMessage(context.getString(R.string.wifi_info_needs_location_api_26));
		} else if (Build.VERSION.SDK_INT >= 27) {
			builder.setMessage(context.getString(R.string.wifi_info_needs_location_api_27));
		}
		builder.setCancelable(false);
		builder.create().show();
	}

	private void getAllNetworkInformation() {
		final Activity activity = getActivity();
		if (activity != null) {
			wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			if (wifiManager == null) return;
			wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo == null) return;
			dhcpInfo = wifiManager.getDhcpInfo();

			String ssid = wifiInfo.getSSID();
			String bssid = wifiInfo.getBSSID() != null ? wifiInfo.getBSSID().toUpperCase() : activity.getString(R.string.na);
			String ipv4 = NetworkUtils.getIPv4Address("wlan0");
			String ipv6 = NetworkUtils.getIPv6Address("wlan0");
			String gatewayIp = NetworkUtils.getGatewayIP(activity.getApplicationContext());
			String hostname = NetworkUtils.getHostname(activity.getApplicationContext());
			String wifiStandard = "";
			if (Build.VERSION.SDK_INT >= 30) {
				wifiStandard = NetworkUtils.getWifiStandard(activity.getApplicationContext());
			}
			int freq = wifiInfo.getFrequency();
			String networkChannel = String.valueOf(NetworkUtils.convertFrequencyToChannel(freq));
			int rssi = wifiInfo.getRssi();
			int rssiConverted = WifiManager.calculateSignalLevel(rssi, 101);
			String distanceFromRssiRounded = String.format("~%.1fm", NetworkUtils.convertFreqRssiToDistance(freq, rssi));
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
			double wifiRXMegabytes = wifiRXBytes / MEGABYTE;
			double wifiTXMegabytes = wifiTXBytes / MEGABYTE;
			double wifiRXGigabytes = wifiRXBytes / GIGABYTE;
			double wifiTXGigabytes = wifiTXBytes / GIGABYTE;
			String wifiRXMegabytesStr = String.format(Locale.US, "%.2f", wifiRXMegabytes);
			String wifiTXMegabytesStr = String.format(Locale.US, "%.2f", wifiTXMegabytes);
			String wifiRXGigabytesStr = String.format(Locale.US, "%.2f", wifiRXGigabytes);
			String wifiTXGigabytesStr = String.format(Locale.US, "%.2f", wifiTXGigabytes);
			String dns1 = dhcpInfo != null ? NetworkUtils.intToIp(dhcpInfo.dns1) : activity.getString(R.string.na);
			String dns2 = dhcpInfo != null ? NetworkUtils.intToIp(dhcpInfo.dns2) : activity.getString(R.string.na);
			String subnetMask = NetworkUtils.getSubnetMask();
			String broadcastAddr = NetworkUtils.getBroadcastAddr();
			String macAddr = Build.VERSION.SDK_INT > 29 ? activity.getString(R.string.na) : NetworkUtils.getMacAddress();
			String networkInterface = NetworkUtils.getNetworkInterface();
			String networkId = String.valueOf(wifiInfo.getNetworkId());
			String loopbackAddr = String.valueOf(InetAddress.getLoopbackAddress());
			String localhostAddr = NetworkUtils.getLocalhostAddress();
			int leaseTime = dhcpInfo != null ? dhcpInfo.leaseDuration : 0;
			int leaseTimeHours = leaseTime / 3600;
			int leaseTimeMinutes = leaseTime / 60;
			String supplicantState = String.valueOf(wifiInfo.getSupplicantState());

			info_ssid = ssid != null && !ssid.equals("<unknown ssid>") ? ssid.replaceAll("^\"|\"$", "") : activity.getString(R.string.na);
			info_bssid = bssid != null && !bssid.equals("02:00:00:00:00:00") ? bssid : activity.getString(R.string.na);
			info_ipv4 = ipv4;
			info_ipv6 = ipv6;
			info_gateway_ip = gatewayIp;
			info_hostname = hostname;
			if (Build.VERSION.SDK_INT >= 30) info_wifi_standard = wifiStandard;
			info_dns1 = dns1;
			info_dns2 = dns2;
			info_subnet_mask = subnetMask != null ? subnetMask : activity.getString(R.string.na);
			info_broadcast_addr = broadcastAddr != null ? broadcastAddr : activity.getString(R.string.na);
			info_network_id = !"-1".equals(networkId) ? networkId : activity.getString(R.string.na);
			info_mac_addr = macAddr;
			info_network_interface = networkInterface;
			info_loopback_addr = loopbackAddr;
			info_localhost_addr = localhostAddr;
			info_frequency = freq + "MHz";
			info_network_channel = networkChannel;
			info_rssi = rssiConverted + "%" + " (" + rssi + "dBm" + ")";
			info_distance = distanceFromRssiRounded;

			if (leaseTime == 0) info_lease_time = activity.getString(R.string.na);
			else if (leaseTime >= 3600) info_lease_time = leaseTime + "s (" + leaseTimeHours + "h)";
			else info_lease_time = leaseTime + "s (" + leaseTimeMinutes + "m)";

			if (Build.VERSION.SDK_INT >= 29) info_network_speed = RXLinkSpd + " / " + TXLinkSpd + " Mbps";
			info_network_speed_legacy = networkSpeed + " / " + networkSpeed + " Mbps";
			info_transmitted_data = wifiTXMegabytesStr + " " + activity.getString(R.string.megabyte) + " (" + wifiTXGigabytesStr + " " + activity.getString(R.string.gigabyte) + ")";
			info_received_data = wifiRXMegabytesStr + " " + activity.getString(R.string.megabyte) + " (" + wifiRXGigabytesStr + " " + activity.getString(R.string.gigabyte) + ")";
			info_supplicant_state = supplicantState;

			info_5ghz_support = wifiManager.is5GHzBandSupported() ? activity.getString(R.string.yes) : activity.getString(R.string.no);
			if (Build.VERSION.SDK_INT >= 30) info_6ghz_support = wifiManager.is6GHzBandSupported() ? activity.getString(R.string.yes) : activity.getString(R.string.no);
			else info_6ghz_support = activity.getString(R.string.no);
			if (Build.VERSION.SDK_INT >= 31) info_60ghz_support = wifiManager.is60GHzBandSupported() ? activity.getString(R.string.yes) : activity.getString(R.string.no);
			else info_60ghz_support = activity.getString(R.string.no);
			info_p2p_support = wifiManager.isP2pSupported() ? activity.getString(R.string.yes) : activity.getString(R.string.no);
			info_tdls_support = wifiManager.isTdlsSupported() ? activity.getString(R.string.yes) : activity.getString(R.string.no);
			if (Build.VERSION.SDK_INT >= 29) {
				info_wpa3_sae_support = wifiManager.isWpa3SaeSupported() ? activity.getString(R.string.yes) : activity.getString(R.string.no);
				info_wpa3_suite_b_support = wifiManager.isWpa3SuiteBSupported() ? activity.getString(R.string.yes) : activity.getString(R.string.no);
			} else {
				info_wpa3_sae_support = activity.getString(R.string.no);
				info_wpa3_suite_b_support = activity.getString(R.string.no);
			}
		}
	}

	private void updateTextviews() {
		textview_ssid.setText(info_ssid);
		textview_bssid.setText(info_bssid);
		textview_ipv4.setText(info_ipv4);
		textview_ipv6.setText(info_ipv6);
		textview_gateway_ip.setText(info_gateway_ip);
		textview_hostname.setText(info_hostname);
		if (Build.VERSION.SDK_INT >= 30) {
			textview_wifi_standard.setText(info_wifi_standard);
		} else if (relativelayout_wifi_standard.getVisibility() != View.GONE) {
			relativelayout_wifi_standard.setVisibility(View.GONE);
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
			Activity a = getActivity();
			if (a == null || !isAdded()) return;
			getAllNetworkInformation();
			a.runOnUiThread(() -> updateTextviews());
			infoHandler.postDelayed(this, keyCardFreqFormatted);
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

	@Override
	public void onStart() {
		super.onStart();
		connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		registerNetworkCallback();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (publicIpRequest != null) {
			publicIpRequest.cancel();
			publicIpRequest = null;
		}
		unregisterNetworkCallback();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHandlerRunning && isWifiConnected()) {
			startInfoHandlerThread();
			startInfoHandler();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (isHandlerRunning) {
			stopInfoHandler();
			stopInfoHandlerThread();
		}
	}

	private void registerNetworkCallback() {
		if (connectivityManager == null) return;

		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override public void onAvailable(Network network) { scheduleCheck(); }
			@Override public void onLost(Network network) { scheduleCheck(); }
			@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
		};

		NetworkRequest request = new NetworkRequest.Builder()
				// Transport-only; no capability filter so we get unvalidated states too
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
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

	public void checkWiFiConnectivity(Boolean shouldStartHandlerThread) {
		if (!isAdded()) return;
		wifiConnected = isWifiConnected();
		if (wifiConnected) {
			showWidgets();
			if (shouldStartHandlerThread && !isHandlerRunning) {
				startInfoHandlerThread();
				startInfoHandler();
			}

			// Check if initial permissions have been handled in MainActivity
			if (MainActivity.areInitialPermissionsHandled() &&
					isLocationPermissionGranted() &&
					!sp.retrieveBoolean(PreferenceKeys.KEY_NEVER_SHOW_GEO_DIALOG, PreferenceDefaults.NEVER_SHOW_GEO_DIALOG)) {

				int gpsDenials = sp.retrieveInt(PreferenceKeys.KEY_GPS_DENIAL_COUNT, PreferenceDefaults.GPS_DENIAL_COUNT);
				if (gpsDenials < MAX_GPS_DENIALS && !isGpsDialogShowingOrPending) {
					/// Notify if GPS is disabled ///
					isGpsDialogShowingOrPending = true;
					new Handler(Looper.getMainLooper()).postDelayed(() -> {
						if (MainActivity.areInitialPermissionsHandled()) {
							requestGPSFeature();
						} else {
							// Reset the flag if permissions aren't handled yet
							isGpsDialogShowingOrPending = false;
						}
					}, 1000);
				}
			}
		} else {
			hideWidgets();
			textview_public_ip.setText(getString(R.string.your_ip_na));
			if (isHandlerRunning) {
				stopInfoHandler();
				stopInfoHandlerThread();
			}
		}
		requireActivity().invalidateMenu();
	}

	private boolean isWifiConnected() {
		return NetworkUtils.isOnline(requireContext(), NetworkUtils.NetworkType.WIFI, false);
	}

	private void showWidgets() {
		textview_noconn.setVisibility(View.GONE);
		cardview_ip.setVisibility(View.VISIBLE);
		cardview_1.setVisibility(View.VISIBLE);
		cardview_2.setVisibility(View.VISIBLE);
		cardview_3.setVisibility(View.VISIBLE);
		cardview_4.setVisibility(View.VISIBLE);
		cardview_5.setVisibility(View.VISIBLE);
		cardview_6.setVisibility(View.VISIBLE);
		fab_update.setVisibility(View.VISIBLE);
	}

	private void hideWidgets() {
		textview_noconn.setVisibility(View.VISIBLE);
		cardview_ip.setVisibility(View.GONE);
		cardview_1.setVisibility(View.GONE);
		cardview_2.setVisibility(View.GONE);
		cardview_3.setVisibility(View.GONE);
		cardview_4.setVisibility(View.GONE);
		cardview_5.setVisibility(View.GONE);
		cardview_6.setVisibility(View.GONE);
		fab_update.setVisibility(View.GONE);
	}

	private void fetchPublicIp() {
		fab_update.setEnabled(false);
		publicIpRequest = NetworkUtils.fetchPublicIp(new NetworkUtils.PublicIpCallback() {
			@Override
			public void onSuccess(String ip) {
				if (!isAdded()) return;
				textview_public_ip.setText(getString(R.string.your_ip, ip));
				new Handler(Looper.getMainLooper()).postDelayed(() -> fab_update.setEnabled(true), 2500);
			}
			@Override
			public void onError(Exception e) {
				if (!isAdded()) return;
				textview_public_ip.setText(getString(R.string.your_ip, getString(R.string.na)));
				new Handler(Looper.getMainLooper()).postDelayed(() -> fab_update.setEnabled(true), 2500);
			}
		});
	}

	private void initCopyableText(Context appContext) {
		textview_public_ip.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.public_ip_address), textview_public_ip.getText().toString());
			return true;
		});
		textview_ssid.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.ssid), textview_ssid.getText().toString());
			return true;
		});
		textview_bssid.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.bssid), textview_bssid.getText().toString());
			return true;
		});
		textview_ipv4.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.ipv4), textview_ipv4.getText().toString());
			return true;
		});
		textview_ipv6.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.ipv6), textview_ipv6.getText().toString());
			return true;
		});
		textview_gateway_ip.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.gateway_ip), textview_gateway_ip.getText().toString());
			return true;
		});
		textview_hostname.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.hostname), textview_hostname.getText().toString());
			return true;
		});
		textview_wifi_standard.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.wifi_standard), textview_wifi_standard.getText().toString());
			return true;
		});
		textview_frequency.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.frequency), textview_frequency.getText().toString());
			return true;
		});
		textview_network_channel.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.network_channel), textview_network_channel.getText().toString());
			return true;
		});
		textview_rssi.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.rssi_signal_strength), textview_rssi.getText().toString());
			return true;
		});
		textview_distance.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.distance), textview_distance.getText().toString());
			return true;
		});
		textview_lease_duration.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.ip_lease_duration), textview_lease_duration.getText().toString());
			return true;
		});
		if (Build.VERSION.SDK_INT >= 29) {
			textview_network_speed.setOnLongClickListener(v -> {
				AppClipboardManager.copyToClipboard(appContext, getString(R.string.network_speed), textview_network_speed.getText().toString());
				return true;
			});
		} else {
			textview_network_speed_legacy.setOnLongClickListener(v -> {
				AppClipboardManager.copyToClipboard(appContext, getString(R.string.network_speed), textview_network_speed_legacy.getText().toString());
				return true;
			});
		}
		textview_transmitted_data.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.transmitted_mbs_gbs), textview_transmitted_data.getText().toString());
			return true;
		});
		textview_received_data.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.received_mbs_gbs), textview_received_data.getText().toString());
			return true;
		});
		textview_dns1.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.dns_1), textview_dns1.getText().toString());
			return true;
		});
		textview_dns2.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.dns_2), textview_dns2.getText().toString());
			return true;
		});
		textview_subnet_mask.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.subnet_mask), textview_subnet_mask.getText().toString());
			return true;
		});
		textview_broadcast_address.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.broadcast_address), textview_broadcast_address.getText().toString());
			return true;
		});
		textview_network_id.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.network_id), textview_network_id.getText().toString());
			return true;
		});
		textview_mac_address.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.mac_address), textview_mac_address.getText().toString());
			return true;
		});
		textview_network_interface.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.network_interface), textview_network_interface.getText().toString());
			return true;
		});
		textview_loopback_address.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.loopback_address), textview_loopback_address.getText().toString());
			return true;
		});
		textview_localhost.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.localhost_address), textview_localhost.getText().toString());
			return true;
		});
		textview_wpa_supplicant_state.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.wpa_supplicant_state), textview_wpa_supplicant_state.getText().toString());
			return true;
		});
		textview_5ghz_support.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string._5ghz_band_support), textview_5ghz_support.getText().toString());
			return true;
		});
		textview_6ghz_support.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string._6ghz_band_support), textview_6ghz_support.getText().toString());
			return true;
		});
		textview_60ghz_support.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string._60ghz_band_support), textview_60ghz_support.getText().toString());
			return true;
		});
		textview_wifi_direct_support.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.wifi_direct_support), textview_wifi_direct_support.getText().toString());
			return true;
		});
		textview_tdls_support.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(appContext, getString(R.string.tdls_support), textview_tdls_support.getText().toString());
			return true;
		});
		if (Build.VERSION.SDK_INT >= 29) {
			textview_wpa3_sae_support.setOnLongClickListener(v -> {
				AppClipboardManager.copyToClipboard(appContext, getString(R.string.wpa3_sae_support), textview_wpa3_sae_support.getText().toString());
				return true;
			});
			textview_wpa3_suite_b_support.setOnLongClickListener(v -> {
				AppClipboardManager.copyToClipboard(appContext, getString(R.string.wpa3_suite_b_support), textview_wpa3_suite_b_support.getText().toString());
				return true;
			});
		}
	}

	public void copyAllTextviews() {
		Activity act = getActivity();
		if (act != null) {
			StringBuilder strB = new StringBuilder();
			strB.append(act.getString(R.string.ssid) + ": " + info_ssid).append("\n")
					.append(act.getString(R.string.bssid) + ": " + info_bssid).append("\n")
					.append(act.getString(R.string.ipv4) + ": " + info_ipv4).append("\n")
					.append(act.getString(R.string.ipv6) + ": " + info_ipv6).append("\n")
					.append(act.getString(R.string.gateway_ip) + ": " + info_gateway_ip).append("\n")
					.append(act.getString(R.string.hostname) + ": " + info_hostname).append("\n")
					.append(act.getString(R.string.wifi_standard) + ": " + info_wifi_standard).append("\n")
					.append(act.getString(R.string.frequency) + ": " + info_frequency).append("\n")
					.append(act.getString(R.string.network_channel) + ": " + info_network_channel).append("\n")
					.append(act.getString(R.string.rssi_signal_strength) + ": " + info_rssi).append("\n")
					.append(act.getString(R.string.distance) + ": " + info_distance).append("\n")
					.append(act.getString(R.string.ip_lease_duration) + ": " + info_lease_time).append("\n");
			if (Build.VERSION.SDK_INT >= 29) {
				strB.append(act.getString(R.string.network_speed) + ": " + info_network_speed).append("\n");
			} else {
				strB.append(act.getString(R.string.network_speed) + ": " + info_network_speed_legacy).append("\n");
			}
			strB.append(act.getString(R.string.transmitted_mbs_gbs) + ": " + info_transmitted_data).append("\n")
					.append(act.getString(R.string.received_mbs_gbs) + ": " + info_received_data).append("\n")
					.append(act.getString(R.string.dns_1) + ": " + info_dns1).append("\n")
					.append(act.getString(R.string.dns_2) + ": " + info_dns2).append("\n")
					.append(act.getString(R.string.subnet_mask) + ": " + info_subnet_mask).append("\n")
					.append(act.getString(R.string.network_id) + ": " + info_network_id).append("\n")
					.append(act.getString(R.string.mac_address) + ": " + info_mac_addr).append("\n")
					.append(act.getString(R.string.network_interface) + ": " + info_network_interface).append("\n")
					.append(act.getString(R.string.loopback_address) + ": " + info_loopback_addr).append("\n")
					.append(act.getString(R.string.localhost_address) + ": " + info_localhost_addr).append("\n")
					.append(act.getString(R.string.wpa_supplicant_state) + ": " + info_supplicant_state).append("\n")
					.append(act.getString(R.string._5ghz_band_support) + ": " + info_5ghz_support).append("\n")
					.append(act.getString(R.string._6ghz_band_support) + ": " + info_6ghz_support).append("\n")
					.append(act.getString(R.string._60ghz_band_support) + ": " + info_60ghz_support).append("\n")
					.append(act.getString(R.string.wifi_direct_support) + ": " + info_p2p_support).append("\n")
					.append(act.getString(R.string.tdls_support) + ": " + info_tdls_support).append("\n")
					.append(act.getString(R.string.wpa3_sae_support) + ": " + info_wpa3_sae_support).append("\n")
					.append(act.getString(R.string.wpa3_suite_b_support) + ": " + info_wpa3_suite_b_support);
			AppClipboardManager.copyToClipboard(act.getApplicationContext(), "all_info_text", String.valueOf(strB));
		}
	}
}