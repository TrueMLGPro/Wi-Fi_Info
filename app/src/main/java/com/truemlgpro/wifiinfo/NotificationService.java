package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.wifi.*;
import android.net.*;
import android.support.annotation.*;
import android.support.v4.content.*;
import android.support.v4.app.*;

public class NotificationService extends Service
{
	
	private NotificationManager notificationManager;
	private Notification.Builder builder;

	@Override
	public void onCreate()
	{
		handler.postDelayed(runnable, 1000);
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		if (handler != null) {
			handler.removeCallbacks(runnable);
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent.getAction() != null && intent.getAction().equals("ACTION_STOP")) {
			stopSelf();
		}
		
		if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
			/// Android 8 - Android 9 ///
			
			int NOTIFICATION_ID = 1301;

			Intent NotificationIntent = new Intent(this, MainActivity.class);
			PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Intent intentAction = new Intent(this, ActionButtonReceiver.class);
			intentAction.setAction("ACTION_STOP");
			PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, intentAction, PendingIntent.FLAG_ONE_SHOT);

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(this, channelID);

			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			String bssd = wInfo.getBSSID().toUpperCase();
			int rssi = wInfo.getRssi();
			int freq = wInfo.getFrequency();
			int networkSpeed = wInfo.getLinkSpeed();
			int ipAddress = wInfo.getIpAddress();
			int network_id = wInfo.getNetworkId();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			String smallInfo = "SSID: " + ssid + " | " + "Signal Strength: " + rssi + "dBm";
			String extendedInfo = "SSID: " + ssid + "\n" + "BSSID: " + bssd + "\n" + "Signal Strength: " + rssi + "dBm" + "\n" + 
					"Frequency: " + freq + "MHz" + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "ID: " + network_id;

