package com.tstech.soundlevelinstrument.presenter;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.tstech.soundlevelinstrument.back.PopuWindowBack;
import com.tstech.soundlevelinstrument.bean.ExpWarningSetting;
import com.tstech.soundlevelinstrument.util.InfoUtil;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.view.CalculatorPopupWindow;
import com.tstech.soundlevelinstrument.viewinterface.WarningSettingView;
import common.check.unit.FFTChecker;
import common.check.unit.OtherCheaker;
import common.check.unit.SPLChecker;
import common.check.unit.VoiceCheckUtil;
import common.check.unit.VoiceCheckerInterface;

public class WarningSettingPresenter extends BasePresenter<WarningSettingView> implements PopuWindowBack {

	private ExpWarningSetting warningSetting;
	private VoiceCheckUtil mCheckUtil;

	public WarningSettingPresenter() {
		warningSetting = new ExpWarningSetting();
		readSaveWarning();
	}

	
	
	@Override
	public void attach(WarningSettingView mView) {
		super.attach(mView);
		mCheckUtil = mView.getCheckUtil();
	}



	public void restorWarning() {
		readSaveWarning();
		setWarningToUI(warningSetting);
	}

	private void setWarningToUI(ExpWarningSetting warning) {
		mView.initWarning(warning);
	}

	public void saveWaringSetting() {
		saveWarning(warningSetting);
	}

	public void resetWaring() {
		setSafearea("" + ExpWarningSetting.safe);
		setWarningarea("" + ExpWarningSetting.warn);
		setDangerarea("" + ExpWarningSetting.danger);

		setFrequencysafe("" + ExpWarningSetting.safe);
		setFrequencywarning("" + ExpWarningSetting.warn);
		setFrequencydanger("" + ExpWarningSetting.danger);

		setDangersafe("" + ExpWarningSetting.safe);
		setDangerwarning("" + ExpWarningSetting.warn);
		setDangerdanger("" + ExpWarningSetting.danger);

		// setSafeCondition("A|(B&C)");
		setWarningCondition("A|(B&C)");
		setDangerCondition("A|(B&C)");

		setWarningToUI(warningSetting);
	}

	private void saveWarning(ExpWarningSetting warning) {
		InfoUtil.getSharedPreferences().edit().putString(InfoUtil.SP_Safearea, "" + warning.getSafearea())
				.putString(InfoUtil.SP_Warningarea, "" + warning.getWarningarea())
				.putString(InfoUtil.SP_Dangerarea, "" + warning.getDangerarea())

				.putString(InfoUtil.SP_Frequencysafe, "" + warning.getFrequencysafe())
				.putString(InfoUtil.SP_Frequencywarning, "" + warning.getFrequencywarning())
				.putString(InfoUtil.SP_Frequencydanger, "" + warning.getFrequencydanger())

				.putString(InfoUtil.SP_Dangersafe, "" + warning.getDangersafe())
				.putString(InfoUtil.SP_Dangerwarning, "" + warning.getDangerwarning())
				.putString(InfoUtil.SP_Dangerdanger, "" + warning.getDangerdanger())

				// .putString(InfoUtil.SP_SafeCondition,
				// "" + warning.getSafeCondition())
				.putString(InfoUtil.SP_WarningCondition, "" + warning.getWarningCondition())
				.putString(InfoUtil.SP_DangerCondition, "" + warning.getDangerCondition()).commit();

		setSplWarning(warning.getSafearea());
		setSplError(warning.getDangerarea());

		setFftWarning(warning.getFrequencysafe());
		setFftError(warning.getFrequencydanger());

		setOtherWarning(warning.getDangersafe());
		setOtherError(warning.getDangerdanger());

		mCheckUtil.setCondition(warning.getWarningCondition(), VoiceCheckerInterface.WARINING);
		mCheckUtil.setCondition(warning.getDangerCondition(), VoiceCheckerInterface.ERROR);

	}

	private void setOtherError(String dangerdanger) {
		float oe = Float.parseFloat(dangerdanger);
		OtherCheaker.setWaringValue(oe);
	}

	private void setOtherWarning(String dangersafe) {
		float ow = Float.parseFloat(dangersafe);
		OtherCheaker.setWaringValue(ow);
	}

	private void setFftError(String frequencydanger) {
		float fe = Float.parseFloat(frequencydanger);
		FFTChecker.setErrorValue(fe);
	}

	private void setFftWarning(String frequencysafe) {
		float fs = Float.parseFloat(frequencysafe);
		FFTChecker.setWaringValue(fs);
	}

	private void setSplError(String dangerarea) {
		double dd = Double.parseDouble(dangerarea);
		SPLChecker.setErrorValue(dd);
	}

	private void setSplWarning(String safearea) {
		double ss = Double.parseDouble(safearea);
		SPLChecker.setWaringValue(ss);
	}

