package com.truemlgpro.wifiinfo.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.receivers.ActionButtonReceiver;
import com.truemlgpro.wifiinfo.receivers.ScreenStateReceiver;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;

public class ConnectionStateService extends Service {
	private Notification notification21_25;
	private Notification notification26_28;
	private Notification notification29;
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
	private IntentFilter screenIntentFilter;
	private boolean isScreenStateReceiverRegistered;

	private Context localizedContext;

	public static boolean isConnectionStateServiceRunning;

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private Handler mainHandler;
	private final Runnable doCheck = this::handleConnectivityChange;

	@Override
	public void onCreate() {
		super.onCreate();

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mainHandler = new Handler(Looper.getMainLooper());

		localizedContext = LocaleManager.getLocalizedContext(getApplicationContext());
		state_online = getLocalizedString(R.string.connection_status_online);
		state_offline = getLocalizedString(R.string.connection_status_offline);

		// Screen state receiver
		screenStateReceiver = new ScreenStateReceiver();
		screenIntentFilter = new IntentFilter();
		screenIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		isScreenStateReceiverRegistered = false;

		registerNetworkCallback();

		isConnectionStateServiceRunning = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterNetworkCallback();

		if (isScreenStateReceiverRegistered) {
			unregisterReceiver(screenStateReceiver);
			isScreenStateReceiverRegistered = false;
		}

		stopForeground(true);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (nm != null) {
			if (Build.VERSION.SDK_INT < 26) nm.cancel(NOTIFICATION_ID_API21_25);
			else if (Build.VERSION.SDK_INT < 29) nm.cancel(NOTIFICATION_ID_API26_28);
			else nm.cancel(NOTIFICATION_ID_API29);
		}

		isConnectionStateServiceRunning = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean wifi = isWifiOnline();

		if (Build.VERSION.SDK_INT < 26) {
			if (wifi) showOnlineNotificationAPI21_25(this); else showOfflineNotificationAPI21_25(this);
			startForeground(NOTIFICATION_ID_API21_25, notification21_25);
		} else if (Build.VERSION.SDK_INT < 29) {
			if (wifi) showOnlineNotificationAPI26_28(this); else showOfflineNotificationAPI26_28(this);
			startForeground(NOTIFICATION_ID_API26_28, notification26_28);
		} else {
			if (wifi) showOnlineNotificationAPI29(this); else showOfflineNotificationAPI29(this);
			// SPECIAL_USE for Android 14+
			startForeground(
					NOTIFICATION_ID_API29,
					notification29,
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
							? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
							: 0
			);
		}

		applyNotificationServicePolicy(wifi);
		return START_STICKY;
	}

	private void registerNetworkCallback() {
		if (connectivityManager == null) return;

		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override public void onAvailable(Network network) { scheduleCheck(); }
			@Override public void onLost(Network network) { scheduleCheck(); }
			@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
		};

