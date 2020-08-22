package com.truemlgpro.wifiinfo;

import android.os.*;
import android.content.*;
import android.preference.*;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.*;
import android.support.v7.preference.CheckBoxPreference;
import me.anwarshahriar.calligrapher.*;

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
