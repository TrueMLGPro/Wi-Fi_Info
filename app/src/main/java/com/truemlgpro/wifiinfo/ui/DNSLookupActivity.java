package com.truemlgpro.wifiinfo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
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

import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties;
import org.minidns.hla.ResolverApi;
import org.minidns.hla.ResolverResult;
import org.minidns.record.Data;
import org.minidns.record.Record;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class DNSLookupActivity extends AppCompatActivity {
	private TextView textview_nonetworkconn;
	private ProgressBar dns_lookup_progress_bar;
	private Button get_dns_info_button;
	private TextInputLayout input_layout_dns;
	private EditText edit_text_dns;
	private Spinner spinner_dns_record_types;
	private ScrollView dns_lookup_results_scroll;
	private TextView dns_lookup_textview;

	private Menu toolbarDnsMenu;

	private BroadcastReceiver NetworkConnectivityReceiver;

	private String url_ip;
	private String dns_record_type;

	final String lineSeparator = "\n---------------------\n";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.dns_lookup_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		dns_lookup_progress_bar = (ProgressBar) findViewById(R.id.dns_lookup_progress_bar);
		get_dns_info_button = (Button) findViewById(R.id.get_dns_info_button);
		input_layout_dns = (TextInputLayout) findViewById(R.id.input_layout_dns);
		edit_text_dns = (EditText) findViewById(R.id.edit_text_dns);
		spinner_dns_record_types = (Spinner) findViewById(R.id.spinner_dns_record_types);
		dns_lookup_results_scroll = (ScrollView) findViewById(R.id.dns_lookup_results_scroll);
		dns_lookup_textview = (TextView) findViewById(R.id.dns_lookup_textview);

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		FontManager.init(this, getApplicationContext(), true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		actionbar.setTitle(getResources().getString(R.string.dns_lookup));

		toolbar.setNavigationOnClickListener(v -> {
			// Back button pressed
			finish();
		});

		get_dns_info_button.setOnClickListener(v -> {
			url_ip = edit_text_dns.getText().toString();
			if (TextUtils.isEmpty(url_ip)) {
				url_ip = "google.com";
				edit_text_dns.setText(url_ip);
			}
			startDnsLookup();
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
			setEnabled(edit_text_dns, false);
			dns_lookup_progress_bar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			ResolverResult<? extends Data> result;
			try {
				AndroidUsingLinkProperties.setup(getApplicationContext());
				Class<Data> recordDataClass = Record.TYPE.valueOf(dns_record_type).getDataClass();
				if (recordDataClass == null) {
					return String.format(getString(R.string.record_type_not_supported), dns_record_type, lineSeparator);
				}
				result = ResolverApi.INSTANCE.resolve(url_ip, recordDataClass);
			} catch (IOException e) {
				return String.format(getString(R.string.dns_lookup_failed_exception), dns_record_type, e.getMessage(), lineSeparator);
			}

			if (!result.wasSuccessful()) {
				return String.format(getString(R.string.dns_lookup_failed_response), dns_record_type, result.getResponseCode(), lineSeparator);
			}

			Set<? extends Data> answers = result.getAnswers();
			if (answers.isEmpty()) {
				return String.format(getString(R.string.dns_lookup_no_records), dns_record_type, lineSeparator);
			}

			StringBuilder out = new StringBuilder();
			out.append(getString(R.string.dns_record_type)).append(dns_record_type).append("\n")
				.append(getString(R.string.url_ip)).append(url_ip).append(lineSeparator);
			for (Data answer: answers) {
				out.append(answer).append("\n");
			}
			return out.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			setEnabled(get_dns_info_button, true);
			setEnabled(edit_text_dns, true);
			dns_lookup_progress_bar.setVisibility(View.INVISIBLE);
			appendResultsText(result);
		}
	}

	private void appendResultsText(final String text) {
		runOnUiThread(() -> {
			dns_lookup_textview.append(text + "\n");
			dns_lookup_results_scroll.post(() -> {
				View lastChild = dns_lookup_results_scroll.getChildAt(dns_lookup_results_scroll.getChildCount() - 1);
				int bottom = lastChild.getBottom() + dns_lookup_results_scroll.getPaddingBottom();
				int sy = dns_lookup_results_scroll.getScrollY();
				int sh = dns_lookup_results_scroll.getHeight();
				int delta = bottom - (sy + sh);

				dns_lookup_results_scroll.smoothScrollBy(0, delta);
			});
		});
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
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo cellularCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifiCheck.isConnected()) { // Wi-Fi Connectivity Check
			showWidgets();
			if (toolbarDnsMenu != null) {
				if (!toolbarDnsMenu.findItem(R.id.clear_dns_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_dns_log, true);
				}
			}
		} else if (isSimCardPresent(this) && Objects.nonNull(cellularCheck) && cellularCheck.isConnected()) { // Cellular Connectivity Check
			showWidgets();
			if (toolbarDnsMenu != null) {
				if (!toolbarDnsMenu.findItem(R.id.clear_dns_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_dns_log, true);
				}
			}
		} else {
			if (shouldClearLog) { dns_lookup_textview.setText(""); }
			if (toolbarDnsMenu != null) {
				if (toolbarDnsMenu.findItem(R.id.clear_dns_log).isEnabled()) {
					setToolbarItemEnabled(R.id.clear_dns_log, false);
				}
			}
			hideWidgets();
		}
	}

	public void showWidgets() {
		dns_lookup_textview.setVisibility(View.VISIBLE);
		get_dns_info_button.setVisibility(View.VISIBLE);
		spinner_dns_record_types.setVisibility(View.VISIBLE);
		input_layout_dns.setVisibility(View.VISIBLE);
		dns_lookup_results_scroll.setVisibility(View.VISIBLE);
		dns_lookup_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		dns_lookup_textview.setVisibility(View.GONE);
		get_dns_info_button.setVisibility(View.GONE);
		spinner_dns_record_types.setVisibility(View.GONE);
		input_layout_dns.setVisibility(View.GONE);
		dns_lookup_results_scroll.setVisibility(View.GONE);
		dns_lookup_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> {
			if (view != null) {
				view.setEnabled(enabled);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		NetworkConnectivityReceiver = new DNSLookupActivity.NetworkConnectivityReceiver();
		registerReceiver(NetworkConnectivityReceiver, filter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(NetworkConnectivityReceiver);
	}

	private void setToolbarItemEnabled(int item, Boolean enabled) {
		if (toolbarDnsMenu != null) {
			toolbarDnsMenu.findItem(item).setEnabled(enabled);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dns_tool_action_bar_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toolbarDnsMenu = menu;
		checkNetworkConnectivity(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.clear_dns_log) {
			dns_lookup_textview.setText("");
		}
		return true;
	}
}
