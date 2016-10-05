package common.audio.unit.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class PlayAudioUtil extends BaseAudioUtil {
	private AudioTrack mAudioTrack;

	public PlayAudioUtil(int simpleRate) {
		super(simpleRate);
		if (BUFFER_SIZE > 0) {
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_OUT_STEREO,
					AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE,
					AudioTrack.MODE_STREAM);
		}
	}

	@Override
	public void start() {
		mAudioTrack.play();
	}

	@Override
	public void stop() {
		mAudioTrack.release();
	}

	public int work(short[] mBuffer, int i, int lenght) {
		return mAudioTrack.write(mBuffer, 0, lenght);
	}

}
