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

	NetworkInfo WiFiCheck;
	NetworkInfo CellularCheck;

	private String publicIPFetched;
	private boolean siteReachable = false;

	boolean switchIP = true;

	@Override
	public void onClick() {
		super.onClick();
		showIPAddress();
		switchIP = !switchIP;
	}

	@Override
	public void onStartListening() {
		qs_tile = getQsTile();
		qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_qs_tile));
		qs_tile.setState(Tile.STATE_INACTIVE);
		qs_tile.updateTile();
	}

	private void showIPAddress() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		CellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		// Local IP
		if (switchIP) {
			qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_update));
			qs_tile.setLabel("Local IP");
			qs_tile.setState(Tile.STATE_ACTIVE);
			qs_tile.updateTile();
			new Thread(() -> {
				SystemClock.sleep(500);
				if (WiFiCheck.isConnected() || CellularCheck.isConnected()) {
					qs_tile.setLabel(getIPv4Address());
					qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_success));
				} else {
					qs_tile.setLabel("No Connection");
					qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_fail));
				}
				qs_tile.setState(Tile.STATE_INACTIVE);
				qs_tile.updateTile();
			}).start();
		} else {
			// Public IP
			qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_update));
			qs_tile.setLabel("Public IP");
			qs_tile.setState(Tile.STATE_INACTIVE);
			qs_tile.updateTile();
			new Thread(() -> {
				SystemClock.sleep(500);
				qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi));
				qs_tile.setState(Tile.STATE_ACTIVE);
				qs_tile.updateTile();
				if (WiFiCheck.isConnected() || CellularCheck.isConnected()) {
					PublicIPRunnable runnableIP = new PublicIPRunnable();
					new Thread(runnableIP).start();
				} else {
					qs_tile.setLabel("No Connection");
					qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_fail));
				}
				qs_tile.setState(Tile.STATE_INACTIVE);
				qs_tile.updateTile();
			}).start();
		}
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

	private String getPublicIPAddress() {
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

	public boolean isReachable(String url) {
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

	@SuppressWarnings("deprecation")
	class PublicIPRunnable implements Runnable {
		@Override
		public void run() {
			new AsyncTask<String, Void, Void>() {
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
						qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_success));
						qs_tile.updateTile();
					} else {
						qs_tile.setLabel("No Connection");
						qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_fail));
						qs_tile.updateTile();
					}
				}
			}.execute();
		}
	}
}
