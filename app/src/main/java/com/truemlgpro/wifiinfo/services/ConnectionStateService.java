package com.truemlgpro.wifiinfo.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.truemlgpro.wifiinfo.receivers.ActionButtonReceiver;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.receivers.ScreenStateReceiver;
import com.truemlgpro.wifiinfo.ui.MainActivity;
import com.truemlgpro.wifiinfo.ui.SettingsActivity;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;

public class ConnectionStateService extends Service {
	private BroadcastReceiver ConnectionStateReceiver;
	private Notification.Builder builder;
	private String state_online = "";
	private String state_offline = "";

	private ScreenStateReceiver screenStateReceiver;
	private IntentFilter intentFilter;
	private boolean isScreenStateReceiverRegistered;
	private boolean isHandlerPosted;

	public static boolean isConnectionStateServiceRunning;
	public static boolean isNotificationServiceRunning;

	@Override
	public void onCreate() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		ConnectionStateReceiver = new ConnectionStateReceiver();
		registerReceiver(ConnectionStateReceiver, filter);

		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenStateReceiver = new ScreenStateReceiver();

		isConnectionStateServiceRunning = true;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(ConnectionStateReceiver);
		if (isScreenStateReceiverRegistered) {
			unregisterReceiver(screenStateReceiver);
			isScreenStateReceiverRegistered = false;
		}

		if (isHandlerPosted) {
			handler.removeCallbacks(runnable);
			isHandlerPosted = false;
		}

		isConnectionStateServiceRunning = false;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		state_online = getString(R.string.connection_status_online);
		state_offline = getString(R.string.connection_status_offline);
		ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo WiFi_NI = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isConnected = WiFi_NI != null && WiFi_NI.isConnected();

		if (isConnected) {
			if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
				/// ANDROID 8 - ANDROID 9 ///
				int NOTIFICATION_ID = 1004;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);

