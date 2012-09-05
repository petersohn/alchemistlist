package com.kangirigungi.pairs.tools;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StringTable {
	private static final String TAG = "StringTable";
	
	private SQLiteDatabase database;
	private String tableName;
	private String columnIdName;
	private String columnValueName;
	private String[] columnNames;
	
	public StringTable(SQLiteDatabase database, String tableName, 
			String columnIdName, String columnValueName) {
		this.database = database;
		this.tableName = tableName;
		this.columnIdName = columnIdName;
		this.columnValueName = columnValueName;
		this.columnNames = new String[] {columnIdName, columnValueName};
	}
	
	public String getString(long id) {
		Log.v(TAG, "getString("+id+")");
    	Cursor cursor =
    			database.query(tableName, 
	            		columnNames, 
	            		columnIdName+" = ?", 
	            		new String[] {Long.valueOf(id).toString()},
	                    null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	Log.v(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
            return cursor.getString(1);
        }
        Log.v(TAG, "No result");
        return null;
	}
	
	public Cursor searchString(String match, boolean exact) throws SQLException {
    	Log.v(TAG, "searchString("+match+", "+exact+")");
    	if (!exact) {
    		match += "%";
    	}
        Cursor cursor =
            database.query(tableName, 
            		columnNames, 
            		columnValueName+(exact ? " = ?" : " like ?"), 
            		new String[] {match},
                    null, null, columnValueName, null);
        if (cursor != null) {
        	Log.v(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
        }
        return cursor;
    }
	
	 public long addString(String value) throws SQLException {
	    	Log.v(TAG, "addString("+value+")");
	    	Cursor cursor = searchString(value, true);
	    	if (cursor != null && cursor.getCount() > 0) {
	    		Log.v(TAG, "Found in database.");
	    		cursor.moveToFirst();
	    		return cursor.getLong(0);
	    	}
	    	Log.v(TAG, "Not found in database.");
	        ContentValues args = new ContentValues();
	        args.put(columnValueName, value);
	        return database.insertOrThrow(tableName, null, args);
	    }
	
}
