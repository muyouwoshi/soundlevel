package common.check.unit;

public interface VoiceCheckerInterface{
	public static final int ERROR = 0x01;
	public static final int WARINING = 0x02;
	public static final int NORMAL = 0x03;
	public boolean cheak(double spl,float fft,float percent);
}