	private void readSaveWarning() {
		SharedPreferences sp = InfoUtil.getSharedPreferences();
		warningSetting.setSafearea(sp.getString(InfoUtil.SP_Safearea, "" + ExpWarningSetting.safe));
		warningSetting.setWarningarea(sp.getString(InfoUtil.SP_Warningarea, "" + ExpWarningSetting.warn));
		warningSetting.setDangerarea(sp.getString(InfoUtil.SP_Dangerarea, "" + ExpWarningSetting.danger));

		warningSetting.setFrequencysafe(sp.getString(InfoUtil.SP_Frequencysafe, "" + ExpWarningSetting.safe));
		warningSetting.setFrequencywarning(sp.getString(InfoUtil.SP_Frequencywarning, "" + ExpWarningSetting.warn));
		warningSetting.setFrequencydanger(sp.getString(InfoUtil.SP_Frequencydanger, "" + ExpWarningSetting.danger));

		warningSetting.setDangersafe(sp.getString(InfoUtil.SP_Dangersafe, "" + ExpWarningSetting.safe));
		warningSetting.setDangerwarning(sp.getString(InfoUtil.SP_Dangerwarning, "" + ExpWarningSetting.warn));
		warningSetting.setDangerdanger(sp.getString(InfoUtil.SP_Dangerdanger, "" + ExpWarningSetting.danger));

		// warningSetting.setSafeCondition(sp.getString(InfoUtil.SP_SafeCondition,
		// "A|(B&C)"));
		warningSetting.setWarningCondition(sp.getString(InfoUtil.SP_WarningCondition, "A|(B&C)"));
		warningSetting.setDangerCondition(sp.getString(InfoUtil.SP_DangerCondition, "A|(B&C)"));
	}

	// public void showSafePopu(FragmentActivity activity, View parent) {
	// CalculatorPopupWindow safePopuWindow = new CalculatorPopupWindow(
	// activity, this, 0);
	// safePopuWindow.showPopupWindow(parent);
	// }

	public void showWarningPopu(FragmentActivity activity, View parent) {
		CalculatorPopupWindow warningPopuWindow = new CalculatorPopupWindow(activity, this,
				VoiceCheckerInterface.WARINING);
		warningPopuWindow.showPopupWindow(parent);
	}

	public void showAlarmPopu(FragmentActivity activity, View parent) {
		CalculatorPopupWindow dangerPopuWindow = new CalculatorPopupWindow(activity, this, VoiceCheckerInterface.ERROR);
		dangerPopuWindow.showPopupWindow(parent);
	}

	public void setSafearea(String safearea) {
		try {
			warningSetting.setSafearea(safearea);
		} catch (Exception e) {
			warningSetting.setSafearea("");
		}
	}

	public void setWarningarea(String warningarea) {
		try {
			warningSetting.setWarningarea(warningarea);
		} catch (Exception e) {
			warningSetting.setWarningarea("");
		}
	}

	public void setDangerarea(String dangerarea) {
		try {
			warningSetting.setDangerarea(dangerarea);
		} catch (Exception e) {
			warningSetting.setDangerarea("");
		}
	}

	public void setFrequencysafe(String frequencysafe) {
		try {
			warningSetting.setFrequencysafe(frequencysafe);
		} catch (Exception e) {
			warningSetting.setFrequencysafe("");
		}
	}

	public void setFrequencywarning(String frequencywarning) {
		try {
			warningSetting.setFrequencywarning(frequencywarning);
		} catch (Exception e) {
			warningSetting.setFrequencywarning("");
		}
	}

	public void setFrequencydanger(String frequencydanger) {
		try {
			warningSetting.setFrequencydanger(frequencydanger);
		} catch (Exception e) {
			warningSetting.setFrequencydanger("");
		}
	}

	public void setDangersafe(String dangersafe) {
		try {
			warningSetting.setDangersafe(dangersafe);
		} catch (Exception e) {
			warningSetting.setDangersafe("");
		}
	}

	public void setDangerwarning(String dangerwarning) {
		try {
			warningSetting.setDangerwarning(dangerwarning);
		} catch (Exception e) {
			warningSetting.setDangerwarning("");
		}
	}

	public void setDangerdanger(String dangerdanger) {
		try {
			warningSetting.setDangerdanger(dangerdanger);
		} catch (Exception e) {
			warningSetting.setDangerdanger("");
		}
	}

	public void setWarningCondition(String warningCondition) {
		try {
			mView.setTvWarning(warningCondition);
			warningSetting.setWarningCondition(warningCondition);
		} catch (Exception e) {
			warningSetting.setWarningCondition("");
		}
	}

	public void setDangerCondition(String dangerCondition) {
		try {
			mView.setTvDanger(dangerCondition);
			warningSetting.setDangerCondition(dangerCondition);
		} catch (Exception e) {
			warningSetting.setDangerCondition("");
		}
	}

	@Override
	public boolean setPopuWindow(String result, int state) {
		try {
			mCheckUtil.getChecker(mCheckUtil.getFormula(result));
		} catch (Exception e) {
			e.printStackTrace();
			ToastUtil.showToast(mView.getActivityContext(), "输入有误");
			return false;
		}
		
		switch (state) {
		case VoiceCheckerInterface.ERROR:
			setDangerCondition(result);
			break;
		case VoiceCheckerInterface.WARINING:
			setWarningCondition(result);
			break;
		}
		
		return true;
	}

}
