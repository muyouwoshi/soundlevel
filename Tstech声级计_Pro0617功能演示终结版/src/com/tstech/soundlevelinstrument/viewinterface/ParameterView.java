package com.tstech.soundlevelinstrument.viewinterface;

import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;

import com.tstech.soundlevelinstrument.adapter.ParaSpAdapter;
import com.tstech.soundlevelinstrument.bean.ExpParameter;

public interface ParameterView {
	public FragmentActivity getActivityContext();

	public void reSetFreqRange(ArrayAdapter freqRang_Adapter);//刷新频率范围

	public void reSetAcquiRes(ArrayAdapter adapterRes);

	public void init(ExpParameter mParam);
}
