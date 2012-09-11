package com.kangirigungi.alchemistlist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.Database.StringContainer;
import com.kangirigungi.alchemistlist.tools.SubClickableAdapter.OnSubItemClickListener;
import com.kangirigungi.alchemistlist.tools.SubClickableListAdapter;
import com.kangirigungi.alchemistlist.tools.Utils;

public class ManageIngredient extends ManageTextBase {
	private static final String TAG = "ManageIngredient";
	
	private static final int ACTIVITY_CHOOSE_EFFECT = 0;
	
	private DbAdapter dbAdapter;
	
	@Override
    public void initManageText(Bundle savedInstanceState) {
        setContentView(R.layout.activity_manage_ingredient);

        Button button = (Button)findViewById(R.id.ingredient_addEffect);
        button.setOnClickListener(new OnClickListener() {
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
	
	private void refreshList() {
		Log.d(TAG, "refreshList()");
    	Cursor cursor = dbAdapter.getEffectsFromIngredient(getId());
    	final ListView list = (ListView)findViewById(R.id.manage_list);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		list.setAdapter(null);
    	} else {
    		SubClickableListAdapter adapter = new SubClickableListAdapter(
    				new SimpleCursorAdapter(this, R.layout.activity_manage_list_item, 
	    			cursor, new String[] {DbAdapter.EFFECTS_VALUE}, 
	    			new int[] {R.id.text1}));
    		adapter.setOnClickListener(R.id.text1, new OnSubItemClickListener() {
				@Override
				public void onSubItemClick(View subView, int position) {
					manageEffect(list.getItemIdAtPosition(position));
				}
			});
    		adapter.setOnClickListener(R.id.btnRemove, new OnSubItemClickListener() {
				@Override
				public void onSubItemClick(View subView, int position) {
					removeEffect(list.getItemIdAtPosition(position));
				}
			});
	    	list.setAdapter(adapter);
    	}
	}

	private void manageEffect(long id) {
		Intent i = new Intent(this, ManageEffect.class);
   		i.putExtra("dbName", dbAdapter.getDbName());
   		i.putExtra("id", id);
        startActivity(i);
	}
	
	private void removeEffect(long id) {
		dbAdapter.deleteIngredientEffect(getId(), id);
		refreshList();
	}
	
	private void launchEffectChooser() {
    	Log.v(TAG, "launchEffectChooser()");
    	Intent i = new Intent(this, EffectTextChooser.class);
   		i.putExtra("dbName", dbAdapter.getDbName());
        startActivityForResult(i, ACTIVITY_CHOOSE_EFFECT);
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_CHOOSE_EFFECT:
    		onEffectChooserResult(resultCode, data);
    		break;
    	}
    }

	private void onEffectChooserResult(int resultCode, Intent data) {
		Log.d(TAG, "EffectTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.v(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		Utils.printBundle(TAG, extras);
    		long id = extras.getLong("id");
    		Log.i(TAG, "Adding effect "+id+" to ingredient.");
    		dbAdapter.addIngredientEffect(getId(), id);
    		refreshList();
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
	}
}
