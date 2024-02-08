package com.truemlgpro.wifiinfo.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.utils.AppClipboardManager;
import com.truemlgpro.wifiinfo.utils.SharedPreferencesManager;

public class SettingsFragment extends PreferenceFragmentCompat {
	private PackageInfo pi;
	private SwitchPreference ntfcSwitchPreference;
	private ActivityResultLauncher<Intent> notificationSettingsLauncher;
	private ActivityResultLauncher<String> requestPermissionLauncher;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);

		boolean notificationPreferenceState = new SharedPreferencesManager(requireActivity().getApplicationContext())
				.retrieveBoolean(PreferenceKeys.KEY_PREF_SHOW_NTFC, PreferenceDefaults.SHOW_NTFC);
		boolean areNotificationsEnabled = NotificationManagerCompat.from(requireActivity()).areNotificationsEnabled();

		ntfcSwitchPreference = findPreference(PreferenceKeys.KEY_PREF_SHOW_NTFC);
		assert ntfcSwitchPreference != null;

		ntfcSwitchPreference.setChecked(areNotificationsEnabled && notificationPreferenceState);

		ntfcSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			boolean isNtfcSwitchEnabled = (boolean) newValue;
			if (isNtfcSwitchEnabled) {
				if (Build.VERSION.SDK_INT >= 33) {
					if (!isNotificationPermissionGranted()) {
						if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
							requestNotificationPermission();
						} else {
							openAppSettings();
						}
					}
				} else {
					if (!areNotificationsEnabled) {
						openAppSettings();
					}
				}
			}
			return true;
		});

		notificationSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
				result -> ntfcSwitchPreference.setChecked(areNotificationsEnabled));

		if (Build.VERSION.SDK_INT >= 33) {
			requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					if (!ntfcSwitchPreference.isChecked()) {
						ntfcSwitchPreference.setChecked(true);
					}
				} else {
					if (ntfcSwitchPreference.isChecked()) {
						ntfcSwitchPreference.setChecked(false);
					}
				}
			});
		}

		if (Build.VERSION.SDK_INT >= 26) {
			CheckBoxPreference colorizeNtfcPref = findPreference(PreferenceKeys.KEY_PREF_COLORIZE_NTFC);
			assert colorizeNtfcPref != null;
			colorizeNtfcPref.setVisible(true);
			colorizeNtfcPref.setIcon(R.drawable.format_color_fill_24px);
		}

		try {
			pi = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		setPreferenceSummary("app_version_pref", pi.versionName);
		setPreferenceSummary("android_version_pref", Build.VERSION.RELEASE);
		setPreferenceSummary("sdk_version_code_pref", String.valueOf(Build.VERSION.SDK_INT));
		setPreferenceSummary("device_model_pref", Build.MODEL);
		setPreferenceSummary("product_name_pref", Build.PRODUCT);

		setCopyOnPreferenceClickListener("app_version_pref");
		setCopyOnPreferenceClickListener("android_version_pref");
		setCopyOnPreferenceClickListener("sdk_version_code_pref");
		setCopyOnPreferenceClickListener("device_model_pref");
		setCopyOnPreferenceClickListener("product_name_pref");
	}

	private void openAppSettings() {
		Intent intentNtfcSettings = new Intent();
		intentNtfcSettings.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
		if (Build.VERSION.SDK_INT >= 26) {
			intentNtfcSettings.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName());
		} else {
			intentNtfcSettings.putExtra("app_package", requireActivity().getPackageName());
			intentNtfcSettings.putExtra("app_uid", requireActivity().getApplicationInfo().uid);
		}
		notificationSettingsLauncher.launch(intentNtfcSettings);
	}

	private boolean hasPermissions(Context context, String... permissions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			for (String permission : permissions) {
				if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

	private void setPreferenceSummary(String key, CharSequence summary) {
		findPreference(key).setSummary(summary);
	}

	private void setCopyOnPreferenceClickListener(String key) {
		findPreference(key).setOnPreferenceClickListener(preference -> {
			AppClipboardManager.copyToClipboard(requireContext(), (String) preference.getTitle(), (String) preference.getSummary());
			return true;
		});
	}

	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	private boolean isNotificationPermissionGranted() {
		boolean permissionGranted = true;
		if (Build.VERSION.SDK_INT >= 33) {
			permissionGranted = hasPermissions(requireActivity(), Manifest.permission.POST_NOTIFICATIONS);
		}
		return permissionGranted;
	}

	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	private void requestNotificationPermission() {
		requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
	}
}
