package com.kangirigungi.alchemistlist.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class InputQuery {
	
	private final static String TAG = "InputQuery";
	
	private AlertDialog alert;
	private EditText input;
	private boolean showing;
	private String textAtShow;
	
	public InputQuery(Context context,
			CharSequence title, CharSequence message, 
			CharSequence defaultValue,
			final InputQueryResultListener resultListener) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		Log.v(TAG, "Creating. Title: " + title + ". Message: " + message + ". Default value: " + defaultValue);
		alertBuilder.setTitle(title);
		alertBuilder.setMessage(message);

		// Set an EditText view to get user input 
		input = new EditText(context);
		if (defaultValue != null) {
			input.setText(defaultValue);
		}
		alertBuilder.setView(input);
		
		alertBuilder.setPositiveButton(context.getString(android.R.string.ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String result = input.getText().toString();
				Log.v(TAG, "OK button clicked. Value = "+result);
				showing = false;
				textAtShow = null;
				if (resultListener != null) {
					resultListener.onOk(result);
				}
			}
		});

		alertBuilder.setNegativeButton(context.getString(android.R.string.cancel), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Log.v(TAG, "Cancel button called.");
				showing = false;
				if (resultListener != null) {
					resultListener.onCancel();
				}
				setText(textAtShow);
				textAtShow = null;
			}
		});
		showing = false;
		alert = alertBuilder.create();
	}
	
	public void show() {
		if (!showing) {
			Log.v(TAG, "Showing. Current value = "+getText());
			showing = true;
			textAtShow = getText().toString();
			alert.show();
		} else {
			Log.v(TAG, "Already showing.");
		}
	}
	
	public void dismiss() {
		alert.dismiss();
	}
	
	public boolean isShowing() {
		return showing;
	}
	
	public CharSequence getText() {
		return input.getText();
	}
	
	public void setText(CharSequence value) {
		Log.v(TAG, "setText("+value+")");
		input.setText(value);
	}
	
	public void saveState(Bundle bundle, String id) {
		Log.v(TAG, "saveState("+id+")");
		bundle.putBoolean(id+"_showing", showing);
		bundle.putCharSequence(id+"_text", getText());
		if (textAtShow != null) {
			bundle.putString(id+"_textAtShow", textAtShow);
		}
	}
	
	public void restoreState(Bundle bundle, String id) {
		Log.v(TAG, "restoreState("+id+")");
		if (bundle == null) {
			return;
		}
		CharSequence s = bundle.getCharSequence(id+"_text");
		if (s != null) {
			setText(s);
		}
		String ss = bundle.getString(id+"_textAtShow");
		if (ss != null) {
			textAtShow = ss;
		}
		if (bundle.getBoolean(id+"_showing")) {
			show();
		}
	}
}
