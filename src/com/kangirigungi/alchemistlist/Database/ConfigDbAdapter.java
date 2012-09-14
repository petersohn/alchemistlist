package com.kangirigungi.alchemistlist.Database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class ConfigDbAdapter {
	private static final String TAG = "ConfigDbAdapter";
    
    private Context context;
    
    private DbManager dbManager;
    private SQLiteDatabase database;
    private StringTable databasesWrapper;
    
    /**
     * Database creation sql statement
     */

    private static final String DATABASE_NAME = "config";
    
    private static final String TABLE_DATABASES = "databases";
    public static final String DATABASES_ID = "_id";
    public static final String DATABASES_NAME = "name";
    
    private static final String TABLE_LAST_DATABASE = "last_database";
    public static final String LAST_DATABASE_ID = "_id";
    public static final String LAST_DATABASE_NAME = "name";
    
    private static final int DATABASE_VERSION = 2;

    private class DatabaseHelper extends SQLiteOpenHelper {

    	private static final String TAG = "ConfigDbAdapter.DatabaseHelper";
    	
        DatabaseHelper() {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.i(TAG, "Creating database.");
            db.execSQL("create table "+TABLE_DATABASES+" (" +
            		DATABASES_ID+" integer primary key," +
            		DATABASES_NAME+" text not null);");
            db.execSQL("create table "+TABLE_LAST_DATABASE+" (" +
            		LAST_DATABASE_ID+" integer primary key," +
            		LAST_DATABASE_NAME+" text not null);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            if (oldVersion < 2) {
            	upgradeFrom1To2(db);
            }
        }
        
        private void upgradeFrom1To2(SQLiteDatabase db) {
        	db.execSQL("create table "+TABLE_LAST_DATABASE+" (" +
            		LAST_DATABASE_ID+" integer primary key," +
            		LAST_DATABASE_NAME+" text not null);");
        }
    } // DatabaseHelper

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ConfigDbAdapter(Context ctx) {
        this.context = ctx;
        dbManager = new DbManager();
    }

    public ConfigDbAdapter open() throws SQLException {
    	dbManager.open(new DatabaseHelper());
    	database = dbManager.getDatabase();
    	databasesWrapper = new StringTable(database, 
    			TABLE_DATABASES, DATABASES_ID, DATABASES_NAME);
        return this;
    }

    public void close() {
        dbManager.close();
        database = null;
        databasesWrapper = null;
    }
    
    public StringTable getDatabasesWrapper() {
    	return databasesWrapper;
    }
    
    public void deleteDatabase(String name) {
    	Log.v(TAG, "deleteDatabase("+name+")");
        database.delete(TABLE_DATABASES, DATABASES_NAME+"=?", 
        		new String[] {name});
    	database.delete(TABLE_LAST_DATABASE, LAST_DATABASE_NAME+"=?", 
         		new String[] {name});
    }
    
    public void deleteLastDatabase() {
    	database.delete(TABLE_LAST_DATABASE, null, null); 
    }
    
    public String getLastDatabase() {
    	Log.v(TAG, "getLastDatabase()");
        Cursor cursor =
            database.query(TABLE_LAST_DATABASE, 
            		new String[] {LAST_DATABASE_NAME}, 
            		null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	cursor.moveToFirst();
        	String result = cursor.getString(0);
        	Log.d(TAG, "Found last database: " + result);
            return result;
        }
        if (cursor != null) {
        	cursor.close();
        }
        Log.d(TAG, "Last database not found");
        return null;
    }
    
    public void saveLastDatabase(String dbName) {
    	Log.d(TAG, "saveLastDatabase("+dbName+")");
    	ContentValues args = new ContentValues();
        args.put(LAST_DATABASE_NAME, dbName);
        if (getLastDatabase() == null) {
        	database.insert(TABLE_LAST_DATABASE, null, args);
        } else {
        	database.update(TABLE_LAST_DATABASE, args, null, null);
        }
    }
}
