package com.tstech.soundlevelinstrument.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.adapter.ParaSpAdapter;
import com.tstech.soundlevelinstrument.back.IKeyBack;
import com.tstech.soundlevelinstrument.bean.ExpParameter;
import com.tstech.soundlevelinstrument.presenter.ParameterPresenter;
import com.tstech.soundlevelinstrument.util.AnimationUtil;
import com.tstech.soundlevelinstrument.viewinterface.ParameterView;

public class ParameterSettingFragment extends
		BaseMvpFragment<ParameterView, ParameterPresenter> implements
		ParameterView, OnClickListener, IKeyBack, OnItemSelectedListener,
		OnFocusChangeListener {

	private View view;
	/** 采样频率 */
	private Spinner mSpFrequency;
	/** 频率范围 */
	private Spinner mSpScope;
	/** 频率分辨率 */
	private Spinner mSpResolution;
	/** 加窗函数 */
	private Spinner mSpFunction;
	/** 计权 */
	private Spinner mSpWeighted;
	/** 平均次数 */
	private EditText mEtAverage;
	/** 档位选择 */
	private Spinner mSpSelect;
	/** 重置 */
	private Button mBtReset;
	/** 保存 */
	private Button mBtSave;
	/** 用于判断是否是第一次加载 */
	private int firstShow;
	
	private String [] mFrequencyArray ;
	private String [] mFunctionArray ;
	private String [] mWeightedArray ;
	private String [] mSelectArray ;

	public ParameterSettingFragment() {

	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_parametersetting, container,
				false);
		initView(); // 初始化界面
		initData();	// 初始化spinner数据
		return view;
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		mSpFrequency = (Spinner) view.findViewById(R.id.ps_frequency);
		mSpScope = (Spinner) view.findViewById(R.id.ps_scope);
		mSpResolution = (Spinner) view.findViewById(R.id.ps_resolution);
		mSpFunction = (Spinner) view.findViewById(R.id.ps_function);
		mSpWeighted = (Spinner) view.findViewById(R.id.ps_weighted);
		mSpSelect = (Spinner) view.findViewById(R.id.ps_select);
		mEtAverage = (EditText) view.findViewById(R.id.ps_average);
		mBtReset = (Button) view.findViewById(R.id.ps_Reset);
		mBtSave = (Button) view.findViewById(R.id.ps_Save);
		
		mBtReset.setOnClickListener(this);
		mBtSave.setOnClickListener(this);
	}
	
	/** 初始化spinner数据*/
	private void initData() {
		//采样频率
		mFrequencyArray = getResources().getStringArray(R.array.frequency);
		ArrayAdapter<String> mFreqAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.spinner_item,mFrequencyArray);
		mFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpFrequency.setAdapter(mFreqAdapter);
		
		//加窗函数
		mFunctionArray = getResources().getStringArray(R.array.function);
		ArrayAdapter<String> mFunAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.spinner_item,mFunctionArray);
		mFunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpFunction.setAdapter(mFunAdapter);
		
		//计权
		mWeightedArray = getResources().getStringArray(R.array.weighted);
		ArrayAdapter<String> mWeightedAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.spinner_item,mWeightedArray);
		mWeightedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpWeighted.setAdapter(mWeightedAdapter);
		
		//档位选择
		mSelectArray = getResources().getStringArray(R.array.select);
		ArrayAdapter<String> mSelectAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.spinner_item,mSelectArray);
		mSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpSelect.setAdapter(mSelectAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		// presenter.restoreParameter();//读入前次设置
		firstShow = 0;
	}

	@Override
	public void doKeyBack() {
		getActivity().getSupportFragmentManager().beginTransaction().hide(this)
				.commit();
	}

	@Override
	public ParameterPresenter initPresenter() {
		return new ParameterPresenter();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ps_Reset:
			removeListener();
			presenter.resetParameter();
			addListener();
			//将光标始终放在末尾
			mEtAverage.setSelection(mEtAverage.getText().toString().length());
			break;

		case R.id.ps_Save:
			presenter.setAverage(mEtAverage.getText().toString()); // 平均次数
																	// onFocusChange
																	// 不起作用
			presenter.saveParameter();
			break;
		}
	}

	@Override
	public FragmentActivity getActivityContext() {
		return getActivity();
	}

	// spinner 点击事件
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		switch (parent.getId()) {
		case R.id.ps_frequency: // 采样频率

			// if (firstShow < 6) firstShow += 1;
			// else if(((TextView)
			// view)!=null)presenter.setAcquiFreq(((TextView)
			// view).getText().toString());
			presenter.setAcquiFreq(((TextView) view).getText().toString());
			break;

		case R.id.ps_scope: // 频率范围
			// if (firstShow < 6) firstShow += 1;
			// else if(((TextView) view)!=null)presenter.setScope(((TextView)
			// view).getText().toString());
			presenter.setScope(((TextView) view).getText().toString());
			break;

		case R.id.ps_resolution: // 频率分辨率
			// if (firstShow < 6) firstShow += 1;
			// else if(((TextView) view)!=null)presenter.setFreqRes(((TextView)
			// view).getText().toString());
			presenter.setFreqRes(((TextView) view).getText().toString());
			break;

		case R.id.ps_function: // 加窗函数
			// if (firstShow < 6) firstShow += 1;
			// else if(((TextView) view)!=null)presenter.setFunction(position);
			presenter.setFunction(position);
			break;
		case R.id.ps_weighted: // 计权
			// if (firstShow < 6) firstShow += 1;
			// else if(((TextView) view)!=null)presenter.setWeight(position);
			presenter.setWeight(position);
			break;
		case R.id.ps_select: // 档位选择
			// if (firstShow < 6) firstShow += 1;
			// else if(((TextView) view)!=null)presenter.setSelect(position);
			presenter.setSelect(position);
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v.getId() == R.id.ps_average)
			presenter.setAverage(((EditText) v).getText().toString()); // 平均次数
	}

	@Override
	public void reSetAcquiRes(ArrayAdapter adapterRes) {
		mSpResolution.setAdapter(adapterRes);// 把值set到频率范围的spinner
	}

	@Override
	public void reSetFreqRange(ArrayAdapter freqRang_Adapter) { // 采样频率传来的
		mSpScope.setAdapter(freqRang_Adapter); // 把值set到频率范围的spinner
	}

	@Override
	public void init(ExpParameter mParam) {

		setSelectedItem(mSpFrequency, "" + mParam.getAcquiFreq());
		// presenter.setAcquiFreq("" + mParam.getAcquiFreq());
		setSelectedItem(mSpScope, mParam.getFreqRange() % 1 == 0 ? ""
				+ ((int) mParam.getFreqRange()) : "" + mParam.getFreqRange());
		// presenter.setScope("" + mParam.getFreqRange());
		setSelectedItem(mSpResolution, mParam.getFreqRes() % 1 == 0 ? ""
				+ ((int) mParam.getFreqRes()) : "" + mParam.getFreqRes());
		mSpFunction.setSelection(mParam.getWindowType());
		mSpWeighted.setSelection(mParam.getWeighting());
		mSpSelect.setSelection(mParam.getSelect());
		mEtAverage.setText("" + mParam.getAverageCount());

	}
	
	//TODO SpinnerAdapter 和 ArrayAdapter的区别造成spinner显示的格式不一样
	public void setSelectedItem(Spinner spinner, String item) {
		SpinnerAdapter adapter = spinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			if (adapter.getItem(i).toString().equals(item)) {
				spinner.setSelection(i, true);

				break;
			}
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isResumed()) {
			
			removeListener();
			presenter.restoreParameter();
			addListener();
		}
	}

	private void removeListener() {
		mSpFrequency.setOnItemSelectedListener(null);
		mSpScope.setOnItemSelectedListener(null);
		mSpResolution.setOnItemSelectedListener(null);
		mSpFunction.setOnItemSelectedListener(null);
		mSpWeighted.setOnItemSelectedListener(null);
		mSpSelect.setOnItemSelectedListener(null);
		mEtAverage.setOnFocusChangeListener(null);
	}

	private void addListener() {

		mSpFrequency.setOnItemSelectedListener(this);
		mSpScope.setOnItemSelectedListener(this);
		mSpResolution.setOnItemSelectedListener(this);
		mSpFunction.setOnItemSelectedListener(this);
		mSpWeighted.setOnItemSelectedListener(this);
		mSpSelect.setOnItemSelectedListener(this);
		mEtAverage.setOnFocusChangeListener(this);
	}

}
