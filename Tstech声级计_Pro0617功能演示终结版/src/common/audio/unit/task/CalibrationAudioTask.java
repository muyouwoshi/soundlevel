package common.audio.unit.task;

import java.util.ArrayList;
import java.util.List;

import com.tstech.soundlevelinstrument.algorithm.FFTHelper;
import com.tstech.soundlevelinstrument.algorithm.Helper;
import com.tstech.soundlevelinstrument.algorithm.SPLHelper;
import com.tstech.soundlevelinstrument.util.InfoUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import common.audio.unit.audio.RecordAudioUtil;
import common.audio.unit.factory.AudioUtilFactory;
import common.audio.unit.factory.RecordAudioFactory;

public class CalibrationAudioTask extends
		BaseAudioTask<RecordAudioUtil, RecordAudioFactory> {
	private int abandonCount = 2;
	private short[] mBuffer;
	private Handler mHandler;

	public CalibrationAudioTask(RecordAudioFactory utilFactory, Handler handler) {
		super(utilFactory);
		mHandler = handler;
	}

	@Override
	public void run() {
		mAudioUtil.start();
		doAbandon();
		while (isRunning) {
			doCalibration();
		}
	}

	long calibrationStartTime;
	private void doCalibration(){
		List<Double> maxList = new ArrayList<Double>();

		/** 5s内反推94dB对应的A0 */

		int max = 0;
		InfoUtil.getSaveFilePath();
		if (calibrationStartTime == 0) {
			calibrationStartTime = System.currentTimeMillis();
		}

		double r = mAudioUtil.work(mBuffer, 0, BUFFER_SIZE);
		double v = 0;
		
		for (int i = 0; i < r; i++) {
			v += mBuffer[i] * mBuffer[i];
			if (max < Math.abs(mBuffer[i]))
				max = mBuffer[i] > 0 ? mBuffer[i] : mBuffer[i];
		}
		
		double mean = v / r;

		double t94 = 94d / 10d;
		double b94 = mean / Math.pow(10d, t94);// A0


//		proofreadList.add(b94);
		maxList.add((double) max);

		// 将数据传递给主线程
		Message msgstatus = mHandler.obtainMessage();
		msgstatus.obj = "proofread"; // proofread：校对
		Bundle bdstatus = new Bundle();
		bdstatus.putString("volume", String.valueOf(b94));
		msgstatus.setData(bdstatus);
		mHandler.sendMessage(msgstatus);

//		// 达到5s即停止校对，返回平均值
//		long tooktimes = System.currentTimeMillis() - calibrationStartTime;
//		if (5000 < tooktimes) { // 5S限制
//
//			calibrationStartTime = 0;
//
//			double ddd = 0;
//			int size = proofreadList.size();
//			for (int i = 0; i < size; i++) {
//
//				Double d1 = proofreadList.get(i);
//
//				if (Math.abs(d1) < 10E-307) // 防止某些手机（联想，华为）出现0.0导致Infinity
//					continue;
//
//				ddd += d1;
//				overValue += 6.3d / d1; // 计算一个忽悠值
//			}
//
//			proofreadList.clear();
//			proofreadList = new ArrayList<Double>();
//
//			proofreadValueTmp = ddd / size;
//			double ov = overValue / size;
//			overValue = 0;
//
//			double kv = 0;
//			for (int k = 0; k < maxList.size(); k++) {
//				kv += maxList.get(k);
//			}
//			double kmax = kv / maxList.size();
//			double p94 = Helper.dB2Pa(94);
//
//			double px = (p94 / 0.707) / kmax * 0x7fff;
//			double bx = 20 * Math.log10(px / 0.00002);
//
//			SPLHelper.getInstance().setRange(bx);
//			FFTHelper.getInstance().setRange(bx);
//
//			pressureRatio = (float) (Math.pow(proofreadValueTmp, 0.5) / 0.00002);
//
//			Message result = mHandler.obtainMessage();
//			result.obj = "proofread_result"; // 最终使用的校对值
//			Bundle bdresult = new Bundle();
//			bdresult.putString("volume",
//					String.valueOf(proofreadValueTmp * ov)); // 忽悠人滴，靠近6.3
//			result.setData(bdresult);
//			mHandler.sendMessage(result);
//
//			stop();
	}
	
	private void doAbandon() {
		for (int i = 0; i < abandonCount; i++) {
			mAudioUtil.work(mBuffer, 0, BUFFER_SIZE);
		}
	}

	@Override
	public int getState() {
		return AudioUtilFactory.CALIBRATION;
	}

}
