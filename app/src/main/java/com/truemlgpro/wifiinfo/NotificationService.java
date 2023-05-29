package com.truemlgpro.wifiinfo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class NotificationService extends Service {
	private Notification notification26_28;
	private Notification notification29;
	private Notification notification21_25;
	private Notification.Builder builder;

	private final int NOTIFICATION_ID_API26_28 = 1001;
	private final int NOTIFICATION_ID_API29 = 1002;
	private final int NOTIFICATION_ID_API21_25 = 1003;

	private boolean shouldPostAnUpdate = true;

	private int visSigStrgNtfcColor;

	@Override
	public void onCreate() {
		super.onCreate();
		handler.post(runnable);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shouldPostAnUpdate = false;
		handler.removeCallbacks(runnable);
		if (Build.VERSION.SDK_INT < 24) {
			stopForeground(true);
		} else {
			stopForeground(STOP_FOREGROUND_REMOVE);
		}
		stopSelf();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
			/// Android 8 - Android 9 ///
			showNotificationAPI26_28();
			startForeground(NOTIFICATION_ID_API26_28, notification26_28);
		} else if (Build.VERSION.SDK_INT >= 29) {
			/// Android 10 & higher ///
			showNotificationAPI29();
			startForeground(NOTIFICATION_ID_API29, notification29);
		} else if (Build.VERSION.SDK_INT < 26) {
			/// Android 5 - Android 7 ///
			showNotificationAPI21_25();
			startForeground(NOTIFICATION_ID_API21_25, notification21_25);
		}
		return START_NOT_STICKY;
	}
	
	/// Handler for Notification Updates ///
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (!shouldPostAnUpdate) {
				return;
			}
			int keyNtfcFreqFormatted = Integer.parseInt(new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_NTFC_FREQ, MainActivity.ntfcUpdateInterval));
			if (Build.VERSION.SDK_INT >= 29) {
				showNotificationAPI29();
			} else if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
				showNotificationAPI26_28();
			} else if (Build.VERSION.SDK_INT < 26) {
				showNotificationAPI21_25();
			}
			handler.postDelayed(runnable, keyNtfcFreqFormatted);
		}
	};

	private Intent getNtfcSettingsActivityIntent() {
		Intent intentActionSettings = new Intent();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			intentActionSettings.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			intentActionSettings.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
			intentActionSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			intentActionSettings.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			intentActionSettings.putExtra("app_package", getPackageName());
			intentActionSettings.putExtra("app_uid", getApplicationInfo().uid);
			intentActionSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		return intentActionSettings;
	}

	/// ANDROID 8 - ANDROID 9 ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showNotificationAPI26_28() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 10011, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10012, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(this, 10013, getNtfcSettingsActivityIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
		builder = new Notification.Builder(this, channelID);

		boolean keyNtfcColor = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, MainActivity.colorizeNtfc);
		boolean keyVisSigStrgNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, MainActivity.visualizeSigStrg);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) {
			ssid = getString(R.string.na);
		} else {
			ssid = ssid.replaceAll("^\"|\"$", "");
		}
		String bssid = "";
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		}
		if (bssid.contains("02:00:00:00:00:00")) {
			bssid = getString(R.string.na);
		}
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			} else if (RSSIconv >= 50 && RSSIconv < 75) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			} else if (RSSIconv >= 1 && RSSIconv < 50) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
			}
		} else {
			visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", freqRssiToDistance(freq, rssi));
		int channel = convertFrequencyToChannel(freq);
		int networkSpeed = wInfo.getLinkSpeed();

		String collapsedInfo = getString(R.string.ssid) + ": " + ssid + " | " + getString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = getString(R.string.ssid) + ": " + ssid + "\n" + getString(R.string.bssid) + ": " + bssid + "\n" + getString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" +
			getString(R.string.distance) + ": " + distanceFromRssiRounded + "\n" + getString(R.string.frequency) + ": " + freq + "MHz" + "\n" + getString(R.string.network_channel) + ": " + channel + "\n" +
			getString(R.string.network_speed) + ": " + networkSpeed + " / " + networkSpeed + " Mbps";

		notification26_28 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.wifi_24px)
			.setContentTitle(getString(R.string.local_ip) + ": " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.stop_24px, getString(R.string.stop_services), pIntentActionStop)
			.addAction(R.drawable.settings_24px, getString(R.string.notification_settings), pIntentActionSettings)
			.setChannelId(channelID)
			.setColorized(keyNtfcColor)
			.setColor(visSigStrgNtfcColor)
			.setCategory(Notification.CATEGORY_SERVICE)
			.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setAutoCancel(false)
			.build();

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
		builder = new Notification.Builder(this, channelID);

		boolean keyNtfcColor = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_CLR_CHECK, MainActivity.colorizeNtfc);
		boolean keyVisSigStrgNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, MainActivity.visualizeSigStrg);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) {
			ssid = getString(R.string.na);
		} else {
			ssid = ssid.replaceAll("^\"|\"$", "");
		}
		String bssid = "";
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		}
		if (bssid.contains("02:00:00:00:00:00")) {
			bssid = getString(R.string.na);
		}
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			} else if (RSSIconv >= 50 && RSSIconv < 75) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			} else if (RSSIconv >= 1 && RSSIconv < 50) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
			}
		} else {
			visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", freqRssiToDistance(freq, rssi));
		int channel = convertFrequencyToChannel(freq);
		int TXLinkSpd = wInfo.getTxLinkSpeedMbps();
		int RXLinkSpd = wInfo.getRxLinkSpeedMbps();
		String networkSpeed = RXLinkSpd + " / " + TXLinkSpd + " Mbps";

		String collapsedInfo = getString(R.string.ssid) + ": " + ssid + " | " + getString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = getString(R.string.ssid) + ": " + ssid + "\n" + getString(R.string.bssid) + ": " + bssid + "\n" + getString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" +
			getString(R.string.distance) + ": " + distanceFromRssiRounded + "\n" + getString(R.string.frequency) + ": " + freq + "MHz" + "\n" + getString(R.string.network_channel) + ": " + channel + "\n" +
			getString(R.string.network_speed) + ": " + networkSpeed;

		notification29 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.wifi_24px)
			.setContentTitle(getString(R.string.local_ip) + ": " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.stop_24px, getString(R.string.stop_services), pIntentActionStop)
			.addAction(R.drawable.settings_24px, getString(R.string.notification_settings), pIntentActionSettings)
			.setChannelId(channelID)
			.setColorized(keyNtfcColor)
			.setColor(visSigStrgNtfcColor)
			.setCategory(Notification.CATEGORY_SERVICE)
			.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setAutoCancel(false)
			.build();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID_API29, notification29);
	}

	/// ANDROID 5 - ANDROID 7 ///
	public void showNotificationAPI21_25() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 10031, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10032, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(this, 10033, getNtfcSettingsActivityIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager notificationManager;
		builder = new Notification.Builder(this);
		boolean keyVisSigStrgNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_VIS_SIG_STRG_CHECK, MainActivity.visualizeSigStrg);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) {
			ssid = getString(R.string.na);
		} else {
			ssid = ssid.replaceAll("^\"|\"$", "");
		}
		String bssid;
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		} else {
			bssid = getString(R.string.na);
		}
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			} else if (RSSIconv >= 50 && RSSIconv < 75) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			} else if (RSSIconv >= 1 && RSSIconv < 50) {
				visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
			}
		} else {
			visSigStrgNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", freqRssiToDistance(freq, rssi));
		int channel = convertFrequencyToChannel(freq);
		int networkSpeed = wInfo.getLinkSpeed();

		String collapsedInfo = getString(R.string.ssid) + ": " + ssid + " | " + getString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = getString(R.string.ssid) + ": " + ssid + "\n" + getString(R.string.bssid) + ": " + bssid + "\n" + getString(R.string.rssi) + ": " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" +
			getString(R.string.distance) + ": " + distanceFromRssiRounded + "\n" + getString(R.string.frequency) + ": " + freq + "MHz" + "\n" + getString(R.string.network_channel) + ": " + channel + "\n" +
			getString(R.string.network_speed) + ": " + networkSpeed + " / " + networkSpeed + " Mbps";

		notification21_25 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.wifi_24px)
			.setContentTitle(getString(R.string.local_ip) + ": " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.stop_24px, getString(R.string.stop_services), pIntentActionStop)
			.addAction(R.drawable.settings_24px, getString(R.string.notification_settings), pIntentActionSettings)
			.setPriority(Notification.PRIORITY_LOW)
			.setColor(visSigStrgNtfcColor)
			.setCategory(Notification.CATEGORY_SERVICE)
			.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setAutoCancel(false)
			.build();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID_API21_25, notification21_25);
	}

	/// END ///

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

	@RequiresApi(Build.VERSION_CODES.O)
	private String createNotificationChannel(NotificationManager notificationManager) {
		String channelID = "wifi_info";
		CharSequence channelName = "Notification Service";
		NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);
		channel.setDescription("Main Wi-Fi Info Notification");
		channel.setShowBadge(false);
		notificationManager.createNotificationChannel(channel);
		return channelID;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
