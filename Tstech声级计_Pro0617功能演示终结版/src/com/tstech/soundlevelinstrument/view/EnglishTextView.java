package com.tstech.soundlevelinstrument.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 * 英文字体textView
 * @author TS-YFZX-CQE
 *
 */
public class EnglishTextView extends TextView{

	public EnglishTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}


	public EnglishTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public EnglishTextView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		AssetManager as = context.getAssets();
//		Typeface font  = Typeface.createFromAsset(as, "fonts/Roboto-Thin.ttf"); 
		Typeface font  = Typeface.createFromAsset(as, "fonts/Roboto-Light.ttf"); 
		setTypeface(font);
	}
	
}
