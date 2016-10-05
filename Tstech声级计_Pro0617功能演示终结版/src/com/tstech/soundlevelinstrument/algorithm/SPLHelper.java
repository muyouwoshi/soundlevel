package com.tstech.soundlevelinstrument.algorithm;

import java.util.ArrayList;
import java.util.Observable;

import android.os.Handler;

import com.tstech.soundlevelinstrument.back.DataRecordBack;
import com.tstech.soundlevelinstrument.view.SPLSurfaceController;

public class SPLHelper extends Helper<Arith_SPL, SPLSurfaceController> {
	private Handler splPeakVauleHandler;
	private DataRecordBack mBack;

	private static class SPLHelperHolder {
		private static final SPLHelper INSTANCE = new SPLHelper();
	}

	protected SPLHelper() {
		super();
		arith = new Arith_SPL();
	}

	public static final SPLHelper getInstance() {
		return SPLHelperHolder.INSTANCE;
	}

	public void setSPLPeakVauleHandler(Handler splPeakVauleHandler) {
		this.splPeakVauleHandler = splPeakVauleHandler;
	}

	public Handler getSPLPeakVauleHandler() {
		return this.splPeakVauleHandler;
	}

	public SPLHelper setSPLavgTime(int avgTime) {
		arith.setAvarage(avgTime);
		return this;
	}

	public SPLHelper setSPLweighting(int weighting) {
		arith.setWeighting(weighting);
		return this;
	}

	public SPLHelper setRange(double range) {
		arith.setRange(range);
		return this;
	}

	@Override
	public float getChangeX() {
		return changeX;
	}

	@Override
	public void clearBufferResult() {
		SPLSurfaceController sfvc = viewList.get(0);
		sfvc.setData(new ArrayList<Double>());
	}

	@Override
	protected void caculate(int[] data) {
		if (data == null || data.length == 0)
			return;
		int length = data.length;

		arith.calculate(data, length);
		double[] SPLResultData = arith.getResult();
		SPLSurfaceController sfvc = viewList.get(0);
		int maxCount = sfvc.getMaxCount();
		ArrayList<Double> dis = sfvc.getData();
		
		double maxValue = 0;
		
		for (int j = 0; j < SPLResultData.length; j++) {
			dis.add(SPLResultData[j]);
			if(SPLResultData[j]> maxValue) maxValue = SPLResultData[j];
			for (int i = 0; i < dis.size()-maxCount  ; i++) {
				dis.remove(0);
				changeX += 1f;
			}
		}
		mBack.SPLValueUpdate(maxValue);
		sfvc.refresh();
		
		preSize = size;

	}

	@Override
	public void update(Observable observable, Object data) {

	}

	public void setDataRecordBack(DataRecordBack back) {
		mBack = back;
	}

}
