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
import com.kangirigungi.alchemistlist.Database.DbSqlQueries;
import com.kangirigungi.alchemistlist.Database.StringContainer;
import com.kangirigungi.alchemistlist.tools.MultiListAdapter;
import com.kangirigungi.alchemistlist.tools.OverrideListAdapter;
import com.kangirigungi.alchemistlist.tools.SubClickableOverride;
import com.kangirigungi.alchemistlist.tools.Utils;
import com.kangirigungi.alchemistlist.tools.SubClickableOverride.OnSubItemClickListener;

public class ManageEffect extends ManageTextBase {
	private static final String TAG = "ManageEffect";
	
	private static final int ACTIVITY_MANAGE_INGREDIENT = 1;
	
	private class IngredientClicked implements OnSubItemClickListener {
		@Override
		public void onSubItemClick(View subView, int position) {
			long id = list.getItemIdAtPosition(position);
			Log.v(TAG, "Click on item. Position: "+position+". Id: "+
					dbAdapter.getEffectsWrapper().getString(id));
			manageIngredient(id);
		}
	}
	
	private DbAdapter dbAdapter;
	
	private ListView list;
	private IngredientClicked ingredientClicked;
	private int backgroundColor;
	
	
	@Override
    public void initManageText(Bundle savedInstanceState) {
        setContentView(R.layout.activity_manage_effect);

        ingredientClicked = new IngredientClicked();
        list = (ListView)findViewById(R.id.manage_list);
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
        backgroundColor = getResources().getColor(R.color.background_effect);
        findViewById(R.id.mainLayout).setBackgroundColor(backgroundColor);
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
	
	private void addEffectsAdapter(Vector<ListAdapter> adapters) {
		Cursor cursor = dbAdapter.getIngredientsFromEffect(getId());
    	if (cursor == null) {
    		Log.d(TAG, "No normal result.");
    	} else {
    		SubClickableOverride override = new SubClickableOverride();
    		override.setOnClickListener(android.R.id.text1, ingredientClicked);
			OverrideListAdapter itemAdapter = new OverrideListAdapter(
					new SimpleCursorAdapter(this, R.layout.manage_list_item, 
	    			cursor, new String[] {DbSqlQueries.EFFECTS_VALUE}, 
	    			new int[] {R.id.text1}),
	    			override);
			
			adapters.add(Utils.createColorCorrectListAdapter(backgroundColor, itemAdapter));
    	}
	}
	
	private void addNonEmptyExcludedIngredientsAdapter(Vector<ListAdapter> adapters) {
		Cursor cursor = dbAdapter.getNonEmptyExcludedIngredients(getId());
    	if (cursor == null) {
    		Log.d(TAG, "No excluded result.");
    	} else {
    		SubClickableOverride override = new SubClickableOverride();
    		override.setOnClickListener(
    				R.id.text1, 
    				ingredientClicked);
    		OverrideListAdapter excludedAdapter = new OverrideListAdapter(
	    			new SimpleCursorAdapter(this, R.layout.manage_list_item_excluded, 
	    			cursor, new String[] {DbSqlQueries.EFFECTS_VALUE}, 
	    			new int[] {R.id.text1}),
	    			override);
    		
    		adapters.add(Utils.createColorCorrectListAdapter(backgroundColor, excludedAdapter));
    	}
	}
	
	private void addEmptyExcludedIngredientsAdapter(Vector<ListAdapter> adapters) {
		Cursor cursor = dbAdapter.getEmptyExcludedIngredients(getId());
    	if (cursor == null) {
    		Log.d(TAG, "No excluded result.");
    	} else {
    		SubClickableOverride override = new SubClickableOverride();
    		override.setOnClickListener(
    				R.id.text1, 
    				ingredientClicked);
    		OverrideListAdapter excludedAdapter = new OverrideListAdapter(
	    			new SimpleCursorAdapter(this, R.layout.manage_list_item_excluded_empty, 
	    			cursor, new String[] {DbSqlQueries.EFFECTS_VALUE}, 
	    			new int[] {R.id.text1}),
	    			override);
    		
    		adapters.add(Utils.createColorCorrectListAdapter(backgroundColor, excludedAdapter));
    	}
	}
	
	private void refreshList() {
		Log.d(TAG, "refreshList()");
		Vector<ListAdapter> adapters = new Vector<ListAdapter>();
		addEffectsAdapter(adapters);
		addNonEmptyExcludedIngredientsAdapter(adapters);
		addEmptyExcludedIngredientsAdapter(adapters);
    	list.setAdapter(new MultiListAdapter(adapters));
	}

	private void manageIngredient(long id) {
		
		Bundle extras = new Bundle();
   		extras.putLong("id", id);
        Utils.startActivityWithDb(this, ManageIngredient.class, 
        		dbAdapter.getDbName(), ACTIVITY_MANAGE_INGREDIENT, extras);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_MANAGE_INGREDIENT:
			refreshList();
			refresh();
    		break;
    	}
    }
	
	@Override
	protected String getRenameTitle() {
		return getString(R.string.renameEffectTitle);
	}

	@Override
	protected String getRenameMessage() {
		return getString(R.string.renameEffectQuestion);
	}
	
	@Override
	protected String getDeleteMessage() {
		return getString(R.string.deleteEffectQuestion);
	}

}
