package com.truemlgpro.wifiinfo;

import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;

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
			new Thread(new Runnable() {
				@Override
				public void run() {
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
				}
			}).start();
		} else {
			// Public IP
			qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_update));
			qs_tile.setLabel("Public IP");
			qs_tile.setState(Tile.STATE_INACTIVE);
			qs_tile.updateTile();
			new Thread(new Runnable() {
				@Override
				public void run() {
					SystemClock.sleep(500);
					qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi));
					qs_tile.setState(Tile.STATE_ACTIVE);
					qs_tile.updateTile();
					if (WiFiCheck.isConnected() || CellularCheck.isConnected()) {
						QSTileService.PublicIPRunnable runnableIP = new QSTileService.PublicIPRunnable();
						new Thread(runnableIP).start();
					} else {
						qs_tile.setLabel("No Connection");
						qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_fail));
					}
					qs_tile.setState(Tile.STATE_INACTIVE);
					qs_tile.updateTile();
				}
			}).start();
		}
	}

	private String getIPv4Address() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Wi-Fi Info", ex.toString());
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

	public boolean isReachable(String url) throws IOException {
		boolean reachable = false;
		int code;

		try {
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(3000);
			connection.connect();
			code = connection.getResponseCode();
			connection.disconnect();
			if (code == 200) {
				reachable = true;
			} else {
				reachable = false;
			}
		} catch (Exception e) {
			reachable = false;
		}
		return reachable;
	}

	class PublicIPRunnable implements Runnable {
		@Override
		public void run() {
			new AsyncTask<String, Void, Void>() {
				@Override
				protected Void doInBackground(String[] voids) {
					publicIPFetched = getPublicIPAddress();
					String url_ip = "https://api.ipify.org";
					try {
						if (isReachable(url_ip)) {
							siteReachable = true;
						} else {
							siteReachable = false;
						}
					} catch (IOException e) {
						e.printStackTrace();
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
					}

					if (!siteReachable) {
						qs_tile.setLabel("No Connection");
						qs_tile.setIcon(Icon.createWithResource(QSTileService.this, R.drawable.ic_wifi_fail));
						qs_tile.updateTile();
					}
				}
			}.execute();
		}
	}
}