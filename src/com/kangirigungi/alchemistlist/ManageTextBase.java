package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.app.Dialog;
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
import com.kangirigungi.alchemistlist.tools.YesNoDialog;

public abstract class ManageTextBase extends Activity {
	private static final String TAG = "ManageTextBase";
	
	private static final int DIALOG_RENAME = 0;
	private static final int DIALOG_DELETE = 1;
	
	private long id;
	
	protected abstract StringContainer getStringContainer();
	protected abstract void initManageText(Bundle savedInstanceState);
	protected void prepareResult(Intent resultIntent) {}
	protected abstract String getRenameTitle();
	protected abstract String getRenameMessage();
	protected abstract String getDeleteMessage();
	
	public long getId() {
		return id;
	}
	
	private Button btnRename;
	private TextView nameField;
	
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
				showDialog(DIALOG_RENAME, null);
			}
		});
        
        Bundle extras = getIntent().getExtras();
        id = extras.getLong("id");
        refresh();
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
    	switch(id) {
        case DIALOG_RENAME:
            return createRenameDialog(); 
        case DIALOG_DELETE:
        	return createDeleteDialog();
        default:
            return null;
        }
    }
    
    private Dialog createRenameDialog() {
    	return InputQuery.create(this, 
    			getRenameTitle(), getRenameMessage(), 
    			new InputQuery.ResultListener() {
					
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
    
    private Dialog createDeleteDialog() {
    	return YesNoDialog.create(this, null, 
    			getDeleteMessage(), 
    			new YesNoDialog.ResultListener() {
					@Override
					public void onYes() {
						Log.d(TAG, "Delete confirmed.");
		        	   getStringContainer().deleteString(getId());
		        	   finishOk(true);
					}
					
					@Override
					public void onNo() {
						Log.d(TAG, "Delete cancelled.");
					}
				});
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    	switch(id) {
        case DIALOG_RENAME:
        	InputQuery.setText(dialog, nameField.getText());
        	break;
    	default:
        	Log.w(TAG, "Invalid dialog id: "+id);	
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
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
    
    protected void refresh() {
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
    	showDialog(DIALOG_DELETE, null);
    }
}
