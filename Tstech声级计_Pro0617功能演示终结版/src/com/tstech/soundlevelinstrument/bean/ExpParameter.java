package com.tstech.soundlevelinstrument.bean;


public class ExpParameter {
	//加窗
	public final static int WINDOW_HANNING = 0;
	public final static int WINDOW_HANMMING = 1;
	//记权
	public final static int WEIGHT_NONE = 0;
	public final static int WEIGHT_A = 1;
	//快慢档
	public final static int SELECT_FAST = 0;
	public final static int SELECT_SLOW = 1;
	
	/** 默认采样率 */
	public final static int DEFAULT_ACQUI = 48000;
	/** 默认频率范围*/
	public final static float DEFAULT_FREQRANGE = 12000f;
	/** 默认频率分辨率 */
	public final static float DEFAULT_RES = 11.71875f;
	/** 默认重叠率 */
	public final static float DEFAULT_OVER = 0.75f;
	/** 默认平均 */
	public final static int DEFAULT_AVERAGE = 3;
	/** 默认窗函数 */
	public final static int DEFAULT_WINDOW = WINDOW_HANNING;
	/** 默认计权 */
	public final static int DEFAULT_WEIGHT = WEIGHT_NONE;
	/** 默认快慢档 */
	public final static int DEFAULT_SELECT = SELECT_FAST;

	
	private int acquiFreq = DEFAULT_ACQUI;
	private float freqRange = DEFAULT_FREQRANGE;
	private float freqRes = DEFAULT_RES;
	private float overlap = DEFAULT_OVER;
	private int averageCount = DEFAULT_AVERAGE;
	private int windowType = DEFAULT_WINDOW;
	private int weighting = DEFAULT_WEIGHT;
	private int select = DEFAULT_SELECT;
	
	
	public int getAcquiFreq() {
		return acquiFreq;
	}
	public void setAcquiFreq(int acquiFreq) {
		this.acquiFreq = acquiFreq;
	}
	
	public float getFreqRange() {
		return freqRange;
	}
	public void setFreqRange(float freqRange) {
		this.freqRange = freqRange;
	}
	public float getFreqRes() {
		return freqRes;
	}
	public void setFreqRes(float freqRes) {
		this.freqRes = freqRes;
	}
	public float getOverlap() {
		return overlap;
	}
	public void setOverlap(float overlap) {
		this.overlap = overlap;
	}
	public int getAverageCount() {
		return averageCount;
	}
	public void setAverageCount(int averageCount) {
		this.averageCount = averageCount;
	}
	public int getWindowType() {
		return windowType;
	}
	public void setWindowType(int windowType) {
		this.windowType = windowType;
	}
	public int getWeighting() {
		return weighting;
	}
	public void setWeighting(int weighting) {
		this.weighting = weighting;
	}
	public int getSelect() {
		return select;
	}
	public void setSelect(int select) {
		this.select = select;
	}
}
