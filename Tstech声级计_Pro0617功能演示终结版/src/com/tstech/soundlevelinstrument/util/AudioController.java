package com.tstech.soundlevelinstrument.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import java.util.ArrayList;
import java.util.List;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tstech.soundlevelinstrument.algorithm.FFTHelper;
import com.tstech.soundlevelinstrument.algorithm.Helper;
import com.tstech.soundlevelinstrument.algorithm.SPLHelper;
import com.tstech.soundlevelinstrument.back.DataRecordBack;

public class AudioController {

	public final static int UNUSEFUL = -0x02;
	public final static int NEED_CALIBRATION = -0x01;
	
	public final static int STOP = 0x00;
	public final static int DO_RECORD = 0x01;
	public final static int DO_CALIBRATION = 0x02;
	public final static int DO_PLAY = 0x03;
	
	private boolean hasCalibration;

	private int mState = STOP;
	private int abandonCount = 2; // 抛弃 abandonCount 组不正常的采集数据

	/** 回放文件丢失状态*/
	public final static int MISS_FILE = -0x01;
	
	/** 默认采样频率 */
	private int SAMPLE_RATE_IN_HZ = 48000;
	/** 消息处理器 */
	private Handler mHandler;
	private DataRecordBack mDataReceiver;
	/** 获取最小缓存空间 */
	private int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, // 目标声音采样频率
			AudioFormat.CHANNEL_IN_STEREO, // 声道
			AudioFormat.ENCODING_PCM_16BIT); // 编码格式

	private Thread mThread;

	/** 录音机 */
	private AudioRecord mAudioRecord;
	/** 放音 */
	private AudioTrack mAudioTrack;

	/** 校对开始时间 */
	private long calibrationStartTime = 0;

	private List<Double> proofreadList = new ArrayList<Double>();

	/** 5s校对的反对数 */
	private double proofreadValueTmp;

	/** 监测使用的反对数 */
	private double proofreadValue = 1;
	private double overValue = 0;
	private float pressureRatio = 1;

	short[] mBuffer = new short[BUFFER_SIZE];
	ByteBuffer mByteBuffer;
	
	private File mFile;
	private DataInputStream mDis;

	private static class AudioUtilHolder {
		private static final AudioController INSTANCE = new AudioController();
	}

	private AudioController() {

	}

	public static final AudioController getInstance() {
		return AudioUtilHolder.INSTANCE;
	}

	private boolean getMinBufferSize() {
		SAMPLE_RATE_IN_HZ = InfoUtil.getSimpleRate();
		BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, // 目标声音采样频率
				AudioFormat.CHANNEL_IN_STEREO, // 声道
				AudioFormat.ENCODING_PCM_16BIT); // 编码格式

		if (BUFFER_SIZE < 0) {
			return false;
		}

		mBuffer = new short[BUFFER_SIZE];
		mByteBuffer = ByteBuffer.allocate(BUFFER_SIZE*2);
		mByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		return true;
	}

	/**
	 * <b>功能</b>: start，启动录音 <br/>
	 * 
	 * @param mode
	 *            0-监测, 1-校对<br/>
	 * @param handler
	 *            更新标定界面的handler
	 * @return {@link #DO_RECORD} 开始监测 <br/>
	 *         {@link #DO_CALIBRATION} 开始录音 <br/>
	 *         {@link #NEED_CALIBRATION} 需要标定 <br/>
	 */
	public int start(int mode, Handler handler) {
		mHandler = handler;
		mDataReceiver = null;
		return start(mode);
	}

	/**
	 * <b>功能</b>: start，启动录音 <br/>
	 * 
	 * @param mode
	 *            0-监测, 1-校对<br/>
	 * @param dataReceiver
	 *            监测时处理数据的接口
	 * @return {@link #DO_RECORD} 开始监测 <br/>
	 *         {@link #DO_CALIBRATION} 开始录音 <br/>
	 *         {@link #NEED_CALIBRATION} 需要标定 <br/>
	 */
	public int start(int mode, DataRecordBack dataReceiver) {
		mDataReceiver = dataReceiver;
		mHandler = null;
		return start(mode);
	}

	public int play(int mode, DataRecordBack dataReceiver, String filePath,Handler handler) {
		mHandler = handler;
		mDataReceiver = dataReceiver;
		File replayFile = new File(filePath);
		if (!replayFile.exists() || !filePath.contains(".pcm")) {
			return MISS_FILE;
		} else {

			mFile = replayFile;
			try {
				
				BUFFER_SIZE = AudioTrack.getMinBufferSize(48000,
						AudioFormat.CHANNEL_OUT_STEREO,
						AudioFormat.ENCODING_PCM_16BIT);
				if (BUFFER_SIZE < 0) {
					return UNUSEFUL;
				}else{
					mState = mode;
					mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 48000,
							AudioFormat.CHANNEL_OUT_STEREO,
							AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE,
							AudioTrack.MODE_STREAM);
					mBuffer = new short[BUFFER_SIZE];
					mThread = new MyPlayThread();
					mThread.start();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mState;
	}
	
	class MyPlayThread extends Thread{
		public void run(){
			try {
				mAudioTrack.play();
				mDis = new DataInputStream(new BufferedInputStream(
						new FileInputStream(mFile)));
				while (mState>STOP && mDis.available() > 0) {
					int i = 0;
					int length = mDis.available()/2< mBuffer.length? mDis.available()/2:mBuffer.length;
					while (i <length) {
						mBuffer[i] = mDis.readShort();
						i += 1;
					}
					mAudioTrack.write(mBuffer, 0, mBuffer.length);
					mDataReceiver.dataRecord(mBuffer);
				}
				mHandler.sendEmptyMessage(STOP);
				AudioController.this.stop();
				
			}catch(Exception e){
				
				e.printStackTrace();
			}
		}
		
	}

	private int start(int mode) {
		if (!getMinBufferSize())
			return UNUSEFUL;

		// recordOrCalibration == -1 && mode == 0 未标定并开始监测
		if (!hasCalibration && mode == DO_RECORD) {
			return NEED_CALIBRATION;
		}else{
			
			mState = mode;
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, // 声音来源
					SAMPLE_RATE_IN_HZ, // 频率
					AudioFormat.CHANNEL_IN_STEREO, // 声道
					AudioFormat.ENCODING_PCM_16BIT, // 编码
					BUFFER_SIZE); // 缓存空间
			mAudioRecord.startRecording();
			doAbandon();
			mThread = new MyThread();
			mThread.start();
			return mState;
		}

	}

	public boolean setSampleRate(int sampleRate) {
		SAMPLE_RATE_IN_HZ = sampleRate;
		return true;
	}

	public void stop() {
		mState = mState > STOP ? STOP : mState;

		if (mAudioRecord != null) {
//			mAudioRecord.stop();
			mAudioRecord.release();
			mAudioRecord = null;
		}

		if (mAudioTrack != null) {
//			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}

		if (mDis != null) {
			try {
				mDis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mDis = null;
		}
		
		mThread = null;
	}

	public int getState() {
		return mState;
	}
	
	public boolean hasCalibration(){
		return hasCalibration;
	}

	class MyThread extends Thread {
		@Override
		public void run() {

			while (mState > STOP) {

				switch (mState) {
				// /////////////////////////////////////////// 校准
				case DO_CALIBRATION:
					hasCalibration = true;
					doCalibration();
					break;
				// ///////////////////////////////////////// 监测
				case DO_RECORD:
					doRecord();
					break;
				}
			}
		}

	}

	/** 监测 */

	public void doRecord() {
		int length = mAudioRecord.read(mByteBuffer, BUFFER_SIZE*2);
		ShortBuffer shorts = mByteBuffer.asShortBuffer();
		
//		int length = mAudioRecord.read(mBuffer, 0, BUFFER_SIZE - 1);
		length = length > 0 ? length : 0;

//		short[] rData = new short[length];
		short[] rData = new short[BUFFER_SIZE];
		shorts.get(rData,0,length/2);
//		System.arraycopy(mBuffer, 0, rData, 0, length/2);

		mDataReceiver.dataRecord(rData);

	}

	List<Double> maxList = new ArrayList<Double>();

	/** 5s内反推94dB对应的A0 */
	private void doCalibration() {
		int max = 0;
		InfoUtil.getSaveFilePath();
		if (calibrationStartTime == 0) {
			calibrationStartTime = System.currentTimeMillis();
		}

		short[] buffer = new short[BUFFER_SIZE];
		double r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
		double v = 0;
		for (int i = 0; i < r; i++) {
			v += buffer[i] * buffer[i];
			if (max < Math.abs(buffer[i]))
				max = buffer[i] > 0 ? buffer[i] : 0-buffer[i];
		}
		
		double mean = v / r;

		double t94 = 94d / 10d;
		double b94 = mean / Math.pow(10d, t94);// A0

		// double t114 = 114d / 10d;
		// double b114 = meean / Math.pow(10d, t114);

		proofreadList.add(b94);
		maxList.add((double) max);

		// 将数据传递给主线程
		Message msgstatus = mHandler.obtainMessage();
		msgstatus.obj = "proofread"; // proofread：校对
		Bundle bdstatus = new Bundle();
		bdstatus.putString("volume", String.valueOf(b94));
		msgstatus.setData(bdstatus);
		mHandler.sendMessage(msgstatus);

		// 达到5s即停止校对，返回平均值
		long tooktimes = System.currentTimeMillis() - calibrationStartTime;
		if (5000 < tooktimes) { // 5S限制

			calibrationStartTime = 0;

			double ddd = 0;
			int size = proofreadList.size();
			for (int i = 0; i < size; i++) {

				Double d1 = proofreadList.get(i);

				if (Math.abs(d1) < 10E-307) // 防止某些手机（联想，华为）出现0.0导致Infinity
					continue;

				ddd += d1;
				overValue += 6.3d / d1; // 计算一个忽悠值
			}

			proofreadList.clear();
			proofreadList = new ArrayList<Double>();

			proofreadValueTmp = ddd / size;
			double ov = overValue / size;
			overValue = 0;

			double kv = 0;
			for (int k = 0; k < maxList.size(); k++) {
				kv += maxList.get(k);
			}
			double kmax = kv / maxList.size();
			double p94 = Helper.dB2Pa(94);

			double px = (p94 / 0.707) / kmax * 0x7fff;
			double bx = 20 * Math.log10(px / 0.00002);

//			bx = 117;
			SPLHelper.getInstance().setRange(93);
			FFTHelper.getInstance().setRange(93);

			pressureRatio = (float) (Math.pow(proofreadValueTmp, 0.5) / 0.00002);

			Message result = mHandler.obtainMessage();
			result.obj = "proofread_result"; // 最终使用的校对值
			Bundle bdresult = new Bundle();
			bdresult.putString("volume", String.valueOf(proofreadValueTmp * ov)); // 忽悠人滴，靠近6.3
			result.setData(bdresult);
			mHandler.sendMessage(result);

			stop();
		}
	}

	private void doAbandon() {
		for (int i = 0; i < abandonCount; i++) {
			mAudioRecord.read(mBuffer, 0, BUFFER_SIZE);
		}
	}

}
