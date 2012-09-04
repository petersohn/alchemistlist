package com.kangirigungi.pairs;

import com.kangirigungi.pairs.DbAdapter.DbAdapter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TextChooser extends Activity {

	private static final String TAG = "MainActivity";
	
	int textId;
	private DbAdapter dbAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_chooser);
        Bundle extras = getIntent().getExtras();
        textId = extras.getInt("textId");
//        EditText valueField = (EditText)findViewById(R.id.textValue);
//        valueField.setText(extras.getCharSequence("value"));
        
        Button btn = (Button)findViewById(R.id.buttonOk);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "OK button clicked.");
				EditText valueField = (EditText)findViewById(R.id.textValue);
            	Intent resultIntent = new Intent();
            	String value = valueField.getText().toString();
            	resultIntent.putExtra("result", value);
            	resultIntent.putExtra("textId", textId);
            	resultIntent.putExtra("id", dbAdapter.addString(value));
            	setResult(RESULT_OK, resultIntent);
            	finish();

			}
		});
        EditText valueField = (EditText)findViewById(R.id.textValue);
        valueField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				refreshList();
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
        ListView list = (ListView)findViewById(R.id.searchList);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				Log.d(TAG, "List item clicked.");
				String value = dbAdapter.getString(id);
				if (value == null) {
					Log.e(TAG, "Value not found: " + id);
					return;
				}
				Intent resultIntent = new Intent();
            	resultIntent.putExtra("result", value);
            	resultIntent.putExtra("textId", textId);
            	resultIntent.putExtra("id", id);
            	setResult(RESULT_OK, resultIntent);
            	finish();
			}
		});
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_text_chooser, menu);
        return true;
    }
    
    @Override
   	protected void onDestroy() {
       	dbAdapter.close();
   		super.onDestroy();
   	}
    
    private void refreshList() {
    	Log.d(TAG, "refreshList()");
    	EditText valueField = (EditText)findViewById(R.id.textValue);
    	String value = valueField.getText().toString();
    	ListView list = (ListView)findViewById(R.id.searchList);
    	if (value.length() == 0) {
    		Log.d(TAG, "Empty string.");
    		list.setAdapter(null);
    		return;
    	}
		Cursor cursor = dbAdapter.searchString(value);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		list.setAdapter(null);
    	} else {
	    	list.setAdapter(new SimpleCursorAdapter(
	    			this, android.R.layout.simple_list_item_1, 
	    			cursor, new String[] {DbAdapter.STRINGS_VALUE}, 
	    			new int[] {android.R.id.text1}));
    	}
    }
}
