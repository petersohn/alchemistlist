package com.kangirigungi.alchemistlist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.Database.StringContainer;

public class ManageEffect extends ManageTextBase {
	private static final String TAG = "ManageEffect";
	
	private static final int ACTIVITY_MANAGE_INGREDIENT = 1;
	
	private DbAdapter dbAdapter;
	
	@Override
    public void initManageText(Bundle savedInstanceState) {
        setContentView(R.layout.activity_manage_effect);

        ListView list = (ListView)findViewById(R.id.manage_list);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				manageIngredient(id);
			}
		});
        
        Bundle extras = getIntent().getExtras();
        dbAdapter = new DbAdapter(this);
        String dbName = extras.getString("dbName");
        if (dbName != null) {
        	dbAdapter.open(extras.getString("dbName"));
        } else {
        	Log.e(TAG, "No database.");
        	return;
        }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refreshList();
	}
	
	@Override
   	protected void onDestroy() {
       	dbAdapter.close();
   		super.onDestroy();
   	}
	    
	@Override
	protected StringContainer getStringContainer() {
		return dbAdapter.getEffectsWrapper();
	}
	
	private void refreshList() {
		Log.d(TAG, "refreshList()");
    	Cursor cursor = dbAdapter.getIngredientsFromEffect(getId());
    	final ListView list = (ListView)findViewById(R.id.manage_list);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		list.setAdapter(null);
    	} else {
    		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
    				this, android.R.layout.simple_list_item_1, 
	    			cursor, new String[] {DbAdapter.EFFECTS_VALUE}, 
	    			new int[] {android.R.id.text1});
	    	list.setAdapter(adapter);
    	}
	}

	private void manageIngredient(long id) {
		Intent i = new Intent(this, ManageIngredient.class);
   		i.putExtra("dbName", dbAdapter.getDbName());
   		i.putExtra("id", id);
        startActivityForResult(i, ACTIVITY_MANAGE_INGREDIENT);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_MANAGE_INGREDIENT:
    		if (resultCode == RESULT_OK) {
    			refreshList();
    		}
    		break;
    	}
    }
}
