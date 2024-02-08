package com.truemlgpro.wifiinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		boolean keyBoot = new SharedPreferencesManager(context).retrieveBoolean(PreferenceKeys.KEY_PREF_START_ON_BOOT, PreferenceDefaults.START_ON_BOOT);

		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			if (keyBoot) {
				Intent serviceIntent = new Intent(context, ConnectionStateService.class);
				if (android.os.Build.VERSION.SDK_INT < 26) {
					context.startService(serviceIntent);
				} else {
					context.startForegroundService(serviceIntent);
				}
			}
		}
	}
}
