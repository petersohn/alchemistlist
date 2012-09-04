package com.kangirigungi.pairs.tools;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StringTable {
	private static final String TAG = "StringTable";
	
	private SQLiteDatabase database;
	private String tableName;
	private String[] columnNames;
	
	public StringTable(SQLiteDatabase database, String tableName, 
			String columnIdName, String columnValueName) {
		this.database = database;
		this.tableName = tableName;
		this.columnNames = new String[2];
		columnNames[0] = columnIdName;
		columnNames[1] = columnValueName;
	}
	
	public String getString(long id) {
		Log.v(TAG, "getString("+id+")");
    	Cursor cursor =
    			database.query(tableName, 
	            		columnNames, 
	            		columnNames[0]+" = ?", 
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
            		columnNames[1]+(exact ? " = ?" : " like ?"), 
            		new String[] {match},
                    null, null, columnNames[1], null);
        if (cursor != null) {
        	Log.v(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
        }
        return cursor;
    }
	
}
