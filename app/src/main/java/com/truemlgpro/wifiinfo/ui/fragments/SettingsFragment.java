package com.truemlgpro.wifiinfo.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.truemlgpro.wifiinfo.R;
import com.truemlgpro.wifiinfo.interfaces.PreferenceDefaults;
import com.truemlgpro.wifiinfo.interfaces.PreferenceKeys;
import com.truemlgpro.wifiinfo.interfaces.PreferenceValues;
import com.truemlgpro.wifiinfo.utils.app.AppClipboardManager;
import com.truemlgpro.wifiinfo.utils.ui.InsetsController;
import com.truemlgpro.wifiinfo.utils.app.SharedPreferencesManager;

public class SettingsFragment extends PreferenceFragmentCompat {
	private AppBarLayout app_bar_layout;
	private RecyclerView recycler_view;
	private PackageInfo pi;
	private SwitchPreferenceCompat ntfcSwitchPreference;
	private ActivityResultLauncher<Intent> notificationSettingsLauncher;
	private ActivityResultLauncher<String> requestPermissionLauncher;

	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_settings, container, false);

		MaterialToolbar toolbar = root.findViewById(R.id.toolbar);
		AppCompatActivity activity = (AppCompatActivity) requireActivity();
		activity.setSupportActionBar(toolbar);

		ViewGroup prefContainer = root.findViewById(R.id.preference_container);
		View prefsView = super.onCreateView(inflater, prefContainer, savedInstanceState);
		prefContainer.addView(prefsView);

		app_bar_layout = root.findViewById(R.id.appbarlayout_settings);
		recycler_view = prefsView.findViewById(androidx.preference.R.id.recycler_view);

		initInsets();
		return root;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);

		initThemeDependencies();

		boolean notificationPreferenceState = new SharedPreferencesManager(requireActivity().getApplicationContext())
				.retrieveBoolean(PreferenceKeys.KEY_PREF_SHOW_NOTIFICATION, PreferenceDefaults.SHOW_NOTIFICATION);
		boolean areNotificationsEnabled = NotificationManagerCompat.from(requireActivity()).areNotificationsEnabled();

		ntfcSwitchPreference = findPreference(PreferenceKeys.KEY_PREF_SHOW_NOTIFICATION);
		ntfcSwitchPreference.setChecked(areNotificationsEnabled && notificationPreferenceState);
		ntfcSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			boolean isNtfcSwitchEnabled = (boolean) newValue;
			findPreference(PreferenceKeys.KEY_PREF_SHOW_NOTIFICATION);
			ListPreference ntfcFreq = findPreference(PreferenceKeys.KEY_PREF_NOTIFICATION_INTERVAL);
			SwitchPreferenceCompat ntfcColorize = findPreference(PreferenceKeys.KEY_PREF_COLORIZE_NOTIFICATION);
			SwitchPreferenceCompat ntfcVisSig = findPreference(PreferenceKeys.KEY_PREF_VISUALIZE_SIGNAL_STRENGTH);
			SwitchPreferenceCompat ntfcShowHide = findPreference(PreferenceKeys.KEY_PREF_SERVICE_SCREEN_STATE);

			ntfcFreq.setEnabled(isNtfcSwitchEnabled);
			ntfcColorize.setEnabled(isNtfcSwitchEnabled);
			ntfcVisSig.setEnabled(isNtfcSwitchEnabled);
			ntfcShowHide.setEnabled(isNtfcSwitchEnabled);

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
					if (!NotificationManagerCompat.from(requireActivity()).areNotificationsEnabled()) {
						openAppSettings();
					}
				}
			}
			return true;
		});

		notificationSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
				result -> {
					boolean nowEnabled = NotificationManagerCompat.from(requireActivity()).areNotificationsEnabled();
					ntfcSwitchPreference.setChecked(nowEnabled);
				});

		if (Build.VERSION.SDK_INT >= 33) {
			requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					if (!ntfcSwitchPreference.isChecked()) ntfcSwitchPreference.setChecked(true);
				} else {
					if (ntfcSwitchPreference.isChecked()) ntfcSwitchPreference.setChecked(false);
				}
			});
		}

		if (Build.VERSION.SDK_INT >= 26) {
			SwitchPreferenceCompat colorizeNtfcPref = findPreference(PreferenceKeys.KEY_PREF_COLORIZE_NOTIFICATION);
			if (colorizeNtfcPref != null) {
				colorizeNtfcPref.setVisible(true);
				colorizeNtfcPref.setIcon(R.drawable.settings_format_color_fill_24px);
			}
		}

		try {
			pi = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		setPreferenceSummary("app_version_pref", pi != null ? pi.versionName : "");
		setPreferenceSummary("android_version_pref", Build.VERSION.RELEASE);
		setPreferenceSummary("sdk_version_code_pref", String.valueOf(Build.VERSION.SDK_INT));
		setPreferenceSummary("device_model_pref", Build.MODEL);
		setPreferenceSummary("product_name_pref", Build.PRODUCT);

		setCopyOnPrefClickListener("app_version_pref");
		setCopyOnPrefClickListener("android_version_pref");
		setCopyOnPrefClickListener("sdk_version_code_pref");
		setCopyOnPrefClickListener("device_model_pref");
		setCopyOnPrefClickListener("product_name_pref");
	}

	private void initThemeDependencies() {
		SwitchPreferenceCompat darkSwitch = findPreference(PreferenceKeys.KEY_PREF_DARK_THEME);
		SwitchPreferenceCompat amoledSwitch = findPreference(PreferenceKeys.KEY_PREF_AMOLED_THEME);
		ListPreference amoledCardList = findPreference(PreferenceKeys.KEY_PREF_CARD_STYLE_AMOLED);
		SwitchPreferenceCompat monetSwitch = findPreference(PreferenceKeys.KEY_PREF_MONET_THEME);
		ListPreference contrastList = findPreference(PreferenceKeys.KEY_PREF_THEME_CONTRAST_VARIANT);

		if (darkSwitch != null && amoledSwitch != null && amoledCardList != null) {
			boolean isDark = darkSwitch.isChecked();
			amoledSwitch.setEnabled(isDark);
			amoledCardList.setEnabled(isDark && amoledSwitch.isChecked());

			darkSwitch.setOnPreferenceChangeListener((p, newVal) -> {
				boolean on = (Boolean) newVal;
				amoledSwitch.setEnabled(on);
				amoledCardList.setEnabled(on && amoledSwitch.isChecked());
				return true;
			});

			amoledSwitch.setOnPreferenceChangeListener((p, newVal) -> {
				boolean on = (Boolean) newVal;
				boolean darkOn = darkSwitch.isChecked();
				amoledCardList.setEnabled(darkOn && on);
				return true;
			});
		}

		if (monetSwitch != null && contrastList != null) {
			String currentContrast = contrastList.getValue();
			if (currentContrast == null || currentContrast.trim().isEmpty()) {
				currentContrast = PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED;
				contrastList.setValue(PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED);
			}

			boolean overlaysEnabled = !PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED.equals(currentContrast);
			if (overlaysEnabled) {
				monetSwitch.setChecked(false);
				monetSwitch.setEnabled(false);
			} else {
				monetSwitch.setEnabled(true);
			}

			if (monetSwitch.isChecked()) {
				if (!overlaysEnabled) {
					contrastList.setValue(PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED);
				}
				contrastList.setEnabled(false);
			} else {
				contrastList.setEnabled(true);
			}

			contrastList.setOnPreferenceChangeListener((preference, newValue) -> {
				String newVal = String.valueOf(newValue);
				boolean enablingOverlay = !PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED.equals(newVal);
				if (enablingOverlay) {
					monetSwitch.setChecked(false);
					monetSwitch.setEnabled(false);
				} else {
					monetSwitch.setEnabled(true);
				}
				return true;
			});

			monetSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
				boolean turnOn = (Boolean) newValue;
				if (turnOn) {
					if (!PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED.equals(contrastList.getValue())) {
						contrastList.setValue(PreferenceValues.THEME_CONTRAST_VARIANT_DISABLED);
					}
					contrastList.setEnabled(false);
				} else {
					contrastList.setEnabled(true);
				}
				return true;
			});
		}
	}

	private void initInsets() {
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
				recycler_view,
				new InsetsController.Config.Builder()
						.insetTypes(WindowInsetsCompat.Type.displayCutout())
						.edges(InsetsController.EDGE_HORIZONTAL)
						.applyToPadding()
						.consume(false)
						.build()
		);
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

	private void setCopyOnPrefClickListener(String key) {
		findPreference(key).setOnPreferenceClickListener(preference -> {
			AppClipboardManager.copyToClipboard(requireContext(), (String) preference.getTitle(), (String) preference.getSummary());
			return true;
		});
	}

	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	private boolean isNotificationPermissionGranted() {
		if (Build.VERSION.SDK_INT >= 33) {
			return hasPermissions(requireActivity(), Manifest.permission.POST_NOTIFICATIONS);
		}
		return true;
	}

	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	private void requestNotificationPermission() {
		requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
	}
}
