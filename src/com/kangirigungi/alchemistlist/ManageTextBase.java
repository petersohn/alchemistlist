package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.kangirigungi.alchemistlist.Database.StringContainer;
import com.kangirigungi.alchemistlist.tools.InputQuery;
import com.kangirigungi.alchemistlist.tools.InputQueryResultListener;

public abstract class ManageTextBase extends Activity {
	private static final String TAG = "ManageTextBase";
	
	private long id;
	
	protected abstract StringContainer getStringContainer();
	protected abstract void initManageText(Bundle savedInstanceState);
	protected void prepareResult(Intent resultIntent) {}
	protected abstract CharSequence getRenameTitle();
	protected abstract CharSequence getRenameMessage();
	
	public long getId() {
		return id;
	}
	
	private Button btnRename;
	TextView nameField;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        initManageText(savedInstanceState);
        
        nameField = (TextView)findViewById(R.id.manage_name);
        btnRename = (Button)findViewById(R.id.manage_btnRename);
        
        btnRename.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				rename();
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
    
    private void rename() {
    	InputQuery q = new InputQuery(this);
    	q.run(getRenameTitle(), getRenameMessage(), nameField.getText(), 
    			new InputQueryResultListener() {
					
					@Override
					public void onOk(String result) {
						Log.i(TAG, "Renameing to "+result);
				    	getStringContainer().changeString(id, result);
				    	refresh();
					}
					
					@Override
					public void onCancel() {
						Log.d(TAG, "Rename cancelled.");
					}
				});
    }
    
    private void refresh() {
    	Log.d(TAG, "refresh()");
    	String value = getStringContainer().getString(id);
    	Log.v(TAG, "Name = " + value);
    	nameField.setText(value);
    }

    private void finishOk(boolean deleted) {
    	Intent result = new Intent();
    	prepareResult(result);
    	result.putExtra("deleted", deleted);
    	setResult(RESULT_OK, result);
    	finish();
	}
    
    private void delete() {
    	getStringContainer().deleteString(id);
    	finishOk(true);
    }
}
