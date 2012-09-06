package com.kangirigungi.pairs;

import com.kangirigungi.pairs.Database.DbAdapter;
import com.kangirigungi.pairs.Database.StringContainer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StringTextChooser extends TextChooserBase {
	private static final String TAG = "StringTextChooser";
	
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
		return dbAdapter.getStringsWrapper();
	}

	

}
