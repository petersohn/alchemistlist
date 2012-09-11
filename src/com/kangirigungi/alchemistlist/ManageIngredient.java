package com.kangirigungi.alchemistlist;

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
import com.kangirigungi.alchemistlist.tools.SubClickableListAdapter;

public class ManageIngredient extends ManageTextBase {
	private static final String TAG = "IngredientTextChooser";
	
	private DbAdapter dbAdapter;
	
	@Override
    public void initManageText(Bundle savedInstanceState) {
        setContentView(R.layout.activity_manage_ingredient);

        ListView list = (ListView)findViewById(R.id.manage_list);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				
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
        refreshList();
	}
	
	@Override
   	protected void onDestroy() {
       	dbAdapter.close();
   		super.onDestroy();
   	}
	    
	@Override
	protected StringContainer getStringContainer() {
		return dbAdapter.getIngredientsWrapper();
	}
	
	private void refreshList() {
		Log.d(TAG, "refreshList()");
    	Cursor cursor = dbAdapter.getEffectFromIngredient(getId());
    	ListView list = (ListView)findViewById(R.id.manage_list);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		list.setAdapter(null);
    	} else {
    		SubClickableListAdapter adapter = new SubClickableListAdapter(
    				new SimpleCursorAdapter(this, R.layout.activity_manage_list_item, 
	    			cursor, new String[] {DbAdapter.EFFECTS_ID}, 
	    			new int[] {R.id.text1}));
//    		adapter.setOnClickListener(R.id.btnRemove, );
	    	list.setAdapter(adapter);
    	}
	}

	

}
