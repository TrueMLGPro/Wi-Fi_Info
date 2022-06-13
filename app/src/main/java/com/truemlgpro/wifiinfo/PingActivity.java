package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import me.anwarshahriar.calligrapher.Calligrapher;

public class PingActivity extends AppCompatActivity
{

	private TextView textview_nonetworkconn;
	private TextInputLayout textInputLayoutPing;
	private TextInputLayout textInputLayoutTimeout;
	private TextInputLayout textInputLayoutTTL;
	private TextInputLayout textInputLayoutTimes;
	private EditText edit_text_ping;
	private EditText edit_text_timeout;
	private EditText edit_text_ttl;
	private EditText edit_text_times;
	private LinearLayout layout_ping_results;
	private ScrollView ping_results_scroll;
	private TextView ping_text;
	private Button ping_button;
	private Button ping_button_cancel;
	
	private BroadcastReceiver NetworkConnectivityReceiver;
	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private NetworkInfo CellularCheck;
	
	private Ping pinger;

	private HandlerThread pingHandlerThread;
	private Handler pingHandler;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	private String url_ip = "";
	private final String lineSeparator = "\n----------------------------\n";

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
		setContentView(R.layout.ping_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		textInputLayoutPing = (TextInputLayout) findViewById(R.id.input_layout_ping);
		edit_text_ping = (EditText) findViewById(R.id.edit_text_ping);
		textInputLayoutTimeout = (TextInputLayout) findViewById(R.id.input_layout_timeout);
		edit_text_timeout = (EditText) findViewById(R.id.edit_text_timeout);
		textInputLayoutTTL = (TextInputLayout) findViewById(R.id.input_layout_ttl);
		edit_text_ttl = (EditText) findViewById(R.id.edit_text_ttl);
		textInputLayoutTimes = (TextInputLayout) findViewById(R.id.input_layout_times);
		edit_text_times = (EditText) findViewById(R.id.edit_text_times);
		ping_button = (Button) findViewById(R.id.ping_button);
		ping_button_cancel = (Button) findViewById(R.id.ping_button_cancel);
		layout_ping_results = (LinearLayout) findViewById(R.id.layout_ping_results);
		ping_results_scroll = (ScrollView) findViewById(R.id.ping_scroll);
		ping_text = (TextView) findViewById(R.id.ping_textview);
		
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
		
		ping_button.setOnClickListener(v -> preparePinger());
		
		ping_button_cancel.setOnClickListener(v -> {
			if (pinger != null) {
				pinger.cancel();
			}
		});
	}
	
	private String getGateway() {
		if (!WiFiCheck.isConnected()) {
			return "0.0.0.0";
		}
		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifi.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}

	private void preparePinger() {
		setEnabled(ping_button, false);
		setEnabled(ping_button_cancel, true);
		url_ip = edit_text_ping.getText().toString();

		if (TextUtils.isEmpty(url_ip)) {
			if (wifi_connected) {
				url_ip = getGateway();
				Toast.makeText(getBaseContext(), "No IP or URL given...\nUsing Gateway IP: " + getGateway(), Toast.LENGTH_LONG).show();
			} else if (cellular_connected) {
				url_ip = "google.com";
				Toast.makeText(getBaseContext(), "No IP or URL given...\nUsing Google URL: " + url_ip, Toast.LENGTH_LONG).show();
			}
		}

		if (!TextUtils.isEmpty(edit_text_timeout.getText().toString())) {
			if (Integer.parseInt(edit_text_timeout.getText().toString()) < 1) {
				appendResultsText("Timeout value cannot be lower than 1 ms");
				appendResultsText("Changing timeout to the default value");
				edit_text_timeout.setText("3000");
			}
		} else {
			appendResultsText("Timeout text field cannot be empty");
			appendResultsText("Changing timeout to the default value");
			edit_text_timeout.setText("3000");
		}

		if (!TextUtils.isEmpty(edit_text_ttl.getText().toString())) {
			if (Integer.parseInt(edit_text_ttl.getText().toString()) < 1) {
				appendResultsText("TTL value cannot be lower than 1");
				appendResultsText("Changing it to the default value");
				edit_text_ttl.setText("30");
			}
		} else {
			appendResultsText("TTL text field cannot be empty");
			appendResultsText("Changing it to the default value");
			edit_text_ttl.setText("30");
		}

		if (!TextUtils.isEmpty(edit_text_times.getText().toString())) {
			if (Integer.parseInt(edit_text_times.getText().toString()) < 1) {
				appendResultsText("You cannot ping a host " + edit_text_times.getText().toString() + " times");
				appendResultsText("Changing it to the default value");
				edit_text_times.setText("5");
			}
		} else {
			appendResultsText("You cannot ping a host if you don't define how many packets you want to send");
			appendResultsText("Changing it to the default value");
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
				appendResultsText("Pinging IP: " + pingHostAddress);
				appendResultsText("Hostname: " + pingHostname);
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
		pinger = Ping.onAddress(url_ip).setTimeOutMillis(timeout).setDelayMillis(500).setTimeToLive(ttl).setTimes(times).doPing(new Ping.PingListener() {
			@Override
			public void onResult(PingResult pingResult) {
				if (pingResult.isReachable()) {
					appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));
				} else {
					appendResultsText("Connection Timeout");
				}
			}

			@Override
			public void onFinished(PingStats pingStats) {
				appendResultsText(String.format("Pings: %d, Packets lost: %d",
						pingStats.getNoPings(), pingStats.getPacketsLost()));
				appendResultsText(String.format("Min / Avg / Max Latency:\n%.2f / %.2f / %.2f ms",
						pingStats.getMinTimeTaken(), pingStats.getAverageTimeTaken(), pingStats.getMaxTimeTaken()));
				appendResultsText(lineSeparator);
				setEnabled(ping_button, true);
				setEnabled(ping_button_cancel, false);
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				appendResultsText(e.getMessage());
				setEnabled(ping_button, true);
				setEnabled(ping_button_cancel, false);
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
			ping_text.setText("...\n");
			edit_text_ping.setText("");
			wifi_connected = true;
			cellular_connected = false;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			ping_text.setText("...\n");
			edit_text_ping.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}

		// Cellular Connectivity Check

		if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			showWidgets();
			ping_text.setText("...\n");
			edit_text_ping.setText("");
			wifi_connected = false;
			cellular_connected = true;
		} else if (!CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			ping_text.setText("...\n");
			edit_text_ping.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}
	
	public void showWidgets() {
		ping_text.setVisibility(View.VISIBLE);
		ping_button.setVisibility(View.VISIBLE);
		ping_button_cancel.setVisibility(View.VISIBLE);
		textInputLayoutPing.setVisibility(View.VISIBLE);
		textInputLayoutTimeout.setVisibility(View.VISIBLE);
		textInputLayoutTTL.setVisibility(View.VISIBLE);
		textInputLayoutTimes.setVisibility(View.VISIBLE);
		layout_ping_results.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		ping_text.setVisibility(View.GONE);
		ping_button.setVisibility(View.GONE);
		ping_button_cancel.setVisibility(View.GONE);
		textInputLayoutPing.setVisibility(View.GONE);
		textInputLayoutTimeout.setVisibility(View.GONE);
		textInputLayoutTTL.setVisibility(View.GONE);
		textInputLayoutTimes.setVisibility(View.GONE);
		layout_ping_results.setVisibility(View.GONE);
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
	        ping_results_scroll.post(() -> ping_results_scroll.fullScroll(View.FOCUS_DOWN));
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.ping_tool_action_bar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.clear_ping_log) {
			ping_text.setText("...\n");
		}
		return true;
	}
}
