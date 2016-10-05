package common.audio.unit.factory;

import common.audio.unit.audio.BaseAudioUtil;

public abstract class AudioUtilFactory<T extends BaseAudioUtil> {
	
	public final static int RECORD = 0x01;
	public final static int CALIBRATION = 0x02;
	public final static int PLAY = 0x03;
	
	protected int simpleRate;
	public AudioUtilFactory(int rate){
		simpleRate = rate;
	}

	public abstract T createAudioUtil();
}
