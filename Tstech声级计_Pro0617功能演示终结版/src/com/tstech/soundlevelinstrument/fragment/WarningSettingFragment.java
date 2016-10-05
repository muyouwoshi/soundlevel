package com.tstech.soundlevelinstrument.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.activity.MonitorActivity;
import com.tstech.soundlevelinstrument.back.IKeyBack;
import com.tstech.soundlevelinstrument.bean.ExpWarningSetting;
import com.tstech.soundlevelinstrument.presenter.WarningSettingPresenter;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.view.CalculatorPopupWindow;
import com.tstech.soundlevelinstrument.viewinterface.WarningSettingView;
import common.check.unit.VoiceCheckUtil;

/**
 * 预警设置
 */
public class WarningSettingFragment extends
		BaseMvpFragment<WarningSettingView, WarningSettingPresenter> implements
		OnClickListener, IKeyBack, WarningSettingView, OnFocusChangeListener {

	private View view;
	/** 总压声级 安全区 */
	private EditText mEtSafearea;
	/** 总压声级 预警区 */
	private EditText mEtWarningarea;
	/** 总压声级 高危区 */
	private EditText mEtDangerarea;

	/** 主频声压级 安全区 */
	private EditText mEtFrequencysafe;
	/** 主频声压级 预警区 */
	private EditText mEtFrequencywarning;
	/** 主频声压级 高危区 */
	private EditText mEtFrequencydanger;

	/** 主频能量占比 安全区 */
	private EditText mEtDangersafe;
	/** 主频能量占比 预警区 */
	private EditText mEtDangerwarning;
	/** 主频能量占比 高危区 */
	private EditText mEtDangerdanger;

	/** 重置 */
	private Button mBtReset;
	/** 保存 */
	private Button mBtSave;
	/** 设置安全条件 */
//	private EditText mTvSafe;
//	private ImageButton mImbSafe;
	/** 设置预警条件 */
	private EditText mTvWarning;
	private ImageButton mImbWarning;
	/** 设置报警条件 */
	private EditText mTvDanger;
	private ImageButton mImbDanger;
	/** 提示文本*/
	private TextView tip1,tip2,tip3,tip4,tip5,tip6,tip7,tip8,tip9;
	

	public WarningSettingFragment() {

	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_warningsetting, container,
				false);

		initView();
		addListener();
		return view;
	}

	private void initView() {

		mEtSafearea = (EditText) view.findViewById(R.id.ws_safearea);
		mEtWarningarea = (EditText) view.findViewById(R.id.ws_warningarea);
		mEtDangerarea = (EditText) view.findViewById(R.id.ws_dangerarea);
		mEtFrequencysafe = (EditText) view.findViewById(R.id.ws_Frequencysafe);
		mEtFrequencywarning = (EditText) view
				.findViewById(R.id.ws_Frequencywarning);
		mEtFrequencydanger = (EditText) view
				.findViewById(R.id.ws_Frequencydanger);
		mEtDangersafe = (EditText) view.findViewById(R.id.ws_Dangersafe);
		mEtDangerwarning = (EditText) view.findViewById(R.id.ws_Dangerwarning);
		mEtDangerdanger = (EditText) view.findViewById(R.id.ws_Dangerdanger);

//		mTvSafe = (EditText) view.findViewById(R.id.safety_condition);
		mTvWarning = (EditText) view.findViewById(R.id.warning_condition);
		mTvDanger = (EditText) view.findViewById(R.id.alarm_condition);

		tip1 = (TextView) view.findViewById(R.id.tip1);
		tip2 = (TextView) view.findViewById(R.id.tip2);
		tip3 = (TextView) view.findViewById(R.id.tip3);
		tip4 = (TextView) view.findViewById(R.id.tip4);
		tip5 = (TextView) view.findViewById(R.id.tip5);
		tip6 = (TextView) view.findViewById(R.id.tip6);
		tip7 = (TextView) view.findViewById(R.id.tip7);
		tip8 = (TextView) view.findViewById(R.id.tip8);
		tip9 = (TextView) view.findViewById(R.id.tip9);
		
//		mImbSafe = (ImageButton) view.findViewById(R.id.setting_safe);
		mImbWarning = (ImageButton) view.findViewById(R.id.setting_warning);
		mImbDanger = (ImageButton) view.findViewById(R.id.setting_alarm);

		mBtReset = (Button) view.findViewById(R.id.ws_Reset);
		mBtSave = (Button) view.findViewById(R.id.ws_Save);

//		mImbSafe.setOnClickListener(this);
		mImbWarning.setOnClickListener(this);
		mImbDanger.setOnClickListener(this);
		mBtReset.setOnClickListener(this);
		mBtSave.setOnClickListener(this);
	}

	private void getWarning() {
		presenter.setSafearea(mEtSafearea.getText().toString());
		presenter.setWarningarea(mEtWarningarea.getText().toString());
		presenter.setDangerarea(mEtDangerarea.getText().toString());
		
		presenter.setFrequencysafe(mEtFrequencysafe.getText().toString());
		presenter.setFrequencywarning(mEtFrequencywarning.getText().toString());
		presenter.setFrequencydanger(mEtFrequencydanger.getText().toString());
		
		presenter.setDangersafe(mEtDangersafe.getText().toString());
		presenter.setDangerwarning(mEtDangerwarning.getText().toString());
		presenter.setDangerdanger(mEtDangerdanger.getText().toString());
		
//		presenter.setSafeCondition(mTvSafe.getText().toString());
		presenter.setWarningCondition(mTvWarning.getText().toString());
		presenter.setDangerCondition(mTvDanger.getText().toString());
	}

	private void addListener() {

		mEtSafearea.setOnFocusChangeListener(this);
		mEtWarningarea.setOnFocusChangeListener(this);
		mEtDangerarea.setOnFocusChangeListener(this);
		
		mEtFrequencysafe.setOnFocusChangeListener(this);
		mEtFrequencywarning.setOnFocusChangeListener(this);
		mEtFrequencydanger.setOnFocusChangeListener(this);
		
		mEtDangersafe.setOnFocusChangeListener(this);
		mEtDangerwarning.setOnFocusChangeListener(this);
		mEtDangerdanger.setOnFocusChangeListener(this);

//		mTvSafe.setOnFocusChangeListener(this);
		mTvWarning.setOnFocusChangeListener(this);
		mTvDanger.setOnFocusChangeListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ws_Reset: // 重置
			presenter.resetWaring();
			break;

		case R.id.ws_Save: // 保存
			getWarning();
			presenter.saveWaringSetting();
			setTextViewShow();
			ToastUtil.showToast(getActivity(), "保存信息");
			break;

//		case R.id.setting_safe:
//			presenter.showSafePopu(getActivity(), mBtSave);
//			break;

		case R.id.setting_warning:
			presenter.showWarningPopu(getActivity(), mBtSave);
			break;

		case R.id.setting_alarm:
			presenter.showAlarmPopu(getActivity(), mBtSave);
			break;

		}
	}

	/**
	 * 显示提示文本
	 */
	private void setTextViewShow() {
		tip1.setText("num<"+mEtSafearea.getText().toString());
		tip2.setText(mEtWarningarea.getText().toString()+"<=num<90");
		tip3.setText(mEtDangerarea.getText().toString()+"<=num");
		tip4.setText("num<"+mEtFrequencysafe.getText().toString());
		tip5.setText(mEtFrequencywarning.getText().toString()+"<=num<90");
		tip6.setText(mEtFrequencydanger.getText().toString()+"<=num");
		tip7.setText("num<"+mEtDangersafe.getText().toString());
		tip8.setText(mEtDangerwarning.getText().toString()+"<=num<90");
		tip9.setText(mEtDangerdanger.getText().toString()+"<=num");
		
	}

	@Override
	public void doKeyBack() {
		getActivity().getSupportFragmentManager().beginTransaction().hide(this)
				.commit();
	}

	@Override
	public WarningSettingPresenter initPresenter() {
		// TODO Auto-generated method stub
		return new WarningSettingPresenter();
	}

	@Override
	public FragmentActivity getActivityContext() {
		// TODO Auto-generated method stub
		return getActivity();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isResumed()) {
			presenter.restorWarning();
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void initWarning(ExpWarningSetting warningSetting) {
		mEtSafearea.setText("" + warningSetting.getSafearea());
		mEtWarningarea.setText("" + warningSetting.getWarningarea());
		mEtDangerarea.setText("" + warningSetting.getDangerarea());
		mEtFrequencysafe.setText("" + warningSetting.getFrequencysafe());
		mEtFrequencywarning.setText("" + warningSetting.getFrequencywarning());
		mEtFrequencydanger.setText("" + warningSetting.getFrequencydanger());
		mEtDangersafe.setText("" + warningSetting.getDangersafe());
		mEtDangerwarning.setText("" + warningSetting.getDangerwarning());
		mEtDangerdanger.setText("" + warningSetting.getDangerdanger());

//		mTvSafe.setText("" + warningSetting.getSafeCondition());
		mTvWarning.setText("" + warningSetting.getWarningCondition());
		mTvDanger.setText("" + warningSetting.getDangerCondition());
	}

	@Override
	public void onFocusChange(View v, boolean flag) {

		switch (v.getId()) {
		case R.id.ws_safearea:
			presenter.setSafearea(((EditText) v).getText().toString());
			break;

		case R.id.ws_warningarea:
			presenter.setWarningarea(((EditText) v).getText().toString());
			break;

		case R.id.ws_dangerarea:
			presenter.setDangerarea(((EditText) v).getText().toString());
			break;

		case R.id.ws_Frequencysafe:
			presenter.setFrequencysafe(((EditText) v).getText().toString());
			break;

		case R.id.ws_Frequencywarning:
			presenter.setFrequencywarning(((EditText) v).getText().toString());
			break;

		case R.id.ws_Frequencydanger:
			presenter.setFrequencydanger(((EditText) v).getText().toString());
			break;

		case R.id.ws_Dangersafe:
			presenter.setDangersafe(((EditText) v).getText().toString());
			break;

		case R.id.ws_Dangerwarning:
			presenter.setDangerwarning(((EditText) v).getText().toString());
			break;

		case R.id.ws_Dangerdanger:
			presenter.setDangerdanger(((EditText) v).getText().toString());
			break;

//		case R.id.safety_condition:
//			presenter.setSafeCondition(((EditText) v).getText().toString());
//			break;
			
		case R.id.warning_condition:
			presenter.setWarningCondition(((EditText) v).getText().toString());
			break;
			
		case R.id.alarm_condition:
			presenter.setDangerCondition(((EditText) v).getText().toString());
			break;

		}
	}

//	@Override
//	public void setTvSafe(String safeResult) {
//
//		mTvSafe.setText(safeResult);
//		((MonitorActivity) getActivity()).setCheckCondition(result);
//	}

	@Override
	public void setTvWarning(String warnResult) {
		mTvWarning.setText(warnResult);
	}

	@Override
	public void setTvDanger(String dangerResult) {
		mTvDanger.setText(dangerResult);
	}



	@Override
	public VoiceCheckUtil getCheckUtil() {

		return ((MonitorActivity)getActivity()).getPresent().getCheckUtil();
	}
}
