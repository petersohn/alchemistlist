package com.kangirigungi.alchemistlist.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DbAdapter.DatabaseHelper";
	
	private static final int DATABASE_VERSION = 6;
	
    DatabaseHelper(Context context, String dbName) {
        super(context, DbAdapter.DATABASE_NAME_BASE + dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            Log.i(TAG, "Opening database.");
            try {
            	db.execSQL("PRAGMA foreign_keys=ON;");
            	Cursor errors = db.rawQuery("PRAGMA integrity_check", null);
            	Log.v(TAG, "Number of messages found: "+errors.getCount());
            	for (errors.moveToFirst(); !errors.isAfterLast(); errors.moveToNext()) {
            		String s = errors.getString(0);
            		if (s.equals("ok")) {
            			Log.d(TAG, s);
            		} else {
            			Log.w(TAG, s);
            		}
            	}
            	errors.close();
            } catch (SQLException e) {
            	Log.e(TAG, e.getMessage());
            }
        }
    }
    
    void createExperimentsTable(SQLiteDatabase db) {
   	 db.execSQL("create table "+DbAdapter.TABLE_EXPERIMENTS+" (" +
   			DbAdapter.EXPERIMENTS_ID+" integer primary key," +
   			DbAdapter.EXPERIMENTS_ID1+" integer not null references "+
   			DbAdapter.TABLE_INGREDIENTS+"("+
   			DbAdapter.INGREDIENTS_ID+") on delete cascade," +
   			DbAdapter.EXPERIMENTS_ID2+" integer not null references "+
   			DbAdapter.TABLE_INGREDIENTS+"("+
   			DbAdapter.INGREDIENTS_ID+") on delete cascade" +
        		");");
   }
   
   @Override
   public void onCreate(SQLiteDatabase db) {
   	Log.i(TAG, "Creating database.");
       db.execSQL("create table "+DbAdapter.TABLE_INGREDIENTS+" (" +
    		   DbAdapter.INGREDIENTS_ID+" integer primary key," +
    		   DbAdapter.INGREDIENTS_VALUE+" text not null);");
       createExperimentsTable(db);
       createEffectsTable(db);
       createIngredientEffectTable(db);
       db.execSQL("PRAGMA foreign_keys=ON;");
   }

   private void recreateDatabase(SQLiteDatabase db) {
   	Log.w(TAG, "Recreating database. All old data will be destroyed.");
   	db.execSQL("DROP TABLE IF EXISTS "+DbAdapter.TABLE_INGREDIENTS);
       db.execSQL("DROP TABLE IF EXISTS "+DbAdapter.TABLE_EXPERIMENTS);
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
       if (oldVersion < 4) {
       	upgradeFrom3To4(db);
       }
       if (oldVersion < 6) {
       	upgradeFrom4To6(db);
       }
   }
   
   private void upgradeFrom2To3(SQLiteDatabase db) {
   	db.execSQL("alter table strings rename to " + DbAdapter.TABLE_INGREDIENTS);
   }
   
   private void upgradeFrom3To4(SQLiteDatabase db) {
   	createEffectsTable(db);
       createIngredientEffectTable(db);
   }
   
   private void upgradeFrom4To6(SQLiteDatabase db) {
   	createExperimentsTable(db);
       db.execSQL("insert into "+DbAdapter.TABLE_EXPERIMENTS+
       		" select * from assoc");
       db.execSQL("drop table assoc");
   }
   
   private void createEffectsTable(SQLiteDatabase db) {
   	db.execSQL("create table "+DbAdapter.TABLE_EFFECTS+" (" +
   			DbAdapter.EFFECTS_ID+" integer primary key," +
   			DbAdapter.EFFECTS_VALUE+" text not null);");
   }
   
   private void createIngredientEffectTable(SQLiteDatabase db) {
   	db.execSQL("create table "+DbAdapter.TABLE_INGREDIENT_EFFECT+" (" +
   			DbAdapter.INGREDIENT_EFFECT_ID+" integer primary key," +
   			DbAdapter.INGREDIENT_EFFECT_INGREDIENT+" integer not null references "+
   			DbAdapter.TABLE_INGREDIENTS+"("+DbAdapter.INGREDIENTS_ID+") on delete cascade," +
   			DbAdapter.INGREDIENT_EFFECT_EFFECT+" integer not null references "+
   			DbAdapter.TABLE_EFFECTS+"("+DbAdapter.EFFECTS_ID+") on delete cascade" +
       		");");
   	db.execSQL("create index ie_ingredient on "+DbAdapter.TABLE_INGREDIENT_EFFECT+
   			" ("+DbAdapter.INGREDIENT_EFFECT_INGREDIENT+")");
   	db.execSQL("create index ie_effect on "+DbAdapter.TABLE_INGREDIENT_EFFECT+
   			" ("+DbAdapter.INGREDIENT_EFFECT_EFFECT+")");
   }
} // DatabaseHelper
