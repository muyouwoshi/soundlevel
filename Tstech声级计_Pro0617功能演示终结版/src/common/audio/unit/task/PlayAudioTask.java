package common.audio.unit.task;

import common.audio.unit.audio.PlayAudioUtil;
import common.audio.unit.factory.AudioUtilFactory;
import common.audio.unit.factory.PlayAudioFactory;

public class PlayAudioTask extends BaseAudioTask<PlayAudioUtil,PlayAudioFactory>{

	public PlayAudioTask(PlayAudioFactory utilFactory) {
		super(utilFactory);
	}

	@Override
	public void run() {
		
	}
	@Override
	public int getState() {
		return AudioUtilFactory.PLAY;
	}
}
