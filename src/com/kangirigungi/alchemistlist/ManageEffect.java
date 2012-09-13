package com.kangirigungi.alchemistlist;

import java.util.Vector;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.Database.StringContainer;
import com.kangirigungi.alchemistlist.tools.MultiListAdapter;
import com.kangirigungi.alchemistlist.tools.SubClickableListAdapter;
import com.kangirigungi.alchemistlist.tools.SubClickableAdapter.OnSubItemClickListener;

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
	
	private class IngredientClicked implements OnSubItemClickListener {
		private ListView list;
		
		IngredientClicked() {
			list = (ListView)findViewById(R.id.manage_list);
		}
		
		public void onSubItemClick(View subView, int position) {
			long id = list.getItemIdAtPosition(position);
			Log.v(TAG, "Click on item. Position: "+position+". Id: "+
					dbAdapter.getEffectsWrapper().getString(id));
			manageIngredient(id);
		}
	}
	
	private void addEffectsAdapter(Vector<ListAdapter> adapters) {
		Cursor cursor = dbAdapter.getIngredientsFromEffect(getId());
    	if (cursor == null) {
    		Log.d(TAG, "No normal result.");
    	} else {
			SubClickableListAdapter itemAdapter = new SubClickableListAdapter(
					new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
	    			cursor, new String[] {DbAdapter.EFFECTS_VALUE}, 
	    			new int[] {android.R.id.text1}));
			itemAdapter.setOnClickListener(android.R.id.text1, new IngredientClicked());
			adapters.add(itemAdapter);
    	}
	}
	
	private void addExcludedIngredientsAdapter(Vector<ListAdapter> adapters) {
		Cursor cursor = dbAdapter.getExcludedIngredients(getId());
    	if (cursor == null) {
    		Log.d(TAG, "No excluded result.");
    	} else {
    		SubClickableListAdapter excludedAdapter = new SubClickableListAdapter(
	    			new SimpleCursorAdapter(this, R.layout.activity_manage_list_item_excluded, 
	    			cursor, new String[] {DbAdapter.EFFECTS_VALUE}, 
	    			new int[] {R.id.text1}));
    		excludedAdapter.setOnClickListener(
    				R.id.text1, 
    				new IngredientClicked());
	    	adapters.add(excludedAdapter);
    	}
	}
	
	private void refreshList() {
		Log.d(TAG, "refreshList()");
		Vector<ListAdapter> adapters = new Vector<ListAdapter>();
		addEffectsAdapter(adapters);
		addExcludedIngredientsAdapter(adapters);
		ListView list = (ListView)findViewById(R.id.manage_list);
    	list.setAdapter(new MultiListAdapter(adapters));
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
	
	@Override
	protected CharSequence getRenameTitle() {
		return "Rename effect";
	}

	@Override
	protected CharSequence getRenameMessage() {
		return "Choose new name for the effect";
	}

}
