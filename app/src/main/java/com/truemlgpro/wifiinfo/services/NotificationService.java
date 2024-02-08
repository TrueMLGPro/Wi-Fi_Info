package com.truemlgpro.wifiinfo.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.receivers.ActionButtonReceiver;
import com.truemlgpro.wifiinfo.ui.MainActivity;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NotificationService extends Service {
	private Notification notification21_25;
	private Notification notification26_28;
	private Notification notification29;
	private Notification.Builder builder;

	private final String CHANNEL_ID = "wifi_info";
	private final int NOTIFICATION_ID_API21_25 = 1001;
	private final int NOTIFICATION_ID_API26_28 = 1002;
	private final int NOTIFICATION_ID_API29 = 1003;
	private int visualizeSignalStrengthNtfcColor;

	private Context localizedContext;

	public static boolean shouldPostAnUpdate = true;
	public static boolean isNotificationServiceRunning;

	@Override
	public void onCreate() {
		super.onCreate();
		handler.post(runnable);
		isNotificationServiceRunning = true;
		initLocaleConfig();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
		if (Build.VERSION.SDK_INT < 24) {
			stopForeground(true);
		} else {
			stopForeground(STOP_FOREGROUND_REMOVE);
		}
		stopSelf();
		isNotificationServiceRunning = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT < 26) {
			/// Android 5 - Android 7 ///
			showNotificationAPI21_25();
			startForeground(NOTIFICATION_ID_API21_25, notification21_25);
		} else if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
			/// Android 8 - Android 9 ///
			showNotificationAPI26_28();
			startForeground(NOTIFICATION_ID_API26_28, notification26_28);
		} else if (Build.VERSION.SDK_INT >= 29) {
			/// Android 10 & higher ///
			showNotificationAPI29();
			startForeground(NOTIFICATION_ID_API29, notification29, Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE : 0);
		}
		return START_NOT_STICKY;
	}

	private void initLocaleConfig() {
		String localePref = new SharedPreferencesManager(getApplicationContext()).retrieveString(PreferenceKeys.KEY_PREF_APP_LANGUAGE, PreferenceDefaults.APP_LANG);
		if (localePref.equals(PreferenceDefaults.APP_LANG))
			localePref = LocaleManager.getDefaultSystemLocale();
		Locale currentAppLocale = Locale.forLanguageTag(localePref);
		Configuration conf = getApplicationContext().getResources().getConfiguration();
		conf.setLocale(currentAppLocale);
		localizedContext = getApplicationContext().createConfigurationContext(conf);
	}

	@NonNull
	private String getLocalizedString(@StringRes int stringRes) {
		return localizedContext.getResources().getString(stringRes);
	}

	/// Handler for notification updates ///
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (shouldPostAnUpdate) {
				int keyNtfcFreqFormatted = Integer.parseInt(new SharedPreferencesManager(getApplicationContext()).retrieveString(PreferenceKeys.KEY_PREF_NTFC_FREQ, PreferenceDefaults.NTFC_UPDATE_INTERVAL));
				if (Build.VERSION.SDK_INT < 26) {
					showNotificationAPI21_25();
				} else if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
					showNotificationAPI26_28();
				} else if (Build.VERSION.SDK_INT >= 29) {
					showNotificationAPI29();
				}
				handler.postDelayed(runnable, keyNtfcFreqFormatted);
			}
		}
	};

	private Intent getNtfcSettingsActivityIntent() {
		Intent intentActionSettings = new Intent();
		intentActionSettings.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			intentActionSettings.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
			intentActionSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			intentActionSettings.putExtra("app_package", getPackageName());
			intentActionSettings.putExtra("app_uid", getApplicationInfo().uid);
			intentActionSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		return intentActionSettings;
	}

	/// ANDROID 5 - ANDROID 7 ///
	public void showNotificationAPI21_25() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		@SuppressLint("UnspecifiedImmutableFlag")
		PendingIntent content_intent = PendingIntent.getActivity(this, 10031, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10032, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(this, 10033, getNtfcSettingsActivityIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager notificationManager;
		builder = new Notification.Builder(this);
		boolean keyVisSigStrgNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_PREF_VISUALIZE_SIGNAL_STRENGTH, PreferenceDefaults.VISUALIZE_SIGNAL_STRENGTH);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) {
			ssid = getLocalizedString(R.string.na);
		} else {
			ssid = ssid.replaceAll("^\"|\"$", "");
		}
		String bssid;
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		} else {
			bssid = getLocalizedString(R.string.na);
		}
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			} else if (RSSIconv >= 50 && RSSIconv < 75) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			} else if (RSSIconv >= 1 && RSSIconv < 50) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
			}
		} else {
			visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", freqRssiToDistance(freq, rssi));
		int channel = convertFrequencyToChannel(freq);
		int networkSpeed = wInfo.getLinkSpeed();

		String collapsedInfo = getLocalizedString(R.string.ssid) + ": " + ssid + " | " + getLocalizedString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = getLocalizedString(R.string.ssid) + ": " + ssid + "\n" + getLocalizedString(R.string.bssid) + ": " + bssid + "\n" + getLocalizedString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" +
				getLocalizedString(R.string.distance) + ": " + distanceFromRssiRounded + "\n" + getLocalizedString(R.string.frequency) + ": " + freq + "MHz" + "\n" + getLocalizedString(R.string.network_channel) + ": " + channel + "\n" +
				getLocalizedString(R.string.network_speed) + ": " + networkSpeed + " / " + networkSpeed + " Mbps";

		notification21_25 = builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.wifi_24px)
				.setContentTitle(getLocalizedString(R.string.local_ip) + ": " + ip)
				.setContentText(collapsedInfo)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.stop_24px, getLocalizedString(R.string.stop_services), pIntentActionStop)
				.addAction(R.drawable.settings_24px, getLocalizedString(R.string.notification_settings), pIntentActionSettings)
				.setPriority(Notification.PRIORITY_LOW)
				.setColor(visualizeSignalStrengthNtfcColor)
				.setCategory(Notification.CATEGORY_SERVICE)
				.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID_API21_25, notification21_25);
	}

	/// ANDROID 8 - ANDROID 9 ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showNotificationAPI26_28() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		@SuppressLint("UnspecifiedImmutableFlag")
		PendingIntent content_intent = PendingIntent.getActivity(this, 10011, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10012, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(this, 10013, getNtfcSettingsActivityIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

		String ntfcChannelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CHANNEL_ID : "";
		builder = new Notification.Builder(this, ntfcChannelId);

		boolean keyNtfcColor = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_PREF_COLORIZE_NTFC, PreferenceDefaults.COLORIZE_NTFC);
		boolean keyVisSigStrgNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_PREF_VISUALIZE_SIGNAL_STRENGTH, PreferenceDefaults.VISUALIZE_SIGNAL_STRENGTH);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) {
			ssid = getLocalizedString(R.string.na);
		} else {
			ssid = ssid.replaceAll("^\"|\"$", "");
		}
		String bssid = "";
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		}
		if (bssid.contains("02:00:00:00:00:00")) {
			bssid = getLocalizedString(R.string.na);
		}
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			} else if (RSSIconv >= 50 && RSSIconv < 75) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			} else if (RSSIconv >= 1 && RSSIconv < 50) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
			}
		} else {
			visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", freqRssiToDistance(freq, rssi));
		int channel = convertFrequencyToChannel(freq);
		int networkSpeed = wInfo.getLinkSpeed();

		String collapsedInfo = getLocalizedString(R.string.ssid) + ": " + ssid + " | " + getLocalizedString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = getLocalizedString(R.string.ssid) + ": " + ssid + "\n" + getLocalizedString(R.string.bssid) + ": " + bssid + "\n" + getLocalizedString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" +
			getLocalizedString(R.string.distance) + ": " + distanceFromRssiRounded + "\n" + getLocalizedString(R.string.frequency) + ": " + freq + "MHz" + "\n" + getLocalizedString(R.string.network_channel) + ": " + channel + "\n" +
			getLocalizedString(R.string.network_speed) + ": " + networkSpeed + " / " + networkSpeed + " Mbps";

		notification26_28 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.wifi_24px)
			.setContentTitle(getLocalizedString(R.string.local_ip) + ": " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.stop_24px, getLocalizedString(R.string.stop_services), pIntentActionStop)
			.addAction(R.drawable.settings_24px, getLocalizedString(R.string.notification_settings), pIntentActionSettings)
			.setChannelId(ntfcChannelId)
			.setColorized(keyNtfcColor)
			.setColor(visualizeSignalStrengthNtfcColor)
			.setCategory(Notification.CATEGORY_SERVICE)
			.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setAutoCancel(false)
			.build();

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID_API26_28, notification26_28);
	}

	/// ANDROID 10 & higher ///
	@RequiresApi(api = Build.VERSION_CODES.Q)
	public void showNotificationAPI29() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 10021, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10022, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(this, 10023, getNtfcSettingsActivityIntent(), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		String ntfcChannelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CHANNEL_ID : "";
		builder = new Notification.Builder(this, ntfcChannelId);

		boolean keyNtfcColor = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_PREF_COLORIZE_NTFC, PreferenceDefaults.COLORIZE_NTFC);
		boolean keyVisSigStrgNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_PREF_VISUALIZE_SIGNAL_STRENGTH, PreferenceDefaults.VISUALIZE_SIGNAL_STRENGTH);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) {
			ssid = getLocalizedString(R.string.na);
		} else {
			ssid = ssid.replaceAll("^\"|\"$", "");
		}
		String bssid = "";
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		}
		if (bssid.contains("02:00:00:00:00:00")) {
			bssid = getLocalizedString(R.string.na);
		}
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			} else if (RSSIconv >= 50 && RSSIconv < 75) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			} else if (RSSIconv >= 1 && RSSIconv < 50) {
				visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
			}
		} else {
			visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", freqRssiToDistance(freq, rssi));
		int channel = convertFrequencyToChannel(freq);
		int TXLinkSpd = wInfo.getTxLinkSpeedMbps();
		int RXLinkSpd = wInfo.getRxLinkSpeedMbps();
		String networkSpeed = RXLinkSpd + " / " + TXLinkSpd + " Mbps";

		String collapsedInfo = getLocalizedString(R.string.ssid) + ": " + ssid + " | " + getLocalizedString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = getLocalizedString(R.string.ssid) + ": " + ssid + "\n" + getLocalizedString(R.string.bssid) + ": " + bssid + "\n" + getLocalizedString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" +
			getLocalizedString(R.string.distance) + ": " + distanceFromRssiRounded + "\n" + getLocalizedString(R.string.frequency) + ": " + freq + "MHz" + "\n" + getLocalizedString(R.string.network_channel) + ": " + channel + "\n" +
			getLocalizedString(R.string.network_speed) + ": " + networkSpeed;

		notification29 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.wifi_24px)
			.setContentTitle(getLocalizedString(R.string.local_ip) + ": " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.stop_24px, getLocalizedString(R.string.stop_services), pIntentActionStop)
			.addAction(R.drawable.settings_24px, getLocalizedString(R.string.notification_settings), pIntentActionSettings)
			.setChannelId(ntfcChannelId)
			.setColorized(keyNtfcColor)
			.setColor(visualizeSignalStrengthNtfcColor)
			.setCategory(Notification.CATEGORY_SERVICE)
			.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setAutoCancel(false)
			.build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID_API29, notification29);
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
		} catch (SocketException e) {
			Log.e("getIPv4Address()", e.toString());
		}
		return null;
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

	private double freqRssiToDistance(int frequency, int rssi) {
		return Math.pow(10.0D, (27.55D - 20 * Math.log10((double) frequency) + Math.abs(rssi)) / 20.0D);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
