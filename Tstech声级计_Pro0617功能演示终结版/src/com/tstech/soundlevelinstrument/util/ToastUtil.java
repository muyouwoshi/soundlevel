package com.tstech.soundlevelinstrument.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	private static Toast toast;
	
	public static void showToast(Context context, String content){
		showToast(context,content,Toast.LENGTH_SHORT);
	}
	
	public static void showToast(Context context, String content,int longOrShort){
		if(toast == null){
			toast = Toast.makeText(CtxApp.context, content, Toast.LENGTH_SHORT);
		}
		else{
			toast.setText(content);
		}
		
		toast.show();
	}
}
