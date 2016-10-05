package com.tstech.soundlevelinstrument.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 * 中文字体textView
 * @author TS-YFZX-CQE
 *
 */
public class ChineseTextView extends TextView{

	public ChineseTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}


	public ChineseTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ChineseTextView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		AssetManager as = context.getAssets();
		//兰亭黑
		Typeface font  = Typeface.createFromAsset(as, "fonts/black_GBK.TTF"); 
		setTypeface(font);
	}
	
}
