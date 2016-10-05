package common.check.unit;

public class ANDChecker implements VoiceCheckerInterface {
	private VoiceCheckerInterface checkerA;
	private VoiceCheckerInterface checkerB;
    public ANDChecker(VoiceCheckerInterface checkerA ,VoiceCheckerInterface checkerB) {
		this.checkerA = checkerA;
    	this.checkerB = checkerB;
	}
	@Override
	public boolean cheak(double spl, float fft, float percent) {
		
		return checkerA.cheak(spl, fft, percent)&&checkerB.cheak(spl, fft, percent);
	}

}
