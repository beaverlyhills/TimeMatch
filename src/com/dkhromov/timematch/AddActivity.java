package com.dkhromov.timematch;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AddActivity extends SherlockListActivity {
	
	public static final String SELECTED_ITEM = "selectedItem";
	
	ArrayAdapter<TimeZoneCatalog.Item> adapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        adapter = new ArrayAdapter<TimeZoneCatalog.Item>(this, android.R.layout.simple_list_item_1);
        
        TimeZoneCatalog catalog = new TimeZoneCatalog(this);
        
        for ( TimeZoneCatalog.Item i : catalog.getList() ) {
        	adapter.add(i);
        }
        
        setListAdapter(adapter);
    }

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	Intent data = new Intent();
    	data.putExtra(SELECTED_ITEM, adapter.getItem(position).id);
		setResult(RESULT_OK, data);
		finish();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		finish();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
}
