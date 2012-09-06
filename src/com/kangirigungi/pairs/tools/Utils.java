package com.kangirigungi.pairs.tools;

import android.os.Bundle;
import android.util.Log;

public class Utils {
	public static void printBundle(String TAG, Bundle bundle) {
		for (String key: bundle.keySet()) {
			Object obj = bundle.get(key);
			if (obj == null) {
				Log.v(TAG, key + " is null");
			} else {
				Log.v(TAG, key + " = " + obj.toString());
			}
		}
	}
}
