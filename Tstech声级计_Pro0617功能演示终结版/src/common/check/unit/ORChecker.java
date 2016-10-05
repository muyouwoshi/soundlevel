package common.check.unit;

public class ORChecker implements VoiceCheckerInterface {
	private VoiceCheckerInterface checkerA;
	private VoiceCheckerInterface checkerB;
    public ORChecker(VoiceCheckerInterface checkerA ,VoiceCheckerInterface checkerB) {
		this.checkerA = checkerA;
    	this.checkerB = checkerB;
	}
	@Override
	public boolean cheak(double spl, float fft, float percent) {
		
		return checkerA.cheak(spl, fft, percent)||checkerB.cheak(spl, fft, percent);
	}
}
