package com.tstech.soundlevelinstrument.viewinterface;

import com.tstech.soundlevelinstrument.bean.ExpTextMessage;

import android.support.v4.app.FragmentActivity;

public interface TestMessageView {
	public FragmentActivity getActivityContext();
	
	public void initDatas(ExpTextMessage message);

}
