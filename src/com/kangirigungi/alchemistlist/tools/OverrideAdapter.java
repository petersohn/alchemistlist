package com.kangirigungi.alchemistlist.tools;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

public class OverrideAdapter implements Adapter {
	
	public interface AdapterOverride {
		public View onOverride(final int position, View convertView, final ViewGroup parent);
	}
	
	private Adapter other;
	private AdapterOverride override;
	
	public OverrideAdapter(Adapter other, AdapterOverride override) {
		this.other = other;
		this.override = override;
	}
	
	@Override
	public View getView( int position, View convertView, ViewGroup parent) {
		View view = other.getView(position, convertView, parent);
		return override.onOverride(position, view, parent);
	}
	
	@Override
	public int getCount() {
		return other.getCount();
	}

	@Override
	public Object getItem(int position) {
		return other.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return other.getItemId(position);
	}

	@Override
	public int getItemViewType(int position) {
		return other.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return other.getViewTypeCount();
	}

	@Override
	public boolean hasStableIds() {
		return other.hasStableIds();
	}

	@Override
	public boolean isEmpty() {
		return other.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		other.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		other.unregisterDataSetObserver(observer);

	}

}
