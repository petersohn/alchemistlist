package com.kangirigungi.alchemistlist.tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;

public class InputQuery {
	private final static String TAG = "InputQuery";
	
	private final static int TEXT_ID = 1;
	
	public static Dialog create(Context context,
			String title, String message, 
			final InputQueryResultListener resultListener) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		Log.v(TAG, "Creating. Title: " + title + ". Message: " + message + ".");
		alertBuilder.setTitle(title);
		alertBuilder.setMessage(message);

		// Set an EditText view to get user input 
		final EditText input = new EditText(context);
		input.setId(TEXT_ID);
		alertBuilder.setView(input);
		
		alertBuilder.setPositiveButton(context.getString(android.R.string.ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String result = input.getText().toString();
				Log.v(TAG, "OK button clicked. Value = "+result);
				if (resultListener != null) {
					resultListener.onOk(result);
				}
			}
		});

		alertBuilder.setNegativeButton(context.getString(android.R.string.cancel), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Log.v(TAG, "Cancel button called.");
				if (resultListener != null) {
					resultListener.onCancel();
				}
			}
		});
		return alertBuilder.create();
	}
	
	private static EditText getEditText(Dialog dialog) {
		return (EditText)dialog.findViewById(TEXT_ID);
	}
	
	public static void setText(Dialog dialog, CharSequence value) {
		getEditText(dialog).setText(value);
	}
	
	public static CharSequence getText(Dialog dialog) {
		return getEditText(dialog).getText();
	}
}
