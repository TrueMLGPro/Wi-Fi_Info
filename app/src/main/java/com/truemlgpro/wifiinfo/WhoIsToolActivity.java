package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import me.anwarshahriar.calligrapher.Calligrapher;
import thecollectiveweb.com.tcwhois.TCWHOIS;

public class WhoIsToolActivity extends AppCompatActivity {
	private TextView textview_nonetworkconn;
	private ProgressBar who_is_progress_bar;
	private TextInputLayout who_is_input_layout;
	private EditText who_is_edit_text;
	private TextView textview_who_is_results;
	private Button fetch_whois_info_button;
	private ScrollView who_is_scroll;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	private Menu toolbarWhoisMenu;

	private Bundle whoIsBundle = new Bundle();
	private static final String MSG_KEY = "WhoIsQuery";

	private static final int STATE_SUCCESS = 0;
	private static final int STATE_ERROR_MALFORMED_URL = 1;
	private static final int STATE_ERROR_UNKNOWN_HOST = 2;

	private static final int STATE_RUNNABLE_STARTED = 11;
	private static final int STATE_RUNNABLE_FINISHED = 12;

	private static final int MIN_TEXT_LENGTH = 4;
	private static final String EMPTY_STRING = "";

	private BroadcastReceiver NetworkConnectivityReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.who_is_tool_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		who_is_progress_bar = (ProgressBar) findViewById(R.id.who_is_progress_bar);
		who_is_input_layout = (TextInputLayout) findViewById(R.id.url_to_ip_input_layout);
		who_is_edit_text = (EditText) findViewById(R.id.url_to_ip_edit_text);
		fetch_whois_info_button = (Button) findViewById(R.id.fetch_whois_info_button);
		textview_who_is_results = (TextView) findViewById(R.id.textview_who_is_results);
		who_is_scroll = (ScrollView) findViewById(R.id.who_is_scroll);

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

		fetch_whois_info_button.setOnClickListener(v -> {
			if (!shouldShowError()) {
				startWhoIsThread();
				hideError();
			} else {
				showError();
			}
		});
	}

	private boolean shouldShowError() {
		int textLength = who_is_edit_text.getText().length();
		return textLength >= 0 && textLength < MIN_TEXT_LENGTH;
	}

	private void showError() {
		who_is_input_layout.setError(getString(R.string.field_too_short));
	}

	private void hideError() {
		who_is_input_layout.setError(EMPTY_STRING);
	}

	private final Handler msgHandler = new Handler(Looper.myLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case STATE_SUCCESS:
					whoIsBundle = msg.getData();
					String whoIsQuery = whoIsBundle.getString(MSG_KEY);
					appendResultsText(whoIsQuery);
					break;
				case STATE_ERROR_MALFORMED_URL:
					appendResultsText(getString(R.string.error_malformed_url));
					break;
				case STATE_ERROR_UNKNOWN_HOST:
					appendResultsText(getString(R.string.error_unknown_host));
					break;
				case STATE_RUNNABLE_STARTED:
					runOnUiThread(() -> who_is_progress_bar.setVisibility(View.VISIBLE));
					setEnabled(fetch_whois_info_button, false);
					break;
				case STATE_RUNNABLE_FINISHED:
					runOnUiThread(() -> who_is_progress_bar.setVisibility(View.INVISIBLE));
					setEnabled(fetch_whois_info_button, true);
					break;
			}
		}
	};

	private final Runnable msgSenderRunnable = new Runnable() {
		@Override
		public void run() {
			msgHandler.sendEmptyMessage(STATE_RUNNABLE_STARTED);
			try {
				String url = who_is_edit_text.getText().toString();
				String ip = URLandIPConverter.convertUrl("https://" + url);
				String fetched_whois_data = getWhoIsInfo(url);
				String lineSeparator = "\n----------------------------\n";
				String output = String.format(getString(R.string.whois_result_output), url, ip, fetched_whois_data, lineSeparator);
				Message msg = msgHandler.obtainMessage(STATE_SUCCESS);
				whoIsBundle.putString(MSG_KEY, output);
				msg.setData(whoIsBundle);
				msgHandler.sendMessage(msg);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				msgHandler.sendEmptyMessage(STATE_ERROR_MALFORMED_URL);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				msgHandler.sendEmptyMessage(STATE_ERROR_UNKNOWN_HOST);
			}
			msgHandler.sendEmptyMessage(STATE_RUNNABLE_FINISHED);
		}
	};

	private void startWhoIsThread() {
		new Thread(msgSenderRunnable).start();
	}

	public String getWhoIsInfo(String url) {
		String whoisData = "";
		try {
			TCWHOIS whoisClient = new TCWHOIS();
			whoisData = whoisClient.getTCWHOIS(url);
		} catch (InterruptedException e) {
			e.printStackTrace();
			appendResultsText(getString(R.string.error_interrupted));
		} catch (ExecutionException e) {
			e.printStackTrace();
			appendResultsText(getString(R.string.error_failed_to_execute));
		}
		return whoisData;
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> {
			if (view != null) {
				view.setEnabled(enabled);
			}
		});
	}

	class NetworkConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkNetworkConnectivity(true);
		}
	}

	public void checkNetworkConnectivity(Boolean shouldClearLog) {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo CellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (WiFiCheck.isConnected() && !CellularCheck.isConnected()) { // Wi-Fi Connectivity Check
			showWidgets();
			if (toolbarWhoisMenu != null) {
				if (!toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, true);
				}
			}
			wifi_connected = true;
			cellular_connected = false;
		} else if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) { // Cellular Connectivity Check
			showWidgets();
			if (toolbarWhoisMenu != null) {
				if (!toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, true);
				}
			}
			wifi_connected = false;
			cellular_connected = true;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			if (shouldClearLog) { textview_who_is_results.setText(""); }
			if (toolbarWhoisMenu != null) {
				if (toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, false);
				}
			}
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	public void showWidgets() {
		who_is_scroll.setVisibility(View.VISIBLE);
		textview_who_is_results.setVisibility(View.VISIBLE);
		who_is_input_layout.setVisibility(View.VISIBLE);
		who_is_edit_text.setVisibility(View.VISIBLE);
		fetch_whois_info_button.setVisibility(View.VISIBLE);
		who_is_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		who_is_scroll.setVisibility(View.GONE);
		textview_who_is_results.setVisibility(View.GONE);
		who_is_input_layout.setVisibility(View.GONE);
		who_is_edit_text.setVisibility(View.GONE);
		fetch_whois_info_button.setVisibility(View.GONE);
		who_is_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void appendResultsText(final String text) {
		runOnUiThread(() -> {
			textview_who_is_results.append(text + "\n");
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
		unregisterReceiver(NetworkConnectivityReceiver);
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
			textview_who_is_results.setText("");
		}
		return true;
	}
}
