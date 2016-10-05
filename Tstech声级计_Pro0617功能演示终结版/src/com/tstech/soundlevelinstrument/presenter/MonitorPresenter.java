package com.tstech.soundlevelinstrument.presenter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.tstech.soundlevelinstrument.algorithm.FFTHelper;
import com.tstech.soundlevelinstrument.algorithm.SPLHelper;
import com.tstech.soundlevelinstrument.back.DataRecordBack;
import com.tstech.soundlevelinstrument.back.FileCallBack;
import com.tstech.soundlevelinstrument.back.IKeyBack;
import com.tstech.soundlevelinstrument.fragment.CalibrationFragment;
import com.tstech.soundlevelinstrument.util.AudioController;
import com.tstech.soundlevelinstrument.util.CtxApp;
import com.tstech.soundlevelinstrument.util.DialogUtil;
import com.tstech.soundlevelinstrument.util.FileWriter;
import com.tstech.soundlevelinstrument.util.InfoUtil;
import com.tstech.soundlevelinstrument.util.SaveConfigXmlUtil;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.view.LoaderMessageDialog;
import com.tstech.soundlevelinstrument.viewinterface.MonitorView;
import common.check.unit.VoiceCheckUtil;
import common.check.unit.VoiceCheckUtil.ErrorConditionException;
import common.check.unit.VoiceCheckUtil.WarnConditionException;
import common.check.unit.VoiceCheckerInterface;

