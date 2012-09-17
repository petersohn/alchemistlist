package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.kangirigungi.alchemistlist.Database.ConfigDbAdapter;
import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.tools.MultiColorOverride;
import com.kangirigungi.alchemistlist.tools.OverrideListAdapter;
import com.kangirigungi.alchemistlist.tools.Utils;

public class ExperimentActivity extends Activity {

	private static final int ACTIVITY_CHOOSE_INGREDIENT = 0;
	private static final int ACTIVITY_MANAGE_INGREDIENT = 1;
	private static final int ACTIVITY_MANAGE_EFFECT = 10;
	private static final String TAG = "ExperimentActivity";
	
	private static class TextId {
		Long id;
		Button view;
	}
	
	private TextId[] textIds;
	private DbAdapter dbAdapter;
	private ConfigDbAdapter config;
	private String dbName;
	private ListView list;
	private boolean isMatchList;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	isMatchList = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        
        textIds = new TextId[] { new TextId(), new TextId() };
        fillTextId(0, R.id.experiment_item1Display, 
        		R.id.experiment_item1Clear, R.id.experiment_item1Manage);
        fillTextId(1, R.id.experiment_item2Display, 
        		R.id.experiment_item2Clear, R.id.experiment_item2Manage);
        Button btn = (Button)findViewById(R.id.experiment_btnAddAssoc);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddAssocClick(v);
			}
		});
        btn = (Button)findViewById(R.id.experiment_btnDelAssoc);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDelAssocClick(v);
			}
		});
        list = (ListView)findViewById(R.id.experiment_displayList);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				if (isMatchList) {
					onMatchListItemClick(view, id);
				} else {
					onPairListItemClick(id);
				}
				
			}
		});
        
        Bundle extras = getIntent().getExtras();
        dbName = extras.getString("dbName");
        
        config = new ConfigDbAdapter(this);
        config.open();
        dbAdapter = new DbAdapter(this);
        dbAdapter.open(dbName);
        TextView indicator = (TextView)findViewById(R.id.experiment_dbNameIndicator);
    	indicator.setText(dbName);
        if (savedInstanceState != null) {
        	Long value = (Long)savedInstanceState.getSerializable("item1");
        	if (value != null) {
        		setTextId(0, value.longValue());
        	}
        	value = (Long)savedInstanceState.getSerializable("item2");
        	if (value != null) {
        		setTextId(1, value.longValue());
        	}
        }
        refreshList();
    }
    
    private void fillTextId(final int num, int displayId, int clearId, int manageId) {
    	 Button btn = (Button)findViewById(clearId);
         btn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onClearClick(v, num);
 			}
 		});
         btn = (Button)findViewById(displayId);
         textIds[num].view = btn;
         btn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onChooseClick(v, num);
 			}
 		});
         btn = (Button)findViewById(manageId);
         btn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onManageClick(v, num);
 			}
 		});
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("item1", textIds[0].id);
        outState.putSerializable("item2", textIds[1].id);
    }
   
    @Override
	protected void onDestroy() {
    	Log.v(TAG, "onDestroy()");
    	dbAdapter.close();
    	config.close();
		super.onDestroy();
	}
    
    private void setTextId(int textId, long id) {
		textIds[textId].view.setText(dbAdapter.getIngredientsWrapper().getString(id));
		textIds[textId].id = id;
    }
    
    private void onChooseClick(View v, int textId) {
    	Intent i = new Intent(this, IngredientTextChooser.class);
    	i.putExtra("textId", textId);
    	i.putExtra("value", textIds[textId].view.getText());
    	if (dbName != null) {
    		i.putExtra("dbName", dbName);
    	}
        startActivityForResult(i, ACTIVITY_CHOOSE_INGREDIENT);
    }
    
    private void clearAll() {
    	clearText(0);
    	clearText(1);
    	refreshList();
    }
    
    private void clearText(int textId) {
    	
    	textIds[textId].view.setText("");
    	textIds[textId].id = null;
    }
    
    private void onClearClick(View v, int textId) {
		clearText(textId);
		refreshList();
    }
    
    private void onManageClick(View v, int textId) {
    	Log.d(TAG, "onManageClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	if (textIds[textId].id == null) {
    		Log.d(TAG, "Text field empty.");
    		return;
    	}
    	Intent i = new Intent(this, ManageIngredient.class);
    	i.putExtra("id", textIds[textId].id);
    	i.putExtra("dbName", dbName);
    	startActivityForResult(i, ACTIVITY_MANAGE_INGREDIENT);
	}
    
    private void onAddAssocClick(View v) {
    	Log.d(TAG, "onAddAssocClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds[0].id;
    	Long id2 = textIds[1].id;
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Adding association: " + 
    				id1 + " ("+textIds[0].view.getText() + ")" +
    				" <--> "+
    				id2 + " ("+textIds[1].view.getText() + ")");
    		dbAdapter.addAssoc(id1, id2);
    		refreshList();
    	}
    }
    
    private void onDelAssocClick(View v) {
    	Log.d(TAG, "onDelAssocClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds[0].id;
    	Long id2 = textIds[1].id;
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Deleting association: " + 
    				id1 + " ("+textIds[0].view.getText() + ")" +
    				" <--> "+
    				id2 + " ("+textIds[1].view.getText() + ")");
    		dbAdapter.deleteAssoc(id1, id2);
    		refreshList();
    	}
    }
    
    private void refreshList() {
    	Log.d(TAG, "refreshList()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds[0].id;
    	Long id2 = textIds[1].id;
    	Cursor cursor = null;
    	if (id1 != null && id2 != null) {
    		Log.d(TAG, "Query with two strings.");
    		cursor = dbAdapter.searchExperiment(id1, id2);
    		if (cursor.getCount() == 0) {
    			fillMatchList();
    			isMatchList = true;
    			return;
    		}
    	} else
    	if (id1 != null) {
    		Log.d(TAG, "Query with first string.");
    		cursor = dbAdapter.searchExperiment(id1.longValue());
    	} else
    	if (id2 != null) {
    		Log.d(TAG, "Query with second string.");
    		cursor = dbAdapter.searchExperiment(id2.longValue());
    	}
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		list.setAdapter(null);
    	} else {
    		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
    				this, android.R.layout.two_line_list_item, 
	    			cursor, new String[] {"value1", "value2"}, 
	    			new int[] {android.R.id.text1, android.R.id.text2});
	    	list.setAdapter(adapter);
    	}
    	isMatchList = false;
    }
    
   private void fillMatchList() {
    	Cursor cursor = dbAdapter.getPairing(textIds[0].id, textIds[1].id);
    	MultiColorOverride override = new MultiColorOverride(
    			R.id.text1, R.id.categoryIndicator, 
    			new Integer[] {
    					R.color.pairing_something,
    					R.color.pairing_yes,
    					R.color.pairing_maybe,
    					R.color.pairing_no}); 
    	OverrideListAdapter adapter = new OverrideListAdapter(
    			new SimpleCursorAdapter(this,
    					R.layout.experiment_list_item,
    					cursor,
    					new String[] {DbAdapter.EFFECTS_VALUE, DbAdapter.PAIRING_CATEGORY},
    					new int[] {R.id.text1, R.id.categoryIndicator}), override);
		list.setAdapter(adapter);
    }
    
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_CHOOSE_INGREDIENT:
    		onStringChooserResult(resultCode, data);
    		break;
    	case ACTIVITY_MANAGE_INGREDIENT:
    		onManageIngredientResult(resultCode, data);
    		break;
    	case ACTIVITY_MANAGE_EFFECT:
    		refreshList();
    		break;
    	}
	}

    private void refreshText(int textId) {
    	if (textIds[textId].id == null) {
    		textIds[textId].view.setText("");
    	} else {
    		textIds[textId].view.setText(
    				dbAdapter.getIngredientsWrapper().getString(textIds[textId].id));
    	}
    }
    
    private void onManageIngredientResult(int resultCode, Intent data) {
    	Log.d(TAG, "ManageTextBase activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Bundle extras = data.getExtras();
    		if (extras.getBoolean("deleted")) {
    			Log.d(TAG, "Ingredient deleted. Clearing contents.");
    			clearAll();
    		} 
    	}
		refreshText(0);
		refreshText(1);
    	refreshList();
    }

    private void onStringChooserResult(int resultCode, Intent data) {
    	Log.d(TAG, "IngredientTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.d(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		Utils.printBundle(TAG, extras);
    		int textId = extras.getInt("textId");
    		String value = extras.getString("result");
    		textIds[textId].view.setText(value);
    		if (value != null && value.length() > 0) {
    			textIds[textId].id = extras.getLong("id");
    		}
    		refreshList();
    	}
    }
    
    private void onMatchListItemClick(View view, long id) {
		TextView indicator = (TextView)view.findViewById(R.id.categoryIndicator);
		int category = Integer.parseInt(indicator.getText().toString());
		if (category != DbAdapter.CATEGORY_SOMETHING) {
			Intent i = new Intent(this, ManageEffect.class);
	    	i.putExtra("id", id);
	    	i.putExtra("dbName", dbName);
	    	startActivityForResult(i, ACTIVITY_MANAGE_EFFECT);
		}
	}
    
    private void onPairListItemClick(long id) {
    	Long[] experiment = dbAdapter.getExperiment(id);
		if (experiment == null) {
			Log.e(TAG, "Value not found: " + id);
			return;
		}
		setTextId(0, experiment[0]);
		setTextId(1, experiment[1]);
		refreshList();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_experiment, menu);
        return true;
    }
    

}
