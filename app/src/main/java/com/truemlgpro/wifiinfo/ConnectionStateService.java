package com.truemlgpro.wifiinfo;

import android.content.*;
import android.widget.*;
import android.net.*;
import android.app.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;

public class ConnectionStateService extends Service 
{

	private BroadcastReceiver ConnectionStateReceiver;
	private Notification.Builder builder;

public class ConnectionStateReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		
		ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo WiFi_NI = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isConnected = WiFi_NI != null && WiFi_NI.isConnected();
		
		if (isConnected) {
			Intent ServiceIntent = new Intent(context, NotificationService.class);
			if (android.os.Build.VERSION.SDK_INT < 26) {
				context.startService(ServiceIntent);
			} else {
				context.startForegroundService(ServiceIntent);
			}
			
			if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
				showOnlineNotificationAPI26_28(context);
			} else if (android.os.Build.VERSION.SDK_INT == 29) {
				showOnlineNotificationAPI29(context);
			} else if (android.os.Build.VERSION.SDK_INT < 26) {
				showOnlineNotificationAPI21(context);
			}
		} else {
			Intent ServiceIntent = new Intent(context, NotificationService.class);
			context.stopService(ServiceIntent);
			
			if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
				showOfflineNotificationAPI26_28(context);
			} else if (android.os.Build.VERSION.SDK_INT == 29) {
				showOfflineNotificationAPI29(context);
			} else if (android.os.Build.VERSION.SDK_INT < 26) {
				showOfflineNotificationAPI21(context);
			}
		}
	}
		public ConnectionStateReceiver() {
			
		}
		
		/// ONLINE NOTIFICATIONS ///
		
		public void showOnlineNotificationAPI26_28(Context context) {
			int NOTIFICATION_ID = 1304;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);

			Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("ConnectionStateService — Online")
				.setWhen(System.currentTimeMillis())
				.setChannelId(channelID)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
		
		public void showOnlineNotificationAPI29(Context context) {
			int NOTIFICATION_ID = 1305;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);

			Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("ConnectionStateService — Online")
				.setWhen(System.currentTimeMillis())
				.setChannelId(channelID)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
		
		public void showOnlineNotificationAPI21(Context context) {
			int NOTIFICATION_ID = 1306;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new Notification.Builder(context);

			Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("ConnectionStateService — Online")
				.setWhen(System.currentTimeMillis())
				.setPriority(Notification.PRIORITY_MIN)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
		
		/// END ///
		
		/// OFFLINE NOTIFICATIONS ///
		
		public void showOfflineNotificationAPI26_28(Context context) {
			int NOTIFICATION_ID = 1304;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);
			
			Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
			intentActionStop.setAction("ACTION_STOP");
			PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, 0, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

			Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("ConnectionStateService — Offline")
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentActionStop)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
		
		public void showOfflineNotificationAPI29(Context context) {
			int NOTIFICATION_ID = 1305;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);
			
			Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
			intentActionStop.setAction("ACTION_STOP");
			PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, 0, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

			Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("ConnectionStateService — Offline")
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentActionStop)
				.setChannelId(channelID)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
		
		public void showOfflineNotificationAPI21(Context context) {
			int NOTIFICATION_ID = 1306;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new Notification.Builder(context);
			
			Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
			intentActionStop.setAction("ACTION_STOP");
			PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, 0, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

			Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
				.setContentTitle("ConnectionStateService — Offline")
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.ic_stop, "Stop Service", pIntentActionStop)
				.setPriority(Notification.PRIORITY_MIN)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
		
		/// END ///
}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		ConnectionStateReceiver = new ConnectionStateReceiver();
		registerReceiver(ConnectionStateReceiver, filter);
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		unregisterReceiver(ConnectionStateReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent.getAction() != null && intent.getAction().equals("ACTION_STOP")) {
			stopSelf();
		}
		
		ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo WiFi_NI = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isConnected = WiFi_NI != null && WiFi_NI.isConnected();
		
		if (isConnected) {
			if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
				int NOTIFICATION_ID = 1304;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);

				Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
					.setContentTitle("ConnectionStateService — Online")
					.setWhen(System.currentTimeMillis())
					.setChannelId(channelID)
					.setColor(getResources().getColor(R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (android.os.Build.VERSION.SDK_INT == 29) {
				int NOTIFICATION_ID = 1305;
				
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);
				
				Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
					.setContentTitle("ConnectionStateService — Online")
					.setWhen(System.currentTimeMillis())
					.setChannelId(channelID)
					.setColor(getResources().getColor(R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (android.os.Build.VERSION.SDK_INT < 26) {
				int NOTIFICATION_ID = 1306;
				
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				builder = new Notification.Builder(this);
				
				Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
					.setContentTitle("ConnectionStateService — Online")
					.setWhen(System.currentTimeMillis())
					.setPriority(NotificationManager.IMPORTANCE_MIN)
					.setColor(getResources().getColor(R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			}
		} else {
			if (android.os.Build.VERSION.SDK_INT >= 26 && android.os.Build.VERSION.SDK_INT < 29) {
				int NOTIFICATION_ID = 1304;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);

				Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
					.setContentTitle("ConnectionStateService — Offline")
					.setWhen(System.currentTimeMillis())
					.setChannelId(channelID)
					.setColor(getResources().getColor(R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (android.os.Build.VERSION.SDK_INT == 29) {
				int NOTIFICATION_ID = 1305;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);

				Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
					.setContentTitle("ConnectionStateService — Offline")
					.setWhen(System.currentTimeMillis())
					.setChannelId(channelID)
					.setColor(getResources().getColor(R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (android.os.Build.VERSION.SDK_INT < 26) {
				int NOTIFICATION_ID = 1306;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				builder = new Notification.Builder(this);

				Notification notification = builder.setSmallIcon(R.drawable.ic_wifi)
					.setContentTitle("ConnectionStateService — Offline")
					.setWhen(System.currentTimeMillis())
					.setPriority(NotificationManager.IMPORTANCE_MIN)
					.setColor(getResources().getColor(R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			}
		}
		return START_STICKY;
	}
	
	@RequiresApi(Build.VERSION_CODES.O)
	private String createNotificationChannel(NotificationManager notificationManager) {
		String channelID = "connection_state_service";
		CharSequence channelName = "Connection State Service";
		NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_MIN);
		channel.setDescription("Wi-Fi Info Watchdog Service Notification");
		channel.setShowBadge(false);
		notificationManager.createNotificationChannel(channel);
		return channelID;
	}

}
