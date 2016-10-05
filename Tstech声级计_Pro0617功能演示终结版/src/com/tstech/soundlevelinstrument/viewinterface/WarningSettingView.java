package com.tstech.soundlevelinstrument.viewinterface;

import com.tstech.soundlevelinstrument.bean.ExpWarningSetting;
import common.check.unit.VoiceCheckUtil;

import android.support.v4.app.FragmentActivity;

public interface WarningSettingView {
	
	public FragmentActivity getActivityContext();
	public void initWarning(ExpWarningSetting warningSetting);
	
//	public void setTvSafe(String result);
	public void setTvWarning(String result);
	public void setTvDanger(String result);
	public VoiceCheckUtil getCheckUtil();
	

}
