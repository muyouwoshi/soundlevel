package com.tstech.soundlevelinstrument.view;

import com.tstech.soundlevelinstrument.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * ReportPopuwindow 封装
 * 用于生成测试报告或者预警报告
 */
public class ReportPopuwindow extends PopupWindow implements OnClickListener{
	
	private View contentView;
	private TextView tv1, tv2 ;
	
	
	public ReportPopuwindow(Activity context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView =  View.inflate(context, R.layout.popwindow_show,
				null);
		
		int hight = context.getWindowManager().getDefaultDisplay().getHeight();
		int width = context.getWindowManager().getDefaultDisplay().getWidth();
	
		this.setContentView(contentView);
		this.setWidth(width * 3 /4 );
		this.setHeight(hight / 5);
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.update();
		
		ColorDrawable dw = new ColorDrawable(0000000000);
		this.setBackgroundDrawable(dw);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.AnimationPreview);
		
		initViews();
		initEvent();
	
	}

	private void initViews() {
		tv1 = (TextView) contentView.findViewById(R.id.pop_text_report);
		tv2 = (TextView) contentView.findViewById(R.id.pop_warning_report);
	}

	private void initEvent() {
		tv1.setOnClickListener(this);
		tv2.setOnClickListener(this);
	}

	/**
	 * 显示popupWindow
	 * 
	 * @param parent
	 */
	public void reportPopupWindow(View parent) {
		if (!this.isShowing()) {
			// popupwindow显示在屏幕中央
			this.showAtLocation(parent, Gravity.CENTER, 0, 0);
		} else {
			this.dismiss();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pop_text_report:
			
			break;
			
		case R.id.pop_warning_report:
			
			break;

		
		}
	}

}
