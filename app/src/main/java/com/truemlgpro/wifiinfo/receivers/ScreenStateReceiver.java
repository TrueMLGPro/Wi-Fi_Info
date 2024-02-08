package com.truemlgpro.wifiinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.truemlgpro.wifiinfo.services.NotificationService;

public class ScreenStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Intent serviceIntent = new Intent(context, NotificationService.class);

		if (action.equals(Intent.ACTION_SCREEN_ON)) {
			if (!NotificationService.isNotificationServiceRunning) {
				if (android.os.Build.VERSION.SDK_INT < 26) {
					context.startService(serviceIntent);
				} else {
					context.startForegroundService(serviceIntent);
				}
			}
		} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
			if (NotificationService.isNotificationServiceRunning) {
				context.stopService(serviceIntent);
			}
		}
	}
}
