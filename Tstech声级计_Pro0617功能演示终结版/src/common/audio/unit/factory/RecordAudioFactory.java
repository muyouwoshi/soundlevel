package common.audio.unit.factory;

import common.audio.unit.audio.RecordAudioUtil;
import common.audio.unit.task.BaseAudioTask;
import common.audio.unit.task.CalibrationAudioTask;
import common.audio.unit.task.RecordAudioTask;

public class RecordAudioFactory extends AudioUtilFactory<RecordAudioUtil> {

	public RecordAudioFactory(int rate) {
		super(rate);
		
	}

	@Override
	public RecordAudioUtil createAudioUtil() {
		return new RecordAudioUtil(simpleRate);
	}
}
