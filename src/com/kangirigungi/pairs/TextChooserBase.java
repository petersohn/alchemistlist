package com.kangirigungi.pairs;

import android.app.Activity;
import android.content.Intent;
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

public abstract class TextChooserBase extends Activity {

	private static final String TAG = "TextChooserBase";
	
	protected abstract void fillList(String value, ListView listView);
	protected void prepareResult(Intent resultIntent) {}
	protected abstract String getValueFromId(long id);
	protected abstract long getIdFromValue(String value);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_chooser);
        
//        EditText valueField = (EditText)findViewById(R.id.textValue);
//        valueField.setText(extras.getCharSequence("value"));
        
        Button btn = (Button)findViewById(R.id.buttonOk);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "OK button clicked.");
				EditText valueField = (EditText)findViewById(R.id.textValue);
            	Intent resultIntent = new Intent();
            	prepareResult(resultIntent);
            	String value = valueField.getText().toString();
            	resultIntent.putExtra("result", value);
            	resultIntent.putExtra("id", getIdFromValue(value));
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
				Intent resultIntent = new Intent();
				prepareResult(resultIntent);
            	resultIntent.putExtra("result", getValueFromId(id));
            	resultIntent.putExtra("id", id);
            	setResult(RESULT_OK, resultIntent);
            	finish();
			}
		});
    }
    
    @Override
   	protected void onStart() {
   		super.onStart();
   		refreshList();
    }
  
    private void refreshList() {
    	Log.d(TAG, "refreshList()");
    	EditText valueField = (EditText)findViewById(R.id.textValue);
    	String value = valueField.getText().toString();
    	ListView listView = (ListView)findViewById(R.id.searchList);
    	fillList(value, listView);
    }
}
