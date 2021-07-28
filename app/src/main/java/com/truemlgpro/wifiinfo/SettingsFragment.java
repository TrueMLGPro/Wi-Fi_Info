package com.truemlgpro.wifiinfo;

import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
{
	
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		
		if (android.os.Build.VERSION.SDK_INT < 26) {
			CheckBoxPreference prefState = (CheckBoxPreference) findPreference("colorize_ntfc_checkbox");
			prefState.setEnabled(false);
			if (prefState.isChecked()) {
				prefState.setChecked(false);
			}
			prefState.setSummary("Fill notification with accent color\n(Android 8+)");
		}
	}
	
}
