package com.truemlgpro.wifiinfo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.Enumeration;

public class NotificationService extends Service {
	private BroadcastReceiver NotificationServiceStopReceiver;
	private Notification notification26_28;
	private Notification notification29;
	private Notification notification21_25;
	private Notification.Builder builder;
	
	private int visSigStrgNtfcColor;

	@Override
	public void onCreate()
	{
		super.onCreate();
		handler.post(runnable);
		IntentFilter filter = new IntentFilter();
		filter.addAction("ACTION_STOP_FOREGROUND");
		NotificationServiceStopReceiver = new NotificationServiceStopReceiver();
		registerReceiver(NotificationServiceStopReceiver, filter);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		handler.removeCallbacks(runnable);
		unregisterReceiver(NotificationServiceStopReceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		/// Notification Settings Button Receiver ///
		try {
			if (intent.getAction() != null && intent.getAction().equals("ACTION_NTFC_SETTINGS")) {
				startNtfcSettingsActivity();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
			/// Android 8 - Android 9 ///
			showNotificationAPI26_28();
			startForeground(1301, notification26_28);
		} else if (android.os.Build.VERSION.SDK_INT >= 29) {
			/// Android 10 & higher ///
			showNotificationAPI29();
			startForeground(1302, notification29);
		} else if (android.os.Build.VERSION.SDK_INT < 26) {
			/// Android 5 - Android 7 ///
			showNotificationAPI21_25();
			startForeground(1303, notification21_25);
		}
		
		return START_NOT_STICKY;
	}
	
	/// Handler for Notification Updates ///
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			String keyNtfcFreq = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_NTFC_FREQ, MainActivity.ntfcUpdateInterval);
			int keyNtfcFreqFormatted = Integer.parseInt(keyNtfcFreq);
			
			if (android.os.Build.VERSION.SDK_INT >= 29) {
				showNotificationAPI29();
			} else if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
				showNotificationAPI26_28();
			} else if (android.os.Build.VERSION.SDK_INT < 26) {
				showNotificationAPI21_25();
			}

