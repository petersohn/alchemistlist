package com.kangirigungi.alchemistlist.Database;

import java.io.File;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DbAdapter {
    private static final String TAG = "DbAdapter";
    
    private Context context;
    
    private DbManager dbManager;
    private SQLiteDatabase database;
    private StringTable ingredientsWrapper;
//    private StringTable effectsWrapper;
    
    /**
     * Database creation sql statement
     */

    private static final String DATABASE_NAME_BASE = "data_";
    
    private static final String TABLE_INGREDIENTS = "ingredients";
    public static final String INGREDIENTS_ID = "_id";
    public static final String INGREDIENTS_VALUE = "value";
    
//    private static final String TABLE_EFFECTS = "effects";
//    public static final String EFFECTS_ID = "_id";
//    public static final String EFFECTS_VALUE = "value";
//    
    private static final String TABLE_ASSOC = "assoc";
    public static final String ASSOC_ID = "_id";
    public static final String ASSOC_ID1 = "id1";
    public static final String ASSOC_ID2 = "id2";
    
    private static final int DATABASE_VERSION = 3;

    private class DatabaseHelper extends SQLiteOpenHelper {

    	private static final String TAG = "DbAdapter.DatabaseHelper";
    	
        DatabaseHelper(String dbName) {
            super(context, DATABASE_NAME_BASE + dbName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.i(TAG, "Creating database.");
            db.execSQL("create table "+TABLE_INGREDIENTS+" (" +
            		INGREDIENTS_ID+" integer primary key," +
            		INGREDIENTS_VALUE+" text not null);");
//            db.execSQL("create table "+TABLE_EFFECTS+" (" +
//            		EFFECTS_ID+" integer primary key," +
//            		EFFECTS_VALUE+" text not null);");
            db.execSQL("create table "+TABLE_ASSOC+" (" +
            		ASSOC_ID+" integer primary key," +
            		ASSOC_ID1+" integer not null references "+TABLE_INGREDIENTS+"("+INGREDIENTS_ID+") on delete cascade," +
            		ASSOC_ID2+" integer not null references "+TABLE_INGREDIENTS+"("+INGREDIENTS_ID+") on delete cascade" +
            		");");
        }

        private void recreateDatabase(SQLiteDatabase db) {
        	Log.w(TAG, "Recreating database. All old data will be destroyed.");
        	db.execSQL("DROP TABLE IF EXISTS "+TABLE_INGREDIENTS);
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_ASSOC);
            onCreate(db);
        }
        
        
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            if (oldVersion < 2) {
            	recreateDatabase(db);
            	return;
            }
            if (oldVersion < 3) {
            	upgradeFrom2To3(db);
            }
//            if (oldVersion < 4) {
//            	upgradeFrom3To4(db);
//            }
        }
        
        private void upgradeFrom2To3(SQLiteDatabase db) {
        	db.execSQL("alter table strings rename to " + TABLE_INGREDIENTS);
        }
        
        private void upgradeFrom3To4(SQLiteDatabase db) {
//        	db.execSQL("create table "+TABLE_EFFECTS+" (" +
//            		EFFECTS_ID+" integer primary key," +
//            		EFFECTS_VALUE+" text not null);");
        }
    } // DatabaseHelper

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.context = ctx;
        dbManager = new DbManager();
    }

    public DbAdapter open(String dbName) throws SQLException {
    	dbManager.open(new DatabaseHelper(dbName));
    	database = dbManager.getDatabase();
    	ingredientsWrapper = new StringTable(
    			database, TABLE_INGREDIENTS, INGREDIENTS_ID, INGREDIENTS_VALUE);
//    	effectsWrapper = new StringTable(
//    			database, TABLE_EFFECTS, EFFECTS_ID, EFFECTS_VALUE);
        return this;
    }

    public void close() {
        dbManager.close();
        database = null;
        ingredientsWrapper = null;
//        effectsWrapper = null;
    }

    public StringTable getIngredientsWrapper() {
    	return ingredientsWrapper;
    }
    
//    public StringTable getEffectsWrapper() {
//    	return effectsWrapper;
//    }
      
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
    			"strings1."+INGREDIENTS_VALUE+" value1, "+
    			" strings2."+INGREDIENTS_VALUE+" value2 from ("+
    			
    			"select * from "+TABLE_ASSOC+
        		" union "+
				"select "+ASSOC_ID+", "+
				ASSOC_ID2+" "+ASSOC_ID1+", "+
				ASSOC_ID1+" "+ASSOC_ID2+" from "+
				TABLE_ASSOC+
				") assoc, " +
				
				TABLE_INGREDIENTS+" strings1, "+
				TABLE_INGREDIENTS+" strings2 " +
				
				" where ("+filter+") and " +
				" strings1."+INGREDIENTS_ID+"=assoc."+ASSOC_ID1+
        		" and strings2."+INGREDIENTS_ID+"=assoc."+ASSOC_ID2+
        		" order by value1 asc, value2 asc";
    }
    
    private Cursor doSearchAssoc(String filter, String[] filterParams) {
    	Log.v(TAG, "searchAssoc()");
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
        		"assoc."+ASSOC_ID1+"=?1",
        		new String[] {Long.toString(id)});
    }
    
    public Cursor searchAssoc(long id1, long id2) throws SQLException {
    	return doSearchAssoc(
    			"(assoc."+ASSOC_ID1+"=?1 and " +
				"assoc."+ASSOC_ID2+"=?2)",
				new String[] {Long.toString(id1), Long.toString(id2)});
    }
    
    public Long[] getAssoc(long id) {
    	Log.v(TAG, "getAssoc("+id+")");
    	Cursor cursor =
    			database.query(TABLE_ASSOC, 
	            		new String[] {ASSOC_ID1, ASSOC_ID2}, 
	            		ASSOC_ID+" = ?", 
	            		new String[] {Long.valueOf(id).toString()},
	                    null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	Log.v(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
            return new Long[] {
            		Long.valueOf(cursor.getLong(0)),
            		Long.valueOf(cursor.getLong(1))};
        }
        Log.v(TAG, "No result");
        return null;
    }

	public void cleanup() {
	}
	
	public void backupDatabase(String filename) throws IOException {
		dbManager.backupDatabase(filename);
	}
	
	public void restoreDatabase(String filename) throws IOException {
		dbManager.restoreDatabase(filename);
	}
	
	public boolean deleteDatabase() {
		Log.d(TAG, "Deleting database");
		String path = database.getPath();
		close();
		File file = new File(path);
		if (file.delete()) {
			Log.i(TAG, "Database deleted");
			return true;
		} else {
			Log.w(TAG, "Failed to delete database");
			return false;
		}
	}
	
}
