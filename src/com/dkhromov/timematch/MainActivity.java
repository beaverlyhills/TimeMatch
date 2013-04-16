package com.dkhromov.timematch;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import roboguice.inject.InjectView;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.inject.Inject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends SherlockActivity implements ReferenceClock {

	private static final String RANGE_START = "rangeStart";
	private static final String RANGE_LENGTH = "rangeEnd";
	private static final int ADD_REQUEST = 1;
	private static final String PREFERENCES_FILE = "clocks";
	private static final String CLOCKS_PREF = "clock";
	private static final String CLOCKS_COUNT = "clocksCount";
	private static final String SETTINGS_MODE = "settingsMode";
	
	ListView listView;
	
	boolean settingsMode = false;
	int rangeStart;
	int rangeLength;
	
	ClockAdapter adapter;
	
	final TimeZone utc = TimeZone.getTimeZone("UTC");
	
	Handler handler;
	
	Updater updateClock;
	private SeekBar position;
	private SeekBar range;
	
	boolean isSwipe;
	private TextView hourLabel;
	private TextView rangeLabel;
	private TextView hintLabel;
	private TextView rangeValue;
	private TextView startValue;
			
	class Updater implements Runnable {
		
		public boolean active = true;
		
		@Override
		public void run() {
			if ( active ) {
				Calendar calendar = Calendar.getInstance(utc);
				adapter.invalidate();
				handler.postDelayed(this, (60-calendar.get(Calendar.SECOND))*1000);
			}
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TimeZoneCatalog catalog = new TimeZoneCatalog(this);
        
        adapter  = new ClockAdapter(this, this, catalog);
        
        SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE, 0);
        int i=0;
        int count = prefs.getInt(CLOCKS_COUNT, 0);
        for (;i<count;i++) {
        	String item = prefs.getString(CLOCKS_PREF+i, null);
        	if ( item == null ) {
        		break;
        	}
        	adapter.addClock(TimeZone.getTimeZone(item));
        }

        if ( savedInstanceState != null ) {
        	rangeStart = savedInstanceState.getInt(RANGE_START);
        	rangeLength = savedInstanceState.getInt(RANGE_LENGTH);
        } else {
			rangeStart = prefs.getInt(RANGE_START, 0);
        	rangeLength = prefs.getInt(RANGE_LENGTH, 1);
        }
        
        if ( i == 0 ) {
        	adapter.addClock(TimeZone.getDefault());
        }
        
        if ( getIntent() != null ) {
        	settingsMode = getIntent().getBooleanExtra(SETTINGS_MODE, false);
        }
        
        range = (SeekBar) findViewById(R.id.timeRange);
        range.setProgress(rangeLength-1);
        int settingsVisibility = settingsMode ? View.VISIBLE : View.GONE;
		range.setVisibility(settingsVisibility);
        range.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				rangeLength = progress + 1;
				startValue.setText(Integer.toString(rangeStart));
		        rangeValue.setText(Integer.toString(rangeLength));
				adapter.invalidate();
				saveSettings();
			}
		});
        
        position = (SeekBar) findViewById(R.id.timeStart);
        position.setProgress(rangeStart);
        position.setVisibility(settingsVisibility);
        position.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				rangeStart = progress;
				adapter.invalidate();
				startValue.setText(Integer.toString(rangeStart));
		        rangeValue.setText(Integer.toString(rangeLength));
				saveSettings();
			}
		});
        
        final GestureDetector detector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onShowPress(MotionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
					float distanceY) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				if ( Math.abs(velocityX) > Math.abs(velocityY) ) {
					isSwipe = settingsMode;
				}
				return false;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
				isSwipe = false;
				return false;
			}
		});
        
        listView = (ListView) findViewById(R.id.clockList);
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if ( isSwipe ) {
					adapter.removeClock(position);
					saveSettings();
				}
			}
		});
        
        hourLabel = (TextView) findViewById(R.id.timeStartLabel);
        rangeLabel = (TextView) findViewById(R.id.timeRangeLable);
        hintLabel = (TextView) findViewById(R.id.settingsHint);
        startValue = (TextView) findViewById(R.id.timeStartValueLabel);
        rangeValue = (TextView) findViewById(R.id.timeRangeValueLabel);
        hourLabel.setVisibility(settingsVisibility);
        rangeLabel.setVisibility(settingsVisibility);
        hintLabel.setVisibility(settingsVisibility);
        startValue.setVisibility(settingsVisibility);
        rangeValue.setVisibility(settingsVisibility);
        
        startValue.setText(Integer.toString(rangeStart));
        rangeValue.setText(Integer.toString(rangeLength));
        
        handler = new Handler();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(RANGE_START, rangeStart);
		outState.putInt(RANGE_LENGTH, rangeLength);
	}

	@Override
	protected void onPause() {
		super.onPause();
		updateClock.active = false;
		updateClock = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateClock = new Updater();
        updateClock.run();
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(settingsMode ? R.menu.settings : R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if ( item.getItemId() == R.id.action_add ) {
			Intent intent = new Intent(getApplicationContext(), AddActivity.class);
			startActivityForResult(intent, ADD_REQUEST);
			return true;
		} else if ( item.getItemId() == R.id.action_settings ) {
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.putExtra(SETTINGS_MODE, true);
			startActivity(intent);
			finish();
			return true;
		} else if ( item.getItemId() == R.id.action_done ) {
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(intent);
			finish();
			return true;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public Calendar getCurrentUTCTime() {
		return Calendar.getInstance(utc);
	}

	@Override
	public int getRangeStart() {
		return rangeStart;
	}

	@Override
	public int getRangeLength() {
		return rangeLength;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( requestCode == ADD_REQUEST && resultCode == RESULT_OK && data != null ) {
			String id = data.getStringExtra(AddActivity.SELECTED_ITEM);
			if ( id != null ) {
				adapter.addClock(TimeZone.getTimeZone(id));
				saveSettings();
			}
		}
	}

	private void saveSettings() {
		SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE, 0);
		SharedPreferences.Editor edit = prefs.edit();
		int count = adapter.getCount();
		for (int i=0; i<count; i++) {
			edit.putString("clock"+i, ((ClockInfo)adapter.getItem(i)).timezone.getID());
		}
		edit.putInt(RANGE_START, rangeStart).
		putInt(RANGE_LENGTH, rangeLength).
		putInt(CLOCKS_COUNT, count).
		commit();
	}
	
}
