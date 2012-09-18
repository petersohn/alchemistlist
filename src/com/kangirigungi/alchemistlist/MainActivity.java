package com.kangirigungi.alchemistlist;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.kangirigungi.alchemistlist.Database.ConfigDbAdapter;
import com.kangirigungi.alchemistlist.Database.DbAdapter;
import com.kangirigungi.alchemistlist.tools.InputQuery;
import com.kangirigungi.alchemistlist.tools.InputQueryResultListener;
import com.kangirigungi.alchemistlist.tools.Utils;

public class MainActivity extends Activity {

	private static final int ACTIVITY_CHOOSE_DATABASE = 0;
	private static final int ACTIVITY_EXPERIMENT = 10;
	private static final int ACTIVITY_CHOOSE_INGREDIENT = 20;
	private static final int ACTIVITY_MANAGE_INGREDIENT = 21;
	private static final int ACTIVITY_CHOOSE_EFFECT = 30;
	private static final int ACTIVITY_MANAGE_EFFECT = 31;
	
	private static final String TAG = "MainActivity";
	private String dbName;
	private DbAdapter dbAdapter;
	private ConfigDbAdapter config;
	private InputQuery backupFilename;
	private InputQuery restoreFilename;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button button = (Button)findViewById(R.id.main_btnExperiment);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchExperimentActivity();
			}
		});
        button = (Button)findViewById(R.id.main_btnManageIngredients);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchIngredientChooser();
			}
		});
        button = (Button)findViewById(R.id.main_btnManageEffects);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchEffectChooser();
			}
		});
        
        config = new ConfigDbAdapter(this);
        config.open();
        dbAdapter = new DbAdapter(this);
        setDbName(config.getLastDatabase());
        
        backupFilename = new InputQuery(this, 
        		getString(R.string.export_title),
    			getString(R.string.export_value), "backup.db",
    			new InputQueryResultListener() {
						@Override
						public void onOk(String result) {
							Log.i(TAG, "Backup database to file: " + result);
							try {
								dbAdapter.backupDatabase(result);
							} catch (IOException e) {
								Log.e(TAG, e.getMessage());
							}
						}
						@Override
						public void onCancel() {
							Log.d(TAG, "Backup cancelled.");			
						}
				});
        restoreFilename = new InputQuery(this, 
        		getString(R.string.import_title),
    			getString(R.string.import_value), "backup.db",
    			new InputQueryResultListener() {
						@Override
						public void onOk(String result) {
							Log.i(TAG, "Restore database from file: " + result);
							try {
								dbAdapter.restoreDatabase(result);
							} catch (IOException e) {
								Log.e(TAG, e.getMessage());
							}
						}
						@Override
						public void onCancel() {
							Log.d(TAG, "Restore cancelled.");			
						}
				});
        if (savedInstanceState != null) {
        	Utils.printBundle(TAG, savedInstanceState);
        }
        backupFilename.restoreState(savedInstanceState, "backupFilename");
        restoreFilename.restoreState(savedInstanceState, "restoreFilename");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        backupFilename.saveState(outState, "backupFilename");
        restoreFilename.saveState(outState, "restoreFilename");
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
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case ACTIVITY_CHOOSE_DATABASE:
    		onDatabaseChooserResult(resultCode, data);
    		break;
    	case ACTIVITY_EXPERIMENT:
    		Log.d(TAG, "Experiment activity finished.");
    		break;
    	case ACTIVITY_CHOOSE_INGREDIENT:
    		onIngredientChooserResult(resultCode, data);
    		break;
    	case ACTIVITY_MANAGE_INGREDIENT:
    		Log.d(TAG, "Manage ingredient activity finished.");
    		break;
    	case ACTIVITY_CHOOSE_EFFECT:
    		onEffectChooserResult(resultCode, data);
    		break;
    	case ACTIVITY_MANAGE_EFFECT:
    		Log.d(TAG, "Manage effect activity finished.");
    		break;
    	}
    }
    
    @Override
	protected void onDestroy() {
    	Log.v(TAG, "onDestroy()");
    	backupFilename.dismiss();
    	restoreFilename.dismiss();
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
    	super.onStart();
       	Log.v(TAG, "onStart()");
       	if (dbName == null) {
       		Log.i(TAG, "No database selected. Selecting one.");
       		selectDatabase();
       	}
    }
    
    private void setDbName(String value) {
    	dbAdapter.close();
    	dbName = value;
    	TextView indicator = (TextView)findViewById(R.id.main_currentDatabase);
    	if (dbName != null) {
    		dbAdapter.open(dbName);
    	}
    	indicator.setText(dbName);
    	
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
    	if (!dbAdapter.deleteDatabase()) {
    		Log.w(TAG, "Could not delete database.");
    		return;
    	}
    	config.deleteDatabase(dbName);
    	setDbName(null);
    }
    
    private void exportDatabase() {
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	backupFilename.show();
    }
    
    private void importDatabase() {
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	restoreFilename.show();
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
    	} else {
    		config.saveLastDatabase(dbName);
    	}
    }
    
    private void onIngredientChooserResult(int resultCode, Intent data) {
    	Log.d(TAG, "IngredientTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.v(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		Utils.printBundle(TAG, extras);
    		long id = extras.getLong("id");
    		Log.d(TAG, "Launching ingredient manager for id " + id);
    		Intent manageIngredientIntent = new Intent(this, ManageIngredient.class);
    		manageIngredientIntent.putExtra("id", id);
    		manageIngredientIntent.putExtra("dbName", dbName);
    		startActivityForResult(manageIngredientIntent, ACTIVITY_MANAGE_INGREDIENT);
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
    }
    
    private void onEffectChooserResult(int resultCode, Intent data) {
    	Log.d(TAG, "EffectTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		Log.v(TAG, "Got OK result from activity.");
    		Bundle extras = data.getExtras();
    		Utils.printBundle(TAG, extras);
    		long id = extras.getLong("id");
    		Log.d(TAG, "Launching effect manager for id " + id);
    		Intent manageEffectIntent = new Intent(this, ManageEffect.class);
    		manageEffectIntent.putExtra("id", id);
    		manageEffectIntent.putExtra("dbName", dbName);
    		startActivityForResult(manageEffectIntent, ACTIVITY_MANAGE_EFFECT);
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
    }
    
    private void launchExperimentActivity() {
    	Log.v(TAG, "launchExperimentActivity()");
    	Intent i = new Intent(this, ExperimentActivity.class);
   		i.putExtra("dbName", dbName);
        startActivityForResult(i, ACTIVITY_EXPERIMENT);
    }
    
    private void launchIngredientChooser() {
    	Log.v(TAG, "launchIngredientChooser()");
    	Intent i = new Intent(this, IngredientTextChooser.class);
   		i.putExtra("dbName", dbName);
        startActivityForResult(i, ACTIVITY_CHOOSE_INGREDIENT);
    }
    
    private void launchEffectChooser() {
    	Log.v(TAG, "launchEffectChooser()");
    	Intent i = new Intent(this, EffectTextChooser.class);
   		i.putExtra("dbName", dbName);
        startActivityForResult(i, ACTIVITY_CHOOSE_EFFECT);
    }

}
