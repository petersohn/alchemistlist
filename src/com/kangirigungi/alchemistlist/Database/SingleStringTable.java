package com.kangirigungi.alchemistlist.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class SingleStringTable implements SingleStringContainer {

	private static String TAG = "SingleStringTable";
	
	private SQLiteDatabase database;
	private String tableName;
	private String columnValueName;
	private String[] columnNames;
	
	public SingleStringTable(SQLiteDatabase database, String tableName, 
			String columnValueName) {
		this.database = database;
		this.tableName = tableName;
		this.columnValueName = columnValueName;
		this.columnNames = new String[] {columnValueName};
	}
	@Override
	public String get() {
		Log.v(TAG, "get()");
        Cursor cursor =
            database.query(tableName, 
            		columnNames, 
            		null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	cursor.moveToFirst();
        	String result = cursor.getString(0);
        	Log.d(TAG, "Found value: " + result);
            return result;
        }
        if (cursor != null) {
        	cursor.close();
        }
        Log.d(TAG, "Value not found");
        return null;
	}

	@Override
	public void set(String value) {
		if (value != null) {
    		Log.d(TAG, "set("+value+")");
	    	ContentValues args = new ContentValues();
	        args.put(columnValueName, value);
	        if (get() == null) {
	        	database.insert(tableName, null, args);
	        } else {
	        	database.update(tableName, args, null, null);
	        }
    	} else {
    		Log.d(TAG, "Deleting database record.");
    		database.delete(tableName, null, null);
    	}
		
	}

}
