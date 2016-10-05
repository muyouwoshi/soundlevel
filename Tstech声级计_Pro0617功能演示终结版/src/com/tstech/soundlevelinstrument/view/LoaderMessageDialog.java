package com.tstech.soundlevelinstrument.view;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.adapter.RecyclerViewAdapter;
import com.tstech.soundlevelinstrument.back.FileCallBack;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class LoaderMessageDialog extends Dialog{
	private FileCallBack mBack;

	private Button confirm;
	private Button cancle;
	private RecyclerShowView rView;
	public LoaderMessageDialog(Context context,FileCallBack back) {
		super(context);
		mBack = back;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View viewDialog = inflater.inflate(R.layout.dialog, null);

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		// TODO 设置dialog对话框的宽高
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				height * 3/4);
		
		setContentView(viewDialog, layoutParams);
		
		confirm = (Button) viewDialog.findViewById(R.id.dialog_confirm); // 确定
		cancle = (Button) viewDialog.findViewById(R.id.dialog_cancel); // 取消
		rView = (RecyclerShowView) viewDialog.findViewById(R.id.file_selector);
		
		// 确定		TODO dialog确定
		confirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(rView.isCheckBox()){
					mBack.setFilePath(rView.getFile());
					dismiss();
				}
			}
		});
		// 取消
		cancle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
//	public FileCallBack getmBack() {
//		return mBack;
//	}
	public void setmBack(FileCallBack mBack) {
		this.mBack = mBack;
	}

}
