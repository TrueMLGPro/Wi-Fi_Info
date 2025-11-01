package com.truemlgpro.wifiinfo.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.services.NotificationService;
import com.truemlgpro.wifiinfo.ui.transition.ThemeTransition;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.KeepScreenOnManager;
import com.truemlgpro.wifiinfo.utils.ui.LocaleManager;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;
import com.truemlgpro.wifiinfo.utils.ui.ThemeManager;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
	private BottomNavigationView bottom_nav_view;

	public static boolean isServiceRunning = false;
	private final int LOCATION_PERMISSION_CODE = 123;

	private static boolean initialPermissionsHandled = false;
	private boolean locationPermissionDialogShown = false;
	private boolean postNotificationPermissionHandled = false;

	private SharedPreferencesManager sp;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		/// Shared Preferences ///
		sp = new SharedPreferencesManager(getApplicationContext());

		/// Themes & Locale ///
		ThemeManager.initializeThemes(this, getApplicationContext());
		LocaleManager.initializeLocale(getApplicationContext());

		/// Set default preferences ///
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		/// Splash Screen API ///
		if (!ThemeTransition.isInProgress()) {
			SplashScreen.installSplashScreen(this);
		}

		if (ThemeTransition.isInProgress()) {
			int prevStatus = ThemeTransition.peekPrevStatusBarColor();
			int prevNav = ThemeTransition.peekPrevNavBarColor();
			if (prevStatus != Integer.MIN_VALUE) getWindow().setStatusBarColor(prevStatus);
			if (prevNav != Integer.MIN_VALUE) getWindow().setNavigationBarColor(prevNav);
		}

		super.onCreate(savedInstanceState);
		WindowCompat.enableEdgeToEdge(getWindow());
		setContentView(R.layout.main);

		/// Initialize ///
		initViews();

		NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment_container_view);
		NavController navController = navHostFragment.getNavController();
		NavigationUI.setupWithNavController(bottom_nav_view, navController);

		/// Keep screen on ///
		KeepScreenOnManager.init(getWindow(), getApplicationContext());

		/// Request permissions ///
		if (Build.VERSION.SDK_INT >= 26) {
			createNotificationChannels();
			if (!isLocationPermissionGranted()) {
				// If user didn't choose to hide the permission request dialog
				if (!new SharedPreferencesManager(getApplicationContext()).retrieveBoolean(PreferenceKeys.KEY_NEVER_SHOW_PERMISSION_REQ_DIALOG, PreferenceDefaults.NEVER_SHOW_PERMISSION_REQ_DIALOG)) {
					requestPermissionsOnStart();
				} else {
					// If dialog is hidden by user preference, mark as handled
					locationPermissionDialogShown = true;
					checkIfAllPermissionsHandled();
				}
			} else {
				// Permission already granted, mark as handled
				locationPermissionDialogShown = true;
				checkIfAllPermissionsHandled();
			}

			/// Create dynamic shortcuts ///
			createShortcuts();
		} else {
			// For API < 26, mark as handled immediately
			initialPermissionsHandled = true;
		}

		/// Services ///
		initForegroundServices();
	}

	private void initViews() {
		CoordinatorLayout content_frame = findViewById(R.id.content_frame);
		bottom_nav_view = findViewById(R.id.bottom_nav_view);

		InsetsController.setInsets(
				content_frame,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.statusBars())
						.edges(InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);

		InsetsController.setInsets(
				bottom_nav_view,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_HORIZONTAL | InsetsController.EDGE_BOTTOM)
						.applyToPadding()
						.consume(false)
						.build()
		);
	}

	private boolean hasPermissions(String... permissions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
			for (String permission : permissions) {
				if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

	private void requestPermissionsOnStart() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
		builder.setTitle(getString(R.string.permission_required))
				.setMessage(getString(R.string.location_permission_is_required_android_8_1_plus))
				.setPositiveButton(getString(R.string.yes), (dialog, id) -> {
					locationPermissionDialogShown = true;

					// Android 8.1 - Android 11
					String[] foregroundCoarseLocationPermission_API27 = {Manifest.permission.ACCESS_COARSE_LOCATION};
					// Android 12+
					String[] foregroundFineLocationPermission_API31 = {Manifest.permission.ACCESS_FINE_LOCATION};
					if (Build.VERSION.SDK_INT >= 27 && Build.VERSION.SDK_INT < 31) {
						ActivityCompat.requestPermissions(MainActivity.this, foregroundCoarseLocationPermission_API27, LOCATION_PERMISSION_CODE);
					} else if (Build.VERSION.SDK_INT >= 31) {
						ActivityCompat.requestPermissions(MainActivity.this, foregroundFineLocationPermission_API31, LOCATION_PERMISSION_CODE);
					}
				})
				.setNegativeButton(getString(R.string.no_thanks), (dialog, id) -> {
					locationPermissionDialogShown = true;
					checkIfAllPermissionsHandled();
				})
				.setNeutralButton(getString(R.string.dont_show_again), (dialog, id) -> {
					sp.storeBoolean(PreferenceKeys.KEY_NEVER_SHOW_PERMISSION_REQ_DIALOG, true);
					locationPermissionDialogShown = true;
					checkIfAllPermissionsHandled();
				})
				.setCancelable(false);
		builder.create().show();
	}

	private boolean isLocationPermissionGranted() {
		// In Android 8.1 (API 27) - 11 (API 30) ACCESS_COARSE_LOCATION needs to be granted to access network information
		// Android 12+ (API 31) needs ACCESS_FINE_LOCATION to be granted though
		boolean permissionGranted = false;
		if (Build.VERSION.SDK_INT >= 27 && Build.VERSION.SDK_INT < 31) {
			// Android 8.1 - Android 11
			permissionGranted = hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
		} else if (Build.VERSION.SDK_INT >= 31) {
			// Android 12+
			permissionGranted = hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
		}
		return permissionGranted;
	}

	public static boolean areInitialPermissionsHandled() {
		return initialPermissionsHandled;
	}

	@RequiresApi(api = Build.VERSION_CODES.N_MR1)
	private void createShortcuts() {
		ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
		ShortcutInfo githubShortcut = new ShortcutInfo.Builder(this, "shortcut_github")
				.setShortLabel(getString(R.string.github_repo))
				.setLongLabel(getString(R.string.open_github_repository))
				.setIcon(Icon.createWithResource(this, R.drawable.ic_github))
				.setRank(2)
				.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TrueMLGPro/Wi-Fi_Info")))
				.build();
		ShortcutInfo releasesShortcut = new ShortcutInfo.Builder(this, "shortcut_releases")
				.setShortLabel(getString(R.string.releases))
				.setLongLabel(getString(R.string.open_github_releases))
				.setRank(1)
				.setIcon(Icon.createWithResource(this, R.drawable.ic_folder))
				.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TrueMLGPro/Wi-Fi_Info/releases")))
				.build();
		shortcutManager.setDynamicShortcuts(Arrays.asList(githubShortcut, releasesShortcut));
	}

	private void initForegroundServices() {
		boolean keyNtfc = sp.retrieveBoolean(PreferenceKeys.KEY_PREF_SHOW_NOTIFICATION, PreferenceDefaults.SHOW_NOTIFICATION);
		if (keyNtfc) {
			if (!isServiceRunning) {
				Intent connectionStateServiceIntent = new Intent(MainActivity.this, ConnectionStateService.class);
				if (Build.VERSION.SDK_INT < 26) {
					startService(connectionStateServiceIntent);
				} else {
					startForegroundService(connectionStateServiceIntent);
				}
				isServiceRunning = true;
			}
		} else {
			if (isServiceRunning) {
				Intent connectionStateServiceIntent = new Intent(MainActivity.this, ConnectionStateService.class);
				Intent notificationServiceIntent = new Intent(MainActivity.this, NotificationService.class);

				stopService(connectionStateServiceIntent);
				stopService(notificationServiceIntent);

				isServiceRunning = false;
			}
		}
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private void createNotificationChannels() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (notificationManager == null) return;

		// ConnectionStateService notification
		String connStateNtfcChannelId = "connection_state_service";
		CharSequence connStateNtfcChannelName = "Connection State Service";
		NotificationChannel connStateNtfcChannel = new NotificationChannel(connStateNtfcChannelId, connStateNtfcChannelName, NotificationManager.IMPORTANCE_MIN);
		connStateNtfcChannel.setDescription("Wi-Fi Info Connection Listener Service Notification");
		connStateNtfcChannel.setShowBadge(false);
		notificationManager.createNotificationChannel(connStateNtfcChannel);

		// NotificationService notification
		String mainNtfcChannelId = "wifi_info";
		CharSequence mainNtfcChannelName = "Notification Service";
		NotificationChannel mainNtfcChannel = new NotificationChannel(mainNtfcChannelId, mainNtfcChannelName, NotificationManager.IMPORTANCE_LOW);
		mainNtfcChannel.setDescription("Main Wi-Fi Info Notification");
		mainNtfcChannel.setShowBadge(false);
		notificationManager.createNotificationChannel(mainNtfcChannel);

		if (Build.VERSION.SDK_INT >= 33) {
			String[] postNotificationsPermission = {Manifest.permission.POST_NOTIFICATIONS};
			ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				postNotificationPermissionHandled = true;
				checkIfAllPermissionsHandled();
				if (isGranted) {
					initForegroundServices();
				}
			});

			if (!hasPermissions(postNotificationsPermission)) {
				requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
			} else {
				postNotificationPermissionHandled = true;
				checkIfAllPermissionsHandled();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (Build.VERSION.SDK_INT >= 30 && requestCode == LOCATION_PERMISSION_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
				builder.setTitle(getString(R.string.background_location_permission))
						.setMessage(getString(R.string.due_to_the_changes_in_android_11))
						.setPositiveButton(getString(android.R.string.ok), (dialog, id) -> {
							Toast.makeText(MainActivity.this, getString(R.string.go_to_permissions_and_location), Toast.LENGTH_LONG).show();
							Toast.makeText(MainActivity.this, getString(R.string.select_allow_all_the_time), Toast.LENGTH_LONG).show();
							Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							Uri uri = Uri.fromParts("package", getPackageName(), null);
							intent.setData(uri);
							startActivity(intent);
							checkIfAllPermissionsHandled();
						})
						.setNegativeButton(getString(R.string.no_thanks), (dialog, id) -> checkIfAllPermissionsHandled())
						.setCancelable(false);
				builder.create().show();
			} else {
				checkIfAllPermissionsHandled();
			}
		} else if (requestCode == LOCATION_PERMISSION_CODE) {
			// For API < 30
			checkIfAllPermissionsHandled();
		}
	}

	private void checkIfAllPermissionsHandled() {
		if (Build.VERSION.SDK_INT >= 33) {
			// For API 33+, check both location and notification permissions
			if (locationPermissionDialogShown && postNotificationPermissionHandled) {
				initialPermissionsHandled = true;
			}
		} else if (Build.VERSION.SDK_INT >= 26) {
			// For API 26-32, only check location permission
			if (locationPermissionDialogShown) {
				initialPermissionsHandled = true;
			}
		} else {
			// For API < 26
			initialPermissionsHandled = true;
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		ThemeTransition.notifyThemeReadyAfterFirstDraw(this, 100L);
	}

	@SuppressLint("MissingSuperCall")
	@Override
	public void onBackPressed() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
		builder.setTitle(getString(R.string.are_you_sure))
				.setMessage(getString(R.string.do_you_want_to_exit))
				.setPositiveButton(getString(R.string.exit), (dialog, id) -> finishAffinity())
				.setNegativeButton(getString(android.R.string.cancel), null);
		builder.setCancelable(false);
		builder.create().show();
	}
}
