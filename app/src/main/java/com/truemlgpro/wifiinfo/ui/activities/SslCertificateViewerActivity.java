package com.truemlgpro.wifiinfo.ui.activities;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;

import com.stealthcopter.networktools.SslCertTools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SslCertificateViewerActivity extends AppCompatActivity {
	private EditText input_host, input_port, input_timeout, input_sni;
	private MaterialButton btn_fetch, btn_advanced;
	private LinearProgressIndicator progress;
	private View advanced_group;
	private TextView txt_result;

	private ConstraintLayout row_host;
	private MaterialCardView cert_card_view;
	private ScrollView cert_results_scroll;
	private TextView no_network_text;

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Handler main = new Handler(Looper.getMainLooper());
	private final Runnable doCheck = this::checkAndUpdateConnectivity;

	private boolean advancedOpen = false;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.ssl_certificate_viewer_activity);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		input_host = findViewById(R.id.input_host);
		input_port = findViewById(R.id.input_port);
		input_timeout = findViewById(R.id.input_timeout);
		input_sni = findViewById(R.id.input_sni);
		btn_fetch = findViewById(R.id.btn_fetch);
		btn_advanced = findViewById(R.id.btn_advanced);
		progress = findViewById(R.id.progress);
		advanced_group = findViewById(R.id.group_advanced);
		txt_result = findViewById(R.id.txtResult);
		row_host = findViewById(R.id.row_host);
		cert_card_view = findViewById(R.id.cardview_cert_results);
		cert_results_scroll = findViewById(R.id.cert_results_scroll);
		no_network_text = findViewById(R.id.textview_nonetworkconn);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		toolbar.setNavigationOnClickListener(v -> finish());

		btn_fetch.setOnClickListener(v -> startFetch());

		btn_advanced.setOnClickListener(v -> {
			advancedOpen = !advancedOpen;
			advanced_group.setVisibility(advancedOpen ? View.VISIBLE : View.GONE);
			btn_advanced.setIconResource(advancedOpen ? R.drawable.arrow_drop_up_24px : R.drawable.arrow_drop_down_24px);
		});

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		updateConnectivityUI(isOnline());

		initInsets();
	}

	private void registerNetworkCallback() {
		if (connectivityManager == null) return;

		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override public void onAvailable(Network network) { scheduleCheck(); }
			@Override public void onLost(Network network) { scheduleCheck(); }
			@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
		};

		NetworkRequest request = new NetworkRequest.Builder()
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
				.build();

		try {
			if (Build.VERSION.SDK_INT >= 26) {
				connectivityManager.registerNetworkCallback(request, networkCallback, main);
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
		main.removeCallbacks(doCheck);
	}

	private void scheduleCheck() {
		main.removeCallbacks(doCheck);
		main.postDelayed(doCheck, 100);
	}

	private void checkAndUpdateConnectivity() {
		updateConnectivityUI(isOnline());
	}

	private boolean isOnline() {
		return NetworkUtils.isOnline(this, NetworkUtils.NetworkType.ANY);
	}

	private void updateConnectivityUI(boolean connected) {
		if (connected) showWidgets(); else hideWidgets();
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_ssl);
		LinearLayout linear_layout_root = findViewById(R.id.linear_layout_root_ssl);

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
				linear_layout_root,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_BOTTOM | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	private void showWidgets() {
		if (no_network_text != null) no_network_text.setVisibility(View.GONE);
		if (row_host != null) row_host.setVisibility(View.VISIBLE);
		if (btn_fetch != null) btn_fetch.setVisibility(View.VISIBLE);

		if (advanced_group != null) {
			advanced_group.setVisibility(advancedOpen ? View.VISIBLE : View.GONE);
		}

		if (cert_card_view != null) cert_card_view.setVisibility(View.VISIBLE);
		if (cert_results_scroll != null) cert_results_scroll.setVisibility(View.VISIBLE);
		if (progress != null) progress.setVisibility(View.INVISIBLE);

		setInputsEnabled(true);
	}

	private void hideWidgets() {
		if (no_network_text != null) no_network_text.setVisibility(View.VISIBLE);
		if (row_host != null) row_host.setVisibility(View.GONE);
		if (btn_fetch != null) btn_fetch.setVisibility(View.GONE);
		if (advanced_group != null) advanced_group.setVisibility(View.GONE);
		if (cert_card_view != null) cert_card_view.setVisibility(View.GONE);
		if (cert_results_scroll != null) cert_results_scroll.setVisibility(View.GONE);
		if (progress != null) progress.setVisibility(View.GONE);

		setInputsEnabled(false);
	}

	private void setInputsEnabled(boolean enabled) {
		if (input_host != null) input_host.setEnabled(enabled);
		if (input_port != null) input_port.setEnabled(enabled);
		if (input_timeout != null) input_timeout.setEnabled(enabled);
		if (input_sni != null) input_sni.setEnabled(enabled);
		if (btn_advanced != null) btn_advanced.setEnabled(enabled);
		if (btn_fetch != null) btn_fetch.setEnabled(enabled);
	}

	private void startFetch() {
		String host = String.valueOf(input_host.getText()).trim();
		if (TextUtils.isEmpty(host)) {
			host = "google.com";
			input_host.setText(host);
		}
		final int port = parsePort(String.valueOf(input_port.getText()).trim());
		final int timeout = parseInt(String.valueOf(input_timeout.getText()).trim(), 10000);
		final String sniHost = String.valueOf(input_sni.getText()).trim();

		setLoading(true);
		txt_result.setText("");

		String finalHost = host;
		executor.execute(() -> {
			try {
				SslCertTools.Result result =
						SslCertTools.fetchCertificate(finalHost, port, timeout, TextUtils.isEmpty(sniHost) ? null : sniHost);

				String report = SslCertTools.buildReport(finalHost, port, result);

				main.post(() -> {
					txt_result.setText(report);
					setLoading(false);
				});
			} catch (Throwable t) {
				final String msg = "Error: " + t.getClass().getSimpleName() + " - " + (t.getMessage() != null ? t.getMessage() : "");
				main.post(() -> {
					txt_result.setText(msg);
					setLoading(false);
				});
			}
		});
	}

	private int parsePort(String p) {
		try {
			int v = Integer.parseInt(p);
			if (v < 1 || v > 65535) return 443;
			return v;
		} catch (Exception e) {
			return 443;
		}
	}

	private int parseInt(String s, int def) {
		try {
			if (TextUtils.isEmpty(s)) return def;
			return Integer.parseInt(s);
		} catch (Exception e) {
			return def;
		}
	}

	private void setLoading(boolean loading) {
		btn_fetch.setEnabled(!loading);
		input_host.setEnabled(!loading);
		input_port.setEnabled(!loading);
		input_timeout.setEnabled(!loading);
		input_sni.setEnabled(!loading);
		btn_advanced.setEnabled(!loading);
		progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
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
	}
}
