package com.kangirigungi.pairs;

import com.kangirigungi.pairs.Database.DbAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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
	protected void fillList(String value, ListView listView) {
		if (value.length() == 0) {
    		Log.d(TAG, "Empty string.");
    		listView.setAdapter(null);
    		return;
    	}
		Cursor cursor = dbAdapter.getStringsWrapper().searchString(value, false);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		listView.setAdapter(null);
    	} else {
	    	listView.setAdapter(new SimpleCursorAdapter(
	    			this, android.R.layout.simple_list_item_1, 
	    			cursor, new String[] {DbAdapter.STRINGS_VALUE}, 
	    			new int[] {android.R.id.text1}));
    	}
	}
	
	@Override
	protected void prepareResult(Intent resultIntent) {
    	resultIntent.putExtra("textId", textId);
	}

	@Override
	protected String getValueFromId(long id) {
		String value = dbAdapter.getStringsWrapper().getString(id);
		if (value == null) {
			Log.e(TAG, "Value not found: " + id);
		}
		return value;
	}

	@Override
	protected long getIdFromValue(String value) {
		return dbAdapter.getStringsWrapper().addString(value);
	}

}
