package com.kangirigungi.alchemistlist.tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class YesNoDialog {
	public interface ResultListener {
		public void onYes();
		public void onNo();
	}

	
	private final static String TAG = "YesNoDialog";
	
	public static Dialog create(Context context,
			String title, String message, 
			final ResultListener resultListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title)
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(context.getText(android.R.string.yes), 
		    		new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.v(TAG, "Yes clicked.");
						if (resultListener != null) {
							resultListener.onYes();
						}
					}
		       })
		       .setNegativeButton(context.getText(android.R.string.no), 
		    		   new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Log.v(TAG, "No clicked.");
			        	   if (resultListener != null) {
			        		   resultListener.onNo();
			        	   }
			           }
			       });
		return builder.create();
	}
}
