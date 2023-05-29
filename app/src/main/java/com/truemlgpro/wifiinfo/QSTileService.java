package com.truemlgpro.wifiinfo;

import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QSTileService extends TileService {
	Tile qs_tile;

	NetworkInfo wifiCheck;
	NetworkInfo cellularCheck;

	String publicIPFetched;
	boolean siteReachable = false;
	boolean switchIP = true;

	Icon wifiDefaultIcon;
	Icon wifiSuccessIcon;
	Icon wifiFailIcon;
	Icon updateIcon;

	@Override
	public void onStartListening() {
		super.onStartListening();
		wifiDefaultIcon = Icon.createWithResource(getApplicationContext(), R.drawable.wifi_24px);
		wifiSuccessIcon = Icon.createWithResource(getApplicationContext(), R.drawable.wifi_success_24px);
		wifiFailIcon = Icon.createWithResource(getApplicationContext(), R.drawable.wifi_fail_24px);
		updateIcon = Icon.createWithResource(getApplicationContext(), R.drawable.reload_24px);
		qs_tile = getQsTile();
		qs_tile.setIcon(wifiDefaultIcon);
		qs_tile.setState(Tile.STATE_INACTIVE);
		qs_tile.updateTile();
	}

	@Override
	public void onStopListening() {
		super.onStopListening();
		wifiDefaultIcon = null;
		wifiSuccessIcon = null;
		wifiFailIcon = null;
		updateIcon = null;
	}

	@Override
	public void onClick() {
		super.onClick();
		showIPAddress();
		switchIP = !switchIP;
	}

	private void showIPAddress() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		wifiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		cellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (switchIP) {
			// Local IP
			qs_tile.setIcon(updateIcon);
			qs_tile.setLabel("Local IP");
			qs_tile.setState(Tile.STATE_ACTIVE);
			qs_tile.updateTile();
			showLocalIP();
		} else {
			// Public IP
			qs_tile.setIcon(updateIcon);
			qs_tile.setLabel("Public IP");
			qs_tile.setState(Tile.STATE_ACTIVE);
			qs_tile.updateTile();
			showPublicIP();
		}
	}

	private void showLocalIP() {
		SystemClock.sleep(500);
		if (wifiCheck.isConnected() || cellularCheck.isConnected()) {
			qs_tile.setLabel(getIPv4Address());
			qs_tile.setIcon(wifiSuccessIcon);
		} else {
			qs_tile.setLabel("No Connection");
			qs_tile.setIcon(wifiFailIcon);
		}
		qs_tile.setState(Tile.STATE_INACTIVE);
		qs_tile.updateTile();
	}

	private void showPublicIP() {
		SystemClock.sleep(500);
		qs_tile.setState(Tile.STATE_ACTIVE);
		qs_tile.updateTile();
		if (wifiCheck.isConnected() || cellularCheck.isConnected()) {
			PublicIPAsyncTask publicIPAsyncTask = new PublicIPAsyncTask();
			publicIPAsyncTask.execute();
		} else {
			qs_tile.setLabel("No Connection");
			qs_tile.setIcon(wifiFailIcon);
		}
		qs_tile.setState(Tile.STATE_INACTIVE);
		qs_tile.updateTile();
	}

	private String getIPv4Address() {
		try {
			for (Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces(); enumNetworkInterfaces.hasMoreElements();) {
				NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("getIPv4Address()", ex.toString());
		}
		return null;
	}

	private static String getPublicIPAddress() {
		String publicIP = "";
		try {
			Scanner scanner = new Scanner(new URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A");
			publicIP = scanner.next();
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return publicIP;
	}

	private static boolean isReachable(String url) {
		boolean reachable;
		int code;

		try {
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(3000);
			connection.connect();
			code = connection.getResponseCode();
			connection.disconnect();
			reachable = code == 200;
		} catch (Exception e) {
			reachable = false;
		}
		return reachable;
	}

	class PublicIPAsyncTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String[] voids) {
			String url = "https://api.ipify.org";
			siteReachable = isReachable(url);
			if (siteReachable) {
				publicIPFetched = getPublicIPAddress();
			} else {
				publicIPFetched = "N/A";
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if (siteReachable) {
				qs_tile.setLabel(publicIPFetched);
				qs_tile.setIcon(wifiSuccessIcon);
				qs_tile.updateTile();
			} else {
				qs_tile.setLabel("No Connection");
				qs_tile.setIcon(wifiFailIcon);
				qs_tile.updateTile();
			}
		}
	}
}
