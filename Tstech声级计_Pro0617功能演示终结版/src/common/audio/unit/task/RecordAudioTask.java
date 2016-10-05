package common.audio.unit.task;

import common.audio.unit.audio.RecordAudioUtil;
import common.audio.unit.factory.AudioUtilFactory;
import common.audio.unit.factory.RecordAudioFactory;

public class RecordAudioTask extends BaseAudioTask<RecordAudioUtil,RecordAudioFactory>{
	private int abandonCount =2;
	public RecordAudioTask(RecordAudioFactory utilFactory) {
		super(utilFactory);
	}

	@Override
	public void run() {
		mAudioUtil.start();
		doAbandon();
		while(isRunning){
			mAudioUtil.work(mBuffer, 0, BUFFER_SIZE);
		}
	}
	
	private void doAbandon() {
		for (int i = 0; i < abandonCount; i++) {
			mAudioUtil.work(mBuffer, 0, BUFFER_SIZE);			
		}
	}
	
	@Override
	public int getState() {
		return AudioUtilFactory.RECORD;
	}
}
