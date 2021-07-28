package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenStateReceiver extends BroadcastReceiver
{
	
	public static Boolean screenState = true;
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_SCREEN_ON)) {
			screenState = true;
		}
		
		if (action.equals(Intent.ACTION_SCREEN_OFF)) {
			screenState = false;
		}
	}
	
}
