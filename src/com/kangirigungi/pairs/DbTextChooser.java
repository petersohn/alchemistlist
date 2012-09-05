package com.kangirigungi.pairs;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kangirigungi.pairs.Database.Config;

public class DbTextChooser extends TextChooserBase {
	private static final String TAG = "DbTextChooser";
	private Config config;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        config = new Config(this);
        config.open();
	}
	
	@Override
   	protected void onDestroy() {
       	config.close();
   		super.onDestroy();
   	}
	
	@Override
	protected void fillList(String value, ListView listView) {
		Cursor cursor = config.getDatabasesWrapper().searchString(value, false);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		listView.setAdapter(null);
    	} else {
	    	listView.setAdapter(new SimpleCursorAdapter(
	    			this, android.R.layout.simple_list_item_1, 
	    			cursor, new String[] {Config.DATABASES_NAME}, 
	    			new int[] {android.R.id.text1}));
    	}
	}

	@Override
	protected String getValueFromId(long id) {
		String value = config.getDatabasesWrapper().getString(id);
		if (value == null) {
			Log.e(TAG, "Value not found: " + id);
		}
		return value;
	}

	@Override
	protected long getIdFromValue(String value) {
		return config.getDatabasesWrapper().addString(value);
	}

}
