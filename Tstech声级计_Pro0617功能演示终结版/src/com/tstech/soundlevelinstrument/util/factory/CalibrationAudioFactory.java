package com.tstech.soundlevelinstrument.util.factory;

public class CalibrationAudioFactory implements AudioUtilFactory{

	@Override
	public BaseAudioUtil createAudioUtil(int simpleSize) {
		// TODO Auto-generated method stub
		return new CalibrationAudio();
	}

}
