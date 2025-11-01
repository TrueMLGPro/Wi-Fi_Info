package com.truemlgpro.wifiinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent != null ? intent.getAction() : null;
		if (action == null) return;

		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			boolean startOnBoot = new SharedPreferencesManager(context.getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_PREF_START_ON_BOOT, PreferenceDefaults.START_ON_BOOT);

			if (startOnBoot) {
				Intent serviceIntent = new Intent(context, ConnectionStateService.class);
				if (Build.VERSION.SDK_INT < 26) {
					context.startService(serviceIntent);
				} else {
					context.startForegroundService(serviceIntent);
				}
			}
		}
	}
}
