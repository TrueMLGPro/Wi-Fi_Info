package com.truemlgpro.wifiinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.services.NotificationService;
import com.truemlgpro.wifiinfo.ui.MainActivity;

public class ActionButtonReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction() != null) {
			if (intent.getAction().equals("ACTION_STOP")) {
				NotificationService.shouldPostAnUpdate = false;
				context.stopService(new Intent(context, NotificationService.class));
				context.stopService(new Intent(context, ConnectionStateService.class));
				MainActivity.isServiceRunning = false;
			}

			if (intent.getAction().equals("ACTION_STOP_CONN_STATE_SERVICE")) {
				context.stopService(new Intent(context, ConnectionStateService.class));
				MainActivity.isServiceRunning = false;
			}
		}
	}
}
