package com.kangirigungi.pairs.Database;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kangirigungi.pairs.tools.StringTable;

public class Config {
	private static final String TAG = "Config";
    
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
    
    private static final int DATABASE_VERSION = 1;

    private class DatabaseHelper extends SQLiteOpenHelper {

    	private static final String TAG = "Config.DatabaseHelper";
    	
        DatabaseHelper() {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.i(TAG, "Creating database.");
            db.execSQL("create table "+TABLE_DATABASES+" (" +
            		DATABASES_ID+" integer primary key," +
            		DATABASES_NAME+" text not null);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_DATABASES);
            onCreate(db);
        }
    } // DatabaseHelper

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public Config(Context ctx) {
        this.context = ctx;
        dbManager = new DbManager();
    }

    public Config open() throws SQLException {
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
    }
}
