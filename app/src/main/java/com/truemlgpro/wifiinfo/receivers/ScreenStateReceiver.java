package com.truemlgpro.wifiinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.truemlgpro.wifiinfo.services.NotificationService;

public class ScreenStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent != null ? intent.getAction() : null;
		if (action == null) return;

		Intent serviceIntent = new Intent(context, NotificationService.class);

		if (Intent.ACTION_SCREEN_ON.equals(action)) {
			if (!NotificationService.isNotificationServiceRunning) {
				if (Build.VERSION.SDK_INT < 26) {
					context.startService(serviceIntent);
				} else {
					context.startForegroundService(serviceIntent);
				}
			}
		} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			if (NotificationService.isNotificationServiceRunning) {
				context.stopService(serviceIntent);
			}
		}
	}
}
