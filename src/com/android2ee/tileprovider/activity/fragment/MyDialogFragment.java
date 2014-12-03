package com.android2ee.tileprovider.activity.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.android2ee.tileprovider.R;

public class MyDialogFragment extends DialogFragment {
	
	public static final String TAG = "com.android2ee.tileprovider.mydialogfragment";
	public static final String KEY_TEXT = "com.android.2ee.tileprovider.mydialogfragment.text";

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String text = getArguments().getString(KEY_TEXT);
		return new AlertDialog.Builder(getActivity())
	        .setIcon(android.R.drawable.ic_dialog_info)
	        .setTitle(getString(R.string.title_googlemaps))
	        .setMessage(text)
	        .setCancelable(false)
	        .setPositiveButton(R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	// dismiss the dialog
	                    dialog.dismiss();
	                }
	            }
	        ).create();
	}

	
}
