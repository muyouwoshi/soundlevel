package common.check.unit;

public class SPLChecker implements VoiceCheckerInterface{
	public final static  float DEFAULT_SPL_WARINGVALUE = 70;
	public final static  float DEFAULT_SPL_ERRORGVALUE = 90;
	private static double waringValue = DEFAULT_SPL_WARINGVALUE;
	private static double errorValue = DEFAULT_SPL_ERRORGVALUE;

	public static VoiceState cheak(Double data) {
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

	public static void setWaringValue(double waringValue) {
		SPLChecker.waringValue = waringValue;
	}

	public static double getErrorValue() {
		return errorValue;
	}

	public static void setErrorValue(double errorValue) {
		SPLChecker.errorValue = errorValue;
	}

	@Override
	public boolean cheak(double spl, float fft, float percent) {
		return cheakSPL(spl);
	}

	private boolean cheakSPL(double spl) {
		// TODO Auto-generated method stub
		return (spl >= errorValue);
	}
}