public class MonitorPresenter extends BasePresenter<MonitorView> implements
		DataRecordBack, FileCallBack {
	private VoiceCheckUtil mCheck;
	private AudioController mAudioUtil;
	private FileWriter mWriter;
	private String car = "1";
	private String dw = "1";
	private int mMode = MonitorView.MODE_P;
	private DecimalFormat df;

	private String rePlayPath;
	private String carDwTv;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == AudioController.STOP) {
				mView.resetStartButton(AudioController.DO_RECORD);
			} else if (msg.what == DataRecordBack.SPL_UPDATE) {
				mCheck.addSPLValue((Double) msg.obj);
			} else if (msg.what == DataRecordBack.FFT_UPDATE) {
				mCheck.addFFTValue((float[][]) msg.obj);
			}
		}

	};

	public MonitorPresenter(){
		mAudioUtil = AudioController.getInstance();
		mWriter = new FileWriter();
		FFTHelper.getInstance().setDataRecordBack(this);
		SPLHelper.getInstance().setDataRecordBack(this);
		df = new DecimalFormat("###0.00");
		mCheck = new VoiceCheckUtil(this);
	}

	public void setCarNum(String carNum) {
		car = carNum;
	}

	public void setDwNum(String dwNum) {
		dw = dwNum;
	}

	public void switchMode(int mode) {

		int state = mAudioUtil.getState();
		switch (mode) {
		case MonitorView.MODE_C: // 采集中切换后处理界面
			if (state != AudioController.STOP) {
				ToastUtil.showToast(mView.getContext(), "采集尚未结束");
				mView.setModeSwitch(MonitorView.MODE_P);
				break;
			}
			mMode = mode;
			mView.hideFileSelector(View.GONE);
			break;

		case MonitorView.MODE_P: // 后处理界面切换采集

			if (state != AudioController.STOP) {
				mAudioUtil.stop();
				mView.resetStartButton(AudioController.DO_RECORD);
			}
			mMode = mode;
			mView.hideFileSelector(View.VISIBLE);
			showFileSelectedDialog();
			break;
		}
	}

	private void showFileSelectedDialog() {

	}

	public void onBackPressed(List<Fragment> fragments, Context ctx) {

		boolean hide = true;

		for (int i = 0; i < fragments.size(); i++) {
			if (fragments.get(i).isVisible()) {
				if (fragments.get(i) instanceof IKeyBack) {
					((IKeyBack) fragments.get(i)).doKeyBack();
					hide = false;
				}
			}
		}

		if (hide) {
			DialogUtil.createDialog(ctx, "提示", "确认退出应用？",
					new DialogUtil.DialogCallBack() {
						@Override
						public void confrim() {
							if (mAudioUtil.getState() > 0) // mAudioUtil.getState()
															// > 0 为录音或者标定状态
								mAudioUtil.stop();
							mView.finishActivity();
						}

						@Override
						public void cancle() {
						}
					});
		}
	}

	public void onStart() {
		if (mMode == MonitorView.MODE_C)
			doReplay();
		else
			doRecording();
	}

	public void doReplay() { // TODO
		// String path = getFilePath();
		// Log.e("ard", "doReplaypath:"+ path);
		int state = mAudioUtil.getState();
		if(rePlayPath != null){
			switch (state) {
			case AudioController.STOP:
				resetCalculateResult();
				mAudioUtil
				.play(AudioController.DO_PLAY, this, rePlayPath, mHandler);
				mView.resetStartButton(AudioController.STOP);
				break;
			case AudioController.DO_PLAY:
				mAudioUtil.stop();
				mView.resetStartButton(AudioController.DO_RECORD);
				break;
			}
		}else{
			ToastUtil.showToast(CtxApp.context, "请加载信息！");
		}

	}

	public void doRecording() {
		if (mAudioUtil.hasCalibration()) {

			int state = mAudioUtil.getState();

			switch (state) {

			case AudioController.STOP:
				resetCalculateResult();
				mWriter.prepare(getFilePath());
				state = mAudioUtil.start(AudioController.DO_RECORD, this);
				if (state == AudioController.UNUSEFUL) {
					showDialog(AudioController.UNUSEFUL);
					break;
				}
				mView.resetStartButton(AudioController.STOP);
				break;

			case AudioController.DO_RECORD:
				mAudioUtil.stop();
				mWriter.release();
				mView.resetStartButton(AudioController.DO_RECORD);

				break;

			}
		} else {
			showDialog(AudioController.NEED_CALIBRATION);
		}
	}

	private String getFilePath() {
		String carDw = "/" + car + "车" + dw + "端位";
		//	TODO config
		SaveConfigXmlUtil.saveTemplate(mView.getContext(),carDw);
		return InfoUtil.getSaveDataPath(carDw) + "/" + "data.pcm";
	}

	private void resetCalculateResult() {
		FFTHelper.getInstance().resetCalculateResult();
		SPLHelper.getInstance().resetCalculateResult();
	}

	public void showFileList(Context context) {

		// DialogUtil.createFileListDialog(context);
		LoaderMessageDialog messageDialog = new LoaderMessageDialog(context,
				this);
		messageDialog.setTitle("请选择信息：");
		messageDialog.show();

	}

	private void showDialog(final int state) {
		final FragmentActivity ctx = (FragmentActivity) mView.getContext();
		String title = "提示";
		String content;
		if (state == AudioController.UNUSEFUL) {
			content = "不支持该采样频率，请重新设置采样频率";
		} else {
			content = "尚未标定，是否标定？";
		}
		DialogUtil.createDialog(ctx, title, content,
				new DialogUtil.DialogCallBack() {

					@Override
					public void confrim() {
						if (state == AudioController.NEED_CALIBRATION) {
							FragmentManager fm = ctx
									.getSupportFragmentManager();
							CalibrationFragment fragment = (CalibrationFragment) fm
									.findFragmentByTag("calibration");
							fm.beginTransaction().show(fragment).commit();
						}
					}

					@Override
					public void cancle() {
					}
				});
	}

	@Override
	public void dettach() {
		mAudioUtil.stop();
		super.dettach();
	}

	@Override
	public void dataRecord(short[] data) {
		int length = data.length;
		int[] rData = new int[length];
		for (int i = 0; i < length; i++) {
			if (mAudioUtil.getState() == AudioController.DO_RECORD)
				mWriter.writeToFile(data[i]);
			rData[i] = (int) (data[i] << 8);
		}

		startCaculate(rData);
	}

	private void startCaculate(int[] rData) {
		SPLHelper.getInstance().startCaculate(rData);
		FFTHelper.getInstance().startCaculate(rData);
	}

	public boolean hasStop() {
		int state = mAudioUtil.getState();
		if (state == AudioController.DO_PLAY) {
			mAudioUtil.stop();
			mView.resetStartButton(AudioController.DO_RECORD);
		} else if (state == AudioController.DO_RECORD) {
			ToastUtil.showToast(mView.getContext(), "采集尚未结束");
			return false;
		}
		return true;
	}

	@Override
	public void update(Observable observable, Object data) {

	}

	@Override
	public synchronized void SPLValueUpdate(double value) {
		Message msg = Message.obtain();
		msg.what = DataRecordBack.SPL_UPDATE;
		msg.obj = value;
		mHandler.sendMessage(msg);
	}

	@Override
	public synchronized void FFTValueUpdate(float[] mainFreqs,
			float[] mainFreqValues) {
		float[][] freqAndValue = new float[2][5];
		freqAndValue[0] = mainFreqs;
		freqAndValue[1] = mainFreqValues;

		Message msg = Message.obtain();
		msg.what = DataRecordBack.FFT_UPDATE;
		msg.obj = freqAndValue;
		mHandler.sendMessage(msg);
	}

	public void updateValueDisplay(double spl, float[] freqs, float[] values,
			int state) { // TODO
		updateSPLDisplay(spl); // 显示的分贝数
		updateFFTDisplay(freqs, values); // 表格
		updateVoiceState(state);
	}

	private void updateVoiceState(int state) {
		// Log.e("ard", "isError：" + isError); // TODO 报警灯
		mView.setLightChange(state);

	}

	private void updateFFTDisplay(float[] freqs, float[] values) {
		List<String> FreqsAndValues = new ArrayList<String>();
		for (int i = 0; i < freqs.length; i++) {
			FreqsAndValues.add(df.format(freqs[i]));
			FreqsAndValues.add(df.format(values[i]));
		}
		if (FreqsAndValues.size() == 10)
			mView.updateFFTValues(FreqsAndValues);
	}

	private void updateSPLDisplay(double value) {

		String text = df.format(value);
		// Log.e("ard", "text：" + text);
		mView.updateSPLValue(text);
	}

	@Override
	public void setFilePath(String path) {
		rePlayPath = path + "/data.pcm";
//		ToastUtil.showToast(mView.getContext(), path); // TODO
		// 截取“-车-端位”
		int start = path.lastIndexOf("/");
		int end = path.lastIndexOf("");
		if (start != -1 && end != -1) {
			carDwTv = path.substring(start + 1, end);
			if (carDwTv.contains("车"))
				mView.setTvShow(carDwTv);
		} else {

		}
	}
	
	public VoiceCheckUtil getCheckUtil(){
		return mCheck;
	}
}
