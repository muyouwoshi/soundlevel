package common.check.unit;


public class FFTChecker implements VoiceCheckerInterface{
	public final static float DEFALUT_WARNING = 70;
	public final static float DEFALUT_ERROR = 90;
	private static float waringValue = DEFALUT_WARNING;
	private static float errorValue = DEFALUT_ERROR;
	
	public  VoiceState cheak(Float data) {
		if (data < waringValue)
			return VoiceState.NORMAL_VOICE;
		else if (data >= errorValue)
			return VoiceState.ERROR_VOICE;
		else
			return VoiceState.WARNRING_VOICE;
	}

	public static double getWaringValue() {
		return waringValue;
	}

	public static void setWaringValue(float waringValue) {
		FFTChecker.waringValue = waringValue;
	}

	public static double getErrorValue() {
		return errorValue;
	}

	public static void setErrorValue(float errorValue) {
		FFTChecker.errorValue = errorValue;
	}

	@Override
	public boolean cheak(double spl, float fft, float percent) {
		
		return cheakFFT(fft);
	}

	private boolean cheakFFT(float fft) {
		return (fft >= errorValue);
	}
}
