package com.kangirigungi.alchemistlist.Database;

public class DbSqlQueries {
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
    
    public static final String PAIRING_CATEGORY = "category";
    public static final int CATEGORY_SOMETHING = 0;
    public static final int CATEGORY_YES = 1;
    public static final int CATEGORY_MAYBE = 2;
    public static final int CATEGORY_NO = 3;
    
    
    
    static String columnSelector(String table, String column) {
    	return table+"."+column+" "+column;
    }
    
    static String effectsIdAndValue() {
    	return columnSelector(TABLE_EFFECTS, EFFECTS_ID)+", "+
    			columnSelector(TABLE_EFFECTS, EFFECTS_VALUE);
    }
    
    static String ingredientsIdAndValue() {
    	return columnSelector(TABLE_INGREDIENTS, INGREDIENTS_ID)+", "+
				columnSelector(TABLE_INGREDIENTS, INGREDIENTS_VALUE);
    }
    
    static String searchExperiment1Where(String variable) {
    	return TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID1+"="+variable;
    }
    
    static String searchExperiment2Where(String variable1, String variable2) {
    	return searchExperiment1Where(variable1)+" and "+
    			TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID2+"="+variable2;
    }
    
    static String experimentQueryString(String filter) {
    	return "select "+TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID+" "+EXPERIMENTS_ID+", "+
    			"strings1."+INGREDIENTS_VALUE+" value1, "+
    			"strings2."+INGREDIENTS_VALUE+" value2 from " +
    			TABLE_EXPERIMENTS+", " +
				TABLE_INGREDIENTS+" strings1, "+
				TABLE_INGREDIENTS+" strings2 " +
				
				" where ("+filter+") and " +
				" strings1."+INGREDIENTS_ID+"="+TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID1+
        		" and strings2."+INGREDIENTS_ID+"="+TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID2+
        		" order by value1 asc, value2 asc";
    }
    
    static String ingredientEffectIngredientWhere(String variable) {
    	return 
    			TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_INGREDIENT+"="+
    			variable;
    }
    
    static String ingredientEffectEffectWhere(String variable) {
    	return 
    			TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_EFFECT+"="+
    			variable;
    }
    
    static String ingredientEffectWhere(String variableIngredient, String variableEffect) {
    	return 
    			ingredientEffectIngredientWhere(variableIngredient)+" and "+
    			ingredientEffectEffectWhere(variableEffect);
    }
    
    static String getEffectsQuery(String selectedColumns, String variable) {
    	return "select "+selectedColumns+" from "+
				DbSqlQueries.TABLE_INGREDIENT_EFFECT+", "+
    			DbSqlQueries.TABLE_EFFECTS+" where "+
				ingredientEffectWhere(variable, TABLE_EFFECTS+"."+EFFECTS_ID);
    }
    
    static String getIngredientsQuery(String selectedColumns, String variable) {
    	return "select "+selectedColumns+" from "+
				DbSqlQueries.TABLE_INGREDIENT_EFFECT+", "+
    			DbSqlQueries.TABLE_INGREDIENTS+" where "+
				ingredientEffectWhere(TABLE_INGREDIENTS+"."+INGREDIENTS_ID, "?1");
    }
    
    static String getExcludedEffectsQuery(String selectedColumns, String variable) {
    	return "select distinct "+selectedColumns+" from "+
				TABLE_INGREDIENT_EFFECT+", "+
    			TABLE_EFFECTS+", "+TABLE_EXPERIMENTS+" where "+
				searchExperiment1Where(variable)+" and "+
    			ingredientEffectWhere(
    					TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID2, 
    					TABLE_EFFECTS+"."+EFFECTS_ID)+
				" except "+getEffectsQuery(selectedColumns, variable);
    }
    
    static String getExcludedIngredientsQuery(String selectedColumns, String variable) {
    	return "select distinct "+selectedColumns+" from "+
				TABLE_INGREDIENT_EFFECT+", "+
				TABLE_INGREDIENTS+", "+TABLE_EXPERIMENTS+" where "+
				searchExperiment2Where(
						TABLE_INGREDIENT_EFFECT+"."+INGREDIENT_EFFECT_INGREDIENT, 
						TABLE_INGREDIENTS+"."+INGREDIENTS_ID)+" and "+
				ingredientEffectEffectWhere(variable)+
				" except "+getIngredientsQuery(selectedColumns, variable);
    }
    
    static String pairingColumns(int value) {
    	return effectsIdAndValue()+", "+value+" "+PAIRING_CATEGORY;
    }
    
    static String getCommonEffectsQuery(String selectedColumns,
    		String variable1, String variable2) {
    	return getEffectsQuery(selectedColumns, variable1)+
        		" intersect "+
        		getEffectsQuery(selectedColumns, variable2);
    }
    
    static String getPairingQueryYesPart(
    		String variable1, String variable2) {
    	return "select * from ("+
    		getCommonEffectsQuery(pairingColumns(CATEGORY_YES),
    				variable1, variable2)+")";
    }
    
    static String getPairingQueryNoPart(String variable) {
    	return "select * from ("+
    			getExcludedEffectsQuery(pairingColumns(CATEGORY_NO), variable)+")";
    }
    
    static String getPairingQueryMaybePart(
    		String variable1, String variable2) {
    	return "select * from ("+
    		getEffectsQuery(pairingColumns(CATEGORY_MAYBE), variable1)+
    		" except "+
    		getEffectsQuery(pairingColumns(CATEGORY_MAYBE), variable2)+
        	" except "+
    		getExcludedEffectsQuery(pairingColumns(CATEGORY_MAYBE), variable2)+")";
    }
    
    static String getPairingSomethingCategory(long id, String value) {
    	return "select "+id+" "+EFFECTS_ID+
				", '"+value+"' "+EFFECTS_VALUE+", "+
				CATEGORY_SOMETHING+" "+PAIRING_CATEGORY;
    }
    
    static final String experimentQueryColumns =
    		TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID+" "+EXPERIMENTS_ID+", "+
			"i1."+INGREDIENTS_VALUE+" value1, "+
			"i2."+INGREDIENTS_VALUE+" value2";
    
    static String getExperimentQuery(String selectedColumns, String variable) {
    	return "select "+selectedColumns+", count(assoc.id1) count_ from "+
    			TABLE_EXPERIMENTS+" left join "+
    			"(select assoc1."+INGREDIENT_EFFECT_INGREDIENT+" id1, "+
    			"assoc2."+INGREDIENT_EFFECT_INGREDIENT+" id2 from "+
    			TABLE_INGREDIENT_EFFECT+" assoc1, "+
    			TABLE_INGREDIENT_EFFECT+" assoc2 where "+
    			"id1="+variable+
    			" and assoc1."+INGREDIENT_EFFECT_EFFECT+"="+
    			"assoc2."+INGREDIENT_EFFECT_EFFECT+
    			") assoc on "+
    			searchExperiment2Where("assoc.id1", "assoc.id2")+
    			", "+TABLE_INGREDIENTS+" i1, "+TABLE_INGREDIENTS+" i2 "+
    			"where "+searchExperiment1Where(variable)+" and "
    			+searchExperiment2Where(
    					"i1."+INGREDIENTS_ID,
    					"i2."+INGREDIENTS_ID)+
    			" group by "+TABLE_EXPERIMENTS+"."+EXPERIMENTS_ID+
    			" order by count_ desc, i1."+
    			INGREDIENTS_ID+" asc, i2."+INGREDIENTS_ID+" asc";
    }
}
