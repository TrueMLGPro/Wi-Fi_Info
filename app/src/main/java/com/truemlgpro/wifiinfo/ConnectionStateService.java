package com.truemlgpro.wifiinfo;

import android.content.*;
import android.widget.*;
import android.net.*;
import android.app.*;
import android.os.*;

public class ConnectionStateService extends Service 
{

	private BroadcastReceiver ConnectionStateReceiver;
	
	// Build 128

public class ConnectionStateReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		ConnectivityManager CM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo WiFi_NI = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		boolean isConnected = WiFi_NI != null && WiFi_NI.isConnectedOrConnecting();
		
		if (isConnected) {
			Intent ServiceIntent = new Intent(context, NotificationService.class);
			context.startService(ServiceIntent);
		} else {
			Intent ServiceIntent = new Intent(context, NotificationService.class);
			context.stopService(ServiceIntent);
		}
	}
		public ConnectionStateReceiver() {
			
		}
}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		ConnectionStateReceiver = new ConnectionStateReceiver();
		registerReceiver(ConnectionStateReceiver, filter);
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		unregisterReceiver(ConnectionStateReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent.getAction() != null && intent.getAction().equals("ACTION_STOP")) {
			stopSelf();
		}
		
		return START_NOT_STICKY;
	}

}
