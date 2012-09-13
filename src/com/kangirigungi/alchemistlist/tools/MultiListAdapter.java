package com.kangirigungi.alchemistlist.tools;

import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class MultiListAdapter implements ListAdapter {

	private List<ListAdapter> adapters;
	
	private static class PositionResult {
		ListAdapter adapter;
		int adapterId;
		int position;
		PositionResult(ListAdapter adapter, int adapterId, int position) {
			this.adapter = adapter;
			this.adapterId = adapterId;
			this.position = position;
		}
	}
	
	private PositionResult getInternalPosition(int position) {
		int i = 0;
		int result = position;
		for (ListAdapter adapter: adapters) {
			int count = adapter.getCount();
			if (result < count) {
				return new PositionResult(adapter, i, result);
			}
			result -= count;
			++i;
		}
		return null;
	}
	
	public MultiListAdapter(List<ListAdapter> adapters) {
		this.adapters = adapters;
	}
	
	@Override
	public int getCount() {
		int result = 0;
		for (ListAdapter adapter: adapters) {
			int c = adapter.getCount();
			result += c;
		}
		return result;
	}

	@Override
	public Object getItem(int position) {
		PositionResult p = getInternalPosition(position);
		return p.adapter.getItem(p.position);
	}

	@Override
	public long getItemId(int position) {
		PositionResult p = getInternalPosition(position);
		return p.adapter.getItemId(p.position);
	}

	private int getMaxViewType() {
		int result = 1;
		for (ListAdapter adapter: adapters) {
			result = Math.max(result, adapter.getViewTypeCount());
		}
		return result;
	}
	
	@Override
	public int getItemViewType(int position) {
		PositionResult p = getInternalPosition(position);
		return p.adapterId*getMaxViewType() + p.adapter.getItemViewType(p.position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PositionResult p = getInternalPosition(position);
		return p.adapter.getView(p.position, convertView, parent);
	}

	@Override
	public int getViewTypeCount() {
		int result = 0;
		for (ListAdapter adapter: adapters) {
			result += adapter.getViewTypeCount();
		}
		return result;
	}

	@Override
	public boolean hasStableIds() {
		for (ListAdapter adapter: adapters) {
			if (!adapter.hasStableIds()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		for (ListAdapter adapter: adapters) {
			if (!adapter.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		for (ListAdapter adapter: adapters) {
			adapter.registerDataSetObserver(observer);
		}

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		for (ListAdapter adapter: adapters) {
			adapter.unregisterDataSetObserver(observer);
		}
	}

	@Override
	public boolean areAllItemsEnabled() {
		for (ListAdapter adapter: adapters) {
			if (!adapter.areAllItemsEnabled()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		PositionResult p = getInternalPosition(position);
		return p.adapter.isEnabled(p.position);
	}

}