			handler.postDelayed(runnable, keyNtfcFreqFormatted);
		}
	};

	public class NotificationServiceStopReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null && intent.getAction().equals("ACTION_STOP_FOREGROUND")) {
				handler.removeCallbacks(runnable);
				if (Build.VERSION.SDK_INT < 24) {
					stopForeground(true);
				} else {
					stopForeground(STOP_FOREGROUND_REMOVE);
				}
			}
		}
	}

	/// Notification Settings Activity ///
	public void startNtfcSettingsActivity() {
		Intent Ntfc_Intent = new Intent();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Ntfc_Intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			Ntfc_Intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
			Ntfc_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			Ntfc_Intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			Ntfc_Intent.putExtra("app_package", getPackageName());
			Ntfc_Intent.putExtra("app_uid", getApplicationInfo().uid);
			Ntfc_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		startActivity(Ntfc_Intent);
	}

	/// ANDROID 8 - ANDROID 9 ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showNotificationAPI26_28() {
		int NOTIFICATION_ID = 1301;

		Intent NotificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 0, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent intentActionSettings = new Intent(this, ActionButtonReceiver.class);
		intentActionSettings.setAction("ACTION_NTFC_SETTINGS");
		PendingIntent pIntentActionSettings = PendingIntent.getBroadcast(this, 0, intentActionSettings, PendingIntent.FLAG_UPDATE_CURRENT);

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
			ssid = "N/A";
		}

		String bssid = "";
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		}

		if (bssid.contains("02:00:00:00:00:00")) {
			bssid = "N/A";
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
		int channel = convertFrequencyToChannel(freq);
		int networkSpeed = wInfo.getLinkSpeed();
		String network_id = String.valueOf(wInfo.getNetworkId());
		if (network_id.contains("-1")) {
			network_id = "N/A";
		}

		String collapsedInfo = "SSID: " + ssid + " | " + "Signal Strength: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = "SSID: " + ssid + "\n" + "BSSID: " + bssid + "\n" + "Signal Strength: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" + 
			"Frequency: " + freq + "MHz" + "\n" + "Network Channel: " + channel + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "Network ID: " + network_id;

		notification26_28 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.ic_wifi)
			.setContentTitle("Local IP: " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.ic_stop, "Stop Services", pIntentActionStop)
			.addAction(R.drawable.ic_settings_light, "Notification Settings", pIntentActionSettings)
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
			notificationManager.notify(NOTIFICATION_ID, notification26_28);
	}

	/// ANDROID 10 & higher ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showNotificationAPI29() {
		int NOTIFICATION_ID = 1302;

		Intent NotificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 0, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		Intent intentActionSettings = new Intent(this, ActionButtonReceiver.class);
		intentActionSettings.setAction("ACTION_NTFC_SETTINGS");
		PendingIntent pIntentActionSettings = PendingIntent.getBroadcast(this, 0, intentActionSettings, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		
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
			ssid = "N/A";
		}

		String bssid = "";
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		}

		if (bssid.contains("02:00:00:00:00:00")) {
			bssid = "N/A";
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
		int channel = convertFrequencyToChannel(freq);
		int networkSpeed = wInfo.getLinkSpeed();
		String network_id = String.valueOf(wInfo.getNetworkId());
		if (network_id.contains("-1")) {
			network_id = "N/A";
		}

		String collapsedInfo = "SSID: " + ssid + " | " + "Signal Strength: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = "SSID: " + ssid + "\n" + "BSSID: " + bssid + "\n" + "Signal Strength: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" +
				"Frequency: " + freq + "MHz" + "\n" + "Network Channel: " + channel + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "ID: " + network_id;

		notification29 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.ic_wifi)
			.setContentTitle("Local IP: " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.ic_stop, "Stop Services", pIntentActionStop)
			.addAction(R.drawable.ic_settings_light, "Notification Settings", pIntentActionSettings)
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
		notificationManager.notify(NOTIFICATION_ID, notification29);
	}

	/// ANDROID 5 - ANDROID 7 ///
	public void showNotificationAPI21_25() {
		int NOTIFICATION_ID = 1303;

		Intent NotificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 0, intentActionStop, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent intentActionSettings = new Intent(this, ActionButtonReceiver.class);
		intentActionSettings.setAction("ACTION_NTFC_SETTINGS");
		PendingIntent pIntentActionSettings = PendingIntent.getBroadcast(this, 0, intentActionSettings, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationManager notificationManager;
		builder = new Notification.Builder(this);

		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = mainWifi.getConnectionInfo();
		String ip = getIPv4Address();
		String ssid = wInfo.getSSID();
		if (ssid.equals("<unknown ssid>")) {
			ssid = "N/A";
		}
		String bssid;
		if (wInfo.getBSSID() != null) {
			bssid = wInfo.getBSSID().toUpperCase();
		} else {
			bssid = "N/A";
		}
		int rssi = wInfo.getRssi();
		int RSSIconv = WifiManager.calculateSignalLevel(rssi, 101);
		int freq = wInfo.getFrequency();
		int channel = convertFrequencyToChannel(freq);
		int networkSpeed = wInfo.getLinkSpeed();
		int network_id = wInfo.getNetworkId();

		String collapsedInfo = "SSID: " + ssid + " | " + "Signal Strength: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + " | " + freq + " MHz " + "(Ch: " + channel + ")";
		String extendedInfo = "SSID: " + ssid + "\n" + "BSSID: " + bssid + "\n" + "Signal Strength: " + RSSIconv + "%" + " (" + rssi + "dBm" + ")" + "\n" + 
			"Frequency: " + freq + "MHz" + "\n" + "Network Channel: " + channel + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "ID: " + network_id;

		notification21_25 = builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.ic_wifi)
			.setContentTitle("Local IP: " + ip)
			.setContentText(collapsedInfo)
			.setWhen(System.currentTimeMillis())
			.addAction(R.drawable.ic_stop, "Stop Services", pIntentActionStop)
			.addAction(R.drawable.ic_settings_light, "Notification Settings", pIntentActionSettings)
			.setPriority(Notification.PRIORITY_LOW)
			.setColor(getResources().getColor(R.color.ntfcColor))
			.setCategory(Notification.CATEGORY_SERVICE)
			.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setAutoCancel(false)
			.build();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification21_25);
	}
	
	/// END ///

	public String getIPv4Address() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Wi-Fi Info", ex.toString());
		}
		return null;
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
	public IBinder onBind(Intent intent)
	{
		return null;
	}
		
}
