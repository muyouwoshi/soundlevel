package com.tstech.soundlevelinstrument.algorithm;

public class Arith_FFT extends Arith {
	private int mNativeFFT;
	static{
		System.loadLibrary("FFT");
	}
	public Arith_FFT(){
		mNativeFFT=init();
	}
	@Override
	protected void finalize(){
		try{
			finalizer(mNativeFFT);
		}finally{
			try{
				super.finalize();
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	}
	public void Int_calculate(int[] buffer, int size){
		native_Int_Calculate(mNativeFFT,buffer,size);
	}
	public void Float_calculate(float[] buffer, int size){
		native_Float_Calculate(mNativeFFT,buffer,size);
	}
	public int GetResultInfo(int channels){
		return native_GetResultInfo(mNativeFFT,channels);
	}
	public float[] GetResult(int index){
		return native_GetResult(mNativeFFT,index);
	}
	public void GetMinMaxValue(float min_value, float max_value){
		native_GetMinMaxValue(mNativeFFT,min_value,max_value);
	}
	public void resetResult(){
		 native_ResetResult(mNativeFFT);
		 native_ResetMeanResult(mNativeFFT);
		 native_ResetSignal(mNativeFFT);
	}
	public int[] GetPalette(int nID){
		return native_GetPalette(nID);
	}
	public float[] GetMeanResult(){
		return native_GetMeanResult(mNativeFFT);
	}
	public void SetWindowType(int windowType){
		mNativeFFT=native_SetWindowType(mNativeFFT,windowType);
	}

	public void SetWinShift(int windowShift){
		mNativeFFT=native_SetWinShift(mNativeFFT,windowShift);

	}
	
	public void setSamplerate(int sampleRate){
		mNativeFFT=native_setSimpleRate(mNativeFFT,(double)sampleRate);
	}
	
	public void SetWinLen(int windowLen){
		mNativeFFT=native_SetWinLen(mNativeFFT, windowLen);

	}

	public void setWeight(int weighting,float[] resultData,double samplerate){
		if(resultData==null||resultData.length==0){
			return ;
		}
		setWeight(mNativeFFT, samplerate,resultData,resultData.length,weighting);
	}	
	public int getWinShift(){
		return  native_GetWinShift(mNativeFFT);
	}
	public int getWinLen(){
		return  native_GetWinLen(mNativeFFT);
	}
	public int getWinType(){
		return native_GetWindowType(mNativeFFT);
	}
	public void setRange(double range){
		mNativeFFT = native_setRange(mNativeFFT,range);
	}
	
	public double getRange(){
		return native_getRange(mNativeFFT);
	}

	private native int init();
	private native void finalizer(int nFFT);
	private native void native_Int_Calculate(int nFFT,int[] buffer, int size);
	private native void native_Float_Calculate(int nFFT,float[] buffer, int size);
	private native int native_GetResultInfo(int nFFT,int channels);
	private native float[] native_GetResult(int nFFT,int index);
	private native void native_GetMinMaxValue(int nFFT,float min_value, float max_value);
	private native int[] native_GetPalette(int nID);
	private native float[] native_GetMeanResult(int nFFT);
	private native void native_ResetMeanResult(int nFFT);
	private native void native_ResetResult(int nFFT);
	private native void native_ResetSignal(int nFFT);
	private native int native_SetWindowType(int nFFT,int windowType);
	private native int native_SetWinShift(int nFFT,int windowShift);
	private native int native_SetWinLen(int nFFT,int windowLen);
	private native float[] setWeight(int nFFT,double samplerate,float[] resultData,int count,int weighting);
	private native int native_GetWinShift(int nFFT);
	private native int native_GetWinLen(int nFFT);
	private native int native_GetWindowType(int nFFT);
	private native int native_setRange(int nFFT,double range);
	private native double native_getRange(int nFFT);
	private native int native_setSimpleRate(int nFFT,double sampleRate);
}
