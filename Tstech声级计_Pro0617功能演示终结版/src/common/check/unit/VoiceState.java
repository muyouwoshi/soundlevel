package common.check.unit;

public enum VoiceState {
	NORMAL_VOICE(true, false, false), WARNRING_VOICE(false, true, false), ERROR_VOICE(
			false, false, true);

	public boolean NORMAL;
	public boolean WARNRING;
	public boolean ERROR;

	private VoiceState(boolean normal, boolean warning, boolean error) {
		NORMAL = normal;
		WARNRING = warning;
		ERROR = error;
	}

}
