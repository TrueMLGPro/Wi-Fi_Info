package com.truemlgpro.wifiinfo;

import android.support.v7.preference.*;
import android.support.v7.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Context;

public class SharedPreferencesManager {
		
		private SharedPreferences sPreferences;
		private SharedPreferences.Editor sEditor;
		private Context context;

		public SharedPreferencesManager(Context context) {
			this.context = context;
			sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		}

		private SharedPreferences.Editor getEditor() {
			return sPreferences.edit();
		}

		/**
		 * Store a boolean value in sharedPreferences
		 * @param tag identifies the value
		 * @param value the value itself
		 */

		public void storeBoolean(String tag, boolean value) {
			sEditor = getEditor();
			sEditor.putBoolean(tag,value);
			sEditor.commit();
		}
		/**
		 * Store a string in sharedPreferences
		 * @param tag identifies the value
		 * @param str the string itself
		 */

		public void storeString(String tag, String str) {
			sEditor = getEditor();
			sEditor.putString(tag, str);
			sEditor.commit();
		}

		/**
		 *
		 * @param tag identifies the value
		 * @param defValue default value
		 * @return the stored or default value
		 */

		public boolean retrieveBoolean(String tag, boolean defValue) {
			return sPreferences.getBoolean(tag,defValue);
		}

		/**
		 *
		 * @param tag identifies the string
		 * @param defStr default string
		 * @return the stored or default string
		 */

		public String retrieveString(String tag, String defStr) {
			return sPreferences.getString(tag, defStr);
		}

		/**
		 *
		 * @param tag identifies the value
		 * @param defValue default value
		 * @return the stored or default value
		 */
		public int retrieveInt(String tag, int defValue) {
			return sPreferences.getInt(tag, defValue);
		}

		/**
		 *
		 * @param tag identifies the value
		 * @param defValue the value itself
		 */
		public void storeInt(String tag, int defValue) {
			sEditor = getEditor();
			sEditor.putInt(tag, defValue);
			sEditor.commit();
		}
}
