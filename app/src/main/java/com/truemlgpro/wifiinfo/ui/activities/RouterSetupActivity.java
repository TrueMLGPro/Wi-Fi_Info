package com.truemlgpro.wifiinfo.ui.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.net.NetworkUtils;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.util.Objects;

public class RouterSetupActivity extends AppCompatActivity {
	private Menu toolbarMenu;
	private LinearLayout textview_nonetworkconn_container;
	private MaterialTextView textview_nonetworkconn;
	private LinearLayout webview_container;
	private WebView webview_main;
	private LinearProgressIndicator progressBarLoading;
	private EditText edittext_user;
	private EditText edittext_password;

	private AlertDialog alert;

	private String user;
	private String password;

	private ConnectivityManager connectivityManager;

	private ConnectivityManager.NetworkCallback wifiCallback;
	private ConnectivityManager.NetworkCallback defaultLikeCallback;
	private Handler mainHandler;
	private final Runnable doCheck = this::checkAndUpdateConnectivity;

	private Boolean wifiConnected = false;
	private Boolean isLoggedIn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.router_setup_activity);

		MaterialToolbar toolbar = findViewById(R.id.toolbar);
		textview_nonetworkconn_container = findViewById(R.id.textview_nonetworkconn_container);
		textview_nonetworkconn = findViewById(R.id.textview_nonetworkconn);
		webview_container = findViewById(R.id.webview_container);
		webview_main = findViewById(R.id.webview_main);
		progressBarLoading = findViewById(R.id.router_setup_progress_bar);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayShowHomeEnabled(true);
		}

		KeepScreenOnManager.init(getWindow(), getApplicationContext());
		toolbar.setNavigationOnClickListener(v -> finish());

		initLoginDialog();
		initInsets();

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mainHandler = new Handler(Looper.getMainLooper());

		wifiConnected = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.WIFI, false);
		updateConnectivityUI(wifiConnected);
		if (wifiConnected) {
			showLoginDialog();
		}
	}

	public void initLoginDialog() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle(String.format(getString(R.string.router_login_ip), NetworkUtils.getGatewayIP(RouterSetupActivity.this)))
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
		if (!isFinishing() && alert != null && !alert.isShowing()) {
			alert.show();
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void loadWebview() {
		String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
		WebSettings ws = webview_main.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setDomStorageEnabled(true);
		ws.setSupportZoom(true);
		ws.setBuiltInZoomControls(true);
		ws.setDisplayZoomControls(false);
		ws.setLoadWithOverviewMode(true);
		ws.setUseWideViewPort(true);
		ws.setUserAgentString(userAgent);
		if (Build.VERSION.SDK_INT >= 33) ws.setAlgorithmicDarkeningAllowed(true);

		webview_main.loadUrl("http://" + NetworkUtils.getGatewayIP(RouterSetupActivity.this));

		webview_main.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				progressBarLoading.setProgressCompat(progress, true);
				if (progress < 100 && progressBarLoading.getVisibility() == View.GONE) {
					progressBarLoading.setVisibility(View.VISIBLE);
				} else if (progress == 100 && progressBarLoading.getVisibility() == View.VISIBLE) {
					progressBarLoading.setVisibility(View.GONE);
				}
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				showMaterialAlertDialog(message, result);
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
				showMaterialConfirmDialog(message, result);
				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				showMaterialPromptDialog(message, defaultValue, result);
				return true;
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
					setToolbarItemEnabled(R.id.page_back, webview_main.canGoBack());
					setToolbarItemEnabled(R.id.page_forward, webview_main.canGoForward());
					setToolbarItemEnabled(R.id.page_refresh, true);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				showErrorToast(RouterSetupActivity.this, errorCode);
				if (wifiConnected && !isLoggedIn) {
					showLoginDialog();
				}
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});
	}

	private void showErrorToast(Context mContext, int errorCode) {
		String message = switch (errorCode) {
			case WebViewClient.ERROR_AUTHENTICATION -> getString(R.string.auth_error);
			case WebViewClient.ERROR_TIMEOUT -> getString(R.string.timeout_error);
			case WebViewClient.ERROR_TOO_MANY_REQUESTS -> getString(R.string.too_many_requests_error);
			case WebViewClient.ERROR_UNKNOWN -> getString(R.string.unknown_error);
			case WebViewClient.ERROR_CONNECT -> getString(R.string.connect_error);
			case WebViewClient.ERROR_HOST_LOOKUP -> getString(R.string.host_lookup_error);
			case WebViewClient.ERROR_PROXY_AUTHENTICATION -> getString(R.string.proxy_auth_error);
			case WebViewClient.ERROR_REDIRECT_LOOP -> getString(R.string.redirect_loop_error);
			case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME -> getString(R.string.unsupported_auth_scheme_error);
			case WebViewClient.ERROR_UNSUPPORTED_SCHEME -> getString(R.string.unsupported_scheme_error);
			case WebViewClient.ERROR_IO -> getString(R.string.io_error);
			default -> null;
		};

		if (message != null) {
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		}
	}

	private void showMaterialAlertDialog(String message, JsResult result) {
		new MaterialAlertDialogBuilder(RouterSetupActivity.this)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
				.setOnCancelListener(dialog -> result.cancel())
				.show();
	}

	private void showMaterialConfirmDialog(String message, JsResult result) {
		new MaterialAlertDialogBuilder(RouterSetupActivity.this)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
				.setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel())
				.setOnCancelListener(dialog -> result.cancel())
				.show();
	}

	private void showMaterialPromptDialog(String message, String defaultValue, JsPromptResult result) {
		final EditText input = new EditText(RouterSetupActivity.this);
		input.setText(defaultValue);

		new MaterialAlertDialogBuilder(RouterSetupActivity.this)
				.setMessage(message)
				.setView(input)
				.setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm(input.getText().toString()))
				.setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel())
				.setOnCancelListener(dialog -> result.cancel())
				.show();
	}

	private void initInsets() {
		AppBarLayout app_bar_layout = findViewById(R.id.appbarlayout_router);
		LinearLayout webview_container = findViewById(R.id.webview_container);

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
				webview_container,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_BOTTOM | InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerNetworkCallbacks();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterNetworkCallbacks();
	}

	private void scheduleCheck() {
		if (mainHandler == null) return;
		mainHandler.removeCallbacks(doCheck);
		mainHandler.postDelayed(doCheck, 100);
	}

	private void checkAndUpdateConnectivity() {
		boolean connected = NetworkUtils.isOnline(this, NetworkUtils.NetworkType.WIFI, false);
		updateConnectivityUI(connected);
	}

	private void registerNetworkCallbacks() {
		if (connectivityManager == null) return;

		wifiCallback = new ConnectivityManager.NetworkCallback() {
			@Override public void onAvailable(Network network) { scheduleCheck(); }
			@Override public void onLost(Network network) { scheduleCheck(); }
			@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
		};

		NetworkRequest wifiReq = new NetworkRequest.Builder()
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.build();

		try {
			if (Build.VERSION.SDK_INT >= 26) {
				connectivityManager.registerNetworkCallback(wifiReq, wifiCallback, mainHandler);
			} else {
				connectivityManager.registerNetworkCallback(wifiReq, wifiCallback);
			}
		} catch (Exception ignored) { }

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			defaultLikeCallback = new ConnectivityManager.NetworkCallback() {
				@Override public void onAvailable(Network network) { scheduleCheck(); }
				@Override public void onLost(Network network) { scheduleCheck(); }
				@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
			};
			try {
				if (Build.VERSION.SDK_INT >= 26) {
					connectivityManager.registerDefaultNetworkCallback(defaultLikeCallback, mainHandler);
				} else {
					connectivityManager.registerDefaultNetworkCallback(defaultLikeCallback);
				}
			} catch (Exception ignored) { }
		} else {
			defaultLikeCallback = new ConnectivityManager.NetworkCallback() {
				@Override public void onAvailable(Network network) { scheduleCheck(); }
				@Override public void onLost(Network network) { scheduleCheck(); }
				@Override public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) { scheduleCheck(); }
			};
			NetworkRequest cellReq = new NetworkRequest.Builder()
					.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
					.build();
			try {
				if (Build.VERSION.SDK_INT >= 26) {
					connectivityManager.registerNetworkCallback(cellReq, defaultLikeCallback, mainHandler);
				} else {
					connectivityManager.registerNetworkCallback(cellReq, defaultLikeCallback);
				}
			} catch (Exception ignored) { }
		}
	}

	private void unregisterNetworkCallbacks() {
		if (connectivityManager == null) return;
		try {
			if (wifiCallback != null) {
				connectivityManager.unregisterNetworkCallback(wifiCallback);
				wifiCallback = null;
			}
		} catch (Exception ignored) { }
		try {
			if (defaultLikeCallback != null) {
				connectivityManager.unregisterNetworkCallback(defaultLikeCallback);
				defaultLikeCallback = null;
			}
		} catch (Exception ignored) { }
		if (mainHandler != null) mainHandler.removeCallbacks(doCheck);
	}

	private void updateConnectivityUI(boolean connectedNow) {
		wifiConnected = connectedNow;
		if (connectedNow) {
			showWidgets();
			if (toolbarMenu != null) {
				setToolbarItemEnabled(R.id.page_back, webview_main.canGoBack());
				setToolbarItemEnabled(R.id.page_forward, webview_main.canGoForward());
				setToolbarItemEnabled(R.id.page_refresh, true);
			}
			if (alert != null && !alert.isShowing() && !isLoggedIn) {
				showLoginDialog();
			}
		} else {
			hideWidgets();
			if (toolbarMenu != null) {
				setToolbarItemEnabled(R.id.page_back, false);
				setToolbarItemEnabled(R.id.page_forward, false);
				setToolbarItemEnabled(R.id.page_refresh, false);
			}
			if (alert != null && alert.isShowing()) {
				alert.dismiss();
			}
			isLoggedIn = false; // require re-login when Wi‑Fi drops
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
		if (id == R.id.page_back) {
			webview_main.goBack();
		} else if (id == R.id.page_forward) {
			webview_main.goForward();
		} else if (id == R.id.page_refresh) {
			webview_main.reload();
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (alert != null && alert.isShowing()) {
			alert.dismiss();
		}
		if (webview_main != null) {
			webview_main.setWebViewClient(null);
			webview_main.setWebChromeClient(null);
			webview_main.clearHistory();
			webview_main.clearCache(true);
			webview_main.destroy();
			webview_main = null;
		}
	}
}