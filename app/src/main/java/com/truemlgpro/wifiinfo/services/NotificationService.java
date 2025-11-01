package com.truemlgpro.wifiinfo.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.receivers.ActionButtonReceiver;
import com.truemlgpro.wifiinfo.ui.activities.MainActivity;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;

public class NotificationService extends Service {
	private Notification notification21_25;
	private Notification notification26_28;
	private Notification notification29;
	private Notification.Builder builder;

	private static final String CHANNEL_ID = "wifi_info";
	private static final int NOTIFICATION_ID_API21_25 = 1001;
	private static final int NOTIFICATION_ID_API26_28 = 1002;
	private static final int NOTIFICATION_ID_API29 = 1003;
	private int visualizeSignalStrengthNtfcColor;

	private SharedPreferencesManager sp;
	private Context localizedContext;

	public static boolean shouldPostAnUpdate = true;
	public static boolean isNotificationServiceRunning;

	@Override
	public void onCreate() {
		super.onCreate();
		sp = new SharedPreferencesManager(getApplicationContext());
		localizedContext = LocaleManager.getLocalizedContext(getApplicationContext());
		isNotificationServiceRunning = true;
		handler.post(runnable);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shouldPostAnUpdate = false;
		handler.removeCallbacks(runnable);

		stopForeground(true);
		cancelOwnNotification();

		isNotificationServiceRunning = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT < 26) {
			showNotificationAPI21_25();
			startForeground(NOTIFICATION_ID_API21_25, notification21_25);
		} else if (Build.VERSION.SDK_INT < 29) {
			showNotificationAPI26_28();
			startForeground(NOTIFICATION_ID_API26_28, notification26_28);
		} else {
			showNotificationAPI29();
			// SPECIAL_USE for Android 14+
			startForeground(
					NOTIFICATION_ID_API29,
					notification29,
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
							? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
							: 0
			);
		}
		return START_NOT_STICKY;
	}

	@NonNull
	private String getLocalizedString(@StringRes int stringRes) {
		return localizedContext.getResources().getString(stringRes);
	}

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (!isNotificationServiceRunning || !shouldPostAnUpdate) return;

			int keyNtfcFreqFormatted = Integer.parseInt(
					new SharedPreferencesManager(getApplicationContext()).retrieveString(
							PreferenceKeys.KEY_PREF_NOTIFICATION_INTERVAL, PreferenceDefaults.NOTIFICATION_UPDATE_INTERVAL
					)
			);

			if (Build.VERSION.SDK_INT < 26) {
				showNotificationAPI21_25();
			} else if (Build.VERSION.SDK_INT < 29) {
				showNotificationAPI26_28();
			} else {
				showNotificationAPI29();
			}
			handler.postDelayed(this, keyNtfcFreqFormatted);
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

	private void cancelOwnNotification() {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (nm == null) return;
		if (Build.VERSION.SDK_INT < 26) nm.cancel(NOTIFICATION_ID_API21_25);
		else if (Build.VERSION.SDK_INT < 29) nm.cancel(NOTIFICATION_ID_API26_28);
		else nm.cancel(NOTIFICATION_ID_API29);
	}

	// ANDROID 5 - ANDROID 7
	public void showNotificationAPI21_25() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(
				this, 10031, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
		);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction(ActionButtonReceiver.ACTION_STOP);
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(
				this, 10032, intentActionStop,
				(Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT)
		);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(
				this, 10033, getNtfcSettingsActivityIntent(),
				(Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT)
		);

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder = new Notification.Builder(this);

		boolean keyVisSigStrgNtfc = sp.retrieveBoolean(PreferenceKeys.KEY_PREF_VISUALIZE_SIGNAL_STRENGTH, PreferenceDefaults.VISUALIZE_SIGNAL_STRENGTH);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = NetworkUtils.getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) ssid = getLocalizedString(R.string.na);
		else ssid = ssid.replaceAll("^\"|\"$", "");
		String bssid = (wInfo.getBSSID() != null) ? wInfo.getBSSID().toUpperCase() : getLocalizedString(R.string.na);

		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			else if (RSSIconv >= 50) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			else if (RSSIconv >= 1) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
		} else {
			visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", NetworkUtils.convertFreqRssiToDistance(freq, rssi));
		int channel = NetworkUtils.convertFrequencyToChannel(freq);
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

		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API21_25, notification21_25);
	}

	// ANDROID 8 - ANDROID 9
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showNotificationAPI26_28() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(
				this, 10011, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction(ActionButtonReceiver.ACTION_STOP);
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(
				this, 10012, intentActionStop,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(
				this, 10013, getNtfcSettingsActivityIntent(),
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		String ntfcChannelId = CHANNEL_ID;
		builder = new Notification.Builder(this, ntfcChannelId);

		boolean keyNtfcColor = sp.retrieveBoolean(PreferenceKeys.KEY_PREF_COLORIZE_NOTIFICATION, PreferenceDefaults.COLORIZE_NOTIFICATION);
		boolean keyVisSigStrgNtfc = sp.retrieveBoolean(PreferenceKeys.KEY_PREF_VISUALIZE_SIGNAL_STRENGTH, PreferenceDefaults.VISUALIZE_SIGNAL_STRENGTH);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = NetworkUtils.getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) ssid = getLocalizedString(R.string.na);
		else ssid = ssid.replaceAll("^\"|\"$", "");
		String bssid = (wInfo.getBSSID() != null) ? wInfo.getBSSID().toUpperCase() : getLocalizedString(R.string.na);
		if (bssid.contains("02:00:00:00:00:00")) bssid = getLocalizedString(R.string.na);

		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			else if (RSSIconv >= 50) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			else if (RSSIconv >= 1) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
		} else {
			visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", NetworkUtils.convertFreqRssiToDistance(freq, rssi));
		int channel = NetworkUtils.convertFrequencyToChannel(freq);
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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API26_28, notification26_28);
	}

	// ANDROID 10 & higher
	@RequiresApi(api = Build.VERSION_CODES.Q)
	public void showNotificationAPI29() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(
				this, 10021, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction(ActionButtonReceiver.ACTION_STOP);
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(
				this, 10022, intentActionStop,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);
		PendingIntent pIntentActionSettings = PendingIntent.getActivity(
				this, 10023, getNtfcSettingsActivityIntent(),
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		String ntfcChannelId = CHANNEL_ID;
		builder = new Notification.Builder(this, ntfcChannelId);

		boolean keyNtfcColor = sp.retrieveBoolean(PreferenceKeys.KEY_PREF_COLORIZE_NOTIFICATION, PreferenceDefaults.COLORIZE_NOTIFICATION);
		boolean keyVisSigStrgNtfc = sp.retrieveBoolean(PreferenceKeys.KEY_PREF_VISUALIZE_SIGNAL_STRENGTH, PreferenceDefaults.VISUALIZE_SIGNAL_STRENGTH);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = NetworkUtils.getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) ssid = getLocalizedString(R.string.na);
		else ssid = ssid.replaceAll("^\"|\"$", "");
		String bssid = (wInfo.getBSSID() != null) ? wInfo.getBSSID().toUpperCase() : getLocalizedString(R.string.na);
		if (bssid.contains("02:00:00:00:00:00")) bssid = getLocalizedString(R.string.na);

		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		if (keyVisSigStrgNtfc) {
			if (RSSIconv >= 75) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalHigh);
			else if (RSSIconv >= 50) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalAvg);
			else if (RSSIconv >= 1) visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColorSignalLow);
		} else {
			visualizeSignalStrengthNtfcColor = getResources().getColor(R.color.ntfcColor);
		}
		int freq = wInfo.getFrequency();
		String distanceFromRssiRounded = String.format("~%.1fm", NetworkUtils.convertFreqRssiToDistance(freq, rssi));
		int channel = NetworkUtils.convertFrequencyToChannel(freq);
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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API29, notification29);
	}

	@Override
	public IBinder onBind(Intent intent) { return null; }
}
