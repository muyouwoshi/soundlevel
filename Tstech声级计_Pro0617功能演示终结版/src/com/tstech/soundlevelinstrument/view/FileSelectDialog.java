package com.tstech.soundlevelinstrument.view;

import com.tstech.soundlevelinstrument.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class FileSelectDialog extends Dialog {

	protected FileSelectDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public FileSelectDialog(Context context) {
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		View viewDialog = inflater.inflate(R.layout.dialog, null);
//		View reView = viewDialog.findViewById(R.id.dialog_re);
//		setContentView(reView);
	}

}
