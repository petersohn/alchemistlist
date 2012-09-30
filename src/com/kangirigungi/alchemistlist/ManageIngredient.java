package com.kangirigungi.alchemistlist;

import java.util.Vector;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.Database.DbSqlQueries;
import com.kangirigungi.alchemistlist.Database.StringContainer;
import com.kangirigungi.alchemistlist.tools.MultiListAdapter;
import com.kangirigungi.alchemistlist.tools.OverrideListAdapter;
import com.kangirigungi.alchemistlist.tools.SubClickableOverride;
import com.kangirigungi.alchemistlist.tools.SubClickableOverride.OnSubItemClickListener;
import com.kangirigungi.alchemistlist.tools.Utils;

public class ManageIngredient extends ManageTextBase {
	private static final String TAG = "ManageIngredient";
	
	private static final int ACTIVITY_CHOOSE_EFFECT = 0;
	private static final int ACTIVITY_MANAGE_EFFECT = 1;
	
	private class EffectClicked implements OnSubItemClickListener {
		@Override
		public void onSubItemClick(View subView, int position) {
			long id = list.getItemIdAtPosition(position);
			Log.v(TAG, "Click on item. Position: "+position+". Id: "+
					dbAdapter.getEffectsWrapper().getString(id));
			manageEffect(id);
		}
	}
	
	private DbAdapter dbAdapter;
	
	private Button btnAddEffect;
	private ListView list;
	private EffectClicked effectClicked;
	
	@Override
    public void initManageText(Bundle savedInstanceState) {
        setContentView(R.layout.activity_manage_ingredient);

        btnAddEffect = (Button)findViewById(R.id.ingredient_addEffect);
        list = (ListView)findViewById(R.id.manage_list);
        effectClicked = new EffectClicked();
        
        btnAddEffect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchEffectChooser();
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
        findViewById(R.id.mainLayout).
				setBackgroundColor(getResources().getColor(R.color.background_ingredient));
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
		return dbAdapter.getIngredientsWrapper();
	}
	
	private void addEffectsAdapter(Vector<ListAdapter> adapters) {
		Cursor cursor = dbAdapter.getEffectsFromIngredient(getId());
    	if (cursor == null) {
    		Log.d(TAG, "No normal result.");
    	} else {
    		btnAddEffect.setEnabled(cursor.getCount() < Utils.MAX_EFFECT_PER_INGREDIENT);
    		SubClickableOverride override = new SubClickableOverride();
    		override.setOnClickListener(R.id.text1, effectClicked);
    		override.setOnClickListener(R.id.btnRemove, new OnSubItemClickListener() {
				@Override
				public void onSubItemClick(View subView, int position) {
					removeEffect(list.getItemIdAtPosition(position));
				}
			});
			OverrideListAdapter itemAdapter = new OverrideListAdapter(
					new SimpleCursorAdapter(this, R.layout.manage_list_item_removable, 
	    			cursor, new String[] {DbSqlQueries.EFFECTS_VALUE}, 
	    			new int[] {R.id.text1}),
	    			override);
			
			adapters.add(itemAdapter);
    	}
	}
	
	private void addExcludedEffectsAdapter(Vector<ListAdapter> adapters) {
		Cursor cursor = dbAdapter.getExcludedEffects(getId());
    	if (cursor == null) {
    		Log.d(TAG, "No excluded result.");
    	} else {
    		SubClickableOverride override = new SubClickableOverride();
    		override.setOnClickListener(
    				R.id.text1, 
    				effectClicked);
    		OverrideListAdapter excludedAdapter = new OverrideListAdapter(
	    			new SimpleCursorAdapter(this, R.layout.manage_list_item_excluded, 
	    			cursor, new String[] {DbSqlQueries.EFFECTS_VALUE}, 
	    			new int[] {R.id.text1}),
	    			override);
    		
	    	adapters.add(excludedAdapter);
    	}
	}
	
	private void refreshList() {
		Log.d(TAG, "refreshList()");
		Vector<ListAdapter> adapters = new Vector<ListAdapter>();
		addEffectsAdapter(adapters);
		if (dbAdapter.getEffectNum(getId()) < Utils.MAX_EFFECT_PER_INGREDIENT) {
			addExcludedEffectsAdapter(adapters);			
		}
    	list.setAdapter(new MultiListAdapter(adapters));
	}

	private void manageEffect(long id) {
        Bundle extras = new Bundle();
   		extras.putLong("id", id);
        Utils.startActivityWithDb(this, ManageEffect.class, 
        		dbAdapter.getDbName(), ACTIVITY_MANAGE_EFFECT, extras);
	}
	
	private void removeEffect(long id) {
		dbAdapter.deleteIngredientEffect(getId(), id);
		refreshList();
	}
	
	private void launchEffectChooser() {
    	Log.v(TAG, "launchEffectChooser()");
    	Utils.startEffectTextChooser(this,  
    			dbAdapter.getDbName(), ACTIVITY_CHOOSE_EFFECT, null);
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_CHOOSE_EFFECT:
    		onEffectChooserResult(resultCode, Utils.getExtrasIfExists(data));
    		break;
    	case ACTIVITY_MANAGE_EFFECT:
			refreshList();
			refresh();
    		break;
    	}
    }

	private void onEffectChooserResult(int resultCode, Bundle extras) {
		Log.d(TAG, "EffectTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		long id = extras.getLong("id");
    		Log.i(TAG, "Adding effect "+id+" to ingredient.");
    		dbAdapter.addIngredientEffect(getId(), id);
    		refreshList();
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
	}

	@Override
	protected String getRenameTitle() {
		return getString(R.string.renameIngredientTitle);
	}

	@Override
	protected String getRenameMessage() {
		return getString(R.string.renameIngredientQuestion);
	}
	
	@Override
	protected String getDeleteMessage() {
		return getString(R.string.deleteIngredientQuestion);
	}
}
