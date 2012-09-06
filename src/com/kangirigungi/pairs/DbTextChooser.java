package com.kangirigungi.pairs;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kangirigungi.pairs.Database.ConfigDbAdapter;
import com.kangirigungi.pairs.Database.StringContainer;

public class DbTextChooser extends TextChooserBase {
	private static final String TAG = "DbTextChooser";
	private ConfigDbAdapter config;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        config = new ConfigDbAdapter(this);
        config.open();
	}
	
	@Override
   	protected void onDestroy() {
       	config.close();
   		super.onDestroy();
   	}

	@Override
	protected StringContainer getStringContainer() {
		return config.getDatabasesWrapper();
	}
}
