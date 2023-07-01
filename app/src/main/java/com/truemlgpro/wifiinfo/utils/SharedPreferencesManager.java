package com.truemlgpro.wifiinfo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Helper class for managing SharedPreferences
 */
public class SharedPreferencesManager {
	private final SharedPreferences sharedPreferences;
	private SharedPreferences.Editor sharedPrefsEditor;

	public SharedPreferencesManager(Context context) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	private SharedPreferences.Editor getEditor() {
		return sharedPreferences.edit();
	}

	/**
	 * Store a boolean value in SharedPreferences
	 * @param tag identifies the value
	 * @param value the value itself
	 */
	public void storeBoolean(String tag, boolean value) {
		sharedPrefsEditor = getEditor();
		sharedPrefsEditor.putBoolean(tag, value);
		sharedPrefsEditor.commit();
	}

	/**
	 * Store a string in SharedPreferences
	 * @param tag identifies the value
	 * @param str the string itself
	 */
	public void storeString(String tag, String str) {
		sharedPrefsEditor = getEditor();
		sharedPrefsEditor.putString(tag, str);
		sharedPrefsEditor.commit();
	}

	/**
	* Store an integer in SharedPreferences
	* @param tag identifies the value
	* @param defValue the value itself
	*/
	public void storeInt(String tag, int defValue) {
		sharedPrefsEditor = getEditor();
		sharedPrefsEditor.putInt(tag, defValue);
		sharedPrefsEditor.commit();
	}

	/**
	* Retrieve a boolean from SharedPreferences
	* @param tag identifies the value
	* @param defValue default value
	* @return the stored or default value
	*/
	public boolean retrieveBoolean(String tag, boolean defValue) {
		return sharedPreferences.getBoolean(tag, defValue);
	}

	/**
	* Retrieve a string from SharedPreferences
	* @param tag identifies the string
	* @param defStr default string
	* @return the stored or default string
	*/
	public String retrieveString(String tag, String defStr) {
		return sharedPreferences.getString(tag, defStr);
	}

	/**
	* Retrieve an integer from SharedPreferences
	* @param tag identifies the value
	* @param defValue default value
	* @return the stored or default value
	*/
	public int retrieveInt(String tag, int defValue) {
		return sharedPreferences.getInt(tag, defValue);
	}
}
