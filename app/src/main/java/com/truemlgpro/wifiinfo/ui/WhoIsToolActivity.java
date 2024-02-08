package com.truemlgpro.wifiinfo.ui;

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
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.FontManager;
import com.truemlgpro.wifiinfo.utils.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.LocaleManager;
import com.truemlgpro.wifiinfo.utils.ThemeManager;
import com.truemlgpro.wifiinfo.utils.URLandIPConverter;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import thecollectiveweb.com.tcwhois.TCWHOIS;

public class WhoIsToolActivity extends AppCompatActivity {
	private TextView textview_nonetworkconn;
	private ProgressBar who_is_progress_bar;
	private TextInputLayout who_is_input_layout;
	private EditText who_is_edit_text;
	private TextView textview_who_is_results;
	private Button fetch_whois_info_button;
	private ScrollView who_is_scroll;

	private Menu toolbarWhoisMenu;

	private Bundle whoIsBundle = new Bundle();
	private static final String MSG_KEY = "WhoIsQuery";

	private static final int STATE_SUCCESS = 0;
	private static final int STATE_ERROR_MALFORMED_URL = 1;
	private static final int STATE_ERROR_UNKNOWN_HOST = 2;

	private static final int STATE_RUNNABLE_STARTED = 11;
	private static final int STATE_RUNNABLE_FINISHED = 12;

	private BroadcastReceiver NetworkConnectivityReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

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
		FontManager.init(this, getApplicationContext(), true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		actionbar.setTitle(getResources().getString(R.string.whois_tool));

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		fetch_whois_info_button.setOnClickListener(v -> {
			String whois_url_ip = who_is_edit_text.getText().toString();
			if (TextUtils.isEmpty(whois_url_ip)) {
				whois_url_ip = "google.com";
				who_is_edit_text.setText(whois_url_ip);
			}
			startWhoIsThread();
		});
	}

	private final Handler msgHandler = new Handler(Looper.myLooper()) {
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

			String url = who_is_edit_text.getText().toString();
			AtomicReference<String> ip = new AtomicReference<>("");
			URLandIPConverter.convertUrlToIp(url, ip::set);
			String fetched_whois_data = getWhoIsInfo(url);

			String lineSeparator = "\n----------------------------\n";
			String output = String.format(getString(R.string.whois_result_output), url, ip, fetched_whois_data, lineSeparator);

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

	class NetworkConnectivityReceiver extends BroadcastReceiver {
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
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo cellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifiCheck.isConnected()) { // Wi-Fi Connectivity Check
			showWidgets();
			if (toolbarWhoisMenu != null) {
				if (!toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, true);
				}
			}
		} else if (isSimCardPresent(this) && Objects.nonNull(cellularCheck) && cellularCheck.isConnected()) { // Cellular Connectivity Check
			showWidgets();
			if (toolbarWhoisMenu != null) {
				if (!toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, true);
				}
			}
		} else {
			if (shouldClearLog) { textview_who_is_results.setText(""); }
			if (toolbarWhoisMenu != null) {
				if (toolbarWhoisMenu.findItem(R.id.clear_whois_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_whois_log, false);
				}
			}
			hideWidgets();
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

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> {
			if (view != null) {
				view.setEnabled(enabled);
			}
		});
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
