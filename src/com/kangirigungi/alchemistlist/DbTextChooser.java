package com.kangirigungi.alchemistlist;

import android.os.Bundle;

import com.kangirigungi.alchemistlist.Database.ConfigDbAdapter;
import com.kangirigungi.alchemistlist.Database.StringContainer;

public class DbTextChooser extends TextChooserBase {
//	private static final String TAG = "DbTextChooser";
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
