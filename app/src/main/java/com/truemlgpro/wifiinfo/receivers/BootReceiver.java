package com.truemlgpro.wifiinfo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.ui.MainActivity;
import com.truemlgpro.wifiinfo.ui.SettingsActivity;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean keyBoot = new SharedPreferencesManager(context).retrieveBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, MainActivity.startOnBoot);
		if (keyBoot) {
			Intent ServiceIntent = new Intent(context, ConnectionStateService.class);
			if (android.os.Build.VERSION.SDK_INT < 26) {
				context.startService(ServiceIntent);
			} else {
				context.startForegroundService(ServiceIntent);
			}
		}
	}
}
