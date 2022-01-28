package com.truemlgpro.wifiinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import me.anwarshahriar.calligrapher.Calligrapher;

public class RouterSetupActivity extends AppCompatActivity
{
	private Toolbar toolbar;
	private WebView webview_main;
	private ProgressBar progressBarLoading;
	private SwipeRefreshLayout swipeRefresh;
	private EditText edittext_user;
	private EditText edittext_password;
	private WifiManager mainWifi;
	private NetworkInfo WiFiCheck;
	
	public static String user;
	public static String password;

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
		setContentView(R.layout.router_setup_activity);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		webview_main = (WebView) findViewById(R.id.webview_main);
		progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
		swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Calligrapher calligrapher = new Calligrapher(this);
		String font = new SharedPreferencesManager(getApplicationContext()).retrieveString(SettingsActivity.KEY_PREF_APP_FONT, MainActivity.appFont);
		calligrapher.setFont(this, font, true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);
		
		if (!keyTheme) {
			swipeRefresh.setProgressBackgroundColor(R.color.cardBackgroundDark);
		}
		
		if (keyTheme) {
			if (keyAmoledTheme) {
				swipeRefresh.setProgressBackgroundColor(R.color.cardBackgroundDarkAmoled);
			}
		}
		
		if (!keyTheme) {
			swipeRefresh.setProgressBackgroundColor(R.color.cardBackgroundLight);
		}
		
		swipeRefresh.setColorSchemeResources(R.color.refreshLayoutColor1, R.color.refreshLayoutColor2, R.color.refreshLayoutColor3, R.color.refreshLayoutColor4);
		swipeRefresh.setProgressViewOffset(true, -75, 200);
		swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh()
			{
				webview_main.reload();
			}
		});

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Back button pressed
					finish();
				}
			});
		
		showDialog();
	}
	
	public void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		final View editTextLoginPasswordView = layoutInflater.inflate(R.layout.edit_text_dialog, null);
		builder.setTitle("Router Login — " + getGatewayIP())
			.setView(R.layout.edit_text_dialog)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					Dialog d = (Dialog) dialog;
					edittext_user = d.findViewById(R.id.edit_text_user);
					edittext_password = d.findViewById(R.id.edit_text_password);
					user = edittext_user.getText().toString();
					password = edittext_password.getText().toString();
					loadWebview();
				}
			})
			.setNegativeButton("Cancel", null);
		builder.setCancelable(false);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
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
		webview_main.loadUrl("http://" + user + ":" + password + "@" + getGatewayIP());
		webview_main.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (progress < 100 && progressBarLoading.getVisibility() == View.GONE) {
					progressBarLoading.setVisibility(View.VISIBLE);
				}
				if (progress == 100 && progressBarLoading.getVisibility() == View.VISIBLE) {
					progressBarLoading.setVisibility(View.GONE);
				}
				progressBarLoading.setProgress(progress, true);
			}
		});
		
		webview_main.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (swipeRefresh.isRefreshing()) {
					swipeRefresh.setRefreshing(false);
				}
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				showErrorToast(RouterSetupActivity.this, errorCode);
				showDialog();
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});
	}
	
	private void showErrorToast(Context mContext, int errorCode) {
		String message = null;
		switch (errorCode) {
			case WebViewClient.ERROR_AUTHENTICATION:
				message = "User authentication failed on server.";
				break;
			case WebViewClient.ERROR_TIMEOUT:
				message = "Connection timeout. Try again later.";
				break;
			case WebViewClient.ERROR_TOO_MANY_REQUESTS:
				message = "Too many requests during this load.";
				break;
			case WebViewClient.ERROR_UNKNOWN:
				message = "Unknown error";
				break;
			case WebViewClient.ERROR_BAD_URL:
				message = "Check entered URL.";
				break;
			case WebViewClient.ERROR_CONNECT:
				message = "Failed to connect to the server.";
				break;
			case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
				message = "Failed to perform SSL handshake.";
				break;
			case WebViewClient.ERROR_HOST_LOOKUP:
				message = "Server or proxy hostname lookup failed.";
				break;
			case WebViewClient.ERROR_PROXY_AUTHENTICATION:
				message = "User authentication failed on proxy.";
				break;
			case WebViewClient.ERROR_REDIRECT_LOOP:
				message = "Too many redirects.";
				break;
			case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME:
				message = "Unsupported authentication scheme (not basic or digest).";
				break;
			case WebViewClient.ERROR_UNSUPPORTED_SCHEME:
				message = "Unsupported URI scheme.";
				break;
			case WebViewClient.ERROR_FILE:
				message = "Generic file error.";
				break;
			case WebViewClient.ERROR_FILE_NOT_FOUND:
				message = "File not found.";
				break;
			case WebViewClient.ERROR_IO:
				message = "The server failed to communicate. Try again later.";
				break;
		}

		if (message != null) {
			Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
		}
	}
	
	private String getGatewayIP() {
		ConnectivityManager CM = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		WiFiCheck = CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!WiFiCheck.isConnected()) {
			return "0.0.0.0";
		}
		mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = mainWifi.getDhcpInfo();
		int ip = dhcp.gateway;
		return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}
	
}
