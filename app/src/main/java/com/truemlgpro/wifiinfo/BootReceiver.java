package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Boolean keyBoot = new SharedPreferencesManager(context).retrieveBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, MainActivity.startOnBoot);
		
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
