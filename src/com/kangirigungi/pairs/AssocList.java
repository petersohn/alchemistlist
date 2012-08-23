package com.kangirigungi.pairs;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

public class AssocList extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assoc_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_assoc_list, menu);
        return true;
    }
}
