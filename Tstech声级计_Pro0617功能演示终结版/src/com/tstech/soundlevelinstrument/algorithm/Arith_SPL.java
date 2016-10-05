package com.tstech.soundlevelinstrument.algorithm;


public class Arith_SPL extends Arith{
	private int mNativeSPL;
	static{
			System.loadLibrary("SPL");
	}
	public Arith_SPL(){
		mNativeSPL=init();
	}
	public void finalizer(){
		try{
			finalizer(mNativeSPL);
		}finally{
			try{
				super.finalize();
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	} 
	public int calculate(int[] data,int samples){
		return native_calculate(mNativeSPL,data,samples);
	}
	public double[] getResult(){
		return native_getResult(mNativeSPL);
	}
	public float[] getInfo(){
		return native_getInfo(mNativeSPL);
	}
	public void setSamplerate(int sampleRate){
		mNativeSPL=native_setSimpleRate(mNativeSPL,(double)sampleRate);
	}
	public void setWeighting(int weighting){
		mNativeSPL= setWeight(mNativeSPL,weighting);
		System.out.println(mNativeSPL);
	}
	public void setAvarage(int avarage){
		mNativeSPL= setAva(mNativeSPL,avarage);
		System.out.println(mNativeSPL);
	}
	public void resetResult(){
		 native_resetResult(mNativeSPL);
	}
	public void setRange(double range){
		mNativeSPL = native_setRange(mNativeSPL,range);
	}
	
	public double getRange(){
		return native_getRange(mNativeSPL);
	}
	
	private native int init();
	private native void finalizer(int nSPL);
	private native int native_calculate(int nSPL,int[] data, int samples);
	private native double[] native_getResult(int nSPL);
	private native float[] native_getInfo(int nSPL);
	private native int setWeight(int nSPL,int weighting);
	private native int setAva(int nSPL,int avarage);
	private native void native_resetResult(int nSPL);
	private native int native_setRange(int nSPL,double range);
	private native double native_getRange(int nSPL);
	private native int native_setSimpleRate(int nSPL,double sampleRate);

}
