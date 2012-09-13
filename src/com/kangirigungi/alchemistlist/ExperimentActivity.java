package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
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
import com.kangirigungi.alchemistlist.tools.Utils;

public class ExperimentActivity extends Activity {

	private static final int ACTIVITY_CHOOSE_INGREDIENT = 0;
	private static final int ACTIVITY_MANAGE_INGREDIENT = 1;
	private static final String TAG = "ExperimentActivity";
	
	private SparseArray<Long> textIds;
	private DbAdapter dbAdapter;
	private ConfigDbAdapter config;
	private String dbName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        
        Button btn = (Button)findViewById(R.id.experiment_item1Manage);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onManageClick(v, R.id.experiment_item1Display);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item1Clear);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearClick(v, R.id.experiment_item1Display);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item1Display);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChooseClick(v, R.id.experiment_item1Display);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item2Manage);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onManageClick(v, R.id.experiment_item2Display);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item2Clear);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearClick(v, R.id.experiment_item2Display);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item2Display);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChooseClick(v, R.id.experiment_item2Display);
			}
		});
        btn = (Button)findViewById(R.id.experiment_btnAddAssoc);
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
        ListView list = (ListView)findViewById(R.id.experiment_displayList);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				Long[] assoc = dbAdapter.getAssoc(id);
				if (assoc == null) {
					Log.e(TAG, "Value not found: " + id);
					return;
				}
				setTextId(R.id.experiment_item1Display, assoc[0]);
				setTextId(R.id.experiment_item2Display, assoc[1]);
				refreshList();
			}
		});
        
        Bundle extras = getIntent().getExtras();
        dbName = extras.getString("dbName");
        
        textIds = new SparseArray<Long>();
        config = new ConfigDbAdapter(this);
        config.open();
        dbAdapter = new DbAdapter(this);
        dbAdapter.open(dbName);
        TextView indicator = (TextView)findViewById(R.id.experiment_dbNameIndicator);
    	indicator.setText(dbName);
        if (savedInstanceState != null) {
        	Long value = (Long)savedInstanceState.getSerializable("item1");
        	if (value != null) {
        		setTextId(R.id.experiment_item1Display, value.longValue());
        	}
        	value = (Long)savedInstanceState.getSerializable("item2");
        	if (value != null) {
        		setTextId(R.id.experiment_item2Display, value.longValue());
        	}
        }
        refreshList();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("item1", textIds.get(R.id.experiment_item1Display));
        outState.putSerializable("item2", textIds.get(R.id.experiment_item2Display));
    }
   
    @Override
	protected void onDestroy() {
    	Log.v(TAG, "onDestroy()");
    	dbAdapter.close();
    	config.close();
		super.onDestroy();
	}
    
    private void setTextId(int textId, long id) {
    	Button textView = (Button)findViewById(textId);
		textView.setText(dbAdapter.getIngredientsWrapper().getString(id));
		textIds.put(textId, id);
    }
    
    private void onChooseClick(View v, int textId) {
    	Intent i = new Intent(this, IngredientTextChooser.class);
    	i.putExtra("textId", textId);
    	Button textView = (Button)findViewById(textId);
    	i.putExtra("value", textView.getText());
    	if (dbName != null) {
    		i.putExtra("dbName", dbName);
    	}
        startActivityForResult(i, ACTIVITY_CHOOSE_INGREDIENT);
    }
    
    private void clearAll() {
    	clearText(R.id.experiment_item1Display);
    	clearText(R.id.experiment_item2Display);
    	refreshList();
    }
    
    private void clearText(int textId) {
    	Button textView = (Button)findViewById(textId);
    	textView.setText("");
		textIds.delete(textId);
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
    	if (textIds.get(textId) == null) {
    		Log.d(TAG, "Text field empty.");
    		return;
    	}
    	Intent i = new Intent(this, ManageIngredient.class);
    	i.putExtra("id", textIds.get(textId));
    	i.putExtra("dbName", dbName);
    	startActivityForResult(i, ACTIVITY_MANAGE_INGREDIENT);
	}
    
    private void onAddAssocClick(View v) {
    	Log.d(TAG, "onAddAssocClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds.get(R.id.experiment_item1Display);
    	Long id2 = textIds.get(R.id.experiment_item2Display);
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Adding association: " + 
    				id1 + " ("+((Button)findViewById(R.id.experiment_item1Display)).getText() + ")" +
    				" <--> "+
    				id2 + " ("+((Button)findViewById(R.id.experiment_item2Display)).getText() + ")");
    		dbAdapter.addAssoc(id1.longValue(), id2.longValue());
    		refreshList();
    	}
    }
    
    private void onDelAssocClick(View v) {
    	Log.d(TAG, "onDelAssocClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds.get(R.id.experiment_item1Display);
    	Long id2 = textIds.get(R.id.experiment_item2Display);
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Deleting association: " + 
    				id1 + " ("+((Button)findViewById(R.id.experiment_item1Display)).getText() + ")" +
    				" <--> "+
    				id2 + " ("+((Button)findViewById(R.id.experiment_item2Display)).getText() + ")");
    		dbAdapter.deleteAssoc(id1.longValue(), id2.longValue());
    		refreshList();
    	}
    }
    
    private void refreshList() {
    	Log.d(TAG, "refreshList()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds.get(R.id.experiment_item1Display);
    	Long id2 = textIds.get(R.id.experiment_item2Display);
    	Cursor cursor = null;
    	if (id1 != null && id2 != null) {
    		Log.d(TAG, "Query with two strings.");
    		cursor = dbAdapter.searchAssoc(id1.longValue(), id2.longValue());
    	} else
    	if (id1 != null) {
    		Log.d(TAG, "Query with first string.");
    		cursor = dbAdapter.searchAssoc(id1.longValue());
    	} else
    	if (id2 != null) {
    		Log.d(TAG, "Query with second string.");
    		cursor = dbAdapter.searchAssoc(id2.longValue());
    	}
    	final ListView list = (ListView)findViewById(R.id.experiment_displayList);
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
    	}
	}

    private void refreshText(int textId) {
    	Button textView = (Button)findViewById(textId);
    	if (textIds.get(textId) == null) {
    		textView.setText("");
    	} else {
    		textView.setText(dbAdapter.getIngredientsWrapper().getString(textIds.get(textId)));
    	}
    }
    
    private void onManageIngredientResult(int resultCode, Intent data) {
    	Log.d(TAG, "ManageTextBase activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.d(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		if (extras.getBoolean("deleted")) {
    			Log.d(TAG, "Ingredient deleted. Clearing contents.");
    			clearAll();
    		} else {
    			refreshText(R.id.experiment_item1Display);
    			refreshText(R.id.experiment_item2Display);
    		}
    		refreshList();
    	}
    }

    private void onStringChooserResult(int resultCode, Intent data) {
    	Log.d(TAG, "IngredientTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.d(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		Utils.printBundle(TAG, extras);
    		int textId = extras.getInt("textId");
    		Button Button = (Button)findViewById(textId);
    		String value = extras.getString("result");
    		Button.setText(value);
    		if (value != null && value.length() > 0) {
    			textIds.put(textId, Long.valueOf(extras.getLong("id")));
    		}
    		refreshList();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_experiment, menu);
        return true;
    }
    

}
