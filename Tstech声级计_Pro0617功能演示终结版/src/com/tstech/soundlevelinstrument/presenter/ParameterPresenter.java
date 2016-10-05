package com.tstech.soundlevelinstrument.presenter;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.widget.ArrayAdapter;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.algorithm.FFTHelper;
import com.tstech.soundlevelinstrument.bean.ExpParameter;
import com.tstech.soundlevelinstrument.util.InfoUtil;
import com.tstech.soundlevelinstrument.viewinterface.ParameterView;

public class ParameterPresenter extends BasePresenter<ParameterView> {
	private ExpParameter mParam;

	public ParameterPresenter() {
		mParam = new ExpParameter();
		readSaveState();
		setFFTHelper(mParam);
	}

	/**
	 * @param acquiFreqStr
	 *            频率分辨率
	 */
	public void setAcquiFreq(String acquiFreqStr) {
		mParam.setAcquiFreq(Integer.parseInt(acquiFreqStr));
		setFreqRangeData(acquiFreqStr);
	}

	// 计算频率范围
	private void setFreqRangeData(String acquiFreqStr) {
		float acquiFreq = Float.parseFloat(acquiFreqStr);

		ArrayList<String> freqRang_spinner_list = new ArrayList<String>(); // 存放频率范围

		for (int i = 6; i > 0; i--) {
			float freqRang = (float) (acquiFreq / Math.pow(2, i));

			if (freqRang >= 400) {
				freqRang_spinner_list.add(freqRang % 1 == 0 ? ""
						+ ((int) freqRang) : "" + freqRang);// 计算频率范围
			}
		}

		// ParaSpAdapter freqRang_Adapter = new ParaSpAdapter(
		// mView.getActivityContext(), freqRang_spinner_list, "scope");

		ArrayAdapter<String> freqRang_Adapter = new ArrayAdapter<String>(
				mView.getActivityContext(),
				R.layout.spinner_item, freqRang_spinner_list);
		freqRang_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mView.reSetFreqRange(freqRang_Adapter); // 计算频率范围
	}

	// 设置频率范围
	public void setScope(String freqScopeStr) {
		mParam.setFreqRange(Float.parseFloat(freqScopeStr));
		float acquiScope = Float.parseFloat(freqScopeStr);

		List<String> freqRes_spinner_list = new ArrayList<String>();

		if (acquiScope == 0)
			return;

		freqRes_spinner_list.clear();

		int size = 0;
		if (acquiScope == 51200 || acquiScope == 48000 || acquiScope == 25600
				|| acquiScope == 24000) {
			size = 10;
		} else if (acquiScope == 12800 || acquiScope == 12000) {
			size = 8;
		} else if (acquiScope == 6400 || acquiScope == 6000) {
			size = 7;
		} else if (acquiScope == 3200 || acquiScope == 3000) {
			size = 6;
		} else if (acquiScope == 1600 || acquiScope == 1500) {
			size = 5;
		} else if (acquiScope == 800 || acquiScope == 750) {
			size = 4;
		} else {
			size = 3;
		}
		for (int i = 30; i > 0; i--) {
			float ff = (float) (acquiScope / Math.pow(2, i));
			if (ff > 0.7 && freqRes_spinner_list.size() < size) {
				freqRes_spinner_list.add(ff % 1 == 0 ? "" + ((int) ff) : ""
						+ ff);
			}
		}

		// ParaSpAdapter adapterRes = new ParaSpAdapter(
		// mView.getActivityContext(), freqRes_spinner_list, "Scope");

		ArrayAdapter<String> adapterRes = new ArrayAdapter<String>(
				mView.getActivityContext(),
				R.layout.spinner_item, // TODO
				freqRes_spinner_list);
		adapterRes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mView.reSetAcquiRes(adapterRes); // 计算频率分辨率
	}

	public void setFreqRes(String freqResStr) { // 采样频率，频率分辨率
		mParam.setFreqRes(Float.parseFloat(freqResStr));

	}

	public void setFunction(int position) { // 加窗
		mParam.setWindowType(position);
	}

	public void setWeight(int position) { // 计权
		mParam.setWeighting(position);
	}

	public void setSelect(int position) { // 档位选择
		mParam.setSelect(position);
	}

