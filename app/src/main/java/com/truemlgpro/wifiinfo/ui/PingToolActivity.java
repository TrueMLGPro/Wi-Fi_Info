package com.truemlgpro.wifiinfo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.FontManager;
import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;
import com.truemlgpro.wifiinfo.utils.URLandIPConverter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class PingToolActivity extends AppCompatActivity {
	private TextView textview_nonetworkconn;
	private ProgressBar ping_progress_bar;
	private TextInputLayout text_input_layout_ping;
	private TextInputLayout text_input_layout_timeout;
	private TextInputLayout text_input_layout_ttl;
	private TextInputLayout text_input_layout_times;
	private EditText edit_text_ping;
	private EditText edit_text_timeout;
	private EditText edit_text_ttl;
	private EditText edit_text_times;
	private ScrollView ping_results_scroll;
	private TextView ping_text;
	private Button ping_button;
	private Button ping_button_cancel;

	private Menu toolbarPingMenu;

	private BroadcastReceiver NetworkConnectivityReceiver;
	private ConnectivityManager CM;
	private NetworkInfo wifiCheck;
	private NetworkInfo cellularCheck;

	private Ping pinger;

	private HandlerThread pingHandlerThread;
	private Handler pingHandler;

	private Boolean wifi_connected;
	private Boolean cellular_connected;

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
		setContentView(R.layout.ping_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		ping_progress_bar = (ProgressBar) findViewById(R.id.ping_progress_bar);
		text_input_layout_ping = (TextInputLayout) findViewById(R.id.input_layout_ping);
		text_input_layout_timeout = (TextInputLayout) findViewById(R.id.input_layout_timeout);
		text_input_layout_ttl = (TextInputLayout) findViewById(R.id.input_layout_ttl);
		text_input_layout_times = (TextInputLayout) findViewById(R.id.input_layout_times);
		edit_text_ping = (EditText) findViewById(R.id.edit_text_ping);
		edit_text_timeout = (EditText) findViewById(R.id.edit_text_timeout);
		edit_text_ttl = (EditText) findViewById(R.id.edit_text_ttl);
		edit_text_times = (EditText) findViewById(R.id.edit_text_times);
		ping_button = (Button) findViewById(R.id.ping_button);
		ping_button_cancel = (Button) findViewById(R.id.ping_button_cancel);
		ping_results_scroll = (ScrollView) findViewById(R.id.ping_scroll);
		ping_text = (TextView) findViewById(R.id.ping_textview);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		FontManager.init(this, getApplicationContext(), true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		actionbar.setTitle(getResources().getString(R.string.ping_tool));

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		ping_button.setOnClickListener(v -> preparePinger());

		ping_button_cancel.setOnClickListener(v -> {
			if (pinger != null) {
				pinger.cancel();
			}
		});
	}

	private String getGateway() {
		if (!wifiCheck.isConnected()) {
			return "0.0.0.0";
		}
		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifi.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}

	private void validateField(EditText editText, String input, String defaultValue) {
		if (TextUtils.isEmpty(input)) {
			if (editText.equals(edit_text_timeout)) {
				setInputToDefault(editText, defaultValue, getString(R.string.timeout_field_empty));
				ping_timeout_string = defaultValue;
			} else if (editText.equals(edit_text_ttl)) {
				setInputToDefault(editText, defaultValue, getString(R.string.ttl_field_empty));
				ping_ttl_string = defaultValue;
			} else if (editText.equals(edit_text_times)) {
				setInputToDefault(editText, defaultValue, getString(R.string.packet_amount_not_defined));
				ping_times_string = defaultValue;
			}
		} else if (!isStringInt(input)) {
			if (editText.equals(edit_text_timeout)) {
				setInputToDefault(editText, defaultValue, getString(R.string.timeout_not_integer));
				ping_timeout_string = defaultValue;
			} else if (editText.equals(edit_text_ttl)) {
				setInputToDefault(editText, defaultValue, getString(R.string.ttl_not_integer));
				ping_ttl_string = defaultValue;
			} else if (editText.equals(edit_text_times)) {
				setInputToDefault(editText, defaultValue, String.format(getString(R.string.packet_amount_not_integer), input));
				ping_times_string = defaultValue;
			}
		} else if (Integer.parseInt(input) < 1) {
			if (editText.equals(edit_text_timeout)) {
				setInputToDefault(editText, defaultValue, getString(R.string.timeout_lower_than_1_ms));
				ping_timeout_string = defaultValue;
			} else if (editText.equals(edit_text_ttl)) {
				setInputToDefault(editText, defaultValue, getString(R.string.ttl_lower_than_1));
				ping_ttl_string = defaultValue;
			} else if (editText.equals(edit_text_times)) {
				setInputToDefault(editText, defaultValue, String.format(getString(R.string.packet_amount_lower_than_1), input));
				ping_times_string = defaultValue;
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

		url_ip = edit_text_ping.getText().toString();
		if (TextUtils.isEmpty(url_ip)) {
			if (wifi_connected) {
				url_ip = getGateway();
			} else if (cellular_connected) {
				url_ip = "google.com";
			}
			edit_text_ping.setText(url_ip);
		}

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
				URLandIPConverter.convertUrlToIp(url_ip, result -> {
					pingHostAddress.set(result);
					appendResultsText(String.format(getString(R.string.ping_log_ip), pingHostAddress.get()));
				});

				URLandIPConverter.convertIpToUrl(url_ip, result -> {
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
		pinger = Ping.Companion.onAddress(url_ip).setTimeOutMillis(timeout).setDelayMillis(500).setTimeToLive(ttl).setTimes(times).doPing(new Ping.PingListener() {
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
				e.printStackTrace();
				appendResultsText(e.getMessage());
				appendResultsText(lineSeparator);
				sentPackets = 0;
				runOnUiThread(() -> ping_progress_bar.setVisibility(View.INVISIBLE));
				enableViews();
			}
		});
	}

	public class NetworkConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkNetworkConnectivity(true);
		}
	}

	private boolean isSimCardPresent(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
	}

	public void checkNetworkConnectivity(Boolean shouldClearLog) {
		CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		wifiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		cellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifiCheck.isConnected()) { // Wi-Fi Connectivity Check
			showWidgets();
			if (toolbarPingMenu != null) {
				if (!toolbarPingMenu.findItem(R.id.clear_ping_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_ping_log, true);
				}
			}
			wifi_connected = true;
			cellular_connected = false;
		} else if (isSimCardPresent(this) && Objects.nonNull(cellularCheck) && cellularCheck.isConnected()) { // Cellular Connectivity Check
			showWidgets();
			if (toolbarPingMenu != null) {
				if (!toolbarPingMenu.findItem(R.id.clear_ping_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_ping_log, true);
				}
			}
			wifi_connected = false;
			cellular_connected = true;
		} else {
			if (shouldClearLog) { ping_text.setText(""); }
			if (toolbarPingMenu != null) {
				if (toolbarPingMenu.findItem(R.id.clear_ping_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_ping_log, false);
				}
			}
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	public void showWidgets() {
		ping_text.setVisibility(View.VISIBLE);
		ping_button.setVisibility(View.VISIBLE);
		ping_button_cancel.setVisibility(View.VISIBLE);
		text_input_layout_ping.setVisibility(View.VISIBLE);
		text_input_layout_timeout.setVisibility(View.VISIBLE);
		text_input_layout_ttl.setVisibility(View.VISIBLE);
		text_input_layout_times.setVisibility(View.VISIBLE);
		ping_results_scroll.setVisibility(View.VISIBLE);
		ping_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		ping_text.setVisibility(View.GONE);
		ping_button.setVisibility(View.GONE);
		ping_button_cancel.setVisibility(View.GONE);
		text_input_layout_ping.setVisibility(View.GONE);
		text_input_layout_timeout.setVisibility(View.GONE);
		text_input_layout_ttl.setVisibility(View.GONE);
		text_input_layout_times.setVisibility(View.GONE);
		ping_results_scroll.setVisibility(View.GONE);
		ping_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}
	
	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> {
			if (view != null) {
				view.setEnabled(enabled);
			}
		});
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

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new NetworkConnectivityReceiver();
		registerReceiver(NetworkConnectivityReceiver, filter);
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
		unregisterReceiver(NetworkConnectivityReceiver);
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
