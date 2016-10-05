package common.audio.unit.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;

import com.tstech.soundlevelinstrument.util.InfoUtil;


public abstract class BaseAudioUtil{
	/** 默认采样频率 */
	protected int SAMPLE_RATE_IN_HZ = 48000;
	/** 存储数据的缓冲区大小*/
	protected int BUFFER_SIZE;
		
	public BaseAudioUtil(int simpleRate){
		SAMPLE_RATE_IN_HZ = simpleRate;
		getMinBufferSize();
	}
	
	public int getMinBufferSize() {
		SAMPLE_RATE_IN_HZ = InfoUtil.getSimpleRate();
		BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, // 目标声音采样频率
				AudioFormat.CHANNEL_IN_STEREO, // 声道
				AudioFormat.ENCODING_PCM_16BIT); // 编码格式
		return BUFFER_SIZE;
	}
	
	public abstract void stop();
	public abstract void start();

}
