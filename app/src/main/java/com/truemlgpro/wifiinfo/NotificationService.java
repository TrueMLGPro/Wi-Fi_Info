package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.wifi.*;
import android.net.*;

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
		// Start service and create notification 
		Notification notificationS = new Notification();
		
		startForeground(130, notificationS);
		
		return START_NOT_STICKY;
	}
	
	private Handler handler = new Handler(Looper.getMainLooper());
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (android.os.Build.VERSION.SDK_INT == 29) {
				updateNotificationAPI29();
			} else {
				updateNotification();
			}
			handler.postDelayed(runnable, 1000);
		}
	};
	
	public void updateNotification() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (WiFiCheck.isConnected()) {
		int NOTIFICATION_ID = 130;

		Intent NotificationIntent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification notification = new Notification();
		
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

		builder.setContentIntent(content_intent)
			.setSmallIcon(R.drawable.notification_icon)
			.setContentTitle("IP: " + ip)
			.setContentText(smallInfo)
			.setWhen(System.currentTimeMillis())
			.setPriority(5)
			.setStyle(new Notification.BigTextStyle().bigText(extendedInfo))
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setAutoCancel(false);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
		} else {
			int NOTIFICATION_ID = 130;
			
			builder.setContentTitle("No Connection")
			.setStyle(new Notification.BigTextStyle().bigText("Connect to Wi-Fi"));
			
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, builder.build());
		}
	}
	
	public void updateNotificationAPI29() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (WiFiCheck.isConnected()) {
			int NOTIFICATION_ID = 130;

			Intent NotificationIntent = new Intent(this, MainActivity.class);
			PendingIntent content_intent = PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Notification notification = new Notification();

			builder = new Notification.Builder(this);

			WifiManager mainWifi;
			mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = mainWifi.getConnectionInfo();
			String ssid = wInfo.getSSID();
			int rssi = wInfo.getRssi();
			int ipAddress = wInfo.getIpAddress();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
			String info = "SSID: " + ssid + " | " + "Signal Strength: " + rssi + "dBm";
			
			builder.setContentIntent(content_intent)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle("IP: " + ip)
				.setContentText(info)
				.setWhen(System.currentTimeMillis())
				.setPriority(5)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false);

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, builder.build());
		} else {
			int NOTIFICATION_ID = 130;

			builder.setContentTitle("No Connection")
			.setContentText("Connect to Wi-Fi");

			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, builder.build());
		}
	}
	
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
		
}
