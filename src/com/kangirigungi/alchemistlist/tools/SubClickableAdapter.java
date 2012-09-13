package com.kangirigungi.alchemistlist.tools;

import android.database.DataSetObserver;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;

public class SubClickableAdapter implements Adapter {
	
	private final static String TAG = "SubClickableAdapter";
	
	public static interface OnSubItemClickListener {
		public void onSubItemClick(View subView, int position);
	}
	
	private Adapter other;
	
	private SparseArray<OnSubItemClickListener> onClickListeners;
	
	public SubClickableAdapter(Adapter other) {
		this.other = other;
		onClickListeners = new SparseArray<OnSubItemClickListener>();
	}

	public void setOnClickListener(int id, OnSubItemClickListener listener) {
		onClickListeners.put(id, listener);
	}
	
	public void removeOnClickListener(int id) {
		onClickListeners.remove(id);
	}
	
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		Log.d(TAG, "getView("+position+")");
		final View view = other.getView(position, convertView, parent);
		for(int i = 0; i < onClickListeners.size(); i++) {
			View subView = view.findViewById(onClickListeners.keyAt(i));
			if (subView != null) {
				final OnSubItemClickListener listener = onClickListeners.valueAt(i);
				if (listener != null) {
					subView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							listener.onSubItemClick(v, parent.indexOfChild(view));
						}
					});
				}
			}
		}
		return view;
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
