package common.check.unit;

public class OtherCheaker implements VoiceCheckerInterface{
	public final static float DEFAULT_OTH_WARINGVALUE = 0.7f;
	public final static float DEFAULT_OTH_ERRORVALUE = 0.9f;
	private static float waringValue = DEFAULT_OTH_WARINGVALUE;
	private static float errorValue = DEFAULT_OTH_ERRORVALUE;

	public static VoiceState cheak(double spl, float freqValue) {
		if (freqValue / spl < waringValue)
			return VoiceState.NORMAL_VOICE;
		else if (freqValue / spl >= errorValue)
			return VoiceState.ERROR_VOICE;
		else
			return VoiceState.WARNRING_VOICE;
	}

	public static float getWaringValue() {
		return waringValue;
	}

	public static void setWaringValue(float waringValue) {
		OtherCheaker.waringValue = waringValue;
	}

	public static float getErrorValue() {
		return errorValue;
	}

	public static void setErrorValue(float errorValue) {
		OtherCheaker.errorValue = errorValue;
	}

	@Override
	public boolean cheak(double spl, float fft, float percent) {

		return cheakPercent(percent);
	}

	private boolean cheakPercent(float percent) {
		return (percent >= errorValue);
	}

}
