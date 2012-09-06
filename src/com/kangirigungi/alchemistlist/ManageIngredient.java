package com.kangirigungi.alchemistlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.kangirigungi.alchemistlist.Database.DbAdapter;

public class ManageIngredient extends Activity {

	private DbAdapter dbAdapter;
	private String dbName;
	private long id;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ingredient);
        
        Button button = (Button)findViewById(R.id.ingredient_cancel);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
        button = (Button)findViewById(R.id.ingredient_save);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});
        button = (Button)findViewById(R.id.ingredient_delete);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				delete();
			}
		});
        
        Bundle extras = getIntent().getExtras();
        dbName = extras.getString("dbName");
        dbAdapter = new DbAdapter(this);
        dbAdapter.open(dbName);
        
        id = extras.getLong("id");
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_manage_ingredient, menu);
        return true;
    }
    
    private void refresh() {
    	EditText nameField = (EditText)findViewById(R.id.ingredient_name);
    	nameField.setText(dbAdapter.getStringsWrapper().getString(id));
    }

    private void cancel() {
    	setResult(RESULT_CANCELED);
    	finish();
	}
    
    private void finishOk(boolean deleted) {
    	Intent result = new Intent();
    	result.putExtra("deleted", deleted);
    	setResult(RESULT_OK, result);
    	finish();
	}
    
    private void save() {
    	EditText nameField = (EditText)findViewById(R.id.ingredient_name);
    	dbAdapter.changeString(id, nameField.getText().toString());
    	finishOk(false);
    }
    
    private void delete() {
    	dbAdapter.deleteString(id);
    	finishOk(true);
    }
}
