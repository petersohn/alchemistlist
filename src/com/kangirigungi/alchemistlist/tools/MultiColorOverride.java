package com.kangirigungi.alchemistlist.tools;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kangirigungi.alchemistlist.tools.OverrideAdapter.AdapterOverride;

public class MultiColorOverride implements AdapterOverride {

	private static final String TAG = "MultiColorOverride"; 
	
	private int targetViewId;
	private int indicatorViewId;
	private Integer[] colorIds;
	
	public MultiColorOverride(int targetViewId, int indicatorViewId,
			Integer[] colorIds) {
		this.targetViewId = targetViewId;
		this.indicatorViewId = indicatorViewId;
		this.colorIds = (colorIds == null) ? new Integer[0] : colorIds.clone();
	}
	
	@Override
	public View onOverride(int position, View convertView, ViewGroup parent) {
		TextView indicatorView = (TextView)convertView.findViewById(indicatorViewId);
		if (indicatorView == null) {
			Log.w(TAG, "Indicator view invalid.");
			return convertView;
		}
		int colorPosition;
		try {
			colorPosition = Integer.valueOf(indicatorView.getText().toString());
		} catch (NumberFormatException e) {
			Log.w(TAG, "Indicator view text invalid (not a number).");
			return convertView;
		}
		if (colorPosition < 0 || colorPosition >= colorIds.length) {
			Log.w(TAG, "Indicator view text invalid (out of range).");
			return convertView;
		}
		TextView targetView = (TextView)convertView.findViewById(targetViewId);
		if (targetView == null) {
			Log.w(TAG, "Target view invalid.");
			return convertView;
		}
		targetView.setTextColor(colorIds[colorPosition]);
		return convertView;
	}

}
