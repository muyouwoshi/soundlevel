package com.tstech.soundlevelinstrument.util.factory;

import android.media.AudioFormat;
import android.media.AudioRecord;

public abstract class BaseAudioUtil {

	protected Thread mThread;
	protected int SAMPLE_RATE_IN_HZ = 44100;
	protected boolean flag;
	protected boolean isRunning;
	protected AudioRecord mAudioRecord;

	protected int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, // 目标声音采样频率
			AudioFormat.CHANNEL_IN_STEREO, // 声道
			AudioFormat.ENCODING_PCM_16BIT); // 编码格式

	public void start() {
		isRunning = true;
		mThread = new MyThread();
		mThread.start();
	}
	
	protected int getMinBufferSize(int sampleSize) {
		return AudioRecord.getMinBufferSize(sampleSize, // 目标声音采样频率
				AudioFormat.CHANNEL_IN_STEREO, // 声道
				AudioFormat.ENCODING_PCM_16BIT); // 编码格式
	}

	public boolean setSampleRate(int sampleRate) {
		SAMPLE_RATE_IN_HZ = sampleRate;
		return true;
	}

	public void stop() {
		this.flag = false;
		this.isRunning = false;

		mAudioRecord.stop();
		mAudioRecord.release();
		mAudioRecord = null;

		mThread = null;
	}

	public boolean getWorkState() {
		return isRunning;
	}

	class MyThread extends Thread {
		@Override
		public void run() {
			while (isRunning) {
				if (flag) {
					work();
				}
			}
		}
	}

	protected abstract void work();
}
