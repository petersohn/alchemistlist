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
import com.kangirigungi.alchemistlist.tools.InputQuery;
import com.kangirigungi.alchemistlist.tools.InputQueryResultListener;
import com.kangirigungi.alchemistlist.tools.Utils;

public class ExperimentActivity extends Activity {

	private static final int ACTIVITY_CHOOSE_STRING = 0;
	private static final String TAG = "ExperimentActivity";
	
	private SparseArray<Long> textIds;
	private DbAdapter dbAdapter;
	private ConfigDbAdapter config;
	private String dbName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        
        Button btn = (Button)findViewById(R.id.experiment_item1Button);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChangeClick(v, R.id.experiment_item1Text);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item1Clear);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearClick(v, R.id.experiment_item1Text);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item1Text);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChooseClick(v, R.id.experiment_item1Text);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item2Button);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChangeClick(v, R.id.experiment_item2Text);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item2Clear);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearClick(v, R.id.experiment_item2Text);
			}
		});
        btn = (Button)findViewById(R.id.experiment_item2Text);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChooseClick(v, R.id.experiment_item2Text);
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
        btn = (Button)findViewById(R.id.experiment_btnDelString);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDelStringClick(v);
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
				setTextId(R.id.experiment_item1Text, assoc[0]);
				setTextId(R.id.experiment_item2Text, assoc[1]);
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
        		setTextId(R.id.experiment_item1Text, value.longValue());
        	}
        	value = (Long)savedInstanceState.getSerializable("item2");
        	if (value != null) {
        		setTextId(R.id.experiment_item2Text, value.longValue());
        	}
        }
        refreshList();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("item1", textIds.get(R.id.experiment_item1Text));
        outState.putSerializable("item2", textIds.get(R.id.experiment_item2Text));
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
		textView.setText(dbAdapter.getStringsWrapper().getString(id));
		textIds.put(textId, id);
    }
    
    private void onChooseClick(View v, int textId) {
    	Intent i = new Intent(this, StringTextChooser.class);
    	i.putExtra("textId", textId);
    	Button textView = (Button)findViewById(textId);
    	i.putExtra("value", textView.getText());
    	if (dbName != null) {
    		i.putExtra("dbName", dbName);
    	}
        startActivityForResult(i, ACTIVITY_CHOOSE_STRING);
    }
    
//    private void clearAll() {
//    	clearText(R.id.experiment_item1Text);
//    	clearText(R.id.experiment_item2Text);
//    	refreshList();
//    }
    
    private void clearText(int textId) {
    	Button textView = (Button)findViewById(textId);
    	textView.setText("");
		textIds.delete(textId);
    }
    
    private void onClearClick(View v, int textId) {
		clearText(textId);
		refreshList();
    }
    
    private void onChangeClick(View v, int textId) {
    	Log.d(TAG, "onChangeClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	final Long id = textIds.get(textId);
		if (id == null) {
			Log.d(TAG, "No value");
			return;
		}
		final Button textView = (Button)findViewById(textId);
		InputQuery alert = new InputQuery(this);
		
		alert.run(getString(R.string.change_title),
				getString(R.string.change_value), textView.getText(),
				new InputQueryResultListener() {
					@Override
					public void onOk(String result) {
						Log.i(TAG, "Value changed to " + result);
						dbAdapter.changeString(id.longValue(), result);
						textView.setText(result);
						refreshList();
						
					}
					@Override
					public void onCancel() {
						Log.d(TAG, "Change cancelled.");			
					}
				});
	}
    
    private void onAddAssocClick(View v) {
    	Log.d(TAG, "onAddAssocClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds.get(R.id.experiment_item1Text);
    	Long id2 = textIds.get(R.id.experiment_item2Text);
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Adding association: " + 
    				id1 + " ("+((Button)findViewById(R.id.experiment_item1Text)).getText() + ")" +
    				" <--> "+
    				id2 + " ("+((Button)findViewById(R.id.experiment_item2Text)).getText() + ")");
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
    	Long id1 = textIds.get(R.id.experiment_item1Text);
    	Long id2 = textIds.get(R.id.experiment_item2Text);
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Deleting association: " + 
    				id1 + " ("+((Button)findViewById(R.id.experiment_item1Text)).getText() + ")" +
    				" <--> "+
    				id2 + " ("+((Button)findViewById(R.id.experiment_item2Text)).getText() + ")");
    		dbAdapter.deleteAssoc(id1.longValue(), id2.longValue());
    		refreshList();
    	}
    }
    
    private void onDelStringClick(View v) {
    	Log.d(TAG, "onDelStringClick()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds.get(R.id.experiment_item1Text);
    	Long id2 = textIds.get(R.id.experiment_item2Text);
    	if (id1 != null) {
    		Log.i(TAG, "Deleting string from database: "+
    				((Button)findViewById(R.id.experiment_item1Text)).getText());
    		dbAdapter.deleteString(id1.longValue());
    		clearText(R.id.experiment_item1Text);
    	}
    	if (id2 != null) {
    		Log.i(TAG, "Deleting string from database: "+
    				((Button)findViewById(R.id.experiment_item2Text)).getText());
    		dbAdapter.deleteString(id2.longValue());
    		clearText(R.id.experiment_item2Text);
    	}
    	refreshList();
    }
    
    private void refreshList() {
    	Log.d(TAG, "refreshList()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds.get(R.id.experiment_item1Text);
    	Long id2 = textIds.get(R.id.experiment_item2Text);
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
    	ListView list = (ListView)findViewById(R.id.experiment_displayList);
    	if (cursor == null) {
    		Log.d(TAG, "No result.");
    		list.setAdapter(null);
    	} else {
	    	list.setAdapter(new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, 
	    			cursor, new String[] {"value1", "value2"}, 
	    			new int[] {android.R.id.text1, android.R.id.text2}));
    	}
    }
    
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_CHOOSE_STRING:
    		onStringChooserResult(resultCode, data);
    		break;
    	}
    	
    }
    
    private void onStringChooserResult(int resultCode, Intent data) {
    	Log.d(TAG, "StringTextChooser activity returned with code: " + resultCode);
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
