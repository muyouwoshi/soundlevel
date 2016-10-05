package com.tstech.soundlevelinstrument.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * <b>类名</b>: CalibrateUtil，标定工具<br/>
 */
public class AudioUtil {
	private List<Float> FFTBuffer;

	/** 默认采样频率 */
	private int SAMPLE_RATE_IN_HZ = 48000;
	/** 获取最小缓存空间 */
	private int BUFFER_SIZE = AudioRecord.getMinBufferSize(
			SAMPLE_RATE_IN_HZ, // 目标声音采样频率
			AudioFormat.CHANNEL_IN_STEREO, // 声道
			AudioFormat.ENCODING_PCM_16BIT); // 编码格式
	/** 录音机 */
	private AudioRecord mAudioRecord;
	/** 上下文 */
	private Context ctx;
	/** 消息处理器 */
	private Handler mHandler;
	/** SP工具 */
	private SharedPreferences sp;

	private MyThread mThread;

	private boolean isRunning = true;

	private boolean flag = false;

	
	
	public final static int DO_RECORD = 0;
	public final static int DO_CALIBRATION = 1;
	public final static int NEED_CALIBRATION = -1;
	
	/** 0-record, 1-calibrate **/
	private int recordOrCalibration = NEED_CALIBRATION;
	
	/** 校对开始时间 */
	private long calibrationStartTime = 0;
	
	private List<Double> proofreadList = new ArrayList<Double>();

	/** 5s校对的反对数 */
	private double proofreadValueTmp;

	/** 监测使用的反对数 */
	private double proofreadValue = 1;
	private double overValue = 0;
	
	
	private static class AudioUtilHolder {
		private static final AudioUtil INSTANCE = new AudioUtil();
	}

