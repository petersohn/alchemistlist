package com.kangirigungi.alchemistlist.Database;

import java.io.File;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


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
    
    static final String TABLE_INGREDIENTS = "ingredients";
    public static final String INGREDIENTS_ID = "_id";
    public static final String INGREDIENTS_VALUE = "value";
    
    static final String TABLE_EFFECTS = "effects";
    public static final String EFFECTS_ID = "_id";
    public static final String EFFECTS_VALUE = "value";
    
    static final String TABLE_EXPERIMENTS = "experiments";
    public static final String EXPERIMENTS_ID = "_id";
    public static final String EXPERIMENTS_ID1 = "id1";
    public static final String EXPERIMENTS_ID2 = "id2";
    
    static final String TABLE_INGREDIENT_EFFECT = "ingredient_effect";
    public static final String INGREDIENT_EFFECT_ID = "_id";
    public static final String INGREDIENT_EFFECT_INGREDIENT = "ingredientId";
    public static final String INGREDIENT_EFFECT_EFFECT = "effectId";
    
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
	    			database, TABLE_INGREDIENTS, INGREDIENTS_ID, INGREDIENTS_VALUE);
	    	effectsWrapper = new StringTable(
	    			database, TABLE_EFFECTS, EFFECTS_ID, EFFECTS_VALUE);
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
      
    public void addAssoc(long id1, long id2) throws SQLException {
    	ContentValues args = new ContentValues();
        args.put(EXPERIMENTS_ID1, id1);
        args.put(EXPERIMENTS_ID2, id2);

        database.insertOrThrow(TABLE_EXPERIMENTS, null, args);
    }
    
    public void deleteAssoc(long id1, long id2) {
    	database.delete(TABLE_EXPERIMENTS, EXPERIMENTS_ID1+"=? and "+EXPERIMENTS_ID2+"=?", 
    			new String[] {Long.toString(id1), Long.toString(id2)});
    	database.delete(TABLE_EXPERIMENTS, EXPERIMENTS_ID1+"=? and "+EXPERIMENTS_ID2+"=?", 
    			new String[] {Long.toString(id2), Long.toString(id1)});
    }
   
    private static final String assocQueryBase =
    		"select * from "+TABLE_EXPERIMENTS+
    		" union "+
			"select "+EXPERIMENTS_ID+", "+
			EXPERIMENTS_ID2+" "+EXPERIMENTS_ID1+", "+
			EXPERIMENTS_ID1+" "+EXPERIMENTS_ID2+" from "+
			TABLE_EXPERIMENTS;
    
    private String assocQueryString(String filter) {
    	return "select assoc."+EXPERIMENTS_ID+" "+EXPERIMENTS_ID+", "+
    			"strings1."+INGREDIENTS_VALUE+" value1, "+
    			" strings2."+INGREDIENTS_VALUE+" value2 from " +
    			"("+assocQueryBase+") assoc, " +
				TABLE_INGREDIENTS+" strings1, "+
				TABLE_INGREDIENTS+" strings2 " +
				
				" where ("+filter+") and " +
				" strings1."+INGREDIENTS_ID+"=assoc."+EXPERIMENTS_ID1+
        		" and strings2."+INGREDIENTS_ID+"=assoc."+EXPERIMENTS_ID2+
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
        return dbManager.addCursor(cursor);
    }
    
    public Cursor searchExperiment(long id) throws SQLException {
    	return doSearchAssoc(
        		"assoc."+EXPERIMENTS_ID1+"=?1",
        		new String[] {Long.toString(id)});
    }
    
    public Cursor searchAssoc(long id1, long id2) throws SQLException {
    	return doSearchAssoc(
    			"(assoc."+EXPERIMENTS_ID1+"=?1 and " +
				"assoc."+EXPERIMENTS_ID2+"=?2)",
				new String[] {Long.toString(id1), Long.toString(id2)});
    }
    
    public Long[] getAssoc(long id) {
    	Log.v(TAG, "getAssoc("+id+")");
    	Cursor cursor =
    			database.query(TABLE_EXPERIMENTS, 
	            		new String[] {EXPERIMENTS_ID1, EXPERIMENTS_ID2}, 
	            		EXPERIMENTS_ID+" = ?", 
	            		new String[] {Long.valueOf(id).toString()},
	                    null, null, null, null);
    	Long[] result = null;
        if (cursor != null) {
        	if (cursor.getCount() > 0) {
	        	Log.v(TAG, "Number of results: " + cursor.getCount());
	            cursor.moveToFirst();
	            result = new Long[] {
	            		Long.valueOf(cursor.getLong(0)),
	            		Long.valueOf(cursor.getLong(1))};
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
    	ContentValues args = new ContentValues();
        args.put(INGREDIENT_EFFECT_INGREDIENT, ingredientId);
        args.put(INGREDIENT_EFFECT_EFFECT, effectId);

        database.insertOrThrow(TABLE_INGREDIENT_EFFECT, null, args);
    }
    
    public void deleteIngredientEffect(long ingredientId, long effectId) {
    	database.delete(
    			TABLE_INGREDIENT_EFFECT, 
    			INGREDIENT_EFFECT_INGREDIENT+"=? and "+
    			INGREDIENT_EFFECT_EFFECT+"=?", 
    			new String[] {Long.toString(ingredientId), Long.toString(effectId)});
    }
    
    public Cursor getEffectsFromIngredient(long ingredientId) {
    	Log.v(TAG, "getEffectFromIngredient("+ingredientId+")");
    	String queryString = 
    			"select "+TABLE_EFFECTS+"."+EFFECTS_ID+" "+EFFECTS_ID+", "+
    	    	TABLE_EFFECTS+"."+EFFECTS_VALUE+" "+EFFECTS_VALUE+" from "+
				TABLE_INGREDIENT_EFFECT+", "+TABLE_EFFECTS+" where "+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_INGREDIENT+"=? and "+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_EFFECT+
				"="+TABLE_EFFECTS+"."+EFFECTS_ID+
				" order by "+EFFECTS_VALUE;
    	Log.v(TAG, queryString);
    	Cursor cursor =
    			database.rawQuery(
    					queryString,
    					new String[] {Long.valueOf(ingredientId).toString()});
        return dbManager.addCursor(cursor);
    }
    
    public Cursor getIngredientsFromEffect(long effectId) {
    	Log.v(TAG, "getIngredientFromEffect("+effectId+")");
    	String queryString = 
    			"select "+TABLE_INGREDIENTS+"."+INGREDIENTS_ID+" "+INGREDIENTS_ID+", "+
    			TABLE_INGREDIENTS+"."+INGREDIENTS_VALUE+" "+INGREDIENTS_VALUE+" from "+
				TABLE_INGREDIENT_EFFECT+", "+TABLE_INGREDIENTS+" where "+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_EFFECT+"=? and "+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_INGREDIENT+
				"="+TABLE_INGREDIENTS+"."+INGREDIENTS_ID+
				" order by "+INGREDIENTS_VALUE;
    	Log.v(TAG, queryString);
    	Cursor cursor =
    			database.rawQuery(
    					queryString,
    					new String[] {Long.valueOf(effectId).toString()});
        return dbManager.addCursor(cursor);
    }
    
    private String getExcludedEffectsQuery(String selectedColumns) {
    	return "select distinct "+selectedColumns+" from "+
				TABLE_INGREDIENT_EFFECT+", "+TABLE_EFFECTS+
				", ("+assocQueryBase+") assoc where "+
				"assoc."+EXPERIMENTS_ID1+"=? and "+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_INGREDIENT+
				"=assoc."+EXPERIMENTS_ID2+" and "+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_EFFECT+
				"="+TABLE_EFFECTS+"."+EFFECTS_ID;
    }
    
    public Cursor getExcludedEffects(long ingredientId) {
    	Log.v(TAG, "getExcludedEffects("+ingredientId+")");
    	String queryString =
    			getExcludedEffectsQuery(
		    			TABLE_EFFECTS+"."+EFFECTS_ID+" "+EFFECTS_ID+", "+
		    	    	TABLE_EFFECTS+"."+EFFECTS_VALUE+" "+EFFECTS_VALUE)+
				" order by "+EFFECTS_VALUE;
    			
    	Log.v(TAG, queryString);
    	Cursor cursor =
    			database.rawQuery(
    					queryString,
    					new String[] {Long.valueOf(ingredientId).toString()});
        return dbManager.addCursor(cursor);
    }
    
    private String getExcludedIngredientsQuery(String selectedColumns) {
    	return "select distinct "+selectedColumns+" from "+
				TABLE_INGREDIENT_EFFECT+", "+
				TABLE_INGREDIENTS+", ("+assocQueryBase+") assoc where "+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_EFFECT+"=? and "+
				"assoc."+EXPERIMENTS_ID1+"="+
				TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_INGREDIENT+" and "+
				TABLE_INGREDIENTS+"."+INGREDIENTS_ID+
				"=assoc."+EXPERIMENTS_ID2;
    }
    
    public Cursor getExcludedIngredients(long effectId) {
    	Log.v(TAG, "getExcludedIngredients("+effectId+")");
    	String queryString = 
    			getExcludedIngredientsQuery(
    					TABLE_INGREDIENTS+"."+INGREDIENTS_ID+" "+INGREDIENTS_ID+", "+
    					TABLE_INGREDIENTS+"."+INGREDIENTS_VALUE+" "+INGREDIENTS_VALUE)+
				" order by "+EFFECTS_VALUE;
    			
    	Log.v(TAG, queryString);
    	Cursor cursor =
    			database.rawQuery(
    					queryString,
    					new String[] {Long.valueOf(effectId).toString()});
        return dbManager.addCursor(cursor);
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