				Notification notification = builder.setSmallIcon(R.drawable.wifi_success_24px)
					.setContentTitle(state_online)
					.setWhen(System.currentTimeMillis())
					.setChannelId(channelID)
					.setColor(ContextCompat.getColor(this, R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (Build.VERSION.SDK_INT >= 29) {
				/// ANDROID 10 & higher ///
				int NOTIFICATION_ID = 1005;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);

				Notification notification = builder.setSmallIcon(R.drawable.wifi_success_24px)
					.setContentTitle(state_online)
					.setWhen(System.currentTimeMillis())
					.setChannelId(channelID)
					.setColor(ContextCompat.getColor(this, R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (Build.VERSION.SDK_INT < 26) {
				/// ANDROID 5 - ANDROID 7 ///
				int NOTIFICATION_ID = 1006;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				builder = new Notification.Builder(this);

				Notification notification = builder.setSmallIcon(R.drawable.wifi_success_24px)
					.setContentTitle(state_online)
					.setWhen(System.currentTimeMillis())
					.setPriority(Notification.PRIORITY_MIN)
					.setColor(getResources().getColor(R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			}
		} else {
			if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
				/// ANDROID 8 - ANDROID 9 ///
				int NOTIFICATION_ID = 1004;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);

				Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
				intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
				PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10041, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

				Notification notification = builder.setSmallIcon(R.drawable.wifi_fail_24px)
					.setContentTitle(state_offline)
					.setWhen(System.currentTimeMillis())
					.addAction(R.drawable.stop_24px, getString(R.string.stop_service), pIntentActionStop)
					.setChannelId(channelID)
					.setColor(ContextCompat.getColor(this, R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (Build.VERSION.SDK_INT >= 29) {
				/// ANDROID 10 & higher ///
				int NOTIFICATION_ID = 1005;

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
				builder = new Notification.Builder(this, channelID);

				Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
				intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
				PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10051, intentActionStop, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

				Notification notification = builder.setSmallIcon(R.drawable.wifi_fail_24px)
					.setContentTitle(state_offline)
					.setWhen(System.currentTimeMillis())
					.addAction(R.drawable.stop_24px, getString(R.string.stop_service), pIntentActionStop)
					.setChannelId(channelID)
					.setColor(ContextCompat.getColor(this, R.color.ntfcColor))
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setAutoCancel(false)
					.build();
				startForeground(NOTIFICATION_ID, notification);
			} else if (Build.VERSION.SDK_INT < 26) {
				/// ANDROID 5 - ANDROID 7 ///
				int NOTIFICATION_ID = 1006;

				builder = new Notification.Builder(this);

				Intent intentActionStop = new Intent(this, ActionButtonReceiver.class);
				intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
				PendingIntent pIntentActionStop = PendingIntent.getBroadcast(this, 10061, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

				Notification notification = builder.setSmallIcon(R.drawable.wifi_fail_24px)
					.setContentTitle(state_offline)
					.setWhen(System.currentTimeMillis())
					.addAction(R.drawable.stop_24px, getString(R.string.stop_service), pIntentActionStop)
					.setPriority(Notification.PRIORITY_MIN)
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

	public class ConnectionStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, final Intent intent)
		{
			ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo WiFi_NI = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			boolean isConnected = WiFi_NI != null && WiFi_NI.isConnected();

			Intent ServiceIntent = new Intent(ConnectionStateService.this, NotificationService.class);
			if (isConnected) {
				if (Build.VERSION.SDK_INT < 26) {
					startService(ServiceIntent);
				} else {
					startForegroundService(ServiceIntent);
				}

				if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
					showOnlineNotificationAPI26_28(context);
				} else if (Build.VERSION.SDK_INT >= 29) {
					showOnlineNotificationAPI29(context);
				} else if (Build.VERSION.SDK_INT < 26) {
					showOnlineNotificationAPI21(context);
				}

				boolean keyStartStopScrnStateNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_STRT_STOP_SRVC_CHECK, MainActivity.startStopSrvcScrnState);

				if (keyStartStopScrnStateNtfc) {
					registerReceiver(screenStateReceiver, intentFilter);
					isScreenStateReceiverRegistered = true;
					handler.post(runnable);
					isHandlerPosted = true;
				} else {
					if (isScreenStateReceiverRegistered) {
						unregisterReceiver(screenStateReceiver);
						isScreenStateReceiverRegistered = false;
					}

					if (isHandlerPosted) {
						handler.removeCallbacks(runnable);
						isHandlerPosted = false;
					}
				}
				isNotificationServiceRunning = true;
			} else {
				if (isNotificationServiceRunning) {
					stopService(ServiceIntent);
					isNotificationServiceRunning = false;
				}

				if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
					showOfflineNotificationAPI26_28(context);
				} else if (Build.VERSION.SDK_INT >= 29) {
					showOfflineNotificationAPI29(context);
				} else if (Build.VERSION.SDK_INT < 26) {
					showOfflineNotificationAPI21(context);
				}

				if (isScreenStateReceiverRegistered) {
					unregisterReceiver(screenStateReceiver);
					isScreenStateReceiverRegistered = false;
				}

				if (isHandlerPosted) {
					handler.removeCallbacks(runnable);
					isHandlerPosted = false;
				}
			}
		}

		/// ONLINE NOTIFICATIONS ///

		/// ANDROID 8 - ANDROID 9 ///

		@RequiresApi(api = Build.VERSION_CODES.O)
		public void showOnlineNotificationAPI26_28(Context context) {
			int NOTIFICATION_ID = 1004;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);

			Notification notification = builder.setSmallIcon(R.drawable.wifi_success_24px)
				.setContentTitle(state_online)
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

		/// ANDROID 10 & higher ///

		@RequiresApi(api = Build.VERSION_CODES.O)
		public void showOnlineNotificationAPI29(Context context) {
			int NOTIFICATION_ID = 1005;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);

			Notification notification = builder.setSmallIcon(R.drawable.wifi_success_24px)
				.setContentTitle(state_online)
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

		/// ANDROID 5 - ANDROID 7 ///

		public void showOnlineNotificationAPI21(Context context) {
			int NOTIFICATION_ID = 1006;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new Notification.Builder(context);

			Notification notification = builder.setSmallIcon(R.drawable.wifi_success_24px)
				.setContentTitle(state_online)
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

		/// ANDROID 8 - ANDROID 9 ///

		@RequiresApi(api = Build.VERSION_CODES.O)
		public void showOfflineNotificationAPI26_28(Context context) {
			int NOTIFICATION_ID = 1004;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);

			Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
			intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
			PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, 10041, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

			Notification notification = builder.setSmallIcon(R.drawable.wifi_fail_24px)
				.setContentTitle(state_offline)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.stop_24px, getString(R.string.stop_service), pIntentActionStop)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}

		/// ANDROID 10 & higher ///

		@RequiresApi(api = Build.VERSION_CODES.O)
		public void showOfflineNotificationAPI29(Context context) {
			int NOTIFICATION_ID = 1005;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
			builder = new Notification.Builder(context, channelID);

			Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
			intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
			PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, 10051, intentActionStop, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

			Notification notification = builder.setSmallIcon(R.drawable.wifi_fail_24px)
				.setContentTitle(state_offline)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.stop_24px, getString(R.string.stop_service), pIntentActionStop)
				.setChannelId(channelID)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
		}

		/// ANDROID 5 - ANDROID 7 ///

		public void showOfflineNotificationAPI21(Context context) {
			int NOTIFICATION_ID = 1006;

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new Notification.Builder(context);

			Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
			intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
			PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, 10061, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

			Notification notification = builder.setSmallIcon(R.drawable.wifi_fail_24px)
				.setContentTitle(state_offline)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.stop_24px, getString(R.string.stop_service), pIntentActionStop)
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

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Intent ServiceIntent = new Intent(ConnectionStateService.this, NotificationService.class);
			if (ScreenStateReceiver.screenState) {
				if (!isNotificationServiceRunning) {
					if (android.os.Build.VERSION.SDK_INT < 26) {
						startService(ServiceIntent);
					} else {
						startForegroundService(ServiceIntent);
					}
					isNotificationServiceRunning = true;
				}
			} else {
				if (isNotificationServiceRunning) {
					stopService(ServiceIntent);
					isNotificationServiceRunning = false;
				}
			}
			handler.postDelayed(runnable, 1000);
		}
	};

	@RequiresApi(Build.VERSION_CODES.O)
	private String createNotificationChannel(NotificationManager notificationManager) {
		String channelID = "connection_state_service";
		CharSequence channelName = "Connection State Service";
		NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_MIN);
		channel.setDescription("Wi-Fi Info Connection Listener Service Notification");
		channel.setShowBadge(false);
		notificationManager.createNotificationChannel(channel);
		return channelID;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
