package common.audio.unit.controller;

public abstract class AudioState {
	protected AudioControl mController;
	public AudioState(AudioControl control){
		mController = control;
	}
	
	public abstract void startRecord();
	public abstract void startCalibration();
	public abstract void replay();
	public abstract void stop();
}
