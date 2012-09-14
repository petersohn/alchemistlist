package com.kangirigungi.alchemistlist.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Vector;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;


public class DbManager {

	private static final String TAG = "DbManager";
	
    private boolean isOpen;
    
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;
    private Vector<Cursor> storedCursors;
    
    DbManager() {
    	this.isOpen = false;
    	storedCursors = new Vector<Cursor>();
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
    public void open(SQLiteOpenHelper helper) throws SQLException {
    	if (isOpen) {
    		close();
    	}
    	Log.v(TAG, "open()");
        dbHelper = helper;
        database = dbHelper.getWritableDatabase();
        isOpen = true;
    }

    public void close() {
    	Log.v(TAG, "close()");
    	if (!isOpen) {
    		Log.v(TAG, "Database already closed.");
    		return;
    	}
    	Log.d(TAG, "Closing database: " + database.getPath());
    	Log.v(TAG, "Closing cursors");
    	for (Cursor cursor: storedCursors) {
    		cursor.close();
    	}
    	Log.v(TAG, "Closing database");
        dbHelper.close();
        database = null;
        isOpen = false;
    }
    
    public Cursor addCursor(Cursor cursor) {
    	if (cursor != null) {
    		storedCursors.add(cursor);
    	}
    	return cursor;
    }
    
    public SQLiteDatabase getDatabase() {
    	return database;
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
	
	public void backupDatabase(String filename) throws IOException {
		File sd = Environment.getExternalStorageDirectory();
		
		if (sd.canWrite()) {
		    String currentDBPath = database.getPath();
		    String backupDBPath = filename;
		    File currentDb = new File(currentDBPath);
		    File backupDb = new File(sd, backupDBPath);
		    copyFile(currentDb, backupDb);
		} else {
			Log.e(TAG, "SD card is not writable.");
		}
	}
	
	public void restoreDatabase(String filename) throws IOException {
		File sd = Environment.getExternalStorageDirectory();
		
		if (sd.canRead()) {
		    String currentDBPath = database.getPath();
		    String backupDBPath = filename;
		    File currentDb = new File(currentDBPath);
		    File backupDb = new File(sd, backupDBPath);
		    copyFile(backupDb, currentDb);
		} else {
			Log.e(TAG, "SD card is not readable.");
		}
	}
}