	private AudioUtil() {
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, // 声音来源
				SAMPLE_RATE_IN_HZ, // 频率
				AudioFormat.CHANNEL_IN_STEREO,  // 声道
				AudioFormat.ENCODING_PCM_16BIT, // 编码
				BUFFER_SIZE); // 缓存空间
	}

	public static final AudioUtil getInstance(Handler handler) {
		AudioUtilHolder.INSTANCE.setHandler(handler);
		return AudioUtilHolder.INSTANCE;
	}
	

	private void setHandler(Handler handler) {
		// TODO Auto-generated method stub
		mHandler = handler;
	}

	public boolean setSampleRate(int sampleRate){
		SAMPLE_RATE_IN_HZ = sampleRate;
		return true;
	}
	/**
	 * <b>功能</b>：CalibrateUtil，声音检测工具 <br/>
	 */
	public AudioUtil(Context ctx, Handler handler) {
		this.ctx = ctx;
		sp = ctx.getSharedPreferences("soundlevelinstrument", Context.MODE_PRIVATE);
		mHandler = handler;

		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, // 声音来源
				SAMPLE_RATE_IN_HZ, // 频率
				AudioFormat.CHANNEL_IN_STEREO,  // 声道
				AudioFormat.ENCODING_PCM_16BIT, // 编码
				BUFFER_SIZE); // 缓存空间

		mThread = new MyThread();
		
	}
	
	/**
	 * <b>功能</b>: start，启动录音 <br/>
	 * @param recordOrCalibration 0-监测, 1-校对
	 * 
	 */
	public void start(int recordOrCalibration){
		this.recordOrCalibration = recordOrCalibration;
		mThread = new MyThread();
		mThread.start();
	}

	/**
	 * <b>功能</b>: setFlag，设置录音状态 <br/>
	 * @param recordOrCalibration : 0-监测界面, 1-校对界面
	 * @param flag : true-录音，false-停止录音
	 */
	public void setFlag(boolean flag, int recordOrCalibration) {
		this.recordOrCalibration = recordOrCalibration;

		if (flag) {
			FFTBuffer = new ArrayList<Float>();

			mAudioRecord.startRecording();
		}

		this.flag = flag;
	}

	class MyThread extends Thread {
		@Override
		public void run() {
			while (isRunning) {
				if (flag) {
					switch (recordOrCalibration) {
//					/////////////////////////////////////////// 校准  ////////////////////////////
					case 1: {
						doCalibration();
						break;
					}
					
					/////////////////////////////////////////// 监测 //////////////////////////////
					case 0: {
						doRecord();
						break;
					}
					}
//						
				}
			}
		}
	}
	
	/** 监测 */
	public void doRecord(){
		short[] buffer = new short[BUFFER_SIZE];
		double r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
		double v = 0;
		for (int i = 0; i < buffer.length; i++) {
			v += buffer[i] * buffer[i];
		}
		double mean = v / r;
		double volume = 10d * Math.log10(mean / proofreadValue); // 以 10 为底的对数 94db = 0.0097，114db = 0.0031
		
//		将数据传递给主线程
		Message msgstatus = mHandler.obtainMessage();
		msgstatus.obj = "status";
		Bundle bdstatus = new Bundle();
		bdstatus.putString("volume", String.valueOf(volume));
		msgstatus.setData(bdstatus);
		mHandler.sendMessage(msgstatus);
	}

	/** 5s内反推94dB对应的A0 */
	private void doCalibration() {
		
		if(calibrationStartTime == 0){
			calibrationStartTime = System.currentTimeMillis();
		}
		
		short[] buffer = new short[BUFFER_SIZE];
		double r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
		double v = 0;
		for (int i = 0; i < buffer.length; i++) {
			v += buffer[i] * buffer[i];
		}
		double mean = v / r;
		
		double t94 = 94d / 10d;
		double b94 = mean / Math.pow(10d, t94);//A0
		
//		double t114 = 114d / 10d;
//		double b114 = mean / Math.pow(10d, t114);
		
		proofreadList.add(b94);
		
//		将数据传递给主线程
		Message msgstatus = mHandler.obtainMessage();
		msgstatus.obj = "proofread"; // proofread：校对
		Bundle bdstatus = new Bundle();
		bdstatus.putString("volume", String.valueOf(b94));
		msgstatus.setData(bdstatus);
		mHandler.sendMessage(msgstatus);
		
		// 达到5s即停止校对，返回平均值
		long tooktimes = System.currentTimeMillis() - calibrationStartTime;
		if(5000 < tooktimes){ // 5S限制
			flag = false;
			calibrationStartTime = 0;
			
			double ddd = 0;
			int size = proofreadList.size();
			Log.e("bug11", ""+size);
			for (int i = 0; i < size; i++) {
				
				Double d1 = proofreadList.get(i);
				
				if(Math.abs(d1) < 10E-307 ) // 防止某些手机（联想，华为）出现0.0导致Infinity
					continue;
				
				ddd += d1;
				overValue += 6.3d/d1; // 计算一个忽悠值
			}
			
			proofreadList.clear();
			proofreadList = new ArrayList<Double>();
			
			proofreadValueTmp = ddd / size;
			double ov = overValue / size;
			overValue = 0;
			
			Editor edit = sp.edit();
			edit.putString("proofreadValue", String.valueOf(proofreadValueTmp));
			edit.putString("ov", String.valueOf(ov));
			edit.commit();
			
			Message result = mHandler.obtainMessage();
			result.obj = "proofread_result"; // 最终使用的校对值
			Bundle bdresult = new Bundle();
			bdresult.putString("volume", String.valueOf(proofreadValueTmp * ov)); // 忽悠人滴，靠近6.3
			result.setData(bdresult);
			mHandler.sendMessage(result);
		}
	}
	
	/** 保存5s校对的反对数
	 * @param flag 0=新校对值，1=从sp里取出的上次校对的值 */
	public void saveProofreadValue(int flag) {
		switch (flag) {
		case 0:
			proofreadValue = proofreadValueTmp;
			
			break;
		case 1:
			String pv = sp.getString("proofreadValue", "");
			String ov = sp.getString("ov", "1");
			
			if("".equals(pv)){
				proofreadValue = 1;
				Toast.makeText(ctx, "没有读取到以往使用的校对值", 0).show(); // TODO 使用1
			}
			else{
				proofreadValue = Double.valueOf(pv);
				Double oValue = Double.valueOf(ov);
				
				Message result = mHandler.obtainMessage();
				result.obj = "proofread_result"; // 最终使用的校对值
				Bundle bdresult = new Bundle();
				bdresult.putString("volume", String.valueOf(proofreadValue * oValue));
				result.setData(bdresult);
				mHandler.sendMessage(result);
				
				Toast.makeText(ctx, "上次校对值：" + proofreadValue * oValue, 0).show();
			}
			
			break;
		}
	}
	
	/** 重置监测使用的反对数 */
	public void clearProofreadValue() {
		proofreadValue = 1;
	}

	/**
	 * <b>功能</b>: setIsCalibrating，停止标定while循环<br/>
	 */
	public void stop() {
		this.flag = false;
		this.isRunning = false;
		
		mAudioRecord.stop();
		mAudioRecord.release();
		mAudioRecord = null;
		
		mThread = null;
	}
}

//  ┏┓　　　┏┓ 
//┏┛ ┻━━━┛┻┓ 
//┃　 　　　　　　┃ 　 
//┃　 　　━　　　┃ 
//┃　 ┳┛　┗┳　┃ 
//┃　 　　　　　　┃ 
//┃　 　　┻　　　┃ 
//┃　 　　　　　　┃ 
//┗━ ┓　　　┏━┛ 
//    ┃　　　┃    神兽保佑　　　　　　　　 
//    ┃　　　┃  代码无BUG！ 
//    ┃　　　┗━━━ ┓ 
//    ┃　　　　　　　 ┣┓ 
//    ┃　　　　　　　 ┏┛ 
//    ┗ ┓┓┏━┳┓┏┛ 
//      ┃┫┫　┃┫┫ 
//      ┗┻┛　┗┻┛
