package com.tstech.soundlevelinstrument.view;

import java.util.Stack;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.back.PopuWindowBack;

/**
 * CalculatorPopupWindow 封装
 * 用于 设置预警条件
 *
 */
public class CalculatorPopupWindow extends PopupWindow implements
		OnClickListener {

	private PopuWindowBack mBack;
	private View conentView;
	private EditText tvPreview;
	private Button btnA;
	private Button btnB;
	private Button btnC;
	private Button btnAnd;
	private Button btnOr;
	private Button bracket_left;
	private Button bracket_right;
	private Button btn_ensure;
	private Button btn_backSpace;
	private String result = null;
	private int viewID;

	public CalculatorPopupWindow(Activity context, PopuWindowBack back,
			int viewID) {
		super(context);
		mBack = back;
		this.viewID = viewID;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		conentView = inflater.inflate(R.layout.calculator_window, null);

		int hight = context.getWindowManager().getDefaultDisplay().getHeight();
		int width = context.getWindowManager().getDefaultDisplay().getWidth();

		// 设置popupWindowde的view
		this.setContentView(conentView);
		// 设置窗体的宽
		this.setWidth(width * 4 / 5);
		// 设置窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		// 刷新状态
		this.update();
		// 不设置背景点击repPopuWindow外部区域或Back键都无法dismiss
		ColorDrawable dw = new ColorDrawable(0000000000);
		this.setBackgroundDrawable(dw);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.AnimationPreview);

		initViews();
		initEvent();
	}

	/** 初始化控件 */
	private void initViews() {
		tvPreview = (EditText) conentView.findViewById(R.id.condition_show);
		btnA = (Button) conentView.findViewById(R.id.btn_A);
		btnB = (Button) conentView.findViewById(R.id.btn_B);
		btnC = (Button) conentView.findViewById(R.id.btn_C);
		btnAnd = (Button) conentView.findViewById(R.id.btn_and);
		btnOr = (Button) conentView.findViewById(R.id.btn_or);
		bracket_left = (Button) conentView.findViewById(R.id.btn_brackets1);
		bracket_right = (Button) conentView.findViewById(R.id.btn_brackets2);
		btn_ensure = (Button) conentView.findViewById(R.id.btn_ensure);
		btn_backSpace = (Button) conentView.findViewById(R.id.btn_backspace);

	}

	/** 监听器 */
	private void initEvent() {
		btnA.setOnClickListener(this);
		btnB.setOnClickListener(this);
		btnC.setOnClickListener(this);
		btnAnd.setOnClickListener(this);
		btnOr.setOnClickListener(this);
		bracket_left.setOnClickListener(this);
		bracket_right.setOnClickListener(this);
		btn_ensure.setOnClickListener(this);
		btn_backSpace.setOnClickListener(this);
	}

	/**
	 * 显示popupWindow
	 * 
	 * @param parent
	 */
	public void showPopupWindow(View parent) {
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
		case R.id.btn_A:
			String strA = tvPreview.getText().toString();
			strA += "A";
			tvPreview.setText(strA);
			break;

		case R.id.btn_B:
			String strB = tvPreview.getText().toString();
			strB += "B";
			tvPreview.setText(strB);
			break;

		case R.id.btn_C:
			String strC = tvPreview.getText().toString();
			strC += "C";
			tvPreview.setText(strC);
			break;

		case R.id.btn_and:
			String strAnd = tvPreview.getText().toString();
			strAnd += "&";
			tvPreview.setText(strAnd);
			break;

		case R.id.btn_or:
			String strOr = tvPreview.getText().toString();
			strOr += "|";
			tvPreview.setText(strOr);
			break;

		case R.id.btn_brackets1:
			String strBrackets1 = tvPreview.getText().toString();
			strBrackets1 += "(";
			tvPreview.setText(strBrackets1);
			break;

		case R.id.btn_brackets2:
			String strBrackets2 = tvPreview.getText().toString();
			strBrackets2 += ")";
			tvPreview.setText(strBrackets2);
			break;

		case R.id.btn_ensure:	//TODO 输入字符检测不严谨需修改
			result = tvPreview.getText().toString().trim();
			if(!mBack.setPopuWindow(result, viewID)) tvPreview.setText("");
			break;

		case R.id.btn_backspace:
			String myStr = tvPreview.getText().toString();
			try {
				tvPreview.setText(myStr.substring(0, myStr.length() - 1));
			} catch (Exception e) {
				tvPreview.setText("");
			}
			break;

		}
	}

	// public PopuWindowBack getmBack() {
	// return mBack;
	// }

	public void setmBack(PopuWindowBack mBack) {
		this.mBack = mBack;
	}

}
