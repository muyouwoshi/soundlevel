package common.audio.unit.task;

import java.util.Observable;

import common.audio.unit.audio.BaseAudioUtil;
import common.audio.unit.factory.AudioUtilFactory;

public abstract class BaseAudioTask<V extends BaseAudioUtil,T extends AudioUtilFactory<V>> extends Observable implements
		Runnable {

	protected boolean isRunning;
	protected T mUtilFactory;
	protected V mAudioUtil;
	protected short[] mBuffer;
	protected int BUFFER_SIZE;

	public BaseAudioTask(T audioUtilFactory) {
		mAudioUtil = audioUtilFactory.createAudioUtil();
		BUFFER_SIZE = mAudioUtil.getMinBufferSize() * 2;
		if (BUFFER_SIZE > 0) {
			mBuffer = new short[BUFFER_SIZE];
			mAudioUtil = null;
		}
	}


	public void stopWork(){
		isRunning = false;
		mAudioUtil.stop();
	}
	
	public void start(){
		isRunning = true;
		new Thread(this).start();
	}
	
	@SuppressWarnings("unchecked")
	public void setAudioUtilFactory(AudioUtilFactory<V> utilFactory){
		mUtilFactory =  (T) utilFactory;
	}
	
	public abstract int getState();
	
}
