package com.tstech.soundlevelinstrument.algorithm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.os.Handler;

import com.tstech.soundlevelinstrument.back.DataRecordBack;
import com.tstech.soundlevelinstrument.view.FFTSurfaceController;

public class FFTHelper extends Helper<Arith_FFT, FFTSurfaceController> {

	private Handler fftPeakVauleHandler;

	private int acquiFreq = 48000;
	private float freqRange = 24000;
	private float freqRes = 20f;
	private float overlap = 0.75f;
	private int windowType = 0;
	private int averageCount = 3;
	private int weighting = 0;
	private List<float[]> averageList;
	private int requestCount;

	private DataRecordBack mBack;

	private static class FFTHelperHolder {
		private static final FFTHelper INSTANCE = new FFTHelper();
	}

	protected FFTHelper() {
		super();
		arith = new Arith_FFT();
		averageList = new ArrayList<float[]>();
		requestCount = (int) (freqRange / freqRes);
	}

	public static final FFTHelper getInstance() {
		return FFTHelperHolder.INSTANCE;
	}

	public void setFFTPeakVauleHandler(Handler fftPeakVauleHandler) {
		this.fftPeakVauleHandler = fftPeakVauleHandler;
	}

	public float getAcquiFreq() {
		return acquiFreq;
	}

	public FFTHelper setAcquiFreq(int acquiFreq) {
		this.acquiFreq = acquiFreq;
		return this;
	}

	public float getFreqRange() {
		return freqRange;
	}

	public FFTHelper setFreqRange(float freqRange) {
		this.freqRange = freqRange;
		requestCount = (int) (freqRange / freqRes);
		for (int i = 0; i < viewList.size(); i++) {
			viewList.get(i).setXRang(freqRange);
		}
		return this;
	}

	@Override
	public void addViewQueue(FFTSurfaceController view) {
		super.addViewQueue(view);
		for (int i = 0; i < viewList.size(); i++) {
			viewList.get(i).setXRang(freqRange);
		}
	}

	public float getFreqRes() {
		return freqRes;
	}

	public FFTHelper setFreqRes(float freqRes) {
		this.freqRes = freqRes;
		requestCount = (int) (freqRange / freqRes);
		return this;
	}

	public float getOverlap() {
		return overlap;
	}

	public FFTHelper setOverlap(float overlap) {
		this.overlap = overlap;
		return this;
	}

	public int getWindowType() {
		return windowType;
	}

	public FFTHelper setWindowType(int windowType) {
		this.windowType = windowType;
		return this;
	}

	public int getAveraging() {
		return averageCount;
	}

	public FFTHelper setAveraging(int averaging) {
		this.averageCount = averaging;
		return this;
	}

	public FFTHelper setWeighting(int weighting) {
		this.weighting = weighting;
		return this;
	}

	public void setRange(double range) {
		arith.setRange(range);
	}

	public void commit() {
		acquiFreq = 48000;
		arith.SetWindowType(windowType);
		arith.setSamplerate(acquiFreq);
		BigDecimal winlen_bigDecimal = new BigDecimal(acquiFreq / freqRes)
				.setScale(0, BigDecimal.ROUND_HALF_UP);
		int winlen = winlen_bigDecimal.intValue();
		BigDecimal winshift_bigDecimal = new BigDecimal(winlen * (1 - overlap)
				+ 0.5).setScale(0, BigDecimal.ROUND_HALF_UP);
		int winshift = winshift_bigDecimal.intValue();

		arith.SetWinLen(winlen);
		arith.SetWinShift(winshift);

	}

	public Handler getFFTPeakVauleHandler() {
		return fftPeakVauleHandler;
	}

	@Override
	public float getChangeX() {
		return 0;
	}

	@Override
	public void clearBufferResult() {
		averageList.clear();
		for (int viewIndex = 0; viewIndex < viewList.size(); viewIndex++) {
			FFTSurfaceController sfvc = viewList.get(viewIndex);
			sfvc.clear();
		}
	}

	@Override
	protected void caculate(int[] data) {
	
		for (int viewIndex = 0; viewIndex < viewList.size(); viewIndex++) {
			FFTSurfaceController sfvc = viewList.get(viewIndex);

			if (data == null || data.length == 0)
				return;
			arith.Int_calculate(data, data.length);
			size = arith.GetResultInfo(0);

			float[] dis = new float[requestCount];
			float[] FFTResult = new float[requestCount];
			for (int i = preSize; i < size; i++) {

				FFTResult = arith.GetResult(i);

				if (weighting > 0)
					arith.setWeight(weighting, FFTResult, 48000);
				dis = getAverageResult(FFTResult);

			}
			requestCount = dis.length > requestCount ? requestCount
					: dis.length;

			float[] resultData = new float[requestCount];
			float[] riginalData = new float[requestCount];
			System.arraycopy(dis, 0, resultData, 0, requestCount);
			System.arraycopy(FFTResult,0,riginalData,0,requestCount);
			sfvc.setOriginalData(riginalData);
			sfvc.setData(resultData);
			preSize = size;
			mBack.FFTValueUpdate(sfvc.getMainFreqs(),sfvc.getMainFreqValues());
		}

	}

	// 获取平均值
	private float[] getAverageResult(float[] src) {
		if (averageList.size() == averageCount - 1) {

			float[] replace = new float[src.length];

			for (int index = 0; index < src.length; index++) {
				float value = 0;
				for (int arrayIndex = 0; arrayIndex < averageCount; arrayIndex++) { // 将每组数据的第
																					// index
																					// 数
																					// 加起来求平均
					value += Math.pow(10,
							averageList.get(index)[arrayIndex] / 10);
				}
				replace[index] = (float) (10 * Math.log10(value / averageCount));
			}
			
			averageList.add(replace);
			averageList.remove(0);
			return replace;

		}
		return src;
	}
	

	@Override
	public void update(Observable observable, Object data) {
		
	}

	public void setDataRecordBack(DataRecordBack back) {
		mBack = back;
	}
}
