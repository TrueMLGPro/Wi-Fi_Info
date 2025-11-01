package com.truemlgpro.wifiinfo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.services.ConnectionStateService;
import com.truemlgpro.wifiinfo.services.NotificationService;
import com.truemlgpro.wifiinfo.ui.activities.MainActivity;
import com.truemlgpro.wifiinfo.ui.transition.ThemeTransition;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class App extends Application {
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefChangeListener;
	private Intent connectionStateServiceIntent;
	private Intent notificationServiceIntent;

	@Override
	public void onCreate() {
		super.onCreate();

		CaocConfig.Builder.create()
			.backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) // default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
			.showErrorDetails(true)
			.showRestartButton(true)
			.logErrorOnRestart(false)
			.trackActivities(true)
			.minTimeBetweenCrashesMs(3000)
			.errorDrawable(null)
			.apply();

		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
				// Stub
			}

			@Override
			public void onActivityStarted(Activity activity) {
				// Stub
			}

			@Override
			public void onActivityResumed(Activity activity) {
				if (activity instanceof MainActivity)
					initSharedPrefs(activity);
			}

			@Override
			public void onActivityPaused(Activity activity) {
				if (activity instanceof MainActivity) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					prefs.unregisterOnSharedPreferenceChangeListener(sharedPrefChangeListener);
				}
			}

			@Override
			public void onActivityStopped(Activity activity) {
				// Stub
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
				// Stub
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				// Stub
			}
		});
	}

	private void initSharedPrefs(Activity activity) {
		connectionStateServiceIntent = new Intent(this, ConnectionStateService.class);
		notificationServiceIntent = new Intent(this, NotificationService.class);
		sharedPrefChangeListener = (prefs, key) -> {
			switch (key) {
				case PreferenceKeys.KEY_PREF_DARK_THEME,
					 PreferenceKeys.KEY_PREF_AMOLED_THEME,
					 PreferenceKeys.KEY_PREF_MONET_THEME,
					 PreferenceKeys.KEY_PREF_CARD_STYLE_AMOLED,
					 PreferenceKeys.KEY_PREF_THEME_CONTRAST_VARIANT,
					 PreferenceKeys.KEY_PREF_KEEP_SCREEN_ON ->
						restartMainActivity(activity);
				case PreferenceKeys.KEY_PREF_APP_LANGUAGE -> {
					restartMainActivity(activity);
					if (prefs.getBoolean(PreferenceKeys.KEY_PREF_SHOW_NOTIFICATION, PreferenceDefaults.SHOW_NOTIFICATION))
						restartServices(this);
				}
				case PreferenceKeys.KEY_PREF_SHOW_NOTIFICATION -> {
					if (prefs.getBoolean(PreferenceKeys.KEY_PREF_SHOW_NOTIFICATION, PreferenceDefaults.SHOW_NOTIFICATION)) {
						startServices(this);
					} else {
						stopServices(this);
					}
				}
				case PreferenceKeys.KEY_PREF_SERVICE_SCREEN_STATE -> restartServices(this);
			}
		};

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener);
	}

	private void startServices(Context context) {
		if (!ConnectionStateService.isConnectionStateServiceRunning) {
			if (Build.VERSION.SDK_INT < 26) {
				context.startService(connectionStateServiceIntent);
			} else {
				context.startForegroundService(connectionStateServiceIntent);
			}
		}
	}

	private void stopServices(Context context) {
		context.stopService(notificationServiceIntent);
		context.stopService(connectionStateServiceIntent);
	}

	private void restartServices(Context context) {
		context.stopService(notificationServiceIntent);
		context.stopService(connectionStateServiceIntent);
		if (Build.VERSION.SDK_INT < 26) {
			context.startService(connectionStateServiceIntent);
		} else {
			context.startForegroundService(connectionStateServiceIntent);
		}
	}

	private void restartMainActivity(Activity activity) {
		if (activity instanceof MainActivity act) {
			ThemeTransition.startThemeReveal(
					act,
					act::recreate
			);
		}
	}
}
