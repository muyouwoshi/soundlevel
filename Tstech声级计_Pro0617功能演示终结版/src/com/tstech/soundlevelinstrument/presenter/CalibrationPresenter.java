package com.tstech.soundlevelinstrument.presenter;

import java.util.Observable;
import java.util.Observer;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;

import com.tstech.soundlevelinstrument.util.AudioController;
import com.tstech.soundlevelinstrument.util.DialogUtil;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.viewinterface.CalibrationView;

public class CalibrationPresenter extends BasePresenter<CalibrationView> implements Observer{
	/** 消息处理器 */
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			String s = msg.obj.toString();

			// 校准界面的5s内反对数计算

			if ("proofread".equals(s)) {
				String volume = msg.getData().getString("volume");// 用string传递是为了防止NaN
				double ddb = 0d;

				try {
					ddb = Double.valueOf(volume);
				} catch (Exception e) {
					ToastUtil.showToast(mView.getContext(), "校对错误:" + e);
				}
				
				mView.addToTxtStatus(ddb+"\n");

				post(new Runnable() {
					@Override
					public void run() {
						mView.scrollToBottom();
					}
				});

			}

			// 校准界面反对数计算结果
			else if ("proofread_result".equals(s)){

				String valueCalibration = msg.getData().getString("volume");
				
				mView.endCalWithValue(valueCalibration);
			}
		};
	};
	
	
	private AudioController mAudioUtil;
	public CalibrationPresenter(){
		mAudioUtil = AudioController.getInstance();
//		mAudioUtil.setHandler(handler);
	}

	public void doCalibration() {
		
		int state = mAudioUtil.getState();

		switch (state) {
		
		case AudioController.STOP:
			showDialog();
			
			break;
		case AudioController.DO_CALIBRATION:		//开始录音
			ToastUtil.showToast(mView.getContext(), "标定尚未结束");
			
			break;
		}
				
	}

	private void showDialog() {
		final FragmentActivity ctx = (FragmentActivity) mView.getContext();

		CharSequence content = Html
				.fromHtml("请确认已经插接好<b><font color=red>标定器</font></b>和<b><font color=red>MIC</font></b>。");

		DialogUtil.createDialog(ctx, "提示", content,
				new DialogUtil.DialogCallBack() {
					@Override
					public void confrim() {		//确定
							mView.prooFreading();
							mAudioUtil.start(AudioController.DO_CALIBRATION,mHandler);
						
					}

					@Override
					public void cancle() {		//取消
					}
				});
	}

	public void saveProofreadValue() {

	}

	public void changeProofreadValue() {

	}

	public boolean stop() {
		int state = mAudioUtil.getState();

		if(state == AudioController.DO_CALIBRATION){
			return false;
		}
		mAudioUtil.stop();
		return true;
	}

	@Override
	public void dettach() {
		mAudioUtil.stop();
		super.dettach();
	}

	@Override
	public void update(Observable observable, Object data) {
		
	}
	
	

}
