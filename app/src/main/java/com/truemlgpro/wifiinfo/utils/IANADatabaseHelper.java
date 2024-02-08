package com.truemlgpro.wifiinfo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class IANADatabaseHelper extends SQLiteAssetHelper {
	private static final String DATABASE_NAME = "iana_ports.db";
	private static final String TABLE_NAME = "ports";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_NUMBER = "number";
	private static final String COLUMN_DESCRIPTION = "description";
	private static final String COLUMN_PROTOCOL = "protocol";

	public IANADatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@SuppressLint("Range")
	public String getServiceName(int portNumber, String protocol) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + COLUMN_NAME + " FROM " + TABLE_NAME +
				" WHERE (" + COLUMN_NUMBER + " = ? AND " + COLUMN_PROTOCOL + " = '')" +
				" OR (" + COLUMN_NUMBER + " = ? AND " + COLUMN_PROTOCOL + " = ?)";

		String[] selectionArgs = new String[3];
		selectionArgs[0] = Integer.toString(portNumber);
		selectionArgs[1] = Integer.toString(portNumber);
		selectionArgs[2] = protocol;

		Cursor cursor = db.rawQuery(query, selectionArgs);
		String serviceName = null;
		if (cursor.moveToFirst()) {
			serviceName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
			if (serviceName == null || serviceName.isEmpty()) {
				serviceName = "-";
			}
		}
		cursor.close();
		db.close();

		return serviceName;
	}

	@SuppressLint("Range")
	public String getServiceDescription(int portNumber, String protocol) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + COLUMN_DESCRIPTION + " FROM " + TABLE_NAME +
				" WHERE (" + COLUMN_NUMBER + " = ? AND " + COLUMN_PROTOCOL + " = '')" +
				" OR (" + COLUMN_NUMBER + " = ? AND " + COLUMN_PROTOCOL + " = ?)";

		String[] selectionArgs = new String[3];
		selectionArgs[0] = Integer.toString(portNumber);
		selectionArgs[1] = Integer.toString(portNumber);
		selectionArgs[2] = protocol;

		Cursor cursor = db.rawQuery(query, selectionArgs);
		String serviceDescription = null;
		if (cursor.moveToFirst()) {
			serviceDescription = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
		}
		cursor.close();
		db.close();

		return serviceDescription;
	}
}
