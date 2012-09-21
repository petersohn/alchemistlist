package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.kangirigungi.alchemistlist.Database.ConfigDbAdapter;
import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.Database.DbSqlQueries;
import com.kangirigungi.alchemistlist.tools.MultiColorOverride;
import com.kangirigungi.alchemistlist.tools.OverrideAdapter.AdapterOverride;
import com.kangirigungi.alchemistlist.tools.OverrideListAdapter;
import com.kangirigungi.alchemistlist.tools.Utils;

public class ExperimentActivity extends Activity {

	private static final int ACTIVITY_CHOOSE_INGREDIENT = 0;
	private static final int ACTIVITY_MANAGE_INGREDIENT = 1;
	private static final int ACTIVITY_MANAGE_EFFECT = 10;
	private static final int ACTIVITY_ADD_EFFECT = 20;
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
	private Button btnAddExperiment;
	private Button btnDeleteExperiment;
	private Button btnAddEffect;
	private boolean isMatchList;
	private String[][] singleItemListDescriptions;
	
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
        btnAddExperiment = (Button)findViewById(R.id.experiment_btnAddAssoc);
        btnAddExperiment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddExperimentClick(v);
			}
		});
        btnDeleteExperiment = (Button)findViewById(R.id.experiment_btnDelAssoc);
        btnDeleteExperiment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDelExperimentClick(v);
			}
		});
        btnAddEffect = (Button)findViewById(R.id.experiment_btnAddEffect);
        btnAddEffect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddEffectClick(v);
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
    
    private void onAddEffectClick(View v) {
    	Log.v(TAG, "onAddEffectClick()");
    	Utils.startActivityWithDb(this, EffectTextChooser.class, 
    			dbName, ACTIVITY_ADD_EFFECT);
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
    	Bundle extras = new Bundle();
    	extras.putInt("textId", textId);
    	extras.putString("value", textIds[textId].view.getText().toString());
    	Utils.startActivityWithDb(this, IngredientTextChooser.class, 
    			dbName, ACTIVITY_CHOOSE_INGREDIENT, extras);
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
    	Bundle extras = new Bundle();
    	extras.putLong("id", textIds[textId].id);
    	Utils.startActivityWithDb(this, ManageIngredient.class, 
    			dbName, ACTIVITY_MANAGE_INGREDIENT, extras);
	}
    
    private void onAddExperimentClick(View v) {
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
    		dbAdapter.addExperiment(id1, id2);
    		refreshList();
    	}
    }
    
    private void onDelExperimentClick(View v) {
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
    		dbAdapter.deleteExperiment(id1, id2);
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
    	if (id1 != null && id2 != null) {
    		Log.d(TAG, "Query with two strings.");
			fillMatchList();
			isMatchList = true;
			return;
    	}
    	if (id1 != null) {
    		Log.d(TAG, "Query with first string.");
    		fillSingleItemList(id1);
    	} else
    	if (id2 != null) {
    		Log.d(TAG, "Query with second string.");
    		fillSingleItemList(id2);
    	} else {
    		btnAddExperiment.setVisibility(View.VISIBLE);
        	btnAddExperiment.setEnabled(false);
        	btnDeleteExperiment.setVisibility(View.GONE);
        	btnAddEffect.setEnabled(false);
    	}
    	isMatchList = false;
    }
    
    private void fillSingleItemList(long id) {
    	Cursor cursor = dbAdapter.searchExperiment(id);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		list.setAdapter(null);
    		btnAddEffect.setEnabled(false);
    		return;
    	}
		btnAddEffect.setEnabled(dbAdapter.getEffectNum(id) < 
				Utils.MAX_EFFECT_PER_INGREDIENT);
		btnAddExperiment.setVisibility(View.VISIBLE);
    	btnAddExperiment.setEnabled(false);
    	btnDeleteExperiment.setVisibility(View.GONE);
    	
    	AdapterOverride override = new AdapterOverride() {
			@Override
			public View onOverride(int position, View convertView, ViewGroup parent) {
				updateSingleListItem(position, convertView);
				return convertView;
			}
		};
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
				this, R.layout.experiment_single_list_item, 
    			cursor, new String[] {"value1", "value2"}, 
    			new int[] {R.id.text1, R.id.text2});
		OverrideListAdapter adapter = new OverrideListAdapter(cursorAdapter, override);
		singleItemListDescriptions = new String[adapter.getCount()][];
    	list.setAdapter(adapter);
    }
    
    private String[] getSingleListItem(int position) {
    	if (singleItemListDescriptions[position] != null) {
    		return singleItemListDescriptions[position];
    	}
    	
    	long id = list.getItemIdAtPosition(position);
    	Long[] experiment = dbAdapter.getExperiment(id);
		Cursor cursor = dbAdapter.getCommonEffects(experiment[0], experiment[1]);
		String[] result = new String[cursor.getCount()];
		int i = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext(), ++i) {
			result[i] = cursor.getString(1);
		}
		singleItemListDescriptions[position] = result;
		return result;
    }
    
    private void updateSingleListItem(int position, View view) {
    	String[] effects = getSingleListItem(position);
    	
		int colorId = (effects.length > 0) ? R.color.pairing_yes : R.color.pairing_no;
		int color = getResources().getColor(colorId);
		TextView text = (TextView)view.findViewById(R.id.text1);
		text.setTextColor(color);
		text = (TextView)view.findViewById(R.id.text2);
		text.setTextColor(color);
		
		LinearLayout effectContainer = 
				(LinearLayout)view.findViewById(R.id.descriptionText);
		effectContainer.removeAllViews();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT;
		for (String effect: effects) {
			TextView effectText = new TextView(this);
			effectText.setTextColor(color);
			effectText.setText(effect);
			effectText.setLayoutParams(params);
			effectContainer.addView(effectText);
		}
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
    					new String[] {DbSqlQueries.EFFECTS_VALUE, DbSqlQueries.PAIRING_CATEGORY},
    					new int[] {R.id.text1, R.id.categoryIndicator}), override);
		list.setAdapter(adapter);
		if (dbAdapter.hasExperiment(textIds[0].id, textIds[1].id)) {
			btnAddExperiment.setVisibility(View.GONE);
			btnDeleteExperiment.setVisibility(View.VISIBLE);
			btnDeleteExperiment.setEnabled(true);
		} else {
			btnAddExperiment.setVisibility(View.VISIBLE);
			btnAddExperiment.setEnabled(true);
			btnDeleteExperiment.setVisibility(View.GONE);
		}
		btnAddEffect.setEnabled(
				dbAdapter.getEffectNum(textIds[0].id) < Utils.MAX_EFFECT_PER_INGREDIENT &&
				dbAdapter.getEffectNum(textIds[1].id) < Utils.MAX_EFFECT_PER_INGREDIENT);
    }
    
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_CHOOSE_INGREDIENT:
    		onStringChooserResult(resultCode, Utils.getExtrasIfExists(data));
    		break;
    	case ACTIVITY_MANAGE_INGREDIENT:
    		onManageIngredientResult(resultCode, Utils.getExtrasIfExists(data));
    		break;
    	case ACTIVITY_MANAGE_EFFECT:
    		refreshList();
    		break;
    	case ACTIVITY_ADD_EFFECT:
    		onAddEffectResult(resultCode, Utils.getExtrasIfExists(data));
    		break;
    	}
	}
    
    private void onAddEffectResult(int resultCode, Bundle extras) {
    	Log.d(TAG, "EffectTectChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		long effectId = extras.getLong("id");
    		for (TextId textId: textIds) {
    			if (textId.id != null) {
    				Log.i(TAG, "Adding effect "+effectId+" to ingredient "+textId.id+".");
    				dbAdapter.addIngredientEffect(textId.id, effectId);
    			}
    		}
    		refreshList();
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
    
    private void onManageIngredientResult(int resultCode, Bundle extras) {
    	Log.d(TAG, "ManageTextBase activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		if (extras.getBoolean("deleted")) {
    			Log.d(TAG, "Ingredient deleted. Clearing contents.");
    			clearAll();
    		} 
    	}
		refreshText(0);
		refreshText(1);
    	refreshList();
    }

    private void onStringChooserResult(int resultCode, Bundle extras) {
    	Log.d(TAG, "IngredientTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
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
		if (category != DbSqlQueries.CATEGORY_SOMETHING) {
			Bundle extras = new Bundle();
	    	extras.putLong("id", id);
	    	Utils.startActivityWithDb(this, ManageEffect.class, 
	    			dbName, ACTIVITY_MANAGE_EFFECT, extras);
	    	
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
