package com.truemlgpro.wifiinfo;

import android.app.*;
import android.content.*;
import android.os.*;

public class ActionButtonReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		context.startService(new Intent(context, NotificationService.class).setAction("ACTION_STOP"));
		context.startService(new Intent(context, ConnectionStateService.class).setAction("ACTION_STOP"));
	}
	
	public ActionButtonReceiver() {
		
	}
	
}
