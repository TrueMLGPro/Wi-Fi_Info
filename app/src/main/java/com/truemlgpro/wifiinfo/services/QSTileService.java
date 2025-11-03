package com.truemlgpro.wifiinfo.services;

import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QSTileService extends TileService {
	private Tile qs_tile;
	private boolean switchIP = false;

	private Icon wifiSuccessIcon;
	private Icon wifiFailIcon;
	private Icon wifiDefaultIcon;
	private Icon updateIcon;

	private NetworkUtils.Cancelable publicIpRequest;

	@Override
	public void onStartListening() {
		super.onStartListening();
		wifiDefaultIcon = Icon.createWithResource(getApplicationContext(), R.drawable.wifi_24px);
		wifiSuccessIcon = Icon.createWithResource(getApplicationContext(), R.drawable.wifi_success_24px);
		wifiFailIcon = Icon.createWithResource(getApplicationContext(), R.drawable.wifi_fail_24px);
		updateIcon = Icon.createWithResource(getApplicationContext(), R.drawable.reload_24px);
		qs_tile = getQsTile();

		if (qs_tile != null) {
			qs_tile.setIcon(wifiDefaultIcon);
			qs_tile.setState(Tile.STATE_INACTIVE);
			qs_tile.updateTile();
		}
	}

	@Override
	public void onStopListening() {
		super.onStopListening();
		if (publicIpRequest != null) {
			publicIpRequest.cancel();
			publicIpRequest = null;
		}
		qs_tile = null;
		wifiDefaultIcon = null;
		wifiSuccessIcon = null;
		wifiFailIcon = null;
		updateIcon = null;
	}

	@Override
	public void onClick() {
		super.onClick();
		if (qs_tile == null) return;
		showIPAddress();
		switchIP = !switchIP;
	}

	private void showIPAddress() {
		qs_tile.setIcon(updateIcon);
		qs_tile.setState(Tile.STATE_ACTIVE);
		if (switchIP) {
			qs_tile.setLabel("Public IP");
		} else {
			qs_tile.setLabel("Local IP");
		}
		qs_tile.updateTile();

		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			if (qs_tile == null) return;

			ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			if (CM == null) return;

			NetworkInfo wifiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo cellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			boolean isConnected = (wifiCheck != null && wifiCheck.isConnected()) || (cellularCheck != null && cellularCheck.isConnected());

			if (!isConnected) {
				qs_tile.setLabel(getString(R.string.no_connection));
				qs_tile.setIcon(wifiFailIcon);
				qs_tile.setState(Tile.STATE_INACTIVE);
				qs_tile.updateTile();
				return;
			}

			if (switchIP) {
				qs_tile.setLabel(NetworkUtils.getIPv4Address());
				qs_tile.setIcon(wifiSuccessIcon);
				qs_tile.setState(Tile.STATE_INACTIVE);
				qs_tile.updateTile();
			} else {
				if (publicIpRequest != null) {
					publicIpRequest.cancel();
				}
				publicIpRequest = NetworkUtils.fetchPublicIp(new NetworkUtils.PublicIpCallback() {
					@Override
					public void onSuccess(String ip) {
						if (qs_tile == null) return;
						qs_tile.setLabel(ip);
						qs_tile.setIcon(wifiSuccessIcon);
						qs_tile.setState(Tile.STATE_INACTIVE);
						qs_tile.updateTile();
					}

					@Override
					public void onError(Exception e) {
						if (qs_tile == null) return;
						qs_tile.setLabel(getString(R.string.na));
						qs_tile.setIcon(wifiFailIcon);
						qs_tile.setState(Tile.STATE_INACTIVE);
						qs_tile.updateTile();
					}
				});
			}
		}, 250);
	}
}