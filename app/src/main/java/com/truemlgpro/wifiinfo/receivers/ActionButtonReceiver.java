package com.truemlgpro.wifiinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.services.NotificationService;
import com.truemlgpro.wifiinfo.ui.activities.MainActivity;

public class ActionButtonReceiver extends BroadcastReceiver {
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_STOP_CONN_STATE_SERVICE = "ACTION_STOP_CONN_STATE_SERVICE";

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent != null ? intent.getAction() : null;

		if (ACTION_STOP.equals(action)) {
			NotificationService.shouldPostAnUpdate = false;
			context.stopService(new Intent(context, NotificationService.class));
			context.stopService(new Intent(context, ConnectionStateService.class));
			MainActivity.isServiceRunning = false;
		} else if (ACTION_STOP_CONN_STATE_SERVICE.equals(action)) {
			context.stopService(new Intent(context, ConnectionStateService.class));
			MainActivity.isServiceRunning = false;
		}
	}
}
