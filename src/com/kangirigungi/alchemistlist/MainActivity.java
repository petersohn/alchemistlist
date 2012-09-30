package com.kangirigungi.alchemistlist;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
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
import com.kangirigungi.alchemistlist.tools.Utils;
import com.kangirigungi.alchemistlist.tools.YesNoDialog;

public class MainActivity extends Activity {

	private static final int ACTIVITY_CHOOSE_DATABASE = 0;
	private static final int ACTIVITY_EXPERIMENT = 10;
	private static final int ACTIVITY_CHOOSE_INGREDIENT = 20;
	private static final int ACTIVITY_MANAGE_INGREDIENT = 21;
	private static final int ACTIVITY_CHOOSE_EFFECT = 30;
	private static final int ACTIVITY_MANAGE_EFFECT = 31;
	
	private static final int DIALOG_BACKUP_DATABASE = 0;
	private static final int DIALOG_RESTORE_DATABASE = 1;
	private static final int DIALOG_DELETE_DATABASE = 2;
	
	private static final String TAG = "MainActivity";
	private String dbName;
	private DbAdapter dbAdapter;
	private ConfigDbAdapter config;
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
        
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch(id) {
        case DIALOG_BACKUP_DATABASE:
            return createBackupDialog(); 
        case DIALOG_RESTORE_DATABASE:
        	return createRestoreDialog();
        case DIALOG_DELETE_DATABASE:
        	return createDeleteDatabaseDialog();
        default:
            return null;
        }
    }
    
    private Dialog createBackupDialog() {
    	return InputQuery.create(this, 
        		getString(R.string.export_title),
    			getString(R.string.export_value),
    			new InputQuery.ResultListener() {
						@Override
						public void onOk(String result) {
							Log.i(TAG, "Backup database to file: " + result);
							try {
								dbAdapter.backupDatabase(result);
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
    }
    
    private Dialog createRestoreDialog() {
    	return InputQuery.create(this, 
        		getString(R.string.import_title),
    			getString(R.string.import_value),
    			new InputQuery.ResultListener() {
						@Override
						public void onOk(String result) {
							Log.i(TAG, "Restore database from file: " + result);
							try {
								dbAdapter.restoreDatabase(result);
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
    }
    
    private Dialog createDeleteDatabaseDialog() {
    	return YesNoDialog.create(this, null, 
    			getString(R.string.deleteDatabaseQuestion), 
    					new YesNoDialog.ResultListener() {
							@Override
							public void onYes() {
								if (!dbAdapter.deleteDatabase()) {
							    	   Log.w(TAG, "Could not delete database.");
							    	   return;
					 	           }
					        	   Log.i(TAG, "Deleting database "+dbName);
					 	           config.deleteDatabase(dbName);
					 	           setDbName(null);
							}
							@Override
							public void onNo() {
								Log.d(TAG, "Delete database cancelled.");
							}
						});
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    	switch(id) {
        case DIALOG_BACKUP_DATABASE:
        case DIALOG_RESTORE_DATABASE:
        	InputQuery.setText(dialog, config.getLastBackup().get());
        	break;
        default:
        	Log.v(TAG, "Ignore dialog id: "+id);
    	}
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
    	showDialog(DIALOG_DELETE_DATABASE, null);
    }
    
    private void exportDatabase() {
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	showDialog(DIALOG_BACKUP_DATABASE, null);
    }
    
    private void importDatabase() {
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	showDialog(DIALOG_RESTORE_DATABASE, null);
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
    	Utils.startIngredientTextChooser(this,  
    			dbName, ACTIVITY_CHOOSE_INGREDIENT, null);
    }
    
    private void launchEffectChooser() {
    	Log.v(TAG, "launchEffectChooser()");
    	if (dbName == null) {
    		Log.w(TAG, "No database selected.");
    		return;
    	}
    	Utils.startEffectTextChooser(this,  
    			dbName, ACTIVITY_CHOOSE_EFFECT, null);
    }

}
