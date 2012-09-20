package com.kangirigungi.alchemistlist.Database;

import java.io.File;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kangirigungi.alchemistlist.R;
import com.kangirigungi.alchemistlist.tools.Utils;


public class DbAdapter {
    private static final String TAG = "DbAdapter";
    
    private Context context;
    
    private DbManager dbManager;
    private String dbName;
    private SQLiteDatabase database;
    
    private StringTable ingredientsWrapper;
    private StringTable effectsWrapper;
    
    
    /**
     * Database creation sql statement
     */

    static final String DATABASE_NAME_BASE = "data_";
    
    
    
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
    
    public String getDbName() {
    	return dbName;
    }

    public DbAdapter open(String dbName) throws SQLException {
    	try {
	    	dbManager.open(new DatabaseHelper(context, dbName));
	    	database = dbManager.getDatabase();
	    	ingredientsWrapper = new StringTable(
	    			database, DbSqlQueries.TABLE_INGREDIENTS, 
	    			DbSqlQueries.INGREDIENTS_ID, 
	    			DbSqlQueries.INGREDIENTS_VALUE);
	    	effectsWrapper = new StringTable(
	    			database, DbSqlQueries.TABLE_EFFECTS, 
	    			DbSqlQueries.EFFECTS_ID, 
	    			  DbSqlQueries.EFFECTS_VALUE);
	    	this.dbName = dbName;
    	} catch (SQLException e) {
    		close();
    		throw e;
    	}
        return this;
    }

    public void close() {
        dbManager.close();
        database = null;
        ingredientsWrapper = null;
        effectsWrapper = null;
        dbName = null;
    }

    public StringTable getIngredientsWrapper() {
    	return ingredientsWrapper;
    }
    
    public StringTable getEffectsWrapper() {
    	return effectsWrapper;
    }
      
    public void addExperiment(long id1, long id2) throws SQLException {
    	if (hasExperiment(id1, id2)) {
    		Log.d(TAG, "Experiment already in database. Not inserting.");
    		return;
    	}
    	
    	ContentValues args = new ContentValues();
        args.put(DbSqlQueries.EXPERIMENTS_ID1, id1);
        args.put(DbSqlQueries.EXPERIMENTS_ID2, id2);
        database.insertOrThrow(DbSqlQueries.TABLE_EXPERIMENTS, null, args);
        
        args = new ContentValues();
        args.put(DbSqlQueries.EXPERIMENTS_ID1, id2);
        args.put(DbSqlQueries.EXPERIMENTS_ID2, id1);

        database.insertOrThrow(DbSqlQueries.TABLE_EXPERIMENTS, null, args);
    }
    
    public void deleteExperiment(long id1, long id2) {
    	database.delete(DbSqlQueries.TABLE_EXPERIMENTS, 
    			DbSqlQueries.searchExperiment2Where("?1", "?2"), 
    			new String[] {id1+"", id2+""});
    	database.delete(DbSqlQueries.TABLE_EXPERIMENTS, 
    			DbSqlQueries.searchExperiment2Where("?1", "?2"),
    			new String[] {id2+"", id1+""});
    }
   
    public boolean hasExperiment(long id1, long id2) {
    	return Utils.getCountQuery(database, 
    					"select count(*) from "+
    					DbSqlQueries.TABLE_EXPERIMENTS+" where " +
    					DbSqlQueries.searchExperiment2Where("?1", "?2"),
    					new String[] {id1+"", id2+""}) > 0;
    }
    
    private Cursor doSearchExperiment(String filter, String[] filterParams) {
    	Log.v(TAG, "searchAssoc()");
    	String query = DbSqlQueries.experimentQueryString(filter);
    	Log.v(TAG, query);
        Cursor cursor =
            database.rawQuery(query, filterParams);
        if (cursor != null) {
        	Log.d(TAG, "Number of results: " + cursor.getCount());
            cursor.moveToFirst();
        }
        return dbManager.addCursor(cursor);
    }
    
    public Cursor searchExperiment(long id) throws SQLException {
    	return doSearchExperiment(
    			DbSqlQueries.searchExperiment1Where("?1"),
        		new String[] {id+""});
    }
    
    public Cursor searchExperiment(long id1, long id2) throws SQLException {
    	return doSearchExperiment(
    			DbSqlQueries.searchExperiment2Where("?1", "?2"),
				new String[] {id1+"", id2+""});
    }
    
    public Long[] getExperiment(long id) {
    	Log.v(TAG, "getAssoc("+id+")");
    	Cursor cursor =
    			database.query(DbSqlQueries.TABLE_EXPERIMENTS, 
	            		new String[] {DbSqlQueries.EXPERIMENTS_ID1, DbSqlQueries.EXPERIMENTS_ID2}, 
	            		DbSqlQueries.EXPERIMENTS_ID+"=?", 
	            		new String[] {id+""},
	                    null, null, null, null);
    	Long[] result = null;
        if (cursor != null) {
        	if (cursor.getCount() > 0) {
	        	Log.v(TAG, "Number of results: " + cursor.getCount());
	            cursor.moveToFirst();
	            result = new Long[] {
	            		cursor.getLong(0),
	            		cursor.getLong(1)};
        	} else {
        		Log.v(TAG, "No result");
        	}
        	cursor.close();
        } else {
        	Log.v(TAG, "Null cursor");
        }
        
        return result;
    }
    
