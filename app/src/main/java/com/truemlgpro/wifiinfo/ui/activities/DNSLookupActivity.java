package com.truemlgpro.wifiinfo.ui.activities;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties;
import org.minidns.hla.ResolverApi;
import org.minidns.hla.ResolverResult;
import org.minidns.record.Data;
import org.minidns.record.Record;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DNSLookupActivity extends AppCompatActivity {
	private MaterialTextView textview_nonetworkconn;
	private LinearProgressIndicator dns_lookup_progress_bar;
	private MaterialButton get_dns_info_button;
	private TextInputLayout input_layout_dns;
	private EditText edit_text_dns;
	private Spinner spinner_dns_record_types;
	private MaterialCardView dns_lookup_cardview;
	private ScrollView dns_lookup_results_scroll;
	private MaterialTextView dns_lookup_textview;

	private Menu toolbarDnsMenu;

	private String url_ip;
	private String dns_record_type;

	final String lineSeparator = "\n---------------------\n";

	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback networkCallback;

	private final ExecutorService dnsExecutor = Executors.newSingleThreadExecutor();
	private Future<?> dnsFuture;
	private Handler mainHandler;
	private final Runnable doCheck = this::checkAndUpdateConnectivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.dns_lookup_activity);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		textview_nonetworkconn = findViewById(R.id.textview_nonetworkconn);
		dns_lookup_progress_bar = findViewById(R.id.dns_lookup_progress_bar);
		get_dns_info_button = findViewById(R.id.get_dns_info_button);
		input_layout_dns = findViewById(R.id.input_layout_dns);
		edit_text_dns = findViewById(R.id.edit_text_dns);
		spinner_dns_record_types = findViewById(R.id.spinner_dns_record_types);
		dns_lookup_cardview = findViewById(R.id.dns_lookup_cardview);
		dns_lookup_results_scroll = findViewById(R.id.dns_lookup_results_scroll);
		dns_lookup_textview = findViewById(R.id.dns_lookup_results_textview);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		toolbar.setNavigationOnClickListener(v -> finish());

		mainHandler = new Handler(Looper.getMainLooper());
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		get_dns_info_button.setOnClickListener(v -> {
			url_ip = NetworkUtils.extractDomain(edit_text_dns.getText().toString());
			if (TextUtils.isEmpty(url_ip)) {
				url_ip = "google.com";
			}
			edit_text_dns.setText(url_ip);
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

		updateConnectivityUI(isOnline(), false);

		initInsets();
	}

	private void startDnsLookup() {
		if (dnsFuture != null && !dnsFuture.isDone()) {
			dnsFuture.cancel(true);
		}

		setEnabled(get_dns_info_button, false);
		setEnabled(edit_text_dns, false);
		dns_lookup_progress_bar.setVisibility(View.VISIBLE);

		final String domain = url_ip;
		final String type = dns_record_type;

		dnsFuture = dnsExecutor.submit(() -> {
			String resultText;
			try {
				AndroidUsingLinkProperties.setup(getApplicationContext());
				Class<Data> recordDataClass = Record.TYPE.valueOf(type).getDataClass();
				if (recordDataClass == null) {
					resultText = String.format(getString(R.string.record_type_not_supported), type, lineSeparator);
				} else {
					ResolverResult<? extends Data> result = ResolverApi.INSTANCE.resolve(domain, recordDataClass);

					if (!result.wasSuccessful()) {
						resultText = String.format(getString(R.string.dns_lookup_failed_response), type, result.getResponseCode(), lineSeparator);
					} else {
						Set<? extends Data> answers = result.getAnswers();
						if (answers.isEmpty()) {
							resultText = String.format(getString(R.string.dns_lookup_no_records), type, lineSeparator);
						} else {
							StringBuilder out = new StringBuilder();
							out.append(getString(R.string.dns_record_type)).append(type).append("\n")
									.append(getString(R.string.url_ip)).append(domain).append(lineSeparator);
							for (Data answer : answers) {
								if (Thread.currentThread().isInterrupted()) return; // canceled
								out.append(answer).append("\n");
							}
							resultText = out.toString();
						}
					}
				}
			} catch (IOException e) {
				resultText = String.format(getString(R.string.dns_lookup_failed_exception), type, e.getMessage(), lineSeparator);
			} catch (Throwable t) {
				resultText = String.format(getString(R.string.dns_lookup_failed_exception), type, t.getMessage(), lineSeparator);
			}

			final String toAppend = resultText;
			runOnUiThread(() -> {
				setEnabled(get_dns_info_button, true);
				setEnabled(edit_text_dns, true);
				dns_lookup_progress_bar.setVisibility(View.INVISIBLE);
				appendResultsText(toAppend);
			});
		});
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

	@Override
	protected void onStart() {
		super.onStart();
		registerNetworkCallback();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterNetworkCallback();
		if (dnsFuture != null && !dnsFuture.isDone()) {
			dnsFuture.cancel(true);
			dnsFuture = null;
		}
	}

	private void registerNetworkCallback() {
		if (connectivityManager == null) return;

		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override public void onAvailable(Network network) { scheduleCheck(); }
			@Override public void onLost(Network network) { runOnUiThread(() -> updateConnectivityUI(false, true)); }
			@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
		};

		NetworkRequest request = new NetworkRequest.Builder()
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
				.build();

		try {
			if (Build.VERSION.SDK_INT >= 26) {
				connectivityManager.registerNetworkCallback(request, networkCallback, mainHandler);
			} else {
				connectivityManager.registerNetworkCallback(request, networkCallback);
			}
		} catch (Exception ignored) { }
	}

	private void unregisterNetworkCallback() {
		if (connectivityManager != null && networkCallback != null) {
			try { connectivityManager.unregisterNetworkCallback(networkCallback); } catch (Exception ignored) { }
			networkCallback = null;
		}
		if (mainHandler != null) mainHandler.removeCallbacks(doCheck);
	}

	private void scheduleCheck() {
		if (mainHandler == null) return;
		mainHandler.removeCallbacks(doCheck);
		mainHandler.postDelayed(doCheck, 100);
	}

	private void checkAndUpdateConnectivity() {
		updateConnectivityUI(isOnline(), false);
	}

	private boolean isOnline() {
		return NetworkUtils.isOnline(this, NetworkUtils.NetworkType.ANY, false);
	}

	private void updateConnectivityUI(boolean connected, boolean shouldClearLog) {
		if (connected) {
			showWidgets();
			if (toolbarDnsMenu != null && !toolbarDnsMenu.findItem(R.id.clear_dns_log).isEnabled()) {
				setToolbarItemEnabled(R.id.clear_dns_log, true);
			}
		} else {
			if (shouldClearLog) dns_lookup_textview.setText("");
			if (toolbarDnsMenu != null && toolbarDnsMenu.findItem(R.id.clear_dns_log).isEnabled()) {
				setToolbarItemEnabled(R.id.clear_dns_log, false);
			}
			hideWidgets();
		}
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_dns);
		LinearLayout linear_layout_root = findViewById(R.id.linear_layout_root_dns);

		InsetsController.setInsets(
				app_bar_layout,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_TOP | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);

		InsetsController.setInsets(
				linear_layout_root,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_BOTTOM | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	public void showWidgets() {
		dns_lookup_textview.setVisibility(View.VISIBLE);
		get_dns_info_button.setVisibility(View.VISIBLE);
		spinner_dns_record_types.setVisibility(View.VISIBLE);
		input_layout_dns.setVisibility(View.VISIBLE);
		dns_lookup_cardview.setVisibility(View.VISIBLE);
		dns_lookup_results_scroll.setVisibility(View.VISIBLE);
		dns_lookup_progress_bar.setVisibility(View.INVISIBLE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		dns_lookup_textview.setVisibility(View.GONE);
		get_dns_info_button.setVisibility(View.GONE);
		spinner_dns_record_types.setVisibility(View.GONE);
		input_layout_dns.setVisibility(View.GONE);
		dns_lookup_cardview.setVisibility(View.GONE);
		dns_lookup_results_scroll.setVisibility(View.GONE);
		dns_lookup_progress_bar.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void setEnabled(final View view, final boolean enabled) {
		runOnUiThread(() -> { if (view != null) view.setEnabled(enabled); });
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
		updateConnectivityUI(isOnline(), false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.clear_dns_log) {
			dns_lookup_textview.setText("");
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dnsFuture != null && !dnsFuture.isDone()) {
			dnsFuture.cancel(true);
		}
		dnsExecutor.shutdownNow();
	}
}
