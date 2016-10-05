package common.audio.unit.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class RecordAudioUtil extends BaseAudioUtil {
	private AudioRecord mAudioRecord;

	public RecordAudioUtil(int simpleRate) {
		super(simpleRate);
		if (BUFFER_SIZE > 0) {
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, // 声音来源
					SAMPLE_RATE_IN_HZ, // 频率
					AudioFormat.CHANNEL_IN_STEREO, // 声道
					AudioFormat.ENCODING_PCM_16BIT, // 编码
					BUFFER_SIZE); // 缓存空间
		}
	}

	@Override
	public void start() {
		mAudioRecord.startRecording();
	}

	@Override
	public void stop() {
		mAudioRecord.release();
	}

	public int work(short[] mBuffer, int i, int length) {
		return mAudioRecord.read(mBuffer, i, length);
	}

}
