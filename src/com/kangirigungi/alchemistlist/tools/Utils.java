package com.kangirigungi.alchemistlist.tools;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class Utils {
	public static final int MAX_EFFECT_PER_INGREDIENT = 4;
	
	public static void printBundle(String TAG, Bundle bundle) {
		for (String key: bundle.keySet()) {
			Object obj = bundle.get(key);
			if (obj == null) {
				Log.v(TAG, key + " is null");
			} else {
				Log.v(TAG, key + " = " + obj.toString());
			}
		}
	}
	
	public static Long[] getLongArrayFromCursor(Cursor cursor, int columnIndex) {
		Long[] result = new Long[cursor.getCount()];
		int i = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			if (cursor.isNull(columnIndex)) {
				result[i] = null;
			} else {
				result[i] = cursor.getLong(columnIndex);
			}
			++i;
		}
		return result;
	}
	
	public static long getCountQuery(SQLiteDatabase db, String query, String[] args) {
		Cursor cursor = db.rawQuery(query, args);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			long result = cursor.getLong(0);
			cursor.close();
			return result;
		}
		return 0;
	}
	
	public static Cursor query(SQLiteDatabase db, String query, String[] args, String tag) {
		Log.v(tag, query);
		Cursor cursor = db.rawQuery(query, args);
		return cursor;
	}
}