    public void addIngredientEffect(long ingredientId, long effectId) throws SQLException {
    	Log.v(TAG, "addIngredientEffect("+ingredientId+", "+effectId+")");
    	long num = Utils.getCountQuery(database, "select count(*) from "+
    			DbSqlQueries.TABLE_INGREDIENT_EFFECT+" where "+ 
    			DbSqlQueries.ingredientEffectWhere("?1", "?2"), 
    			new String[] {ingredientId+"", effectId+""});
    	if (num > 0) {
    		Log.d(TAG, "Ingredient and effect association already in database. Not inserting.");
    		return;
    	}
    	ContentValues args = new ContentValues();
        args.put(DbSqlQueries.INGREDIENT_EFFECT_INGREDIENT, ingredientId);
        args.put(DbSqlQueries.INGREDIENT_EFFECT_EFFECT, effectId);

        database.insertOrThrow(DbSqlQueries.TABLE_INGREDIENT_EFFECT, null, args);
    }
    
    public void deleteIngredientEffect(long ingredientId, long effectId) {
    	database.delete(
    			DbSqlQueries.TABLE_INGREDIENT_EFFECT, 
    			DbSqlQueries.ingredientEffectWhere("?1", "?2"), 
    			new String[] {ingredientId+"", effectId+""});
    }

    public long getEffectNum(long ingredientId) {
    	return Utils.getCountQuery(database, 
    			"select count(*) from "+
				DbSqlQueries.TABLE_INGREDIENT_EFFECT+" where "+
				DbSqlQueries.ingredientEffectIngredientWhere("?1"),
				new String[] {ingredientId+""});
    }
    
    public Cursor getEffectsFromIngredient(long ingredientId) {
    	Log.v(TAG, "getEffectFromIngredient("+ingredientId+")");
        return dbManager.addCursor(Utils.query(database, 
				DbSqlQueries.getEffectsQuery(
						DbSqlQueries.effectsIdAndValue(), "?1"), 
				new String[] {ingredientId+""}, TAG));
    }
    
    public Cursor getIngredientsFromEffect(long effectId) {
    	Log.v(TAG, "getIngredientFromEffect("+effectId+")");
    	return dbManager.addCursor(Utils.query(database, 
				DbSqlQueries.getIngredientsQuery(
						DbSqlQueries.ingredientsIdAndValue(), "?1"), 
				new String[] {effectId+""}, TAG));
    }
    
    public Cursor getExcludedEffects(long ingredientId) {
    	Log.v(TAG, "getExcludedEffects("+ingredientId+")");
    	return dbManager.addCursor(Utils.query(database, 
				DbSqlQueries.getExcludedEffectsQuery(
						DbSqlQueries.effectsIdAndValue(), "?1"), 
				new String[] {ingredientId+""}, TAG));
    }
    
    
    
    public Cursor getExcludedIngredients(long effectId) {
    	Log.v(TAG, "getExcludedIngredients("+effectId+")");
    	return dbManager.addCursor(Utils.query(database, 
				DbSqlQueries.getExcludedIngredientsQuery(
						DbSqlQueries.ingredientsIdAndValue(), "?1"), 
				new String[] {effectId+""}, TAG));
    }
    
    private String addNoAndMaybeParts(String queryString,
    		long ingredient1Id, long ingredient2Id, long maxEffectId) {
    	long count1 = getEffectNum(ingredient1Id);
    	long count2 = getEffectNum(ingredient2Id);
    	
    	String v1 = "?1";
    	String v2 = "?2";
    	if (count1 < Utils.MAX_EFFECT_PER_INGREDIENT) {
    		queryString += " union "+
    				DbSqlQueries.getPairingQueryMaybePart(v2, v1);
    	}
    	if (count2 < Utils.MAX_EFFECT_PER_INGREDIENT) {
    		queryString += " union "+
    				DbSqlQueries.getPairingQueryMaybePart(v1, v2);
    	}
    	if (count1 < Utils.MAX_EFFECT_PER_INGREDIENT && 
    			count2 < Utils.MAX_EFFECT_PER_INGREDIENT) {
    		queryString += " union "+
				DbSqlQueries.getPairingQueryNoPart(v1)+
    			" union "+
    			DbSqlQueries.getPairingQueryNoPart(v2);
    		for (long i = Math.max(count1, count2); 
    				i < Utils.MAX_EFFECT_PER_INGREDIENT; ++i) {
    			queryString += 
    					" union "+
    					DbSqlQueries.getPairingSomethingCategory(maxEffectId+i+1, "?");
    		}
    	}
    	return queryString;
    }
    
    public Cursor getPairing(long ingredient1Id, long ingredient2Id) {
    	Log.v(TAG, "getPairing("+ingredient1Id+", "+ingredient2Id+")");
    	
    	String queryString = DbSqlQueries.getPairingQueryYesPart("?1", "?2");
    	long maxEffectId = Utils.getCountQuery(database, 
    			"select max("+DbSqlQueries.EFFECTS_ID+") from "+
    			DbSqlQueries.TABLE_EFFECTS, null);
    	if (!hasExperiment(ingredient1Id, ingredient2Id)) {
	    	queryString = addNoAndMaybeParts(queryString, ingredient1Id, ingredient2Id,
	    			maxEffectId);
    	} else {
    		queryString += " union " +
    				DbSqlQueries.getPairingSomethingCategory(maxEffectId+1, 
    						context.getString(R.string.experimented));
    	}
    	queryString += " order by "+DbSqlQueries.PAIRING_CATEGORY+", "+
    			DbSqlQueries.EFFECTS_VALUE;
        return dbManager.addCursor(
        		Utils.query(database, queryString, 
        				new String[] {
						ingredient1Id+"",
						ingredient2Id+""}, TAG));
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
