package com.kangirigungi.pairs;

import java.io.IOException;

import com.kangirigungi.pairs.DbAdapter.DbAdapter;

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

public class MainActivity extends Activity {

	private static final int ACTIVITY_CHOOSE = 0;
	private static final String TAG = "MainActivity";
	
	private SparseArray<Long> textIds;
	private DbAdapter dbAdapter;
	
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
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
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
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("item1", textIds.get(R.id.item1Text));
        outState.putSerializable("item2", textIds.get(R.id.item2Text));
    }
    
    @Override
	protected void onStop() {
    	dbAdapter.cleanup();
		super.onStop();
	}
    
    private void setTextId(int textId, long id) {
    	Button textView = (Button)findViewById(textId);
		textView.setText(dbAdapter.getString(id));
		textIds.put(textId, id);
    }
    
    private void onChooseClick(View v, int textId) {
    	Intent i = new Intent(this, TextChooser.class);
    	i.putExtra("textId", textId);
    	Button textView = (Button)findViewById(textId);
    	i.putExtra("value", textView.getText());
        startActivityForResult(i, ACTIVITY_CHOOSE);
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
    	Log.d(TAG, "Activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.d(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		for (String key: extras.keySet()) {
    			Object obj = extras.get(key);
    			if (obj == null) {
    				Log.d(TAG, key + " is null");
    			} else {
    				Log.d(TAG, key + " = " + obj.toString());
    			}
    		}
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
        return true;
    }
    
    private void exportDatabase() {
    	InputQuery alert = new InputQuery(this);
    	alert.run(getString(R.string.export_title),
    			getString(R.string.export_value), "backup.db",
				new InputQueryResultListener() {
					@Override
					public void onOk(String result) {
						Log.i(TAG, "Export database to file: " + result);
						try {
							dbAdapter.exportDatabase(result);
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
    	InputQuery alert = new InputQuery(this);
    	alert.run(getString(R.string.import_title),
    			getString(R.string.import_value), "backup.db",
				new InputQueryResultListener() {
					@Override
					public void onOk(String result) {
						Log.i(TAG, "Import database from file: " + result);
						try {
							dbAdapter.importDatabase(result);
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
