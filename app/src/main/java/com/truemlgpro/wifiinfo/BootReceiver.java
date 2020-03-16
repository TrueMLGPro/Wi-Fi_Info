package com.truemlgpro.wifiinfo;

import android.content.*;

public class BootReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent ServiceIntent = new Intent(context, ConnectionStateService.class);
		if (android.os.Build.VERSION.SDK_INT < 26) {
			context.startService(ServiceIntent);
		} else {
			context.startForegroundService(ServiceIntent);
		}
	}
	
}
