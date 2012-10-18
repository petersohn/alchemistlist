package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;

import com.kangirigungi.alchemistlist.Database.StringContainer;

public abstract class TextChooserBase extends Activity {

	private static final String TAG = "TextChooserBase";

	protected abstract StringContainer getStringContainer();
	protected void prepareResult(Intent resultIntent) {}
	
	private RelativeLayout mainLayout;
	private ListView list;
	private EditText valueField;
	private Button btnOk;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_chooser);
        
        btnOk = (Button)findViewById(R.id.textChooser_buttonOk);
        btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "OK button clicked.");
            	Intent resultIntent = new Intent();
            	prepareResult(resultIntent);
            	String value = valueField.getText().toString();
            	resultIntent.putExtra("result", value);
            	resultIntent.putExtra("id", getIdFromValue(value));
            	setResult(RESULT_OK, resultIntent);
            	finish();

			}
		});
        valueField = (EditText)findViewById(R.id.textChooser_textValue);
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
        list = (ListView)findViewById(R.id.textChooser_searchList);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				Log.d(TAG, "List item clicked.");
				Intent resultIntent = new Intent();
				prepareResult(resultIntent);
            	resultIntent.putExtra("result", getValueFromId(id));
            	resultIntent.putExtra("id", id);
            	setResult(RESULT_OK, resultIntent);
            	finish();
			}
		});
        
        mainLayout = (RelativeLayout)findViewById(R.id.activity_text_chooser);

        Bundle extras = getIntent().getExtras();
        Integer backgroundColor = extras.getInt("background");
        if (backgroundColor != null && backgroundColor != 0) {
        	Log.v(TAG, String.format("Color = 0x%x", new Object[] {backgroundColor}));
        	mainLayout.setBackgroundColor(getResources().getColor(backgroundColor));
        }
    }
    
    @Override
   	protected void onStart() {
   		super.onStart();
   		refreshList();
    }
  
    public void refreshList() {
    	Log.d(TAG, "refreshList()");
    	String value = valueField.getText().toString();
    	btnOk.setEnabled(value.length() > 0);
    	fillList(value, list);
    	
    }
    
    private void fillList(String value, ListView listView) {
		Cursor cursor = getStringContainer().searchString(value, false);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		listView.setAdapter(null);
    	} else {
	    	listView.setAdapter(new SimpleCursorAdapter(
	    			this, android.R.layout.simple_list_item_1, 
	    			cursor, new String[] {cursor.getColumnName(1)}, 
	    			new int[] {android.R.id.text1}));
    	}
	}
    
    private String getValueFromId(long id) {
    	String value = getStringContainer().getString(id);
		if (value == null) {
			Log.e(TAG, "Value not found: " + id);
		}
		return value;
    }
    
	private long getIdFromValue(String value) {
		return getStringContainer().addString(value);
	}
}