		NetworkRequest request = new NetworkRequest.Builder()
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
			try { connectivityManager.unregisterNetworkCallback(networkCallback); } catch (Exception ignored) {}
			networkCallback = null;
		}
		if (mainHandler != null) mainHandler.removeCallbacks(doCheck);
	}

	private void scheduleCheck() {
		if (mainHandler == null) return;
		mainHandler.removeCallbacks(doCheck);
		mainHandler.postDelayed(doCheck, 100);
	}

	private void handleConnectivityChange() {
		boolean wifi = isWifiOnline();

		if (Build.VERSION.SDK_INT < 26) {
			if (wifi) showOnlineNotificationAPI21_25(this); else showOfflineNotificationAPI21_25(this);
			notifyNow(NOTIFICATION_ID_API21_25, notification21_25);
		} else if (Build.VERSION.SDK_INT < 29) {
			if (wifi) showOnlineNotificationAPI26_28(this); else showOfflineNotificationAPI26_28(this);
			notifyNow(NOTIFICATION_ID_API26_28, notification26_28);
		} else {
			if (wifi) showOnlineNotificationAPI29(this); else showOfflineNotificationAPI29(this);
			notifyNow(NOTIFICATION_ID_API29, notification29);
		}

		applyNotificationServicePolicy(wifi);
	}

	private void notifyNow(int id, Notification n) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (nm != null) nm.notify(id, n);
	}

	private boolean isWifiOnline() {
		return NetworkUtils.isOnline(this, NetworkUtils.NetworkType.WIFI, false);
	}

	private void applyNotificationServicePolicy(boolean wifiConnected) {
		Intent serviceIntent = new Intent(ConnectionStateService.this, NotificationService.class);

		if (wifiConnected) {
			NotificationService.shouldPostAnUpdate = true;
			if (Build.VERSION.SDK_INT < 26) startService(serviceIntent);
			else startForegroundService(serviceIntent);

			boolean keyStartStopScrnStateNtfc =
					new SharedPreferencesManager(getApplicationContext())
							.retrieveBoolean(PreferenceKeys.KEY_PREF_SERVICE_SCREEN_STATE,
									PreferenceDefaults.SERVICE_SCREEN_STATE);

			if (keyStartStopScrnStateNtfc && !isScreenStateReceiverRegistered) {
				registerReceiver(screenStateReceiver, screenIntentFilter);
				isScreenStateReceiverRegistered = true;
			} else if (!keyStartStopScrnStateNtfc && isScreenStateReceiverRegistered) {
				try { unregisterReceiver(screenStateReceiver); } catch (Exception ignored) {}
				isScreenStateReceiverRegistered = false;
			}
		} else {
			NotificationService.shouldPostAnUpdate = false;
			stopService(serviceIntent);
			if (isScreenStateReceiverRegistered) {
				try { unregisterReceiver(screenStateReceiver); } catch (Exception ignored) {}
				isScreenStateReceiverRegistered = false;
			}
		}
	}

	@NonNull
	private String getLocalizedString(@StringRes int stringRes) {
		return localizedContext.getResources().getString(stringRes);
	}

	/// ONLINE NOTIFICATIONS ///
	/// ANDROID 5 - ANDROID 7 ///
	public void showOnlineNotificationAPI21_25(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API21_25, notification21_25);
	}

	/// ANDROID 8 - ANDROID 9 ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOnlineNotificationAPI26_28(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String channelID = CHANNEL_ID;
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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API26_28, notification26_28);
	}

	/// ANDROID 10 & higher ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOnlineNotificationAPI29(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String channelID = CHANNEL_ID;
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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API29, notification29);
	}

	/// OFFLINE NOTIFICATIONS ///
	/// ANDROID 5 - ANDROID 7 ///
	public void showOfflineNotificationAPI21_25(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder = new Notification.Builder(context);

		Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
		intentActionStop.setAction(ActionButtonReceiver.ACTION_STOP_CONN_STATE_SERVICE);

		PendingIntent pIntentActionStop =
				PendingIntent.getBroadcast(context, SRVC_STOP_REQUEST_CODE_API21_25, intentActionStop,
						PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));

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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API21_25, notification21_25);
	}

	/// ANDROID 8 - ANDROID 9 ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOfflineNotificationAPI26_28(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String channelID = CHANNEL_ID;
		builder = new Notification.Builder(context, channelID);

		Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
		intentActionStop.setAction(ActionButtonReceiver.ACTION_STOP_CONN_STATE_SERVICE);

		PendingIntent pIntentActionStop =
				PendingIntent.getBroadcast(context, SRVC_STOP_REQUEST_CODE_API26_28, intentActionStop,
						(Build.VERSION.SDK_INT >= 23
								? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
								: PendingIntent.FLAG_UPDATE_CURRENT));

		notification26_28 = builder.setSmallIcon(R.drawable.wifi_fail_24px)
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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API26_28, notification26_28);
	}

	/// ANDROID 10 & higher ///
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void showOfflineNotificationAPI29(Context context) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String channelID = CHANNEL_ID;
		builder = new Notification.Builder(context, channelID);

		Intent intentActionStop = new Intent(context, ActionButtonReceiver.class);
		intentActionStop.setAction(ActionButtonReceiver.ACTION_STOP_CONN_STATE_SERVICE);

		PendingIntent pIntentActionStop =
				PendingIntent.getBroadcast(context, SRVC_STOP_REQUEST_CODE_API29, intentActionStop,
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
		if (notificationManager != null) notificationManager.notify(NOTIFICATION_ID_API29, notification29);
	}

	@Override
	public IBinder onBind(Intent intent) { return null; }
}
