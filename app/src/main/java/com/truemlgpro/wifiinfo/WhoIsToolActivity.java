package com.truemlgpro.wifiinfo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import me.anwarshahriar.calligrapher.Calligrapher;
import thecollectiveweb.com.tcwhois.TCWHOIS;

public class WhoIsToolActivity extends AppCompatActivity {

	private static final int MIN_TEXT_LENGTH = 4;
	private static final String EMPTY_STRING = "";

	private TextInputLayout input_layout;
	private EditText edittext_main;
	private TextView textview_who_is_results;
	private TextView textview_nonetworkconn;
	private Button fetch_whois_info_button;
	private LinearLayout layout_who_is_results;
	private ScrollView who_is_scroll;
	private Toolbar toolbar;

	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private NetworkInfo CellularCheck;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

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
		Boolean keyTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, MainActivity.darkMode);
		Boolean keyAmoledTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, MainActivity.amoledMode);

		if (keyTheme) {
			setTheme(R.style.DarkTheme);
		}

		if (keyAmoledTheme) {
			if (keyTheme) {
				setTheme(R.style.AmoledDarkTheme);
			}
		}

		if (!keyTheme) {
			setTheme(R.style.LightTheme);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.who_is_tool_activity);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		input_layout = (TextInputLayout) findViewById(R.id.input_layout);
		edittext_main = (EditText) findViewById(R.id.edittext_main);
		fetch_whois_info_button = (Button) findViewById(R.id.fetch_whois_info_button);
		textview_who_is_results = (TextView) findViewById(R.id.textview_who_is_results);
		layout_who_is_results = (LinearLayout) findViewById(R.id.layout_who_is_results);
		who_is_scroll = (ScrollView) findViewById(R.id.who_is_scroll);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);

		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, MainActivity.appFont);
		calligrapher.setFont(this, font, true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Back button pressed
				finish();
			}
		});

		fetch_whois_info_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!shouldShowError()) {
					startWhoIsThread();
					hideError();
				} else {
					showError();
				}
			}
		});
	}

	private boolean shouldShowError() {
		int textLength = edittext_main.getText().length();
		return textLength >= 0 && textLength < MIN_TEXT_LENGTH;
	}

	private void showError() {
		input_layout.setError("Field is too short...");
	}

	private void hideError() {
		input_layout.setError(EMPTY_STRING);
	}

	@SuppressLint("HandlerLeak")
	private final Handler msgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case STATE_SUCCESS:
					whoIsBundle = msg.getData();
					String whoIsQuery = whoIsBundle.getString(MSG_KEY);
					appendResultsText(whoIsQuery);
					break;
				case STATE_ERROR_MALFORMED_URL:
					appendResultsText("Error: Malformed URL");
					break;
				case STATE_ERROR_UNKNOWN_HOST:
					appendResultsText("Error: Unknown Host");
					break;
				case STATE_RUNNABLE_STARTED:
					setEnabled(fetch_whois_info_button, false);
					break;
				case STATE_RUNNABLE_FINISHED:
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
				String url = edittext_main.getText().toString();
				String ip = URLandIPConverter.convertUrl("https://" + url);
				String fetched_whois_data = getWhoIsInfo(url);
				String output = "Getting Whois data for URL: " + url + "\n"
						+ "IP: " + ip + "\n"
						+ fetched_whois_data + "\n"
						+ "----------------------------------------" + "\n";
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
		String whois_data = "";
		try {
			TCWHOIS whois_client = new TCWHOIS();
			whois_data = whois_client.getTCWHOIS(url);
		} catch (InterruptedException e) {
			e.printStackTrace();
			appendResultsText("Error: Interrupted");
		} catch (ExecutionException e) {
			e.printStackTrace();
			appendResultsText("Error: Failed to execute");
		}
		return whois_data;
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> {
			if (view != null) {
				view.setEnabled(enabled);
			}
		});
	}

	class NetworkConnectivityReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			checkNetworkConnectivity();
		}
	}

	public void checkNetworkConnectivity() {
		CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		CellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		// WI-FI Connectivity Check

		if (WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			showWidgets();
			textview_who_is_results.setText("...\n");
			edittext_main.setText("");
			wifi_connected = true;
			cellular_connected = false;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			textview_who_is_results.setText("...\n");
			edittext_main.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}

		// Cellular Connectivity Check

		if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			showWidgets();
			textview_who_is_results.setText("...\n");
			edittext_main.setText("");
			wifi_connected = false;
			cellular_connected = true;
		} else if (!CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			textview_who_is_results.setText("...\n");
			edittext_main.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	public void showWidgets() {
		textview_who_is_results.setVisibility(View.VISIBLE);
		layout_who_is_results.setVisibility(View.VISIBLE);
		input_layout.setVisibility(View.VISIBLE);
		edittext_main.setVisibility(View.VISIBLE);
		fetch_whois_info_button.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		textview_who_is_results.setVisibility(View.GONE);
		layout_who_is_results.setVisibility(View.GONE);
		input_layout.setVisibility(View.GONE);
		edittext_main.setVisibility(View.GONE);
		fetch_whois_info_button.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void appendResultsText(final String text) {
		runOnUiThread(() -> {
			textview_who_is_results.append(text + "\n");
			who_is_scroll.post(() -> who_is_scroll.fullScroll(View.FOCUS_DOWN));
		});
	}

	@Override
	protected void onStart()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new WhoIsToolActivity.NetworkConnectivityReceiver();
		registerReceiver(NetworkConnectivityReceiver, filter);
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		unregisterReceiver(NetworkConnectivityReceiver);
		super.onStop();
	}
}
