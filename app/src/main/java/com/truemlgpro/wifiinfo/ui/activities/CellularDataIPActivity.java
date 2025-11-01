package com.truemlgpro.wifiinfo.ui.activities;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.app.AppClipboardManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

public class CellularDataIPActivity extends AppCompatActivity {
	private MaterialTextView textview_nocellconn;
	private MaterialCardView cardview_ip;
	private MaterialCardView cardview_local_ip;
	private MaterialTextView textview_public_ip_cell;
	private MaterialTextView textview_local_ipv4_cell;
	private MaterialTextView textview_local_ipv6_cell;
	private FloatingActionButton fab_update_ip;

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private Handler mainHandler;
	private final Runnable doCheck = this::checkAndUpdateConnectivity;
	private NetworkUtils.Cancelable publicIpRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.cellular_data_ip_activity);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		cardview_ip = findViewById(R.id.cardview_ip);
		cardview_local_ip = findViewById(R.id.cardview_local_ip);
		textview_public_ip_cell = findViewById(R.id.textview_public_ip_cell);
		textview_local_ipv4_cell = findViewById(R.id.textview_local_ipv4_cell_value);
		textview_local_ipv6_cell = findViewById(R.id.textview_local_ipv6_cell_value);
		textview_nocellconn = findViewById(R.id.textview_noconn);
		fab_update_ip = findViewById(R.id.fab_update_ip);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		initCopyableText();

		toolbar.setNavigationOnClickListener(v -> finish());

		mainHandler = new Handler(Looper.getMainLooper());
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		fab_update_ip.setOnClickListener(v -> fetchPublicIp());

		updateConnectivityUI(NetworkUtils.isOnline(this, NetworkUtils.NetworkType.CELLULAR, false));

		initInsets();
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerNetworkCallback();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterNetworkCallback();
		if (publicIpRequest != null) {
			publicIpRequest.cancel();
			publicIpRequest = null;
		}
	}

	private void registerNetworkCallback() {
		if (connectivityManager == null) return;

		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override public void onAvailable(Network network) { scheduleCheck(); }
			@Override public void onLost(Network network) { scheduleCheck(); }
			@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
		};

		NetworkRequest request = new NetworkRequest.Builder()
				.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				.build();

		try {
			if (Build.VERSION.SDK_INT >= 26) {
				connectivityManager.registerNetworkCallback(request, networkCallback, mainHandler);
			} else {
				connectivityManager.registerNetworkCallback(request, networkCallback);
			}
		} catch (Exception ignored) { }
	}

	private void unregisterNetworkCallback() {
		if (connectivityManager != null && networkCallback != null) {
			try {
				connectivityManager.unregisterNetworkCallback(networkCallback);
			} catch (Exception ignored) { }
			networkCallback = null;
		}
		if (mainHandler != null) mainHandler.removeCallbacks(doCheck);
	}

	private void scheduleCheck() {
		if (mainHandler == null) return;
		mainHandler.removeCallbacks(doCheck);
		mainHandler.postDelayed(doCheck, 100);
	}

	private void checkAndUpdateConnectivity() {
		boolean cellularConnected = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.CELLULAR, false);
		updateConnectivityUI(cellularConnected);
	}

	private void updateConnectivityUI(boolean cellularConnected) {
		if (cellularConnected) {
			showWidgets();
			textview_local_ipv4_cell.setText(NetworkUtils.getIPv4Address());
			textview_local_ipv6_cell.setText(NetworkUtils.getIPv6Address());
		} else {
			textview_public_ip_cell.setText(getString(R.string.your_ip_na));
			textview_local_ipv4_cell.setText(getString(R.string.na));
			textview_local_ipv6_cell.setText(getString(R.string.na));
			hideWidgets();
		}
	}

	private void initCopyableText() {
		textview_public_ip_cell.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(this, getString(R.string.public_ip_address), textview_public_ip_cell.getText().toString());
			return true;
		});

		textview_local_ipv4_cell.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(this, getString(R.string.ipv4), textview_local_ipv4_cell.getText().toString());
			return true;
		});

		textview_local_ipv6_cell.setOnLongClickListener(v -> {
			AppClipboardManager.copyToClipboard(this, getString(R.string.ipv6), textview_local_ipv6_cell.getText().toString());
			return true;
		});
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_cellular);
		NestedScrollView scroll_view = findViewById(R.id.scroll_view);

		InsetsController.setInsets(
				app_bar_layout,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_TOP | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);

		InsetsController.setInsets(
				scroll_view,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_BOTTOM | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	private void showWidgets() {
		cardview_ip.setVisibility(View.VISIBLE);
		cardview_local_ip.setVisibility(View.VISIBLE);
		textview_nocellconn.setVisibility(View.GONE);
	}

	private void hideWidgets() {
		cardview_ip.setVisibility(View.GONE);
		cardview_local_ip.setVisibility(View.GONE);
		textview_nocellconn.setVisibility(View.VISIBLE);
	}

	private void fetchPublicIp() {
		fab_update_ip.setEnabled(false);
		publicIpRequest = NetworkUtils.fetchPublicIp(
				new NetworkUtils.PublicIpCallback() {
					@Override
					public void onSuccess(String ip) {
						if (isFinishing() || isDestroyed()) return;
						textview_public_ip_cell.setText(getString(R.string.your_ip, ip));
						new Handler(Looper.getMainLooper()).postDelayed(
								() -> fab_update_ip.setEnabled(true),
								5000
						);
					}

					@Override
					public void onError(Exception e) {
						if (isFinishing() || isDestroyed()) return;
						textview_public_ip_cell.setText(getString(R.string.your_ip, getString(R.string.na)));
						new Handler(Looper.getMainLooper()).postDelayed(
								() -> fab_update_ip.setEnabled(true),
								5000
						);
					}
				}
		);
	}
}
