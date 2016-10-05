package com.tstech.soundlevelinstrument.back;

import java.util.Observer;


public interface DataRecordBack extends Observer{
	public static final int SPL_UPDATE =  0x03; 
	public static final int FFT_UPDATE =  0x04; 
	public static final int UPDATE =  0x05; 
	public void dataRecord(short[] data);
	public void SPLValueUpdate(double value);
	public void FFTValueUpdate(float[] mainFreqs,float[] mainFreqValues);
}
