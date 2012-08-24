/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kangirigungi.pairs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DbAdapter {


    private static final String TAG = "DbAdapter";
    private static final String packageName = "com.kangirigungi.pairs";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase database;

    /**
     * Database creation sql statement
     */

    private static final String DATABASE_NAME = "data";
    
    private static final String TABLE_STRINGS = "strings";
    public static final String STRINGS_ID = "_id";
    public static final String STRINGS_VALUE = "value";
    
    private static final String TABLE_ASSOC = "assoc";
    public static final String ASSOC_ID = "_id";
    public static final String ASSOC_ID1 = "id1";
    public static final String ASSOC_ID2 = "id2";
    
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

    	private static final String TAG = "DbAdapter.DatabaseHelper";
    	
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.i(TAG, "Creating database.");
            db.execSQL("create table "+TABLE_STRINGS+" (" +
            		STRINGS_ID+" integer primary key," +
            		STRINGS_VALUE+" text not null);");
            db.execSQL("create table "+TABLE_ASSOC+" (" +
            		ASSOC_ID+" integer primary key," +
            		ASSOC_ID1+" integer not null references "+TABLE_STRINGS+"("+STRINGS_ID+") on delete cascade," +
            		ASSOC_ID2+" integer not null references "+TABLE_STRINGS+"("+STRINGS_ID+") on delete cascade" +
            		");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_STRINGS);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_ASSOC);
            onCreate(db);
        }
    } // DatabaseHelper

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        database = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public String getString(long id) {
    	Log.d(TAG, "getString("+id+")");
    	Cursor cursor =
    			database.query(TABLE_STRINGS, 
	            		new String[] {STRINGS_VALUE}, 
	            		STRINGS_ID+" = ?", 
	            		new String[] {Long.valueOf(id).toString()},
	                    null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	Log.d(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
            return cursor.getString(0);
        }
        Log.d(TAG, "No result");
        return null;
    }
    
    public Cursor searchString(String match) throws SQLException {
    	Log.d(TAG, "searchString("+match+")");
        Cursor cursor =
            database.query(TABLE_STRINGS, 
            		new String[] {STRINGS_ID,STRINGS_VALUE}, 
            		STRINGS_VALUE+" like ?", 
            		new String[] {match+"%"},
                    null, null, null, null);
        if (cursor != null) {
        	Log.d(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
        }
        return cursor;

    }

    public long addString(String name) throws SQLException {
    	Log.d(TAG, "addString("+name+")");
    	Cursor cursor =
                database.query(TABLE_STRINGS, 
                		new String[] {STRINGS_ID}, 
                		STRINGS_VALUE+" = ?", 
                		new String[] {name},
                        null, null, null, null);
    	if (cursor != null && cursor.getCount() > 0) {
    		Log.d(TAG, "Found in database.");
    		cursor.moveToFirst();
    		return cursor.getLong(0);
    	}
    	Log.d(TAG, "Not found in database.");
        ContentValues args = new ContentValues();
        args.put(STRINGS_VALUE, name);
        return database.insertOrThrow(TABLE_STRINGS, null, args);
    }
    
    public void deleteString(long id) {
    	Log.d(TAG, "deleteString("+id+")");
        database.delete(TABLE_STRINGS, STRINGS_ID+"=?", 
        		new String[] {Long.toString(id)});
    }
    
    public void changeString(long id, String value) {
    	Log.d(TAG, "changeString("+id+", "+value+")");
    	ContentValues args = new ContentValues();
    	args.put(STRINGS_VALUE, value);
        database.update(TABLE_STRINGS, args, 
        		STRINGS_ID+"=?",
        		new String[] {Long.toString(id)});
    }
    
    public void addAssoc(long id1, long id2) throws SQLException {
    	ContentValues args = new ContentValues();
        args.put(ASSOC_ID1, id1);
        args.put(ASSOC_ID2, id2);

        database.insertOrThrow(TABLE_ASSOC, null, args);
    }
    
    public void deleteAssoc(long id1, long id2) {
    	database.delete(TABLE_ASSOC, ASSOC_ID1+"=? and "+ASSOC_ID2+"=?", 
    			new String[] {Long.toString(id1), Long.toString(id2)});
    	database.delete(TABLE_ASSOC, ASSOC_ID1+"=? and "+ASSOC_ID2+"=?", 
    			new String[] {Long.toString(id2), Long.toString(id1)});
    }
   
    private String assocQueryString(String filter) {
    	return "select assoc."+ASSOC_ID+" "+ASSOC_ID+", "+
    			"strings1."+STRINGS_VALUE+" value1, "+
    			" strings2."+STRINGS_VALUE+" value2 "+
    			" from "+TABLE_ASSOC+" assoc, "+
        		TABLE_STRINGS+" strings1, "+
    			TABLE_STRINGS+" strings2 " +
        		"where ("+filter+") and " +
        		"strings1."+STRINGS_ID+"=assoc."+ASSOC_ID1+
        		" and strings2."+STRINGS_ID+"=assoc."+ASSOC_ID2;
    }
    
    private Cursor doSearchAssoc(String filter, String[] filterParams) {
    	Log.d(TAG, "searchAssoc()");
    	String query = assocQueryString(filter);
    	Log.v(TAG, query);
        Cursor cursor =
            database.rawQuery(query, filterParams);
        if (cursor != null) {
        	Log.d(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
        }
        return cursor;
    }
    
    public Cursor searchAssoc(long id) throws SQLException {
    	return doSearchAssoc(
    			"assoc."+ASSOC_ID1+"=?1 or " +
        		"assoc."+ASSOC_ID2+"=?1",
        		new String[] {Long.toString(id)});
    }
    
    public Cursor searchAssoc(long id1, long id2) throws SQLException {
    	return doSearchAssoc(
    			"(assoc."+ASSOC_ID1+"=?1 and " +
				"assoc."+ASSOC_ID2+"=?2) or" +
				"(assoc."+ASSOC_ID1+"=?2 and " +
				"assoc."+ASSOC_ID2+"=?1)",
				new String[] {Long.toString(id1), Long.toString(id2)});
    }
    
    public Long[] getAssoc(long id) {
    	Log.d(TAG, "getAssoc("+id+")");
    	Cursor cursor =
    			database.query(TABLE_ASSOC, 
	            		new String[] {ASSOC_ID1, ASSOC_ID2}, 
	            		ASSOC_ID+" = ?", 
	            		new String[] {Long.valueOf(id).toString()},
	                    null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	Log.d(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
            return new Long[] {
            		Long.valueOf(cursor.getLong(0)),
            		Long.valueOf(cursor.getLong(1))};
        }
        Log.d(TAG, "No result");
        return null;
    }

	public void cleanup() {
	}
	
	private void copyFile(File from, File to) throws IOException {
		FileChannel src = null;
        FileChannel dst = null;
        try {
      	  src = new FileInputStream(from).getChannel();
      	  dst = new FileOutputStream(to).getChannel();
            dst.transferFrom(src, 0, src.size());
        } finally {
      	  if (src != null) {
      		  src.close();
      	  }
      	  if (dst != null) {
      		  dst.close();
      	  }
        }
	}
	
	public void exportDatabase(String filename) throws IOException {
		File sd = Environment.getExternalStorageDirectory();
		File data = Environment.getDataDirectory();
		
		if (sd.canWrite()) {
		    String currentDBPath = "//data//"+ packageName +"//databases//"+DATABASE_NAME;
		    String backupDBPath = filename;
		    File currentDb = new File(data, currentDBPath);
		    File backupDb = new File(sd, backupDBPath);
		    copyFile(currentDb, backupDb);
		} else {
			Log.e(TAG, "SD card is not writable.");
		}
	}
	
	public void importDatabase(String filename) throws IOException {
		File sd = Environment.getExternalStorageDirectory();
		File data = Environment.getDataDirectory();
		
		if (sd.canRead()) {
		    String currentDBPath = "//data//"+ packageName +"//databases//"+DATABASE_NAME;
		    String backupDBPath = filename;
		    File currentDb = new File(data, currentDBPath);
		    File backupDb = new File(sd, backupDBPath);
		    copyFile(backupDb, currentDb);
		} else {
			Log.e(TAG, "SD card is not readable.");
		}
	}
}
