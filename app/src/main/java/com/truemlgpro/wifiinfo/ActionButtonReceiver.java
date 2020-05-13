package com.truemlgpro.wifiinfo;

import android.app.*;
import android.content.*;
import android.os.*;

public class ActionButtonReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction() != null && intent.getAction().equals("ACTION_STOP")) {
			context.startService(new Intent(context, NotificationService.class).setAction("ACTION_STOP"));
			context.startService(new Intent(context, ConnectionStateService.class).setAction("ACTION_STOP"));
		} else if (intent.getAction() != null && intent.getAction().equals("ACTION_NTFC_SETTINGS")) {
			context.startService(new Intent(context, NotificationService.class).setAction("ACTION_NTFC_SETTINGS"));
			
			Intent StatusBarCloseIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
			context.sendBroadcast(StatusBarCloseIntent);
		}
	}
	
	public ActionButtonReceiver() {
		
	}
	
}
