package common.audio.unit.controller;

import android.os.Handler;

import com.tstech.soundlevelinstrument.util.InfoUtil;
import common.audio.unit.factory.AudioUtilFactory;
import common.audio.unit.factory.PlayAudioFactory;
import common.audio.unit.factory.RecordAudioFactory;
import common.audio.unit.task.BaseAudioTask;
import common.audio.unit.task.CalibrationAudioTask;
import common.audio.unit.task.PlayAudioTask;
import common.audio.unit.task.RecordAudioTask;

public class AudioController {

	public final static int UNUSEFUL = -0x02;
	public final static int NEED_CALIBRATION = -0x01;
	public final static int STOP = 0x00;
	public final static int DO_RECORD = 0x01;
	public final static int DO_CALIBRATION = 0x02;
	public final static int DO_PLAY = 0x03;

	
	public AudioState State;
	
	
	private int mState = NEED_CALIBRATION;

	private BaseAudioTask<?,?> mThread;
	private AudioUtilFactory<?> mAudioFactory;

	private static class AudioUtilHolder {
		private static final AudioController INSTANCE = new AudioController();
	}

	private AudioController() {

	}

	public static final AudioController getInstance() {
		return AudioUtilHolder.INSTANCE;
	}

	public void startRecording(){
		mAudioFactory = new RecordAudioFactory(InfoUtil.getSimpleRate());
		mThread =  new RecordAudioTask((RecordAudioFactory) mAudioFactory);
	}
	
	public void startCalibration(Handler handler){
		mAudioFactory = new RecordAudioFactory(InfoUtil.getSimpleRate());
		mThread =  new CalibrationAudioTask((RecordAudioFactory) mAudioFactory,handler);
	}
	
	public void startReplay(){
		mAudioFactory = new PlayAudioFactory(InfoUtil.getSimpleRate());
		mThread =  new PlayAudioTask((PlayAudioFactory) mAudioFactory);
	}
	
	
	public void stop() {
		mThread.stopWork();
	}

	public int getState() {
		return mState;
	}
}