	public void setAverage(String averageStr) { // 平均次数
		try {
			mParam.setAverageCount(Integer.parseInt(averageStr));
		} catch (Exception e) {
			mParam.setAverageCount(3);
		}
	}

	public void saveParameter() {
		setFFTHelper(mParam);
		saveToInfo(mParam);
	}

	private void saveToInfo(ExpParameter param) {
		InfoUtil.getSharedPreferences().edit()
				.putString(InfoUtil.ACQUI, "" + param.getAcquiFreq())
				.putString(InfoUtil.FREQRANGE, "" + param.getFreqRange())
				.putString(InfoUtil.FREQRES, "" + param.getFreqRes())
				.putString(InfoUtil.OVERLAP, "" + param.getOverlap())
				.putString(InfoUtil.AVERAGE, "" + param.getAverageCount())
				.putString(InfoUtil.WINDOW, "" + param.getWindowType())
				.putString(InfoUtil.WEIGHT, "" + param.getWeighting())
				.putString(InfoUtil.SELECT, "" + param.getSelect()).commit();
	}

	private void setFFTHelper(ExpParameter param) {
		FFTHelper.getInstance().setAcquiFreq(param.getAcquiFreq())
				.setFreqRange(param.getFreqRange())
				.setFreqRes(param.getFreqRes()).setOverlap(param.getOverlap())
				.setWindowType(param.getWindowType())
				.setWeighting(param.getWeighting())
				.setAveraging(param.getAverageCount()).commit();
	}

	public void restoreParameter() {
		SharedPreferences sp = InfoUtil.getSharedPreferences();

		setAcquiFreq(sp.getString(InfoUtil.ACQUI, ""
				+ ExpParameter.DEFAULT_ACQUI));
		setScope(sp.getString(InfoUtil.FREQRANGE, ""
				+ ExpParameter.DEFAULT_FREQRANGE));

		readSaveState();
		setParameterToUI(mParam);
		setFFTHelper(mParam);
	}

	private void readSaveState() {
		SharedPreferences sp = InfoUtil.getSharedPreferences();

		mParam.setAcquiFreq(Integer.parseInt(sp.getString(InfoUtil.ACQUI, ""
				+ ExpParameter.DEFAULT_ACQUI)));
		mParam.setFreqRange(Float.parseFloat(sp.getString(InfoUtil.FREQRANGE,
				"" + ExpParameter.DEFAULT_FREQRANGE)));
		mParam.setFreqRes(Float.parseFloat(sp.getString(InfoUtil.FREQRES, ""
				+ ExpParameter.DEFAULT_RES)));
		mParam.setAverageCount(Integer.parseInt(sp.getString(InfoUtil.AVERAGE,
				"" + ExpParameter.DEFAULT_AVERAGE)));
		mParam.setOverlap(Float.parseFloat(sp.getString(InfoUtil.OVERLAP, ""
				+ ExpParameter.DEFAULT_OVER)));
		mParam.setWeighting(Integer.parseInt(sp.getString(InfoUtil.WEIGHT, ""
				+ ExpParameter.DEFAULT_WEIGHT)));
		mParam.setWindowType(Integer.parseInt(sp.getString(InfoUtil.WINDOW, ""
				+ ExpParameter.DEFAULT_WINDOW)));
		mParam.setSelect(Integer.parseInt(sp.getString(InfoUtil.SELECT, ""
				+ ExpParameter.DEFAULT_SELECT)));

	}

	private void setParameterToUI(ExpParameter param) {
		mView.init(param);
	}

	public void resetParameter() {
		setAcquiFreq("" + ExpParameter.DEFAULT_ACQUI);
		setScope("" + ExpParameter.DEFAULT_FREQRANGE);
		setFreqRes("" + ExpParameter.DEFAULT_RES);
		setAverage("" + ExpParameter.DEFAULT_AVERAGE);
		mParam.setOverlap(Float.parseFloat("" + ExpParameter.DEFAULT_OVER));
		setWeight(Integer.parseInt("" + ExpParameter.DEFAULT_WEIGHT));
		setFunction(Integer.parseInt("" + ExpParameter.DEFAULT_WINDOW));
		setSelect(Integer.parseInt("" + ExpParameter.DEFAULT_SELECT));

		setParameterToUI(mParam);
	}

}
