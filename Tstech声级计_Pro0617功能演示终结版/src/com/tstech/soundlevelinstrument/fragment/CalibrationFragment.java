package com.tstech.soundlevelinstrument.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.back.IKeyBack;
import com.tstech.soundlevelinstrument.presenter.CalibrationPresenter;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.viewinterface.CalibrationView;

/**
 * 标定界面
 * 
 */
public class CalibrationFragment extends
		BaseMvpFragment<CalibrationView, CalibrationPresenter> implements
		CalibrationView, OnClickListener, IKeyBack {
	private View view;

	/** 校对按钮 */
	private ImageButton mBtnCalibrate;
	/** 校对值存储 */
	private Button mSaveCalibration;
	/** 校对界面发对数值实时显示 */
	private TextView mTxtStatus;
	private ScrollView mScrollStatus;
	/** 使用上次的校对值 */
	private Button mLast;

	/** 校对界面结果校对值 */
	private TextView mTxtCalibrate;

	/** 校对值 */
	private String valueCalibration;

	public CalibrationFragment() {
		
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.page_calibration, container, false);

		initView();

		return view;
	}

	private void initView() {
		mTxtStatus = (TextView) view.findViewById(R.id.calibration_status);
		mScrollStatus = (ScrollView) view
				.findViewById(R.id.calibration_scroll_status);
		mBtnCalibrate = (ImageButton) view.findViewById(R.id.calibration_btn);
		mTxtCalibrate = (TextView) view
				.findViewById(R.id.calibration_flashrate);
		mSaveCalibration = (Button) view.findViewById(R.id.calibration_save);
		mLast = (Button) view.findViewById(R.id.calibration_last);

		mBtnCalibrate.setOnClickListener(this);
		mSaveCalibration.setOnClickListener(this);
		mLast.setOnClickListener(this);

	}

	@Override
	public CalibrationPresenter initPresenter() {
		return new CalibrationPresenter();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.calibration_btn:
			presenter.doCalibration();
			break;
		case R.id.calibration_save:
			presenter.saveProofreadValue();
			break;
		case R.id.calibration_last:
			presenter.changeProofreadValue();
			break;
		}
	}

	public FragmentActivity getContext() {
		return this.getActivity();
	}

	@Override
	public void addToTxtStatus(String string) {
		mTxtStatus.append(string);
	}

	@Override
	public void scrollToBottom() {
		mScrollStatus.fullScroll(ScrollView.FOCUS_DOWN);
	}

	@Override
	public void endCalWithValue(String valueCalibration) {
		mTxtCalibrate.setText(valueCalibration.substring(0, 11));
		mBtnCalibrate.setImageResource(R.drawable.selector_record_unrecord);
		mBtnCalibrate.setEnabled(true);

		mSaveCalibration.setEnabled(true);
		mLast.setEnabled(true);
	}

	@Override
	public void doKeyBack() {
		if (presenter.stop()) {
			getActivity().getSupportFragmentManager().beginTransaction()
					.hide(this).commit();
		} else {
			ToastUtil.showToast(getActivity(), "标定尚未结束");
		}
	}

	@Override
	public void prooFreading() {
		mTxtCalibrate.setText("校对中...");
		mTxtStatus.setText("");
//		mBtnCalibrate.setBackgroundResource(R.drawable.selector_record_record);
		mBtnCalibrate.setImageResource(R.drawable.selector_record_record);
		mBtnCalibrate.setEnabled(false);

		mBtnCalibrate.setImageResource(R.drawable.selector_record_record);

		mSaveCalibration.setEnabled(false);
		valueCalibration = null;

		mLast.setEnabled(false);
	}

}
