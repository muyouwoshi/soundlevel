package com.tstech.soundlevelinstrument.util;

import android.app.Application;
import android.content.Context;

public class CtxApp extends Application {
	
	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

}
