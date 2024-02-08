package com.truemlgpro.wifiinfo.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.receivers.ActionButtonReceiver;
import com.truemlgpro.wifiinfo.receivers.ScreenStateReceiver;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;

import java.util.Locale;

public class ConnectionStateService extends Service {
	private Notification notification21_25;
	private Notification notification26_28;
	private Notification notification29;
	private BroadcastReceiver ConnectionStateReceiver;
	private Notification.Builder builder;
	private String state_online = "";
	private String state_offline = "";

	private static final String CHANNEL_ID = "connection_state_service";
	private static final int NOTIFICATION_ID_API21_25 = 1004;
	private static final int NOTIFICATION_ID_API26_28 = 1005;
	private static final int NOTIFICATION_ID_API29 = 1006;
	private static final int SRVC_STOP_REQUEST_CODE_API21_25 = 10041;
	private static final int SRVC_STOP_REQUEST_CODE_API26_28 = 10051;
	private static final int SRVC_STOP_REQUEST_CODE_API29 = 10061;

	private ScreenStateReceiver screenStateReceiver;
	private IntentFilter intentFilter;
	private boolean isScreenStateReceiverRegistered;

	private Context localizedContext;

	public static boolean isConnectionStateServiceRunning;

	@Override
	public void onCreate() {
		super.onCreate();

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		ConnectionStateReceiver = new ConnectionStateReceiver();
		registerReceiver(ConnectionStateReceiver, filter);

		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenStateReceiver = new ScreenStateReceiver();

		isConnectionStateServiceRunning = true;
		initLocaleConfig();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(ConnectionStateReceiver);
		if (isScreenStateReceiverRegistered) {
			unregisterReceiver(screenStateReceiver);
			isScreenStateReceiverRegistered = false;
		}

		isConnectionStateServiceRunning = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		state_online = getLocalizedString(R.string.connection_status_online);
		state_offline = getLocalizedString(R.string.connection_status_offline);
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isConnected = wifiCheck != null && wifiCheck.isConnected();

		if (isConnected) {
			if (Build.VERSION.SDK_INT < 26) {
				/// ANDROID 5 - ANDROID 7 ///
				showOnlineNotificationAPI21_25(this);
				startForeground(NOTIFICATION_ID_API21_25, notification21_25);
			} else if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
				/// ANDROID 8 - ANDROID 9 ///
				showOnlineNotificationAPI26_28(this);
				startForeground(NOTIFICATION_ID_API26_28, notification26_28);
			} else if (Build.VERSION.SDK_INT >= 29) {
				/// ANDROID 10 & higher ///
				showOnlineNotificationAPI29(this);
				startForeground(NOTIFICATION_ID_API29, notification29, Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE : 0);
			}
		} else {
			if (Build.VERSION.SDK_INT < 26) {
				/// ANDROID 5 - ANDROID 7 ///
				showOfflineNotificationAPI21_25(this);
				startForeground(NOTIFICATION_ID_API21_25, notification21_25);
			} else if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
				/// ANDROID 8 - ANDROID 9 ///
				showOfflineNotificationAPI26_28(this);
				startForeground(NOTIFICATION_ID_API26_28, notification26_28);
			} else if (Build.VERSION.SDK_INT >= 29) {
				/// ANDROID 10 & higher ///
				showOfflineNotificationAPI29(this);
				startForeground(NOTIFICATION_ID_API29, notification29, Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE : 0);
			}
		}
		return START_STICKY;
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

	/// ONLINE NOTIFICATIONS ///
	/// ANDROID 5 - ANDROID 7 ///

	public void showOnlineNotificationAPI21_25(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new Notification.Builder(context);

		notification21_25 = builder.setSmallIcon(R.drawable.wifi_success_24px)
				.setContentTitle(state_online)
				.setWhen(System.currentTimeMillis())
				.setPriority(Notification.PRIORITY_MIN)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
		notificationManager.notify(NOTIFICATION_ID_API21_25, notification21_25);
	}

	/// ANDROID 8 - ANDROID 9 ///

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOnlineNotificationAPI26_28(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CHANNEL_ID : "";
		builder = new Notification.Builder(context, channelID);

		notification26_28 = builder.setSmallIcon(R.drawable.wifi_success_24px)
				.setContentTitle(state_online)
				.setWhen(System.currentTimeMillis())
				.setChannelId(channelID)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
		notificationManager.notify(NOTIFICATION_ID_API26_28, notification26_28);
	}

	/// ANDROID 10 & higher ///

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOnlineNotificationAPI29(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CHANNEL_ID : "";
		builder = new Notification.Builder(context, channelID);

		notification29 = builder.setSmallIcon(R.drawable.wifi_success_24px)
				.setContentTitle(state_online)
				.setWhen(System.currentTimeMillis())
				.setChannelId(channelID)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
		notificationManager.notify(NOTIFICATION_ID_API29, notification29);
	}

	/// END ///

	/// OFFLINE NOTIFICATIONS ///
	/// ANDROID 5 - ANDROID 7 ///

	public void showOfflineNotificationAPI21_25(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new Notification.Builder(context);

		Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
		@SuppressLint("UnspecifiedImmutableFlag")
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, SRVC_STOP_REQUEST_CODE_API21_25, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

		notification21_25 = builder.setSmallIcon(R.drawable.wifi_fail_24px)
				.setContentTitle(state_offline)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.stop_24px, getLocalizedString(R.string.stop_service), pIntentActionStop)
				.setPriority(Notification.PRIORITY_MIN)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
		notificationManager.notify(NOTIFICATION_ID_API21_25, notification21_25);
	}

	/// ANDROID 8 - ANDROID 9 ///

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOfflineNotificationAPI26_28(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CHANNEL_ID : "";
		builder = new Notification.Builder(context, channelID);

		Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
		@SuppressLint("UnspecifiedImmutableFlag")
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, SRVC_STOP_REQUEST_CODE_API26_28, intentActionStop, PendingIntent.FLAG_ONE_SHOT);

		notification26_28 = builder.setSmallIcon(R.drawable.wifi_fail_24px)
				.setContentTitle(state_offline)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.stop_24px, getLocalizedString(R.string.stop_service), pIntentActionStop)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
		notificationManager.notify(NOTIFICATION_ID_API26_28, notification26_28);
	}

	/// ANDROID 10 & higher ///

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOfflineNotificationAPI29(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CHANNEL_ID : "";
		builder = new Notification.Builder(context, channelID);

		Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
		intentActionStop.setAction("ACTION_STOP_CONN_STATE_SERVICE");
		PendingIntent pIntentActionStop = PendingIntent.getBroadcast(context, SRVC_STOP_REQUEST_CODE_API29, intentActionStop, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

		notification29 = builder.setSmallIcon(R.drawable.wifi_fail_24px)
				.setContentTitle(state_offline)
				.setWhen(System.currentTimeMillis())
				.addAction(R.drawable.stop_24px, getLocalizedString(R.string.stop_service), pIntentActionStop)
				.setChannelId(channelID)
				.setColor(getResources().getColor(R.color.ntfcColor))
				.setCategory(Notification.CATEGORY_SERVICE)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setAutoCancel(false)
				.build();
		notificationManager.notify(NOTIFICATION_ID_API29, notification29);
	}

	/// END ///

	public class ConnectionStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			boolean isConnected = wifiCheck != null && wifiCheck.isConnected();

			Intent ServiceIntent = new Intent(ConnectionStateService.this, NotificationService.class);
			if (isConnected) {
				if (Build.VERSION.SDK_INT < 26) {
					startService(ServiceIntent);
				} else {
					startForegroundService(ServiceIntent);
				}

				if (Build.VERSION.SDK_INT < 26) {
					showOnlineNotificationAPI21_25(ConnectionStateService.this);
				} else if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
					showOnlineNotificationAPI26_28(ConnectionStateService.this);
				} else if (Build.VERSION.SDK_INT >= 29) {
					showOnlineNotificationAPI29(ConnectionStateService.this);
				}

				boolean keyStartStopScrnStateNtfc = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_PREF_START_STOP_SVC, PreferenceDefaults.START_STOP_SRVC_SCRN_STATE);

				if (keyStartStopScrnStateNtfc) {
					registerReceiver(screenStateReceiver, intentFilter);
					isScreenStateReceiverRegistered = true;
				} else {
					if (isScreenStateReceiverRegistered) {
						unregisterReceiver(screenStateReceiver);
						isScreenStateReceiverRegistered = false;
					}
				}
			} else {
				if (NotificationService.isNotificationServiceRunning) {
					stopService(ServiceIntent);
				}

				if (Build.VERSION.SDK_INT < 26) {
					showOfflineNotificationAPI21_25(ConnectionStateService.this);
				} else if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 29) {
					showOfflineNotificationAPI26_28(ConnectionStateService.this);
				} else if (Build.VERSION.SDK_INT >= 29) {
					showOfflineNotificationAPI29(ConnectionStateService.this);
				}

				if (isScreenStateReceiverRegistered) {
					unregisterReceiver(screenStateReceiver);
					isScreenStateReceiverRegistered = false;
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
