package com.tstech.soundlevelinstrument.viewinterface;

import android.support.v4.app.FragmentActivity;


public interface CalibrationView {

	public FragmentActivity getContext();

	public void addToTxtStatus(String string);

	public void scrollToBottom();

	public void endCalWithValue(String valueCalibration);
	public void prooFreading();

}
