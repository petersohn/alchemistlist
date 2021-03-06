package com.kangirigungi.alchemistlist.tools;

import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.kangirigungi.alchemistlist.tools.OverrideAdapter.AdapterOverride;

public class SubClickableOverride implements AdapterOverride {

	public static interface OnSubItemClickListener {
		public void onSubItemClick(View subView, int position);
	}
	
	private SparseArray<OnSubItemClickListener> onClickListeners;
	
	public SubClickableOverride() {
		onClickListeners = new SparseArray<OnSubItemClickListener>();
	}

	public void setOnClickListener(int id, OnSubItemClickListener listener) {
		onClickListeners.put(id, listener);
	}
	
	public void removeOnClickListener(int id) {
		onClickListeners.remove(id);
	}
	
	public View onOverride(int position, final View convertView, ViewGroup parent) {
		final AdapterView<?> parentView = (AdapterView<?>)parent;
		if (parentView == null) {
			return convertView;
		}
		for(int i = 0; i < onClickListeners.size(); i++) {
			View subView = convertView.findViewById(onClickListeners.keyAt(i));
			if (subView != null) {
				final OnSubItemClickListener listener = onClickListeners.valueAt(i);
				if (listener != null) {
					subView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// position is not good when using MultiListAdapter
							listener.onSubItemClick(v, 
									parentView.getFirstVisiblePosition()+
									parentView.indexOfChild(convertView));
						}
					});
				}
			}
		}
		return convertView;
	}
	
}
