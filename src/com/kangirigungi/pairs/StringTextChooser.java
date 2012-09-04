package com.kangirigungi.pairs;

import com.kangirigungi.pairs.DbAdapter.DbAdapter;

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
        dbAdapter.open(extras.getString("dbName"));
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
		Cursor cursor = dbAdapter.searchString(value, false);
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
		String value = dbAdapter.getString(id);
		if (value == null) {
			Log.e(TAG, "Value not found: " + id);
		}
		return value;
	}

	@Override
	protected long getIdFromValue(String value) {
		Cursor findResult = dbAdapter.searchString(value, true);
		if (findResult != null && findResult.getCount() > 0) {
			return findResult.getLong(0);
		}
		return dbAdapter.addString(value);
	}

}
