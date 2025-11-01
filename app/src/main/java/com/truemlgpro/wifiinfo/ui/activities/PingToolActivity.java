package com.truemlgpro.wifiinfo.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.util.concurrent.atomic.AtomicReference;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

public class PingToolActivity extends AppCompatActivity {
	private MaterialTextView textview_nonetworkconn;
	private LinearProgressIndicator ping_progress_bar;
	private TextInputLayout text_input_layout_ping;
	private EditText edit_text_ping;
	private EditText edit_text_timeout;
	private EditText edit_text_ttl;
	private EditText edit_text_times;
	private MaterialCardView ping_cardview;
	private ScrollView ping_results_scroll;
	private MaterialTextView ping_text;
	private MaterialButton ping_button;
	private MaterialButton ping_button_cancel;
	private MaterialButton btn_advanced;
	private View group_advanced;

	private Menu toolbarPingMenu;

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private Ping pinger;

	private HandlerThread pingHandlerThread;
	private Handler pingHandler;

	private Handler mainHandler;
	private final Runnable doCheck = () -> checkNetworkConnectivity(false);

	private Boolean wifi_connected = false;
	private Boolean cellular_connected = false;
	private Boolean advancedOpen = false;

	private String url_ip = "";
	private final String lineSeparator = "\n----------------------------\n";
	private int sentPackets = 0;

	private String ping_timeout_string = "";
	private String ping_ttl_string = "";
	private String ping_times_string = "";

