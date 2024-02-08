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
import com.truemlgpro.wifiinfo.ui.SettingsActivity;

public class App extends Application {
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefChangeListener;
	Intent connectionStateServiceIntent;
	Intent notificationServiceIntent;

	@Override
	public void onCreate() {
		super.onCreate();
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
				if (activity.getClass().equals(SettingsActivity.class))
					initSharedPrefs(activity);
			}

			@Override
			public void onActivityPaused(Activity activity) {
				if (activity.getClass().equals(SettingsActivity.class)) {
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
				case PreferenceKeys.KEY_PREF_DARK_MODE,
					PreferenceKeys.KEY_PREF_AMOLED_MODE,
					PreferenceKeys.KEY_PREF_APP_FONT,
					PreferenceKeys.KEY_PREF_KEEP_SCREEN_ON ->
						restartSettingsActivity(activity);
				case PreferenceKeys.KEY_PREF_APP_LANGUAGE -> {
					restartSettingsActivity(activity);
					if (prefs.getBoolean(PreferenceKeys.KEY_PREF_SHOW_NTFC, PreferenceDefaults.SHOW_NTFC))
						restartServices(this);
				}
				case PreferenceKeys.KEY_PREF_SHOW_NTFC -> {
					if (prefs.getBoolean(PreferenceKeys.KEY_PREF_SHOW_NTFC, PreferenceDefaults.SHOW_NTFC)) {
						startServices(this);
					} else {
						stopServices(this);
					}
				}
				case PreferenceKeys.KEY_PREF_START_STOP_SVC -> restartServices(this);
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

	private void restartSettingsActivity(Activity activity) {
		if (activity.getClass().equals(SettingsActivity.class)) {
			Intent activityRestartIntent = new Intent(this, SettingsActivity.class);
			activity.finish();
			activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			activity.startActivity(activityRestartIntent);
			activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
	}
}
