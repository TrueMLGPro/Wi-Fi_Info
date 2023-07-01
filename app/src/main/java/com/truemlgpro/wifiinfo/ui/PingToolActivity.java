package com.truemlgpro.wifiinfo.ui;

import android.annotation.SuppressLint;
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
import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;
import com.truemlgpro.wifiinfo.utils.URLandIPConverter;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Objects;

import me.anwarshahriar.calligrapher.Calligrapher;

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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.ping_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		ping_progress_bar = (ProgressBar) findViewById(R.id.ping_progress_bar);
		text_input_layout_ping = (TextInputLayout) findViewById(R.id.input_layout_ping);
		edit_text_ping = (EditText) findViewById(R.id.edit_text_ping);
		text_input_layout_timeout = (TextInputLayout) findViewById(R.id.input_layout_timeout);
		edit_text_timeout = (EditText) findViewById(R.id.edit_text_timeout);
		text_input_layout_ttl = (TextInputLayout) findViewById(R.id.input_layout_ttl);
		edit_text_ttl = (EditText) findViewById(R.id.edit_text_ttl);
		text_input_layout_times = (TextInputLayout) findViewById(R.id.input_layout_times);
		edit_text_times = (EditText) findViewById(R.id.edit_text_times);
		ping_button = (Button) findViewById(R.id.ping_button);
		ping_button_cancel = (Button) findViewById(R.id.ping_button_cancel);
		ping_results_scroll = (ScrollView) findViewById(R.id.ping_scroll);
		ping_text = (TextView) findViewById(R.id.ping_textview);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, MainActivity.appFont);
		calligrapher.setFont(this, font, true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);

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

	@SuppressLint("SetTextI18n")
	private void preparePinger() {
		setEnabled(ping_button, false);
		setEnabled(ping_button_cancel, true);

		url_ip = edit_text_ping.getText().toString();
		if (TextUtils.isEmpty(url_ip)) {
			if (wifi_connected) {
				url_ip = getGateway();
			} else if (cellular_connected) {
				url_ip = "google.com";
			}
			edit_text_ping.setText(url_ip);
		}

		if (TextUtils.isEmpty(edit_text_timeout.getText().toString())) {
			appendResultsText(getString(R.string.timeout_field_empty));
			appendResultsText(getString(R.string.resetting));
			edit_text_timeout.setText("3000");
		}

		if (!isStringInt(edit_text_timeout.getText().toString())) {
			appendResultsText(getString(R.string.timeout_not_integer));
			appendResultsText(getString(R.string.resetting));
			edit_text_timeout.setText("3000");
		}

		if (Integer.parseInt(edit_text_timeout.getText().toString()) < 1) {
			appendResultsText(getString(R.string.timeout_lower_than_1_ms));
			appendResultsText(getString(R.string.resetting));
			edit_text_timeout.setText("3000");
		}

		if (TextUtils.isEmpty(edit_text_ttl.getText().toString())) {
			appendResultsText(getString(R.string.ttl_field_empty));
			appendResultsText(getString(R.string.resetting));
			edit_text_ttl.setText("30");
		}

		if (!isStringInt(edit_text_ttl.getText().toString())) {
			appendResultsText(getString(R.string.ttl_not_integer));
			appendResultsText(getString(R.string.resetting));
			edit_text_ttl.setText("30");
		}

		if (Integer.parseInt(edit_text_ttl.getText().toString()) < 1) {
			appendResultsText(getString(R.string.ttl_lower_than_1));
			appendResultsText(getString(R.string.resetting));
			edit_text_ttl.setText("30");
		}

		if (TextUtils.isEmpty(edit_text_times.getText().toString())) {
			appendResultsText(getString(R.string.packets_amount_not_defined));
			appendResultsText(getString(R.string.resetting));
			edit_text_times.setText("5");
		}

		if (!isStringInt(edit_text_times.getText().toString())) {
			appendResultsText(getString(R.string.packets_not_integer, edit_text_times.getText().toString()));
			appendResultsText(getString(R.string.resetting));
			edit_text_times.setText("5");
		}

		if (Integer.parseInt(edit_text_times.getText().toString()) < 1) {
			appendResultsText(getString(R.string.packets_lower_than_1, edit_text_times.getText().toString()));
			appendResultsText(getString(R.string.resetting));
			edit_text_times.setText("5");
		}

		int ping_timeout = Integer.parseInt(edit_text_timeout.getText().toString());
		int ping_ttl = Integer.parseInt(edit_text_ttl.getText().toString());
		int ping_times = Integer.parseInt(edit_text_times.getText().toString());

		pingHandlerThread = new HandlerThread("BackgroundPingHandlerThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		pingHandlerThread.start();
		pingHandler = new Handler(pingHandlerThread.getLooper());

		pingHandler.post(() -> {
			try {
				String pingHostAddress = URLandIPConverter.convertUrl("https://" + url_ip);
				InetAddress inetAddress = InetAddress.getByName(url_ip);
				String pingHostname = inetAddress.getHostName();
				appendResultsText(String.format(getString(R.string.ping_log_ip), pingHostAddress));
				appendResultsText(String.format(getString(R.string.ping_log_hostname), pingHostname));
			} catch (UnknownHostException | MalformedURLException e) {
				e.printStackTrace();
				setEnabled(ping_button, true);
				setEnabled(ping_button_cancel, false);
				appendResultsText(lineSeparator);
			}
		});
		startPinger(url_ip, ping_timeout, ping_ttl, ping_times);
	}

	private void startPinger(String url_ip, int timeout, int ttl, int times) {
		runOnUiThread(() -> ping_progress_bar.setVisibility(View.VISIBLE));
		pinger = Ping.Companion.onAddress(url_ip).setTimeOutMillis(timeout).setDelayMillis(500).setTimeToLive(ttl).setTimes(times).doPing(new Ping.PingListener() {
			final long startTime = System.currentTimeMillis();
			@SuppressLint("DefaultLocale")
			@Override
			public void onResult(PingResult pingResult) {
				sentPackets++;
				if (pingResult.isReachable()) {
					appendResultsText(String.format(getString(R.string.ping_successful_response),
							sentPackets, pingResult.getAddress(), pingResult.getTimeTaken(), ttl));
				} else {
					appendResultsText(String.format(getString(R.string.connection_timeout), sentPackets));
				}
			}

			@SuppressLint("DefaultLocale")
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
				setEnabled(ping_button, true);
				setEnabled(ping_button_cancel, false);
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				appendResultsText(e.getMessage());
				sentPackets = 0;
				runOnUiThread(() -> ping_progress_bar.setVisibility(View.INVISIBLE));
				setEnabled(ping_button, true);
				setEnabled(ping_button_cancel, false);
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
		} else if (isSimCardPresent(this) && Objects.nonNull(cellularCheck.isConnected())) { // Cellular Connectivity Check
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
