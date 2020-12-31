package com.truemlgpro.wifiinfo;

import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.stealthcopter.networktools.*;
import com.stealthcopter.networktools.ping.*;
import java.net.*;
import me.anwarshahriar.calligrapher.*;

import android.support.v7.widget.Toolbar;

public class PingActivity extends AppCompatActivity
{
	
	private Toolbar toolbar;
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

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Boolean keyTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_SWITCH, MainActivity.darkMode);
		Boolean keyAmoledTheme = new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(SettingsActivity.KEY_PREF_AMOLED_CHECK, MainActivity.amoledMode);

		if (keyTheme == true) {
			setTheme(R.style.DarkTheme);
		}

		if (keyAmoledTheme == true) {
			if (keyTheme == true) {
				setTheme(R.style.AmoledDarkTheme);
			}
		}

		if (keyTheme == false) {
			setTheme(R.style.LightTheme);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ping_activity);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
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

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Back button pressed
					finish();
				}
			});
		
		ping_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ping();
			}
		});
		
		ping_button_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pinger.cancel();
			}
		});
	}
	
	// Native ping method (not used, unstable) //
	
//	public static String ping(String url) {
//		String str = "";
//		try {
//			java.lang.Process process = Runtime.getRuntime().exec("ping -c 1 " + url);
//			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			int i;
//			char[] buffer = new char[4096];
//			StringBuffer output = new StringBuffer();
//			String op[] = new String[64];
//			String delay[] = new String[8];
//			while ((i = reader.read(buffer)) > 0) {
//				output.append(buffer, 0, i);
//				reader.close();
//				op = output.toString().split("\n");
//				delay = op[1].split("time=");
//				str = delay[1];
//			}
//			reader.close();
//			process.getInputStream().close();
//			process.getOutputStream().close();
//			process.getErrorStream().close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return str;
//	}
	
	private String getGateway() {
		if (!WiFiCheck.isConnected()) {
			return "0.0.0.0";
		}
		WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifi.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}
	
	public void ping() {
		setEnabled(ping_button, false);
		setEnabled(ping_button_cancel, true);
		String url_ip = edit_text_ping.getText().toString();

		if (TextUtils.isEmpty(url_ip)) {
			if (wifi_connected) {
				url_ip = getGateway();
				Toast.makeText(getBaseContext(), "No IP or URL given...\nUsing Gateway IP: " + getGateway(), Toast.LENGTH_LONG).show();
			} else if (cellular_connected) {
				url_ip = "google.com";
				Toast.makeText(getBaseContext(), "No IP or URL given...\nUsing Google URL: " + url_ip, Toast.LENGTH_LONG).show();
			}
		}
		
		int ping_timeout = Integer.parseInt(edit_text_timeout.getText().toString());
		int ping_ttl = Integer.parseInt(edit_text_ttl.getText().toString());
		int ping_times = Integer.parseInt(edit_text_times.getText().toString());
		
		PingResult pingResultInfo = null;
		try {
			pingResultInfo = Ping.onAddress(url_ip).setTimeOutMillis(ping_timeout).doPing();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			appendResultsText(e.getMessage());
            setEnabled(ping_button, true);
			setEnabled(ping_button_cancel, false);
            return;
		}
		
		appendResultsText("Pinging IP: " + pingResultInfo.getAddress().getHostAddress());
        appendResultsText("Hostname: " + pingResultInfo.getAddress().getHostName());
		
		try {
			pinger = Ping.onAddress(url_ip).setTimeOutMillis(ping_timeout).setDelayMillis(500).setTimeToLive(ping_ttl).setTimes(ping_times).doPing(new Ping.PingListener() {
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
					setEnabled(ping_button, true);
					setEnabled(ping_button_cancel, false);
				}

				@Override
				public void onError(Exception e) {
					e.printStackTrace();
					setEnabled(ping_button, true);
					setEnabled(ping_button_cancel, false);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			appendResultsText(e.getMessage());
			setEnabled(ping_button, true);
			setEnabled(ping_button_cancel, false);
			return;
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
        runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (view != null) {
					view.setEnabled(enabled);
				}
			}
		});
    }
	
	private void appendResultsText(final String text) {
        runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ping_text.append(text + "\n");
				ping_results_scroll.post(new Runnable() {
					@Override
					public void run() {
						ping_results_scroll.fullScroll(View.FOCUS_DOWN);
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
		NetworkConnectivityReceiver = new NetworkConnectivityReceiver();
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
