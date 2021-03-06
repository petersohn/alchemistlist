package com.kangirigungi.alchemistlist;

import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.Database.StringContainer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class IngredientTextChooser extends TextChooserBase {
	private static final String TAG = "IngredientTextChooser";
	
	private int textId;
	private DbAdapter dbAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        textId = extras.getInt("textId");
        
        dbAdapter = new DbAdapter(this);
        String dbName = extras.getString("dbName");
        if (dbName != null) {
        	dbAdapter.open(extras.getString("dbName"));
        } else {
        	Log.e(TAG, "No database.");
        }
	}
	
	@Override
   	protected void onDestroy() {
       	dbAdapter.close();
   		super.onDestroy();
   	}
	    
	@Override
	protected void prepareResult(Intent resultIntent) {
    	resultIntent.putExtra("textId", textId);
	}

	@Override
	protected StringContainer getStringContainer() {
		return dbAdapter.getIngredientsWrapper();
	}

	

}
