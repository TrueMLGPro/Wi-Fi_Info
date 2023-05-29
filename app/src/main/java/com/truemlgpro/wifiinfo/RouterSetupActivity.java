package com.truemlgpro.wifiinfo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import me.anwarshahriar.calligrapher.Calligrapher;

public class RouterSetupActivity extends AppCompatActivity {
	private Toolbar toolbar;
	private Menu toolbarMenu;
	private LinearLayout textview_nonetworkconn_container;
	private TextView textview_nonetworkconn;
	private LinearLayout webview_container;
	private WebView webview_main;
	private ProgressBar progressBarLoading;
	private EditText edittext_user;
	private EditText edittext_password;

	private AlertDialog alert;

	private String user;
	private String password;

	private BroadcastReceiver NetworkConnectivityReceiver;
	private WifiManager wifiManager;
	private ConnectivityManager connectivityManager;
	private NetworkInfo wifiCheck;

	private Boolean wifi_connected = false;
	private Boolean isLoggedIn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ThemeManager.initializeThemes(this, getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.router_setup_activity);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		textview_nonetworkconn_container = (LinearLayout) findViewById(R.id.textview_nonetworkconn_container);
		textview_nonetworkconn = (TextView) findViewById(R.id.textview_nonetworkconn);
		webview_container = (LinearLayout) findViewById(R.id.webview_container);
		webview_main = (WebView) findViewById(R.id.webview_main);
		progressBarLoading = (ProgressBar) findViewById(R.id.router_setup_progress_bar);

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

		initLoginDialog();
		checkNetworkConnectivity();

