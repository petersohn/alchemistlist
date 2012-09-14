package com.kangirigungi.alchemistlist.tools;

import android.widget.ListAdapter;

public class OverrideListAdapter extends OverrideAdapter implements ListAdapter {

	private ListAdapter other;
	
	public OverrideListAdapter(ListAdapter other, AdapterOverride override) {
		super(other, override);
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
