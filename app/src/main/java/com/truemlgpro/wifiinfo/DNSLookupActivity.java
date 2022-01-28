package com.truemlgpro.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import me.anwarshahriar.calligrapher.Calligrapher;
import org.minidns.hla.ResolverApi;
import org.minidns.hla.ResolverResult;
import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties;
import org.minidns.record.Data;
import org.minidns.record.Record;

import java.io.IOException;
import java.util.Set;

public class DNSLookupActivity extends AppCompatActivity {

	private Toolbar toolbar;
	private TextView textview_nonetworkconn;
	private Button get_dns_info_button;
	private TextInputLayout input_layout_dns;
	private EditText edit_text_dns;
	private Spinner spinner_dns_record_types;
	private LinearLayout layout_dns_lookup_results;
	private ScrollView dns_lookup_results_scroll;
	private TextView dns_lookup_textview;

	private BroadcastReceiver NetworkConnectivityReceiver;
	private ConnectivityManager CM;
	private NetworkInfo WiFiCheck;
	private NetworkInfo CellularCheck;

	private String url_ip;
	private String dns_record_type;

	public Boolean wifi_connected;
	public Boolean cellular_connected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		setContentView(R.layout.dns_lookup_activity);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		get_dns_info_button = (Button) findViewById(R.id.get_dns_info_button);
		input_layout_dns = (TextInputLayout) findViewById(R.id.input_layout_dns);
		edit_text_dns = (EditText) findViewById(R.id.edit_text_dns);
		spinner_dns_record_types = (Spinner) findViewById(R.id.spinner_dns_record_types);
		layout_dns_lookup_results = (LinearLayout) findViewById(R.id.layout_dns_lookup_results);
		dns_lookup_results_scroll = (ScrollView) findViewById(R.id.dns_lookup_results_scroll);
		dns_lookup_textview = (TextView) findViewById(R.id.dns_lookup_textview);

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

		get_dns_info_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				url_ip = edit_text_dns.getText().toString();
				if (TextUtils.isEmpty(url_ip)) {
					url_ip = "google.com";
					Toast.makeText(getBaseContext(), "No IP or URL given...\nUsing Google URL: " + url_ip, Toast.LENGTH_LONG).show();
				}
				startDnsLookup();
			}
		});

		spinner_dns_record_types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				dns_record_type = spinner_dns_record_types.getSelectedItem().toString();
			}
			public void onNothingSelected(AdapterView<?> parent) {
				dns_record_type = parent.getItemAtPosition(0).toString();
			}
		});
	}

	public void startDnsLookup() {
		DNSLookupTask dnsLookupTask = new DNSLookupTask();
		dnsLookupTask.execute();
	}

	class DNSLookupTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setEnabled(get_dns_info_button, false);
		}

		@Override
		protected String doInBackground(String... params) {
			ResolverResult<? extends Data> result;
			try {
				AndroidUsingLinkProperties.setup(getApplicationContext());
				Class<Data> recordDataClass = Record.TYPE.valueOf(dns_record_type).getDataClass();
				if (recordDataClass == null) {
					return "Record type " + dns_record_type + " is not supported" + "\n-----------------";
				}
				result = ResolverApi.INSTANCE.resolve(url_ip, recordDataClass);
			} catch (IOException e) {
				return "Failed to perform a lookup for record type " + dns_record_type + " | Error message: " + e.getMessage() + "\n-----------------";
			}

			if (!result.wasSuccessful()) {
				return "Failed to perform a lookup for record type " + dns_record_type + " | Response code: " + result.getResponseCode() + "\n-----------------";
			}

			Set<? extends Data> answers = result.getAnswers();
			if (answers.isEmpty()) {
				return "No records available for record type " + dns_record_type + "\n-----------------";
			}

			StringBuilder out = new StringBuilder();
			out.append("DNS Record Type - ").append(dns_record_type).append("\nURL/IP: ").append(url_ip).append("\n-----------------").append("\n");
			for (Data answer : answers) {
				out.append(answer).append("\n");
			}

			return out.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			setEnabled(get_dns_info_button, true);
			appendResultsText(result);
		}
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
				dns_lookup_textview.append(text + "\n");
				dns_lookup_results_scroll.post(new Runnable() {
					@Override
					public void run() {
						dns_lookup_results_scroll.fullScroll(View.FOCUS_DOWN);
					}
				});
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
			dns_lookup_textview.setText("...\n");
			edit_text_dns.setText("");
			wifi_connected = true;
			cellular_connected = false;
		} else if (!WiFiCheck.isConnected() && !CellularCheck.isConnected()) {
			dns_lookup_textview.setText("...\n");
			edit_text_dns.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}

		// Cellular Connectivity Check

		if (CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			showWidgets();
			dns_lookup_textview.setText("...\n");
			edit_text_dns.setText("");
			wifi_connected = false;
			cellular_connected = true;
		} else if (!CellularCheck.isConnected() && !WiFiCheck.isConnected()) {
			dns_lookup_textview.setText("...\n");
			edit_text_dns.setText("");
			hideWidgets();
			wifi_connected = false;
			cellular_connected = false;
		}
	}

	public void showWidgets() {
		dns_lookup_textview.setVisibility(View.VISIBLE);
		get_dns_info_button.setVisibility(View.VISIBLE);
		spinner_dns_record_types.setVisibility(View.VISIBLE);
		input_layout_dns.setVisibility(View.VISIBLE);
		layout_dns_lookup_results.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		dns_lookup_textview.setVisibility(View.GONE);
		get_dns_info_button.setVisibility(View.GONE);
		spinner_dns_record_types.setVisibility(View.GONE);
		input_layout_dns.setVisibility(View.GONE);
		layout_dns_lookup_results.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStart()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new DNSLookupActivity.NetworkConnectivityReceiver();
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
