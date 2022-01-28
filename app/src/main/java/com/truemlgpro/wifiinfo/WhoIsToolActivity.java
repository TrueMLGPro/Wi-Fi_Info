package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
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
	private TextView textview_who_is_text;
	private TextView textview_nonetworkconn;
	private Button convert_button;
	private LinearLayout layout_who_is_results;
	private ScrollView who_is_scroll;
	private Toolbar toolbar;

	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private NetworkInfo CellularCheck;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

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
		convert_button = (Button) findViewById(R.id.convert_button);
		textview_who_is_text = (TextView) findViewById(R.id.textview_who_is_text);
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

		convert_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!shouldShowError()) {
					getWhoIsInfo();
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

	private void scrollDown() {
		who_is_scroll.post(new Runnable() {
			@Override
			public void run() {
				who_is_scroll.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	public void getWhoIsInfo() {
		TCWHOIS whois_client = new TCWHOIS();
		try {
			String url = edittext_main.getText().toString();
			String whois_data = whois_client.getTCWHOIS(url);
			String ip = URLandIPConverter.convertUrl("https://" + url);
			appendResultsText("Getting Whois data for URL: " + url);
			appendResultsText(("IP: " + ip));
			appendResultsText(whois_data);
		} catch (InterruptedException e) {
			e.printStackTrace();
			appendResultsText("Error: Interrupted");
		} catch (ExecutionException e) {
			e.printStackTrace();
			appendResultsText("Error: Failed to execute");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			appendResultsText("Error: Unknown Host");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			appendResultsText("Error: Malformed URL");
		}
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
			textview_who_is_text.setText("...\n");
			edittext_main.setText("");
			wifi_connected = true;
			cellular_connected = false;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			textview_who_is_text.setText("...\n");
			edittext_main.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}

		// Cellular Connectivity Check

		if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			showWidgets();
			textview_who_is_text.setText("...\n");
			edittext_main.setText("");
			wifi_connected = false;
			cellular_connected = true;
		} else if (!CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			textview_who_is_text.setText("...\n");
			edittext_main.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	public void showWidgets() {
		textview_who_is_text.setVisibility(View.VISIBLE);
		layout_who_is_results.setVisibility(View.VISIBLE);
		input_layout.setVisibility(View.VISIBLE);
		edittext_main.setVisibility(View.VISIBLE);
		convert_button.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		textview_who_is_text.setVisibility(View.GONE);
		layout_who_is_results.setVisibility(View.GONE);
		input_layout.setVisibility(View.GONE);
		edittext_main.setVisibility(View.GONE);
		convert_button.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void appendResultsText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textview_who_is_text.append(text + "\n");
				who_is_scroll.post(new Runnable() {
					@Override
					public void run() {
						who_is_scroll.fullScroll(View.FOCUS_DOWN);
					}
				});
			}
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
