package com.kangirigungi.pairs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;

public class InputQuery {
	
	private final static String TAG = "InputQuery";
	
	private Context context;
	
	public InputQuery(Context context) {
		this.context = context;
	}
	
	public void run(CharSequence title, CharSequence message, CharSequence defaultValue,
			final InputQueryResultListener resultListener) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle("Change string");
		alert.setMessage("change the value of the string");

		// Set an EditText view to get user input 
		final EditText input = new EditText(context);
		if (defaultValue != null) {
			input.setText(defaultValue);
		}
		alert.setView(input);

		alert.setPositiveButton(context.getString(android.R.string.ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String result = input.getText().toString();
				Log.v(TAG, "OK button clicked. Value = "+result);
				resultListener.onOk(result);
			}
		});

		alert.setNegativeButton(context.getString(android.R.string.cancel), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Log.v(TAG, "Cancel buttin called.");
				resultListener.onCancel();
			}
		});

		alert.show();
	}
}