		if (wifi_connected) {
			showLoginDialog();
		}
	}

	public void initLoginDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(String.format(getString(R.string.router_login_ip), getGatewayIP()))
			.setView(R.layout.router_setup_login_dialog)
			.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
				Dialog d = (Dialog) dialog;
				edittext_user = d.findViewById(R.id.edit_text_login);
				edittext_password = d.findViewById(R.id.edit_text_password);
				user = edittext_user.getText().toString();
				password = edittext_password.getText().toString();
				loadWebview();
			})
			.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> finish())
			.setNeutralButton(getString(R.string.use_web_interface), (dialog, which) -> loadWebview());
		builder.setCancelable(false);
		alert = builder.create();
	}

	public void showLoginDialog() {
		alert.show();
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void loadWebview() {
		String userAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
		WebSettings ws = webview_main.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setSupportZoom(true);
		ws.setBuiltInZoomControls(true);
		ws.setDisplayZoomControls(false);
		ws.setLoadWithOverviewMode(true);
		ws.setUseWideViewPort(true);
		ws.setUserAgentString(userAgent);
		webview_main.loadUrl("http://" + getGatewayIP());
		webview_main.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (Build.VERSION.SDK_INT >= 24) {
					progressBarLoading.setProgress(progress, true);
				} else {
					progressBarLoading.setProgress(progress);
				}

				if (progress < 100 && progressBarLoading.getVisibility() == View.GONE) {
					progressBarLoading.setVisibility(View.VISIBLE);
				} else if (progress == 100 && progressBarLoading.getVisibility() == View.VISIBLE) {
					progressBarLoading.setVisibility(View.GONE);
				}
			}
		});

		webview_main.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
				handler.proceed(user, password);
				super.onReceivedHttpAuthRequest(view, handler, host, realm);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Objects.requireNonNull(getSupportActionBar()).setSubtitle(view.getTitle());
				isLoggedIn = true;
				if (toolbarMenu != null) {
					if (webview_main.canGoBack()) {
						if (!toolbarMenu.findItem(R.id.page_back).isEnabled()) {
							setToolbarItemEnabled(R.id.page_back, true);
						}
					} else {
						if (toolbarMenu.findItem(R.id.page_back).isEnabled()) {
							setToolbarItemEnabled(R.id.page_back, false);
						}
					}

					if (webview_main.canGoForward()) {
						if (!toolbarMenu.findItem(R.id.page_forward).isEnabled()) {
							setToolbarItemEnabled(R.id.page_forward, true);
						}
					} else {
						if (toolbarMenu.findItem(R.id.page_forward).isEnabled()) {
							setToolbarItemEnabled(R.id.page_forward, false);
						}
					}

					if (!toolbarMenu.findItem(R.id.page_refresh).isEnabled()) {
						setToolbarItemEnabled(R.id.page_refresh, true);
					}
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				showErrorToast(RouterSetupActivity.this, errorCode);
				if (wifi_connected && !isLoggedIn) {
					showLoginDialog();
				}
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});
	}

	private void showErrorToast(Context mContext, int errorCode) {
		String message = null;
		switch (errorCode) {
			case WebViewClient.ERROR_AUTHENTICATION:
				message = getString(R.string.auth_error);
				break;
			case WebViewClient.ERROR_TIMEOUT:
				message = getString(R.string.timeout_error);
				break;
			case WebViewClient.ERROR_TOO_MANY_REQUESTS:
				message = getString(R.string.too_many_requests_error);
				break;
			case WebViewClient.ERROR_UNKNOWN:
				message = getString(R.string.unknown_error);
				break;
			case WebViewClient.ERROR_CONNECT:
				message = getString(R.string.connect_error);
				break;
			case WebViewClient.ERROR_HOST_LOOKUP:
				message = getString(R.string.host_lookup_error);
				break;
			case WebViewClient.ERROR_PROXY_AUTHENTICATION:
				message = getString(R.string.proxy_auth_error);
				break;
			case WebViewClient.ERROR_REDIRECT_LOOP:
				message = getString(R.string.redirect_loop_error);
				break;
			case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME:
				message = getString(R.string.unsupported_auth_scheme_error);
				break;
			case WebViewClient.ERROR_UNSUPPORTED_SCHEME:
				message = getString(R.string.unsupported_scheme_error);
				break;
			case WebViewClient.ERROR_IO:
				message = getString(R.string.io_error);
				break;
		}

		if (message != null) {
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		}
	}

	private String getGatewayIP() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		wifiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!wifiCheck.isConnected()) {
			return "0.0.0.0";
		}
		wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = wifiManager.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}

	class NetworkConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkNetworkConnectivity();
		}
	}

	public void checkNetworkConnectivity() {
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		// WI-FI Connectivity Check
		if (wifiCheck.isConnected()) {
			showWidgets();
			if (toolbarMenu != null) {
				if (webview_main.canGoBack()) {
					if (!toolbarMenu.findItem(R.id.page_back).isEnabled()) {
						setToolbarItemEnabled(R.id.page_back, true);
					}
				}
				if (webview_main.canGoForward()) {
					if (!toolbarMenu.findItem(R.id.page_forward).isEnabled()) {
						setToolbarItemEnabled(R.id.page_forward, true);
					}
				}
				if (!toolbarMenu.findItem(R.id.page_refresh).isEnabled()) {
					setToolbarItemEnabled(R.id.page_refresh, true);
				}
			}
			if (!alert.isShowing() && !isLoggedIn) {
				showLoginDialog();
			}
			wifi_connected = true;
		} else {
			hideWidgets();
			if (toolbarMenu != null) {
				if (toolbarMenu.findItem(R.id.page_back).isEnabled()) {
					setToolbarItemEnabled(R.id.page_back, false);
				}
				if (toolbarMenu.findItem(R.id.page_forward).isEnabled()) {
					setToolbarItemEnabled(R.id.page_forward, false);
				}
				if (toolbarMenu.findItem(R.id.page_refresh).isEnabled()) {
					setToolbarItemEnabled(R.id.page_refresh, false);
				}
			}
			if (alert.isShowing()) {
				alert.dismiss();
			}
			wifi_connected = false;
		}
	}

	public void showWidgets() {
		webview_container.setVisibility(View.VISIBLE);
		webview_main.setVisibility(View.VISIBLE);
		textview_nonetworkconn_container.setVisibility(View.GONE);
		textview_nonetworkconn.setVisibility(View.GONE);
	}

	public void hideWidgets() {
		webview_container.setVisibility(View.GONE);
		webview_main.setVisibility(View.GONE);
		progressBarLoading.setVisibility(View.GONE);
		textview_nonetworkconn_container.setVisibility(View.VISIBLE);
		textview_nonetworkconn.setVisibility(View.VISIBLE);
	}

	private void setToolbarItemEnabled(int item, Boolean enabled) {
		if (toolbarMenu != null) {
			toolbarMenu.findItem(item).setEnabled(enabled);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.router_setup_tool_action_bar_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toolbarMenu = menu;
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case (R.id.page_back):
				webview_main.goBack();
				break;
			case (R.id.page_forward):
				webview_main.goForward();
				break;
			case (R.id.page_refresh):
				webview_main.reload();
				break;
		}
		return true;
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
}
