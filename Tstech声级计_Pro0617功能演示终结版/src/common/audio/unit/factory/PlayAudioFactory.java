package common.audio.unit.factory;

import common.audio.unit.audio.PlayAudioUtil;

public class PlayAudioFactory extends AudioUtilFactory<PlayAudioUtil>{
	

	public PlayAudioFactory(int rate) {
		super(rate);
	}

	@Override
	public PlayAudioUtil createAudioUtil() {
		return new PlayAudioUtil(simpleRate);
	}
	
	
}
