package com.truemlgpro.wifiinfo.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import com.truemlgpro.wifiinfo.R;

/**
 * A helper class for managing the clipboard functionality
 */
public class AppClipboardManager {
	/**
	 * Copies passed text to the clipboard
	 * @param appContext a context to pass, has to be an App Context
	 * @param label a user-visible label for the clip data
	 * @param text the text to be copied to the clip
	 */
	public static void copyToClipboard(Context appContext, String label, String text) {
		ClipboardManager cbm = (ClipboardManager) appContext.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, text);
		cbm.setPrimaryClip(clip);
		Toast.makeText(appContext, appContext.getString(R.string.copied_to_clipboard) + ": " + text, Toast.LENGTH_SHORT).show();
	}
}
