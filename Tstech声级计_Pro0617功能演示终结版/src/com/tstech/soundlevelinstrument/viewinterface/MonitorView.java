package com.tstech.soundlevelinstrument.viewinterface;

import java.util.List;

import common.check.unit.VoiceState;

import android.content.Context;
import android.graphics.Bitmap;

public interface MonitorView {
	/**
	 * 后处理状态
	 */
	public final static int MODE_C=0;	//后处理
	/**
	 * 监测状态
	 */
	public final static int MODE_P=1;	//监测
	public void hideFileSelector(int mode);
	public void finishActivity();
	public void hideLeftMenu();
	public Context getContext();
	public void resetStartButton(int flag);
	public void setModeSwitch(int mode);
	public void updateSPLValue(String str);
	public void updateFFTValues(List<String> freqsAndValues);
	public void setTvShow(String carDwTv);
	public void setLightChange(int state);
	
	
}
