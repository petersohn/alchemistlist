package com.kangirigungi.alchemistlist.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kangirigungi.alchemistlist.tools.Utils;

class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DbAdapter.DatabaseHelper";
	
	private static final int DATABASE_VERSION = 8;
	
	private String dbName;
	
    DatabaseHelper(Context context, String dbName) {
        super(context, DbAdapter.DATABASE_NAME_BASE + dbName, null, DATABASE_VERSION);
        this.dbName = dbName;
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

    private void createExperimentIndices(SQLiteDatabase db) {
    	db.execSQL("create index experiments_ids on "+
    			DbSqlQueries.TABLE_EXPERIMENTS+" ("+
    			DbSqlQueries.EXPERIMENTS_ID1+","+DbSqlQueries.EXPERIMENTS_ID1+")");
    }
    
    private void createExperimentsTable(SQLiteDatabase db) {
   	 db.execSQL("create table "+DbSqlQueries.TABLE_EXPERIMENTS+" (" +
   			DbSqlQueries.EXPERIMENTS_ID+" integer primary key," +
   			DbSqlQueries.EXPERIMENTS_ID1+" integer not null references "+
   			DbSqlQueries.TABLE_INGREDIENTS+"("+
   			DbSqlQueries.INGREDIENTS_ID+") on delete cascade," +
   			DbSqlQueries.EXPERIMENTS_ID2+" integer not null references "+
   			DbSqlQueries.TABLE_INGREDIENTS+"("+
   			DbSqlQueries.INGREDIENTS_ID+") on delete cascade" +
        		");");
   	 createExperimentIndices(db);
   }
    
   @Override
   public void onCreate(SQLiteDatabase db) {
   	Log.i(TAG, "Creating database.");
       db.execSQL("create table "+DbSqlQueries.TABLE_INGREDIENTS+" (" +
    		   DbSqlQueries.INGREDIENTS_ID+" integer primary key," +
    		   DbSqlQueries.INGREDIENTS_VALUE+" text not null);");
       createExperimentsTable(db);
       createEffectsTable(db);
       createIngredientEffectTable(db);
       createLastBackupTable(db);
       db.execSQL("PRAGMA foreign_keys=ON;");
   }

   private void recreateDatabase(SQLiteDatabase db) {
   	Log.w(TAG, "Recreating database. All old data will be destroyed.");
   	db.execSQL("DROP TABLE IF EXISTS "+DbSqlQueries.TABLE_INGREDIENTS);
       db.execSQL("DROP TABLE IF EXISTS "+DbSqlQueries.TABLE_EXPERIMENTS);
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
		if (oldVersion < 7) {
			upgradeFrom6To7(db);
		}
		if (oldVersion < 8) {
			upgradeFrom7To8(db);
		}
   }
   
   private void upgradeFrom2To3(SQLiteDatabase db) {
   	db.execSQL("alter table strings rename to " + DbSqlQueries.TABLE_INGREDIENTS);
   }
   
   private void upgradeFrom3To4(SQLiteDatabase db) {
   	createEffectsTable(db);
       createIngredientEffectTable(db);
   }
   
   private void upgradeFrom4To6(SQLiteDatabase db) {
   	createExperimentsTable(db);
       db.execSQL("insert into "+DbSqlQueries.TABLE_EXPERIMENTS+
       		" select * from assoc");
       db.execSQL("drop table assoc");
   }
   
   private void upgradeFrom6To7(SQLiteDatabase db) {
		createExperimentIndices(db);
		long maxId = Utils.getCountQuery(db, 
				"select max("+DbSqlQueries.EXPERIMENTS_ID+") from "+
						DbSqlQueries.TABLE_EXPERIMENTS, null) + 1;
		db.execSQL("insert into "+DbSqlQueries.TABLE_EXPERIMENTS+
				" select "+DbSqlQueries.EXPERIMENTS_ID+"+"+maxId+" "+
				DbSqlQueries.EXPERIMENTS_ID+", "+
				DbSqlQueries.EXPERIMENTS_ID2+" "+DbSqlQueries.EXPERIMENTS_ID1+", "+
				DbSqlQueries.EXPERIMENTS_ID1+" "+DbSqlQueries.EXPERIMENTS_ID2+" from "+
				DbSqlQueries.TABLE_EXPERIMENTS);
	}
   
   private void upgradeFrom7To8(SQLiteDatabase db) {
	   createLastBackupTable(db);
	}
   
   private void createEffectsTable(SQLiteDatabase db) {
   	db.execSQL("create table "+DbSqlQueries.TABLE_EFFECTS+" (" +
   			DbSqlQueries.EFFECTS_ID+" integer primary key," +
   			DbSqlQueries.EFFECTS_VALUE+" text not null);");
   }
   
   private void createIngredientEffectTable(SQLiteDatabase db) {
   	db.execSQL("create table "+DbSqlQueries.TABLE_INGREDIENT_EFFECT+" (" +
   			DbSqlQueries.INGREDIENT_EFFECT_ID+" integer primary key," +
   			DbSqlQueries.INGREDIENT_EFFECT_INGREDIENT+" integer not null references "+
   			DbSqlQueries.TABLE_INGREDIENTS+"("+DbSqlQueries.INGREDIENTS_ID+") on delete cascade," +
   			DbSqlQueries.INGREDIENT_EFFECT_EFFECT+" integer not null references "+
   			DbSqlQueries.TABLE_EFFECTS+"("+DbSqlQueries.EFFECTS_ID+") on delete cascade" +
       		");");
   	db.execSQL("create index ie_ingredient on "+DbSqlQueries.TABLE_INGREDIENT_EFFECT+
   			" ("+DbSqlQueries.INGREDIENT_EFFECT_INGREDIENT+")");
   	db.execSQL("create index ie_effect on "+DbSqlQueries.TABLE_INGREDIENT_EFFECT+
   			" ("+DbSqlQueries.INGREDIENT_EFFECT_EFFECT+")");
   }
   
   private void createLastBackupTable(SQLiteDatabase db) {
		db.execSQL("create table "+DbSqlQueries.TABLE_LAST_BACKUP+" (" +
			DbSqlQueries.LAST_BACKUP_ID+" integer primary key," +
			DbSqlQueries.LAST_BACKUP_NAME+" text not null);");
		ContentValues values = new ContentValues();
		values.put(DbSqlQueries.LAST_BACKUP_NAME, dbName+".db");
		db.insert(DbSqlQueries.TABLE_LAST_BACKUP, null, values);
   }
} // DatabaseHelper
