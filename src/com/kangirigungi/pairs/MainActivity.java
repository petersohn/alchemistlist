package com.kangirigungi.pairs;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.kangirigungi.pairs.Database.Config;
import com.kangirigungi.pairs.Database.DbAdapter;
import com.kangirigungi.pairs.tools.InputQuery;
import com.kangirigungi.pairs.tools.InputQueryResultListener;
import com.kangirigungi.pairs.tools.Utils;

public class MainActivity extends Activity {

	private static final int ACTIVITY_CHOOSE_STRING = 0;
	private static final int ACTIVITY_CHOOSE_DATABASE = 1;
	private static final String TAG = "MainActivity";
	
	private SparseArray<Long> textIds;
	private DbAdapter dbAdapter;
	private Config config;
	private String dbName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btn = (Button)findViewById(R.id.item1Button);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChangeClick(v, R.id.item1Text);
			}
		});
        btn = (Button)findViewById(R.id.item1Clear);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearClick(v, R.id.item1Text);
			}
		});
        btn = (Button)findViewById(R.id.item1Text);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChooseClick(v, R.id.item1Text);
			}
		});
        btn = (Button)findViewById(R.id.item2Button);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChangeClick(v, R.id.item2Text);
			}
		});
        btn = (Button)findViewById(R.id.item2Clear);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClearClick(v, R.id.item2Text);
			}
		});
        btn = (Button)findViewById(R.id.item2Text);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onChooseClick(v, R.id.item2Text);
			}
		});
        btn = (Button)findViewById(R.id.btnAddAssoc);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddAssocClick(v);
			}
		});
        btn = (Button)findViewById(R.id.btnDelAssoc);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDelAssocClick(v);
			}
		});
        btn = (Button)findViewById(R.id.btnDelString);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDelStringClick(v);
			}
		});
        ListView list = (ListView)findViewById(R.id.listView1);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				Long[] assoc = dbAdapter.getAssoc(id);
				if (assoc == null) {
					Log.e(TAG, "Value not found: " + id);
					return;
				}
				setTextId(R.id.item1Text, assoc[0]);
				setTextId(R.id.item2Text, assoc[1]);
				refreshList();
			}
		});
        textIds = new SparseArray<Long>();
        config = new Config(this);
        config.open();
        dbAdapter = new DbAdapter(this);
        setDbName(config.getLastDatabase());
    	
        if (savedInstanceState != null) {
        	Long value = (Long)savedInstanceState.getSerializable("item1");
        	if (value != null) {
        		setTextId(R.id.item1Text, value.longValue());
        	}
        	value = (Long)savedInstanceState.getSerializable("item2");
        	if (value != null) {
        		setTextId(R.id.item2Text, value.longValue());
        	}
        }
        refreshList();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("item1", textIds.get(R.id.item1Text));
        outState.putSerializable("item2", textIds.get(R.id.item2Text));
    }
   
    @Override
   	protected void onPause() {
       	Log.v(TAG, "onPause()");
   		super.onPause();
   	}
    
    @Override
	protected void onStop() {
    	Log.v(TAG, "onStop()");
    	dbAdapter.cleanup();
		super.onStop();
	}
    
    @Override
	protected void onDestroy() {
    	Log.v(TAG, "onDestroy()");
    	dbAdapter.close();
    	if (dbName == null) {
    		config.deleteLastDatabase();
    	} else {
    		config.saveLastDatabase(dbName);
    	}
    	config.close();
		super.onDestroy();
	}
    
    @Override
   	protected void onStart() {
       	Log.v(TAG, "onStart()");
       	if (dbName == null) {
       		Log.i(TAG, "No database selected. Selecting one.");
       		selectDatabase();
       	}
   		super.onStart();
    }
    
    @Override
   	protected void onResume() {
       	Log.v(TAG, "onResume()");
   		super.onResume();
   	}
    
    private void setDbName(String value) {
    	dbAdapter.close();
    	dbName = value;
    	if (dbName != null) {
    		dbAdapter.open(dbName);
    	}
    	TextView indicator = (TextView)findViewById(R.id.dbNameIndicator);
    	indicator.setText(value);
    	clearAll();
		refreshList();
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
    
    private void clearAll() {
    	clearText(R.id.item1Text);
    	clearText(R.id.item2Text);
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
    	Long id1 = textIds.get(R.id.item1Text);
    	Long id2 = textIds.get(R.id.item2Text);
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Adding association: " + 
    				id1 + " ("+((Button)findViewById(R.id.item1Text)).getText() + ")" +
    				" <--> "+
    				id2 + " ("+((Button)findViewById(R.id.item2Text)).getText() + ")");
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
    	Long id1 = textIds.get(R.id.item1Text);
    	Long id2 = textIds.get(R.id.item2Text);
    	if (id1 != null && id2 != null) {
    		Log.i(TAG, "Deleting association: " + 
    				id1 + " ("+((Button)findViewById(R.id.item1Text)).getText() + ")" +
    				" <--> "+
    				id2 + " ("+((Button)findViewById(R.id.item2Text)).getText() + ")");
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
    	Long id1 = textIds.get(R.id.item1Text);
    	Long id2 = textIds.get(R.id.item2Text);
    	if (id1 != null) {
    		Log.i(TAG, "Deleting string from database: "+
    				((Button)findViewById(R.id.item1Text)).getText());
    		dbAdapter.deleteString(id1.longValue());
    		clearText(R.id.item1Text);
    	}
    	if (id2 != null) {
    		Log.i(TAG, "Deleting string from database: "+
    				((Button)findViewById(R.id.item2Text)).getText());
    		dbAdapter.deleteString(id2.longValue());
    		clearText(R.id.item2Text);
    	}
    	refreshList();
    }
    
    private void refreshList() {
    	Log.d(TAG, "refreshList()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Long id1 = textIds.get(R.id.item1Text);
    	Long id2 = textIds.get(R.id.item2Text);
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
    	ListView list = (ListView)findViewById(R.id.listView1);
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
    	case ACTIVITY_CHOOSE_DATABASE:
    		onDatabaseChooserResult(resultCode, data);
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
    
    private void onDatabaseChooserResult(int resultCode, Intent data) {
    	Log.d(TAG, "DbTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.d(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		Utils.printBundle(TAG, extras);
    		String value = extras.getString("result");
    		if (value != null && value.length() > 0) {
    			setDbName(value);
    		} else {
    			Log.w(TAG, "Value is null or empty.");
    		}
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
    	if (dbName == null) {
    		Log.i(TAG, "No database selected. Exiting.");
    		setResult(RESULT_CANCELED, null);
    		finish();
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.findItem(R.id.menu_backup).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				exportDatabase();
				return false;
			}
		});
        menu.findItem(R.id.menu_restore).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				importDatabase();
				return false;
			}
		});
        
        menu.findItem(R.id.menu_selectDB).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				selectDatabase();
				return false;
			}
		});
        
        menu.findItem(R.id.menu_deleteDB).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				deleteDatabase();
				return false;
			}
		});
        return true;
    }
    
    private void selectDatabase() {
    	Log.v(TAG, "selectDatabase()");
    	Intent i = new Intent(this, DbTextChooser.class);
    	if (dbName != null) {
    		i.putExtra("value", dbName);
    	}
        startActivityForResult(i, ACTIVITY_CHOOSE_DATABASE);
    }
    
    private void deleteDatabase() {
    	Log.v(TAG, "deleteDatabase()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	clearAll();
    	if (!dbAdapter.deleteDatabase()) {
    		Log.w(TAG, "Could not delete database.");
    		return;
    	}
    	config.deleteDatabase(dbName);
    	dbName = null;
    	selectDatabase();
    }
    
    private void exportDatabase() {
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	InputQuery alert = new InputQuery(this);
    	alert.run(getString(R.string.export_title),
    			getString(R.string.export_value), "backup.db",
				new InputQueryResultListener() {
					@Override
					public void onOk(String result) {
						Log.i(TAG, "Export database to file: " + result);
						try {
							dbAdapter.backupDatabase(result);
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
					@Override
					public void onCancel() {
						Log.d(TAG, "Export cancelled.");			
					}
				});
    }
    
    private void importDatabase() {
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	InputQuery alert = new InputQuery(this);
    	alert.run(getString(R.string.import_title),
    			getString(R.string.import_value), "backup.db",
				new InputQueryResultListener() {
					@Override
					public void onOk(String result) {
						Log.i(TAG, "Import database from file: " + result);
						try {
							dbAdapter.restoreDatabase(result);
							refreshList();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
					@Override
					public void onCancel() {
						Log.d(TAG, "Import cancelled.");			
					}
				});
    }
}
