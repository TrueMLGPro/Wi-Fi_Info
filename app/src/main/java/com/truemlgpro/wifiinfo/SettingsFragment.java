package com.truemlgpro.wifiinfo;

import static android.content.Context.CLIPBOARD_SERVICE;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
	private PackageInfo pi;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);

		if (android.os.Build.VERSION.SDK_INT >= 26) {
			CheckBoxPreference colorizeNtfcPref = findPreference("colorize_ntfc_checkbox");
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

		findPreference("app_version_pref").setOnPreferenceClickListener(preference -> {
			copyToClipboard((String) preference.getTitle(), (String) preference.getSummary());
			return true;
		});

		findPreference("android_version_pref").setOnPreferenceClickListener(preference -> {
			copyToClipboard((String) preference.getTitle(), (String) preference.getSummary());
			return true;
		});

		findPreference("sdk_version_code_pref").setOnPreferenceClickListener(preference -> {
			copyToClipboard((String) preference.getTitle(), (String) preference.getSummary());
			return true;
		});

		findPreference("device_model_pref").setOnPreferenceClickListener(preference -> {
			copyToClipboard((String) preference.getTitle(), (String) preference.getSummary());
			return true;
		});

		findPreference("product_name_pref").setOnPreferenceClickListener(preference -> {
			copyToClipboard((String) preference.getTitle(), (String) preference.getSummary());
			return true;
		});
	}

	private void setPreferenceSummary(String key, CharSequence summary) {
		Preference prefObj = findPreference(key);
		assert prefObj != null;
		prefObj.setSummary(summary);
	}

	private void copyToClipboard(String label, String text) {
		ClipboardManager cbm = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, text);
		cbm.setPrimaryClip(clip);
		Toast.makeText(requireContext(), getString(R.string.copied_to_clipboard) + ": " + text, Toast.LENGTH_SHORT).show();
	}
}
