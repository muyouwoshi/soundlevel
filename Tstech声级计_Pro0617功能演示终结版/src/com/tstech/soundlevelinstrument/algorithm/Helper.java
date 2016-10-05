package com.tstech.soundlevelinstrument.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tstech.soundlevelinstrument.view.SurfaceController;


public abstract class Helper<A extends Arith,V extends SurfaceController<?>>  extends Observable implements Runnable,Observer{
	private static double p0 = 0.00002;
	protected float changeX;
	protected final static int BUFFER_SUSECSS = 0;
	protected A arith;
	private List<int[]> srcList;

	protected List<V> viewList = new ArrayList<V>();

	protected  int maxLength;

	protected int lineIndex;
	protected int preSize;
	protected int size;
	protected ExecutorService threadPool;
	
	protected Helper() {
		viewList = new ArrayList<V>();

		threadPool = Executors.newSingleThreadExecutor();

		srcList=new ArrayList<int[]>();
		
	}

	protected  void invalidateAll() {
		int viewCount = viewList.size();
		for(int i = 0; i < viewCount;i++){
			
		}
	}
	
	public void startCaculate(int[] src){
		srcList.add(src);
		
//		if(srcList.size()!=0){
//			for(int i=0;i<srcList.size();i++){
//				caculate(srcList.get(i));
//			}
//			srcList.clear();
//		}
		threadPool.execute(this);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
			synchronized (srcList) {
				if(srcList.size()!=0){
					for(int i=0;i<srcList.size();i++){
						caculate(srcList.get(i));
					}
					srcList.clear();
				}
			}
		
	}
	
	protected abstract void caculate(int[] dataListMap);
	public abstract float getChangeX();
	
	public void addViewQueue(V view){
		if(null == view) return;
		if(!viewList.contains(view)){
			viewList.add(view);
		}		
	}
	
	void remove(V view) {
		// TODO Auto-generated method stub
		if(null!=viewList&&viewList.contains(view)) {
			viewList.remove(view);

			view = null;
			if(viewList.size()==0){
				changeX = 0;
			}
		}

	}
	
	void clear(){

		if(null!= viewList){
			viewList.clear();
		}
	}
	public void resetCalculateResult(){
		if(arith!=null)
			arith.resetResult();
		changeX=0;
		size = 0;
		preSize = 0;
		clearBufferResult();
	}
	public abstract void clearBufferResult();
	
	public static Float dB2Pa(float value){
		
		return (float) (p0 * (Math.pow(10, (value / 20))));
	}
	public static Double dB2Pa(double value){
		return (double)(p0 * (Math.pow(10, (value / 20))));
	}
	
	public static Float dB2dBA(float value){
		return (float) 0;
	}
	public static Float dB2dBC(float value){
		return (float) 0;
	}
}
