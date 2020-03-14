package com.truemlgpro.wifiinfo;

import android.content.*;

public class BootReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent ServiceIntent = new Intent(context, ConnectionStateService.class);
		context.startForegroundService(ServiceIntent);
	}
	
}
