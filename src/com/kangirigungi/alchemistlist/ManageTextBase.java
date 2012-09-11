package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.kangirigungi.alchemistlist.Database.StringContainer;

public abstract class ManageTextBase extends Activity {
	private static final String TAG = "ManageTextBase";
	
	private long id;
	
	protected abstract StringContainer getStringContainer();
	protected abstract void initManageText(Bundle savedInstanceState);
	protected void prepareResult(Intent resultIntent) {}
	
	public long getId() {
		return id;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        initManageText(savedInstanceState);
        
        Button button = (Button)findViewById(R.id.manage_cancel);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
        button = (Button)findViewById(R.id.manage_save);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});
        
        Bundle extras = getIntent().getExtras();
        id = extras.getLong("id");
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_manage_ingredient, menu);
        menu.findItem(R.id.menu_deleteIngredient).
        		setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				delete();
				return false;
			}
		});
        return true;
    }
    
    private void refresh() {
    	Log.d(TAG, "refresh()");
    	EditText nameField = (EditText)findViewById(R.id.manage_name);
    	String value = getStringContainer().getString(id);
    	Log.v(TAG, "Name = " + value);
    	nameField.setText(value);
    }

    private void cancel() {
    	setResult(RESULT_CANCELED);
    	finish();
	}
    
    private void finishOk(boolean deleted) {
    	Intent result = new Intent();
    	prepareResult(result);
    	result.putExtra("deleted", deleted);
    	setResult(RESULT_OK, result);
    	finish();
	}
    
    private void save() {
    	EditText nameField = (EditText)findViewById(R.id.manage_name);
    	getStringContainer().changeString(id, nameField.getText().toString());
    	finishOk(false);
    }
    
    private void delete() {
    	getStringContainer().deleteString(id);
    	finishOk(true);
    }
}