			Notification notification = builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("Local IP: " + ip)
				.setContentText(smallInfo)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentAction)
				.setChannelId(channelID)
				.setColorized(true)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			startForeground(NOTIFICATION_ID, notification);
		} else if (android.os.Build.VERSION.SDK_INT == 29) {
			/// Android 10 ///
			
			int NOTIFICATION_ID = 1302;

			Intent NotificationIntent = new Intent(this, MainActivity.class);
			PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Intent intentAction = new Intent(this, ActionButtonReceiver.class);
			intentAction.setAction("ACTION_STOP");
			PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, intentAction, PendingIntent.FLAG_ONE_SHOT);

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(this, channelID);

			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			int rssi = wInfo.getRssi();
			int ipAddress = wInfo.getIpAddress();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			String info = "SSID: " + ssid + " | " + "Signal Strength: " + rssi + "dBm";

			Notification notification = builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("Local IP: " + ip)
				.setContentText(info)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentAction)
				.setChannelId(channelID)
				.setColorized(true)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();

				startForeground(NOTIFICATION_ID, notification);
		} else if (android.os.Build.VERSION.SDK_INT < 26) {
			/// Android 5 - Android 7 ///
			
			int NOTIFICATION_ID = 1303;

			Intent NotificationIntent = new Intent(this, MainActivity.class);
			PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Intent intentAction = new Intent(this, ActionButtonReceiver.class);
			intentAction.setAction("ACTION_STOP");
			PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, intentAction, PendingIntent.FLAG_ONE_SHOT);

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new Notification.Builder(this);

			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			String bssd = wInfo.getBSSID().toUpperCase();
			int rssi = wInfo.getRssi();
			int freq = wInfo.getFrequency();
			int networkSpeed = wInfo.getLinkSpeed();
			int ipAddress = wInfo.getIpAddress();
			int network_id = wInfo.getNetworkId();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			String smallInfo = "SSID: " + ssid + " | " + "Signal Strength: " + rssi + "dBm";
			String extendedInfo = "SSID: " + ssid + "\n" + "BSSID: " + bssd + "\n" + "Signal Strength: " + rssi + "dBm" + "\n" + 
				"Frequency: " + freq + "MHz" + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "ID: " + network_id;

			Notification notification = builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("Local IP: " + ip)
				.setContentText(smallInfo)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentAction)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			startForeground(NOTIFICATION_ID, notification);
		}
		
		return START_STICKY;
	}
	
	private Handler handler = new Handler(Looper.getMainLooper());
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (android.os.Build.VERSION.SDK_INT == 29) {
				updateNotificationAPI29();
			}
			
			if (android.os.Build.VERSION.SDK_INT < 26) {
				updateNotificationAPI21_25();
			}
			
			if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
				updateNotification();
			}
			handler.postDelayed(runnable, 1000);
		}
	};
	
	/// ANDROID 8 - ANDROID 9 ///
	
	public void updateNotification() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (WiFiCheck.isConnected()) {
			int NOTIFICATION_ID = 1301;

			Intent NotificationIntent = new Intent(this, MainActivity.class);
			PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Intent intentAction = new Intent(this, ActionButtonReceiver.class);
			intentAction.setAction("ACTION_STOP");
			PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, intentAction, PendingIntent.FLAG_ONE_SHOT);

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(this, channelID);

			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			String bssd = wInfo.getBSSID().toUpperCase();
			int rssi = wInfo.getRssi();
			int freq = wInfo.getFrequency();
			int networkSpeed = wInfo.getLinkSpeed();
			int ipAddress = wInfo.getIpAddress();
			int network_id = wInfo.getNetworkId();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			String smallInfo = "SSID: " + ssid + " | " + "Signal Strength: " + rssi + "dBm";
			String extendedInfo = "SSID: " + ssid + "\n" + "BSSID: " + bssd + "\n" + "Signal Strength: " + rssi + "dBm" + "\n" + 
				"Frequency: " + freq + "MHz" + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "ID: " + network_id;

			Notification notification = builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("Local IP: " + ip)
				.setContentText(smallInfo)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentAction)
				.setChannelId(channelID)
				.setColorized(true)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);
		} else {
			int NOTIFICATION_ID = 1301;

			Notification notification = builder.setContentTitle("No Connection")
				.setStyle(new Notification.BigTextStyle().bigText("Connect to Wi-Fi"))
				.build();

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}
	
	/// ANDROID 10 ///
	
	public void updateNotificationAPI29() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (WiFiCheck.isConnected()) {
			int NOTIFICATION_ID = 1302;

			Intent NotificationIntent = new Intent(this, MainActivity.class);
			PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			Intent intentAction = new Intent(this, ActionButtonReceiver.class);
			intentAction.setAction("ACTION_STOP");
			PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, intentAction, PendingIntent.FLAG_ONE_SHOT);
			
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(this, channelID);

			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			int rssi = wInfo.getRssi();
			int ipAddress = wInfo.getIpAddress();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			String info = "SSID: " + ssid + " | " + "Signal Strength: " + rssi + "dBm";
			
			Notification notification = builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("Local IP: " + ip)
				.setContentText(info)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentAction)
				.setChannelId(channelID)
				.setColorized(true)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);
		} else {
			int NOTIFICATION_ID = 1302;

			Notification notification = builder.setContentTitle("No Connection")
			.setContentText("Connect to Wi-Fi")
			.build();

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}
	
	/// ANDROID 5 - ANDROID 7 ///
	
	public void updateNotificationAPI21_25() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (WiFiCheck.isConnected()) {
			int NOTIFICATION_ID = 1303;

			Intent NotificationIntent = new Intent(this, MainActivity.class);
			PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			Intent intentAction = new Intent(this, ActionButtonReceiver.class);
			intentAction.setAction("ACTION_STOP");
			PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, intentAction, PendingIntent.FLAG_ONE_SHOT);
			
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			builder = new Notification.Builder(this);

			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			String bssd = wInfo.getBSSID().toUpperCase();
			int rssi = wInfo.getRssi();
			int freq = wInfo.getFrequency();
			int networkSpeed = wInfo.getLinkSpeed();
			int ipAddress = wInfo.getIpAddress();
			int network_id = wInfo.getNetworkId();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			String smallInfo = "SSID: " + ssid + " | " + "Signal Strength: " + rssi + "dBm";
			String extendedInfo = "SSID: " + ssid + "\n" + "BSSID: " + bssd + "\n" + "Signal Strength: " + rssi + "dBm" + "\n" + 
				"Frequency: " + freq + "MHz" + "\n" + "Network Speed: " + networkSpeed + "MB/s" + "\n" + "ID: " + network_id;

			Notification notification = builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("Local IP: " + ip)
				.setContentText(smallInfo)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentAction)
				.setPriority(Notification.PRIORITY_LOW)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);
		} else {
			int NOTIFICATION_ID = 1303;

			Notification notification = builder.setContentTitle("No Connection")
				.setStyle(new Notification.BigTextStyle().bigText("Connect to Wi-Fi"))
				.build();

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}
	
	@RequiresApi(Build.VERSION_CODES.O)
	private String createNotificationChannel(NotificationManager notificationManager) {
		String channelID = "wifi_info";
		CharSequence channelName = "Wi-Fi Info Notification Service";
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
