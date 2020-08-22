package com.truemlgpro.wifiinfo;

import android.content.*;

public class BootReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Boolean keyBoot = new SharedPreferencesManager(context).retrieveBoolean(SettingsActivity.KEY_PREF_BOOT_SWITCH, MainActivity.startOnBoot);
		
		if (keyBoot == true) {
			Intent ServiceIntent = new Intent(context, ConnectionStateService.class);
			if (android.os.Build.VERSION.SDK_INT < 26) {
				context.startService(ServiceIntent);
			} else {
				context.startForegroundService(ServiceIntent);
			}
		}
	}
	
}
