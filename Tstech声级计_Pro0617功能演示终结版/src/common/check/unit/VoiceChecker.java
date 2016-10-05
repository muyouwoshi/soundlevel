package common.check.unit;

public class VoiceChecker implements VoiceCheckerInterface {

	public final static int NORMAL = 0x01;
	public final static int WARNING = 0x02;
	public final static int ERROR = 0x03;

	public VoiceChecker() {
	}

	@Override
	public boolean cheak(double spl, float fft, float percent) {
	
		return false;
	}

}
