package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import me.anwarshahriar.calligrapher.Calligrapher;

public class URLtoIPActivity extends AppCompatActivity {
    private TextInputLayout mTextInputLayout;
    private EditText mEditText;
	private TextView textview_ipFromURL;
	private TextView textview_nonetworkconn;
	private Button convert_button;
	private ScrollView url_to_ip_scroll;

	private Menu toolbarURLtoIPToolMenu;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	private static final int MIN_TEXT_LENGTH = 4;
	private static final String EMPTY_STRING = "";
	
	private BroadcastReceiver NetworkConnectivityReceiver;

	private final String lineSeparator = "\n----------------------------\n";

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
		new ThemeManager().initializeThemes(this, getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_to_ip_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextInputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        mEditText = (EditText) findViewById(R.id.edittext_main);
		convert_button = (Button) findViewById(R.id.convert_button);
		textview_ipFromURL = (TextView) findViewById(R.id.textview_ipFromURL);
		url_to_ip_scroll = (ScrollView) findViewById(R.id.url_to_ip_scroll);
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

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		convert_button.setOnClickListener(v -> {
			if (!shouldShowError()) {
				String url = mEditText.getText().toString();
				new Thread(() -> {
					try {
						String ip = URLandIPConverter.convertUrl("https://" + url);
						appendResultsText("Converting URL: " + url);
						appendResultsText("IP: " + ip);
						appendResultsText(lineSeparator);
					} catch (MalformedURLException e) {
						e.printStackTrace();
						appendResultsText("Converting URL: " + url);
						appendResultsText("Error: Malformed URL");
						appendResultsText(lineSeparator);
					} catch (UnknownHostException e) {
						e.printStackTrace();
						appendResultsText("Converting URL: " + url);
						appendResultsText("Error: Unknown Host");
						appendResultsText(lineSeparator);
					}
				}).start();
				hideError();
			} else {
				showError();
			}
		});

		checkNetworkConnectivity(false);

    }

    private boolean shouldShowError() {
        int textLength = mEditText.getText().length();
        return textLength >= 0 && textLength < MIN_TEXT_LENGTH;
    }

    private void showError() {
        mTextInputLayout.setError("Field is too short...");
    }

    private void hideError() {
        mTextInputLayout.setError(EMPTY_STRING);
    }
	
	class NetworkConnectivityReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			checkNetworkConnectivity(false);
		}
	}

	public void checkNetworkConnectivity(Boolean calledFromToolbarAction) {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo CellularCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (!calledFromToolbarAction) {
			textview_ipFromURL.setText("...\n");
			mEditText.setText("");
		}

		// WI-FI Connectivity Check

		if (WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			showWidgets();
			if (toolbarURLtoIPToolMenu != null) {
				if (!toolbarURLtoIPToolMenu.findItem(R.id.clear_url_to_ip_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_url_to_ip_log, true);
				}
			}
			wifi_connected = true;
			cellular_connected = false;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			hideWidgets();
			if (toolbarURLtoIPToolMenu != null) {
				if (toolbarURLtoIPToolMenu.findItem(R.id.clear_url_to_ip_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_url_to_ip_log, false);
				}
			}
			wifi_connected = false;
			cellular_connected = false;
		}

		// Cellular Connectivity Check

		if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			showWidgets();
			if (toolbarURLtoIPToolMenu != null) {
				if (!toolbarURLtoIPToolMenu.findItem(R.id.clear_url_to_ip_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_url_to_ip_log, true);
				}
			}
			wifi_connected = false;
			cellular_connected = true;
		} else if (!CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			hideWidgets();
			if (toolbarURLtoIPToolMenu != null) {
				if (toolbarURLtoIPToolMenu.findItem(R.id.clear_url_to_ip_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_url_to_ip_log, false);
				}
			}
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	public void showWidgets() {
		url_to_ip_scroll.setVisibility(View.VISIBLE);
		textview_ipFromURL.setVisibility(View.VISIBLE);
		mTextInputLayout.setVisibility(View.VISIBLE);
		mEditText.setVisibility(View.VISIBLE);
		convert_button.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		url_to_ip_scroll.setVisibility(View.GONE);
		textview_ipFromURL.setVisibility(View.GONE);
		mTextInputLayout.setVisibility(View.GONE);
		mEditText.setVisibility(View.GONE);
		convert_button.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}
	
	private void appendResultsText(final String text) {
        runOnUiThread(() -> {
	        textview_ipFromURL.append(text + "\n");
	        url_to_ip_scroll.post(() -> url_to_ip_scroll.fullScroll(View.FOCUS_DOWN));
        });
    }

	@Override
	protected void onStart()
	{
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new NetworkConnectivityReceiver();
		registerReceiver(NetworkConnectivityReceiver, filter);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		unregisterReceiver(NetworkConnectivityReceiver);
	}

	private void setToolbarItemEnabled(int item, Boolean enabled) {
		if (toolbarURLtoIPToolMenu != null) {
			toolbarURLtoIPToolMenu.findItem(item).setEnabled(enabled);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.url_to_ip_tool_action_bar_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toolbarURLtoIPToolMenu = menu;
		checkNetworkConnectivity(true);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.clear_url_to_ip_log) {
			textview_ipFromURL.setText("...\n");
		}
		return true;
	}
}