	private final String DEFAULT_TIMEOUT = "3000";
	private final String DEFAULT_TTL = "30";
	private final String DEFAULT_PACKETS = "5";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.ping_activity);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		textview_nonetworkconn = findViewById(R.id.textview_nonetworkconn);
		ping_progress_bar = findViewById(R.id.ping_progress_bar);
		text_input_layout_ping = findViewById(R.id.input_layout_ping);
		edit_text_ping = findViewById(R.id.edit_text_ping);
		edit_text_timeout = findViewById(R.id.edit_text_timeout);
		edit_text_ttl = findViewById(R.id.edit_text_ttl);
		edit_text_times = findViewById(R.id.edit_text_times);
		ping_button = findViewById(R.id.ping_button);
		ping_button_cancel = findViewById(R.id.ping_button_cancel);
		ping_cardview = findViewById(R.id.ping_cardview);
		ping_results_scroll = findViewById(R.id.ping_results_scroll);
		ping_text = findViewById(R.id.ping_results_textview);
		btn_advanced = findViewById(R.id.btn_advanced);
		group_advanced = findViewById(R.id.group_advanced);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		toolbar.setNavigationOnClickListener(v -> finish());

		ping_button.setOnClickListener(v -> preparePinger());
		ping_button_cancel.setOnClickListener(v -> { if (pinger != null) pinger.cancel(); });

		btn_advanced.setOnClickListener(v -> {
			advancedOpen = !advancedOpen;
			group_advanced.setVisibility(advancedOpen ? View.VISIBLE : View.GONE);
			btn_advanced.setIconResource(advancedOpen ? R.drawable.arrow_drop_up_24px : R.drawable.arrow_drop_down_24px);
		});

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mainHandler = new Handler(Looper.getMainLooper());

		checkNetworkConnectivity(false);
		initInsets();
	}

	private void validateField(EditText editText, String input, String defaultValue) {
		if (TextUtils.isEmpty(input)) {
			if (editText.equals(edit_text_timeout)) {
				setInputToDefault(editText, defaultValue, getString(R.string.timeout_field_empty));
				ping_timeout_string = DEFAULT_TIMEOUT;
			} else if (editText.equals(edit_text_ttl)) {
				setInputToDefault(editText, defaultValue, getString(R.string.ttl_field_empty));
				ping_ttl_string = DEFAULT_TTL;
			} else if (editText.equals(edit_text_times)) {
				setInputToDefault(editText, defaultValue, getString(R.string.packet_amount_not_defined));
				ping_times_string = DEFAULT_PACKETS;
			}
		} else if (!isStringInt(input)) {
			if (editText.equals(edit_text_timeout)) {
				setInputToDefault(editText, defaultValue, getString(R.string.timeout_not_integer));
				ping_timeout_string = DEFAULT_TIMEOUT;
			} else if (editText.equals(edit_text_ttl)) {
				setInputToDefault(editText, defaultValue, getString(R.string.ttl_not_integer));
				ping_ttl_string = DEFAULT_TTL;
			} else if (editText.equals(edit_text_times)) {
				setInputToDefault(editText, defaultValue, String.format(getString(R.string.packet_amount_not_integer), input));
				ping_times_string = DEFAULT_PACKETS;
			}
		} else if (Integer.parseInt(input) < 1) {
			if (editText.equals(edit_text_timeout)) {
				setInputToDefault(editText, defaultValue, getString(R.string.timeout_lower_than_1_ms));
				ping_timeout_string = DEFAULT_TIMEOUT;
			} else if (editText.equals(edit_text_ttl)) {
				setInputToDefault(editText, defaultValue, getString(R.string.ttl_lower_than_1));
				ping_ttl_string = DEFAULT_TTL;
			} else if (editText.equals(edit_text_times)) {
				setInputToDefault(editText, defaultValue, String.format(getString(R.string.packet_amount_lower_than_1), input));
				ping_times_string = DEFAULT_PACKETS;
			}
		}
	}

	private void setInputToDefault(EditText editText, String defaultStr, String errorMessage) {
		appendResultsText(errorMessage);
		appendResultsText(getString(R.string.resetting));
		appendResultsText(lineSeparator);
		editText.setText(defaultStr);
	}

	private void preparePinger() {
		ping_timeout_string = edit_text_timeout.getText().toString();
		ping_ttl_string = edit_text_ttl.getText().toString();
		ping_times_string = edit_text_times.getText().toString();

		disableViews();

		url_ip = NetworkUtils.extractDomain(edit_text_ping.getText().toString());
		if (TextUtils.isEmpty(url_ip)) {
			if (wifi_connected) {
				url_ip = NetworkUtils.getGatewayIP(PingToolActivity.this);
			} else if (cellular_connected) {
				url_ip = "google.com";
			}
		}
		edit_text_ping.setText(url_ip);

		validateField(edit_text_timeout, ping_timeout_string, DEFAULT_TIMEOUT);
		validateField(edit_text_ttl, ping_ttl_string, DEFAULT_TTL);
		validateField(edit_text_times, ping_times_string, DEFAULT_PACKETS);

		int ping_timeout = Integer.parseInt(ping_timeout_string);
		int ping_ttl = Integer.parseInt(ping_ttl_string);
		int ping_times = Integer.parseInt(ping_times_string);

		pingHandlerThread = new HandlerThread("PingBackgroundHandlerThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		pingHandlerThread.start();
		pingHandler = new Handler(pingHandlerThread.getLooper());

		pingHandler.post(() -> {
			try {
				AtomicReference<String> pingHostAddress = new AtomicReference<>("");
				AtomicReference<String> pingHostname = new AtomicReference<>("");

				NetworkUtils.convertUrlToIp(url_ip, result -> {
					pingHostAddress.set(result);
					appendResultsText(String.format(getString(R.string.ping_log_ip), pingHostAddress.get()));
				});

				NetworkUtils.convertIpToUrl(url_ip, result -> {
					pingHostname.set(result);
					appendResultsText(String.format(getString(R.string.ping_log_hostname), pingHostname.get()));
					appendResultsText(getString(R.string.time_to_live_ttl) + ": " + ping_ttl);
					startPinger(url_ip, ping_timeout, ping_ttl, ping_times);
				});
			} catch (Exception e) {
				e.printStackTrace();
				enableViews();
				appendResultsText(lineSeparator);
			}
		});
	}

	private void startPinger(String url_ip, int timeout, int ttl, int times) {
		runOnUiThread(() -> ping_progress_bar.setVisibility(View.VISIBLE));
		pinger = Ping.Companion.onAddress(url_ip)
				.setTimeOutMillis(timeout)
				.setDelayMillis(500)
				.setTimeToLive(ttl)
				.setTimes(times)
				.doPing(new Ping.PingListener() {
					final long startTime = System.currentTimeMillis();
					@Override
					public void onResult(PingResult pingResult) {
						sentPackets++;
						if (pingResult.isReachable()) {
							appendResultsText(String.format(getString(R.string.ping_successful_response),
									sentPackets, pingResult.getAddress(), pingResult.getTimeTaken()));
						} else {
							appendResultsText(String.format(getString(R.string.connection_timeout), sentPackets));
						}
					}

					@Override
					public void onFinished(PingStats pingStats) {
						long endTime = System.currentTimeMillis();
						appendResultsText("");
						appendResultsText(String.format(getString(R.string.ping_packet_stats),
								pingStats.getNoPings(),
								pingStats.getNoPings() - pingStats.getPacketsLost(),
								pingStats.getPacketsLost(),
								(float) pingStats.getPacketsLost() / (float) pingStats.getNoPings() * 100));
						appendResultsText(String.format(getString(R.string.min_avg_max_latency_stats),
								pingStats.getMinTimeTaken(), pingStats.getAverageTimeTaken(), pingStats.getMaxTimeTaken()));
						appendResultsText(String.format(getString(R.string.total_ping_time), endTime - startTime));
						appendResultsText(lineSeparator);
						sentPackets = 0;
						runOnUiThread(() -> ping_progress_bar.setVisibility(View.INVISIBLE));
						enableViews();
					}

					@Override
					public void onError(Exception e) {
						Log.e("startPinger", e.getMessage());
						appendResultsText(String.valueOf(e.getMessage()));
						appendResultsText(lineSeparator);
						sentPackets = 0;
						runOnUiThread(() -> ping_progress_bar.setVisibility(View.INVISIBLE));
						enableViews();
					}
				});
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerNetworkCallback();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (pinger != null) {
			pinger.cancel();
		}
		if (pingHandlerThread != null) {
			pingHandlerThread.quit();
			pingHandlerThread = null;
		}
		if (pingHandler != null) {
			pingHandler.removeCallbacks(null);
			pingHandler.getLooper().quit();
		}
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

	public void checkNetworkConnectivity(Boolean shouldClearLog) {
		boolean wifi = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.WIFI, false);
		boolean cell = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.CELLULAR, false);

		if (wifi || cell) {
			showWidgets();
			if (toolbarPingMenu != null && !toolbarPingMenu.findItem(R.id.clear_ping_log).isEnabled()) {
				setToolbarItemEnabled(R.id.clear_ping_log, true);
			}
			wifi_connected = wifi;
			cellular_connected = !wifi && cell;
		} else {
			if (shouldClearLog) { ping_text.setText(""); }
			if (toolbarPingMenu != null && toolbarPingMenu.findItem(R.id.clear_ping_log).isEnabled()) {
				setToolbarItemEnabled(R.id.clear_ping_log, false);
			}
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_ping);
		LinearLayout linear_layout_root = findViewById(R.id.linear_layout_root_ping);

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
		ping_text.setVisibility(View.VISIBLE);
		ping_button.setVisibility(View.VISIBLE);
		ping_button_cancel.setVisibility(View.VISIBLE);
		text_input_layout_ping.setVisibility(View.VISIBLE);
		btn_advanced.setVisibility(View.VISIBLE);
		if (group_advanced != null) group_advanced.setVisibility(advancedOpen ? View.VISIBLE : View.GONE);
		ping_cardview.setVisibility(View.VISIBLE);
		ping_results_scroll.setVisibility(View.VISIBLE);
		ping_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	private void hideWidgets() {
		ping_text.setVisibility(View.GONE);
		ping_button.setVisibility(View.GONE);
		ping_button_cancel.setVisibility(View.GONE);
		text_input_layout_ping.setVisibility(View.GONE);
		btn_advanced.setVisibility(View.GONE);
		if (group_advanced != null) group_advanced.setVisibility(View.GONE);
		ping_cardview.setVisibility(View.GONE);
		ping_results_scroll.setVisibility(View.GONE);
		ping_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> { if (view != null) view.setEnabled(enabled); });
	}

	private void enableViews() {
		setEnabled(ping_button, true);
		setEnabled(ping_button_cancel, false);
	}

	private void disableViews() {
		setEnabled(ping_button, false);
		setEnabled(ping_button_cancel, true);
	}

	private void appendResultsText(final String text) {
		runOnUiThread(() -> {
			ping_text.append(text + "\n");
			ping_results_scroll.post(() -> {
				View lastChild = ping_results_scroll.getChildAt(ping_results_scroll.getChildCount() - 1);
				int bottom = lastChild.getBottom() + ping_results_scroll.getPaddingBottom();
				int sy = ping_results_scroll.getScrollY();
				int sh = ping_results_scroll.getHeight();
				int delta = bottom - (sy + sh);
				ping_results_scroll.smoothScrollBy(0, delta);
			});
		});
	}

	private boolean isStringInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void setToolbarItemEnabled(int item, Boolean enabled) {
		if (toolbarPingMenu != null) {
			toolbarPingMenu.findItem(item).setEnabled(enabled);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.ping_tool_action_bar_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toolbarPingMenu = menu;
		checkNetworkConnectivity(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.clear_ping_log) {
			ping_text.setText("");
		}
		return true;
	}
}
