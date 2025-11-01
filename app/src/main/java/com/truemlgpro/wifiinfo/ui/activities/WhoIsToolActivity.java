package com.truemlgpro.wifiinfo.ui.activities;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.net.InetAddress;
import java.util.Objects;

import thecollectiveweb.com.tcwhois.TCWHOIS;

public class WhoIsToolActivity extends AppCompatActivity {
	private MaterialTextView textview_nonetworkconn;
	private LinearProgressIndicator who_is_progress_bar;
	private TextInputLayout who_is_input_layout;
	private EditText who_is_edit_text;
	private MaterialButton fetch_whois_info_button;
	private MaterialCardView who_is_cardview;
	private ScrollView who_is_scroll;
	private MaterialTextView who_is_results_textview;

	private Menu toolbarWhoisMenu;

	private Bundle whoIsBundle = new Bundle();
	private static final String MSG_KEY = "WhoIsQuery";

	private static final int STATE_SUCCESS = 0;
	private static final int STATE_ERROR_MALFORMED_URL = 1;
	private static final int STATE_ERROR_UNKNOWN_HOST = 2;

	private static final int STATE_RUNNABLE_STARTED = 11;
	private static final int STATE_RUNNABLE_FINISHED = 12;

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private Handler mainHandler;
	private final Runnable doCheck = () -> checkNetworkConnectivity(false);

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.who_is_tool_activity);

		Toolbar toolbar = findViewById(R.id.toolbar);
		textview_nonetworkconn = findViewById(R.id.textview_nonetworkconn);
		who_is_progress_bar = findViewById(R.id.who_is_progress_bar);
		who_is_input_layout = findViewById(R.id.url_to_ip_input_layout);
		who_is_edit_text = findViewById(R.id.url_to_ip_edit_text);
		fetch_whois_info_button = findViewById(R.id.fetch_whois_info_button);
		who_is_cardview = findViewById(R.id.who_is_cardview);
		who_is_scroll = findViewById(R.id.who_is_results_scroll);
		who_is_results_textview = findViewById(R.id.who_is_results_textview);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setTitle(getResources().getString(R.string.whois_tool));

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		toolbar.setNavigationOnClickListener(v -> finish());

		fetch_whois_info_button.setOnClickListener(v -> {
			String whois_url_ip = NetworkUtils.extractDomain(who_is_edit_text.getText().toString());
			if (TextUtils.isEmpty(whois_url_ip)) {
				whois_url_ip = "google.com";
			}
			who_is_edit_text.setText(whois_url_ip);
			startWhoIsThread();
		});

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mainHandler = new Handler(Looper.getMainLooper());

		checkNetworkConnectivity(false);
		initInsets();
	}

	private final Handler msgHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case STATE_SUCCESS -> {
					whoIsBundle = msg.getData();
					String whoIsQuery = whoIsBundle.getString(MSG_KEY);
					appendResultsText(whoIsQuery);
				}
				case STATE_ERROR_MALFORMED_URL ->
						appendResultsText(getString(R.string.error_malformed_url));
				case STATE_ERROR_UNKNOWN_HOST ->
						appendResultsText(getString(R.string.error_unknown_host));
				case STATE_RUNNABLE_STARTED -> {
					runOnUiThread(() -> who_is_progress_bar.setVisibility(View.VISIBLE));
					setEnabled(fetch_whois_info_button, false);
				}
				case STATE_RUNNABLE_FINISHED -> {
					runOnUiThread(() -> who_is_progress_bar.setVisibility(View.INVISIBLE));
					setEnabled(fetch_whois_info_button, true);
				}
			}
		}
	};

	private final Runnable msgSenderRunnable = new Runnable() {
		@Override
		public void run() {
			msgHandler.sendEmptyMessage(STATE_RUNNABLE_STARTED);

			String url = who_is_edit_text.getText().toString().trim();
			String host = NetworkUtils.extractDomain(url);
			if (TextUtils.isEmpty(host)) host = "google.com";

			String ipStr = "";
			try {
				InetAddress a = InetAddress.getByName(host);
				ipStr = a != null ? a.getHostAddress() : "";
			} catch (Exception ignored) {}

			String fetched_whois_data = getWhoIsInfo(host);

			String lineSeparator = "\n----------------------------\n";
			String output = String.format(getString(R.string.whois_result_output), host, ipStr, fetched_whois_data, lineSeparator);

			Message msg = msgHandler.obtainMessage(STATE_SUCCESS);
			whoIsBundle.putString(MSG_KEY, output);
			msg.setData(whoIsBundle);
			msgHandler.sendMessage(msg);
			msgHandler.sendEmptyMessage(STATE_RUNNABLE_FINISHED);
		}
	};

	private void startWhoIsThread() {
		new Thread(msgSenderRunnable).start();
	}

	public String getWhoIsInfo(String host) {
		String whoisData = "";
		try {
			TCWHOIS whoisClient = new TCWHOIS();
			whoisData = whoisClient.getTCWHOIS(host);
		} catch (InterruptedException e) {
			e.printStackTrace();
			appendResultsText(getString(R.string.error_interrupted));
		} catch (java.util.concurrent.ExecutionException e) {
			e.printStackTrace();
			appendResultsText(getString(R.string.error_failed_to_execute));
		}
		return whoisData;
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_whois);
		LinearLayout linear_layout_root = findViewById(R.id.linear_layout_root_whois);

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
				connectivityManager.registerNetworkCallback(request, networkCallback, mainHandler);
			} else {
				connectivityManager.registerNetworkCallback(request, networkCallback);
			}
		} catch (Exception ignored) { }
	}

	private void unregisterNetworkCallback() {
		if (connectivityManager != null && networkCallback != null) {
			try { connectivityManager.unregisterNetworkCallback(networkCallback); } catch (Exception ignored) { }
			networkCallback = null;
		}
		if (mainHandler != null) mainHandler.removeCallbacks(doCheck);
	}

	private void scheduleCheck() {
		if (mainHandler == null) return;
		mainHandler.removeCallbacks(doCheck);
		mainHandler.postDelayed(doCheck, 100);
	}

	private boolean isOnline() {
		return NetworkUtils.isOnline(this, NetworkUtils.NetworkType.ANY);
	}

	public void checkNetworkConnectivity(Boolean shouldClearLog) {
		boolean connected = isOnline();

		if (connected) {
			showWidgets();
			if (toolbarWhoisMenu != null) {
				if (!toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, true);
				}
			}
		} else {
			if (shouldClearLog) { who_is_results_textview.setText(""); }
			if (toolbarWhoisMenu != null) {
				if (toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, false);
				}
			}
			hideWidgets();
		}
	}

	public void showWidgets() {
		who_is_input_layout.setVisibility(View.VISIBLE);
		who_is_edit_text.setVisibility(View.VISIBLE);
		fetch_whois_info_button.setVisibility(View.VISIBLE);
		who_is_cardview.setVisibility(View.VISIBLE);
		who_is_scroll.setVisibility(View.VISIBLE);
		who_is_results_textview.setVisibility(View.VISIBLE);
		who_is_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		who_is_input_layout.setVisibility(View.GONE);
		who_is_edit_text.setVisibility(View.GONE);
		fetch_whois_info_button.setVisibility(View.GONE);
		who_is_cardview.setVisibility(View.GONE);
		who_is_scroll.setVisibility(View.GONE);
		who_is_results_textview.setVisibility(View.GONE);
		who_is_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> {
			if (view != null) {
				view.setEnabled(enabled);
			}
		});
	}

	private void appendResultsText(final String text) {
		runOnUiThread(() -> {
			who_is_results_textview.append(text + "\n");
			who_is_scroll.post(() -> {
				View lastChild = who_is_scroll.getChildAt(who_is_scroll.getChildCount() - 1);
				int bottom = lastChild.getBottom() + who_is_scroll.getPaddingBottom();
				int sy = who_is_scroll.getScrollY();
				int sh = who_is_scroll.getHeight();
				int delta = bottom - (sy + sh);

				who_is_scroll.smoothScrollBy(0, delta);
			});
		});
	}

	private void setToolbarItemEnabled(int item, Boolean enabled) {
		if (toolbarWhoisMenu != null) {
			toolbarWhoisMenu.findItem(item).setEnabled(enabled);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.whois_tool_action_bar_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toolbarWhoisMenu = menu;
		checkNetworkConnectivity(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.clear_whois_log) {
			who_is_results_textview.setText("");
		}
		return true;
	}
}
