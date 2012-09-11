package com.kangirigungi.alchemistlist.tools;

import android.widget.ListAdapter;

public class SubClickableListAdapter extends SubClickableAdapter implements ListAdapter {

	private ListAdapter other;
	
	public SubClickableListAdapter(ListAdapter other) {
		super(other);
		this.other = other;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return other.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(int position) {
		return other.isEnabled(position);
	}
	
	

}
