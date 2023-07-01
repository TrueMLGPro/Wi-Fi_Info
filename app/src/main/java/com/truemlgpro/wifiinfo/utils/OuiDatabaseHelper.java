package com.truemlgpro.wifiinfo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class OuiDatabaseHelper extends SQLiteAssetHelper {
	private static final String DATABASE_NAME = "oui.db";
	private static final String TABLE_NAME = "oui";
	private static final String COLUMN_MAC = "mac";
	private static final String COLUMN_VENDOR = "vendor";

	public OuiDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@SuppressLint("Range")
	public String getVendorFromMac(String macAddress) {
		SQLiteDatabase db = this.getReadableDatabase();

		// Remove the last 3 octets from the input MAC address
		String formattedMacAddress = macAddress.replaceAll("[:-]", "").toUpperCase();
		formattedMacAddress = formattedMacAddress.substring(0, 6);

		String query = "SELECT " + COLUMN_VENDOR + " FROM " + TABLE_NAME +
				" WHERE " + COLUMN_MAC + " = '" + formattedMacAddress + "'";

		Cursor cursor = db.rawQuery(query, null);
		String vendor = null;
		if (cursor.moveToFirst()) {
			vendor = cursor.getString(cursor.getColumnIndex(COLUMN_VENDOR));
		}
		cursor.close();
		db.close();

		return vendor;
	}
}
