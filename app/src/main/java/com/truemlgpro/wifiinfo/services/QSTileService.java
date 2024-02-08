package com.truemlgpro.wifiinfo.services;

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

import com.truemlgpro.wifiinfo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QSTileService extends TileService {
	private static Tile qs_tile;

	private NetworkInfo wifiCheck;
	private NetworkInfo cellularCheck;

	private boolean switchIP = true;

	private static Icon wifiSuccessIcon;
	private static Icon wifiFailIcon;
	private Icon wifiDefaultIcon;
	private Icon updateIcon;

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
		qs_tile = null;
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
		SystemClock.sleep(250);
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
		SystemClock.sleep(250);
		qs_tile.setState(Tile.STATE_ACTIVE);
		qs_tile.updateTile();
		if (wifiCheck.isConnected() || cellularCheck.isConnected()) {
			new PublicIPAsyncTask().execute();
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
		} catch (SocketException e) {
			Log.e("getIPv4Address()", e.toString());
		}
		return null;
	}

	private static class PublicIPAsyncTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			HttpURLConnection urlConnection = null;
			String ipAddress = null;
			try {
				URL url = new URL("https://public-ip-api.vercel.app/api/ip/");
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");

				int responseCode = urlConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					StringBuilder responseBuilder = new StringBuilder();
					InputStream inputStream = urlConnection.getInputStream();
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
						String line;
						while ((line = reader.readLine()) != null) {
							responseBuilder.append(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					ipAddress = responseBuilder.toString();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return ipAddress;
		}

		@Override
		protected void onPostExecute(String ipAddress) {
			if (ipAddress == null) {
				ipAddress = "No Connection";
				qs_tile.setIcon(wifiFailIcon);
			} else {
				qs_tile.setIcon(wifiSuccessIcon);
			}
			qs_tile.setLabel(ipAddress);
			qs_tile.updateTile();
		}
	}
}
