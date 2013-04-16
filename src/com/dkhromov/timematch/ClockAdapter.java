package com.dkhromov.timematch;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.google.inject.Inject;

import android.app.Activity;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ClockAdapter implements ListAdapter {

	final Activity context;
	final ReferenceClock reference;
	final TimeZoneCatalog catalog;
	
	final ArrayList<ClockInfo> clocks = new ArrayList<ClockInfo>();
	final ArrayList<DataSetObserver> observers = new ArrayList<DataSetObserver>();
	
	public ClockAdapter(Activity context, ReferenceClock reference, TimeZoneCatalog catalog) {
		this.context = context;
		this.reference = reference;
		this.catalog = catalog;
	}
	
	@Override
	public int getCount() {
		return clocks.size();
	}

	@Override
	public Object getItem(int position) {
		return clocks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View clockView = convertView;
		if ( clockView == null ) {
			clockView = context.getLayoutInflater().inflate(R.layout.clock_item, null);
		}
		
		ImageView img = (ImageView) clockView.findViewById(R.id.clockImage);
		
		ClockInfo clockInfo = clocks.get(position);
		img.setImageDrawable(clockInfo);
		img.invalidate();
		
		TextView label = (TextView) clockView.findViewById(R.id.titleText);
		label.setText(clockInfo.getName());
		
		TextView detail = (TextView) clockView.findViewById(R.id.detailsText);
		detail.setText(clockInfo.getTimeRangeString());
		
		return clockView;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return clocks.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers.remove(observer);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	public void addClock(TimeZone timezone) {
		ClockInfo clock = new ClockInfo(context, reference, timezone, catalog);
		clocks.add(clock);
		for (DataSetObserver o : observers) {
			o.onChanged();
		}
	}

	public void invalidate() {
		for (DataSetObserver o : observers) {
			o.onChanged();
		}
	}

	public void removeClock(int position) {
		clocks.remove(position);
		for (DataSetObserver o : observers) {
			o.onChanged();
		}
	}

}
