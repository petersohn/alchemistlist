package com.kangirigungi.alchemistlist;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
	private Button btnExperiment;
	private Button btnManageIngredient;
	private Button btnManageEffect;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnExperiment = (Button)findViewById(R.id.main_btnExperiment);
        btnExperiment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchExperimentActivity();
			}
		});
        btnManageIngredient = (Button)findViewById(R.id.main_btnManageIngredients);
        btnManageIngredient.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchIngredientChooser();
			}
		});
        btnManageEffect = (Button)findViewById(R.id.main_btnManageEffects);
        btnManageEffect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchEffectChooser();
			}
		});
        
        config = new ConfigDbAdapter(this);
        config.open();
        dbAdapter = new DbAdapter(this);
        setDbName(config.getLastDatabase().get());
        
        String lastDatabase = config.getLastBackup().get();
        if (lastDatabase == null) {
        	lastDatabase = "backup.db";
        }
        
        backupFilename = new InputQuery(this, 
        		getString(R.string.export_title),
    			getString(R.string.export_value), lastDatabase,
    			new InputQueryResultListener() {
						@Override
						public void onOk(String result) {
							Log.i(TAG, "Backup database to file: " + result);
							try {
								dbAdapter.backupDatabase(result);
								restoreFilename.setText(result);
								config.getLastBackup().set(result);
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
    			getString(R.string.import_value), lastDatabase,
    			new InputQueryResultListener() {
						@Override
						public void onOk(String result) {
							Log.i(TAG, "Restore database from file: " + result);
							try {
								dbAdapter.restoreDatabase(result);
								backupFilename.setText(result);
								config.getLastBackup().set(result);
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
    		onDatabaseChooserResult(resultCode, Utils.getExtrasIfExists(data));
    		break;
    	case ACTIVITY_EXPERIMENT:
    		Log.d(TAG, "Experiment activity finished.");
    		break;
    	case ACTIVITY_CHOOSE_INGREDIENT:
    		onIngredientChooserResult(resultCode, Utils.getExtrasIfExists(data));
    		break;
    	case ACTIVITY_MANAGE_INGREDIENT:
    		Log.d(TAG, "Manage ingredient activity finished.");
    		break;
    	case ACTIVITY_CHOOSE_EFFECT:
    		onEffectChooserResult(resultCode, Utils.getExtrasIfExists(data));
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
    		config.getLastDatabase().set(dbName);
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
    	boolean hasDb = dbName != null;
    	if (hasDb) {
    		dbAdapter.open(dbName);
    	}
    	btnExperiment.setEnabled(hasDb);
    	btnManageIngredient.setEnabled(hasDb);
    	btnManageEffect.setEnabled(hasDb);
    	indicator.setText(dbName);
    	config.getLastDatabase().set(dbName);
    }
    
    private void selectDatabase() {
    	Log.v(TAG, "selectDatabase()");
    	Bundle extras = new Bundle();
    	if (dbName != null) {
    		extras.putString("value", dbName);
    	}
        Utils.startActivityWithDb(this, DbTextChooser.class, 
    			null, ACTIVITY_CHOOSE_DATABASE, extras);
    }
    
    private void deleteDatabase() {
    	Log.v(TAG, "deleteDatabase()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Really delete database "+dbName+"?")
    	       .setCancelable(false)
    	       .setPositiveButton(getString(android.R.string.yes), 
    	    		   new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   if (!dbAdapter.deleteDatabase()) {
		    	           Log.w(TAG, "Could not delete database.");
		    	           return;
	    	           }
    	        	   Log.i(TAG, "Deleting database "+dbName);
	    	           config.deleteDatabase(dbName);
	    	           setDbName(null);
	    	           dialog.dismiss();
    	           }
    	       })
    	       .setNegativeButton(getString(android.R.string.no), 
    	    		   new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   Log.i(TAG, "Not deleting database "+dbName);
    	                dialog.cancel();
    	           }
    	       });
    	builder.show();
    	
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
    
    private void onDatabaseChooserResult(int resultCode, Bundle extras) {
    	Log.d(TAG, "DbTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		String value = extras.getString("result");
    		if (value.length() == 0) {
    			value = null;
    		}
    		setDbName(value);
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
    }
    
    private void onIngredientChooserResult(int resultCode, Bundle extras) {
    	Log.d(TAG, "IngredientTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		long id = extras.getLong("id");
    		Log.d(TAG, "Launching ingredient manager for id " + id);
    		Utils.startActivityWithDb(this, ManageIngredient.class, 
	    			dbName, ACTIVITY_MANAGE_INGREDIENT, extras);
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
    }
    
    private void onEffectChooserResult(int resultCode, Bundle extras) {
    	Log.d(TAG, "EffectTextChooser activity returned with code: " + resultCode);
    	if (resultCode == RESULT_OK) {
    		long id = extras.getLong("id");
    		Log.d(TAG, "Launching effect manager for id " + id);
    		Utils.startActivityWithDb(this, ManageEffect.class, 
	    			dbName, ACTIVITY_MANAGE_EFFECT, extras);
    	} else {
    		Log.v(TAG, "DbTextChooser cancelled.");
    	}
    }
    
    private void launchExperimentActivity() {
    	Log.v(TAG, "launchExperimentActivity()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Utils.startActivityWithDb(this, ExperimentActivity.class, 
    			dbName, ACTIVITY_EXPERIMENT);
    }
    
    private void launchIngredientChooser() {
    	Log.v(TAG, "launchIngredientChooser()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Utils.startActivityWithDb(this, IngredientTextChooser.class, 
    			dbName, ACTIVITY_CHOOSE_INGREDIENT);
    }
    
    private void launchEffectChooser() {
    	Log.v(TAG, "launchEffectChooser()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Utils.startActivityWithDb(this, EffectTextChooser.class, 
    			dbName, ACTIVITY_CHOOSE_EFFECT);
    }

}
