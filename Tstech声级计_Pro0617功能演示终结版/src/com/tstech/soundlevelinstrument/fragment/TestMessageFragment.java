package com.tstech.soundlevelinstrument.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.activity.MapActivity;
import com.tstech.soundlevelinstrument.back.IKeyBack;
import com.tstech.soundlevelinstrument.bean.ExpTextMessage;
import com.tstech.soundlevelinstrument.presenter.ParameterPresenter;
import com.tstech.soundlevelinstrument.presenter.TestMessgaePresenter;
import com.tstech.soundlevelinstrument.util.DateTimePickDialogUtil;
import com.tstech.soundlevelinstrument.util.InfoUtil;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.viewinterface.ParameterView;
import com.tstech.soundlevelinstrument.viewinterface.TestMessageView;

/**
 * 实验信息
 */
public class TestMessageFragment extends
		BaseMvpFragment<TestMessageView, TestMessgaePresenter> implements
		OnClickListener, IKeyBack, TestMessageView, OnFocusChangeListener {

	private View view;
	/** 检测编号 */
	private EditText mEdtestNumber;
	/** 内容 */
	private EditText mEdcontent;
	/** 说明 */
	private EditText mEdinstruction;
	/** 姓名 */
	private EditText mEdname;
	/** 车组号 */
	private EditText mEdcarIntemNum;
	/** 车辆号 */
	private EditText mEdcarNumber;
	/** 测点 */
	private EditText mEdtestPoint;
	/** 运营交路及车次 */
	private EditText mEdcRoadNum;
	/** 试验日期 */
	private EditText mEdtestDate;
	/** 试验速度 */
	private EditText mEdtestSpeed;
	/** 运营总里程 */
	private EditText mEdtestDistance;
	/** 旋后里程 */
	private EditText mEdspinAfmileage;
	/** 车轮直径 */
	private EditText mEdwheelDiameter;
	/** 重置 */
	private Button reset;
	/** 保存 */
	private Button save;
	/**
	 * 定位相关的 address:当前位置 refurbish:定位图标
	 */
	private TextView address;
	private ImageView refurbish;

	private SharedPreferences sp;
	private String initDateTime; // 存放当前时间

	public TestMessageFragment() {

	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater
				.inflate(R.layout.fragment_testmessage, container, false);

		initView(); // 初始化界面
		
		return view;
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		// sp=PreferenceManager.getDefaultSharedPreferences(getActivity());
		sp = getActivity().getSharedPreferences("sp_name", 0);

		mEdtestNumber = (EditText) view.findViewById(R.id.testmessage_testNum);
		mEdcontent = (EditText) view.findViewById(R.id.testmessage_content);
		mEdinstruction = (EditText) view
				.findViewById(R.id.testmessage_instruction);
		mEdname = (EditText) view.findViewById(R.id.testmessage_name);
		mEdcarIntemNum = (EditText) view
				.findViewById(R.id.testmessage_carItemNum);
		mEdcarNumber = (EditText) view.findViewById(R.id.testmessage_carNum);
		mEdtestPoint = (EditText) view.findViewById(R.id.testmessage_testPoint);
		mEdcRoadNum = (EditText) view.findViewById(R.id.testmessage_cRoadNum);
		mEdtestDate = (EditText) view.findViewById(R.id.testmessage_testDate);
		mEdtestSpeed = (EditText) view.findViewById(R.id.testmessage_testSpeed);
		mEdtestDistance = (EditText) view
				.findViewById(R.id.testmessage_distance);
		mEdspinAfmileage = (EditText) view
				.findViewById(R.id.testmessage_SpinAfmileage);
		mEdwheelDiameter = (EditText) view
				.findViewById(R.id.testmessage_Wheeldiameter);

		refurbish = (ImageView) view.findViewById(R.id.refurbish_address);
		address = (TextView) view.findViewById(R.id.testmessage_gps);

		// 时间选择器
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String str = formatter.format(new Date());
		initDateTime = str.substring(0, 4) + "年" + str.substring(4, 6) + "月"
				+ str.substring(6, 8) + "日";// + str.substring(8, 10) + ":"
		// + str.substring(10, 12);
		mEdtestDate.setText(initDateTime);
//		Log.e("temp", "当前时间：" + initDateTime);

		mEdtestDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
						getActivity(), initDateTime);
				dateTimePicKDialog.dateTimePicKDialog(mEdtestDate);

			}
		});

		reset = (Button) view.findViewById(R.id.testmessage_Reset);
		save = (Button) view.findViewById(R.id.testmessage_Save);

		// 定位
		refurbish.setOnClickListener(this);
		address.setOnClickListener(this);
		// 重置
		reset.setOnClickListener(this);
		// 保存
		save.setOnClickListener(this);
	}



	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.refurbish_address: // 获取当前地址
			// TODO 获取当前地址
			// 进入MapActivity，通过返回码666，获取MapActivity传回来的位置
			toGetAddress();
			break;

		case R.id.testmessage_gps: // 获取当前地址
			toGetAddress();
			break;

		case R.id.testmessage_Save: // 保存
			getMeDatas();
			presenter.saveMessage();
			ToastUtil.showToast(getActivity(), "保存信息");
			break;

		case R.id.testmessage_Reset: // 重置
			removeMessage();
			presenter.resetTestMessage();
			addMessage();
			setSelectionToEnd();
			break;

		}
	}

	private void getMeDatas() {
		presenter.setTestNum(mEdtestNumber.getText().toString());
		presenter.setContent(mEdcontent.getText().toString());
		presenter.setInstruction(mEdinstruction.getText().toString());
		presenter.setName(mEdname.getText().toString());
		presenter.setCarIntemnum(mEdcarIntemNum.getText().toString());
		presenter.setCarNumber(mEdcarNumber.getText().toString());
		presenter.setTestPoint(mEdtestPoint.getText().toString());
		presenter.setcRoad(mEdcRoadNum.getText().toString());
		presenter.setTestDate(mEdtestDate.getText().toString());
		presenter.setTestSpeed(mEdtestSpeed.getText().toString());
		presenter.setTestDistance(mEdtestDistance.getText().toString());
		presenter.setSpinAfmileage(mEdspinAfmileage.getText().toString());
		presenter.setWheelDiameter(mEdwheelDiameter.getText().toString());
	}

	/** 重置后将光标放在mEdcarNumber的end */
	private void setSelectionToEnd() {
		// 将光标始终放在末尾
		mEdcarNumber.setSelection(mEdcarNumber.getText().toString().length());
	}

	/** 进入获取地址MapActivity */
	private void toGetAddress() {
		Intent i = new Intent(getActivity(), MapActivity.class);
		startActivityForResult(i, 666);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		try {
			switch (resultCode) {
			case 666: // set位置到textView
				address.setText(data.getStringExtra("addressName"));
				break;

			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void doKeyBack() {
		getActivity().getSupportFragmentManager().beginTransaction().hide(this)
				.commit();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isResumed()) {
			removeMessage();
			presenter.restoreMessage();
			addMessage();
		}
	}

	@Override
	public FragmentActivity getActivityContext() {
		return getActivity();
	}

	@Override
	public void initDatas(ExpTextMessage message) {

		mEdtestNumber.setText("" + message.getTestNum());
		mEdcontent.setText("" + message.getContent());
		mEdinstruction.setText("" + message.getInstruction());
		mEdname.setText("" + message.getName());
		mEdcarIntemNum.setText("" + message.getCarIntemnum());
		mEdcarNumber.setText("" + message.getCarNumber());
		mEdtestPoint.setText("" + message.getTestPoint());
		mEdcRoadNum.setText("" + message.getcRoad());
		mEdtestDate.setText("" + message.getTestDate());
		mEdtestSpeed.setText("" + message.getTestSpeed());
		mEdtestDistance.setText("" + message.getTestDistance());
		mEdspinAfmileage.setText("" + message.getSpinAfmileage());
		mEdwheelDiameter.setText("" + message.getWheelDiameter());

	}

	private void removeMessage() {

		mEdtestNumber.setOnFocusChangeListener(null);
		mEdcontent.setOnFocusChangeListener(null);
		mEdinstruction.setOnFocusChangeListener(null);
		mEdname.setOnFocusChangeListener(null);
		mEdcarIntemNum.setOnFocusChangeListener(null);
		mEdcarNumber.setOnFocusChangeListener(null);
		mEdtestPoint.setOnFocusChangeListener(null);
		mEdcRoadNum.setOnFocusChangeListener(null);
		mEdtestDate.setOnFocusChangeListener(null);
		mEdtestSpeed.setOnFocusChangeListener(null);
		mEdtestDistance.setOnFocusChangeListener(null);
		mEdspinAfmileage.setOnFocusChangeListener(null);
		mEdwheelDiameter.setOnFocusChangeListener(null);

	}

	private void addMessage() {

		mEdtestNumber.setOnFocusChangeListener(this);
		mEdcontent.setOnFocusChangeListener(this);
		mEdinstruction.setOnFocusChangeListener(this);
		mEdname.setOnFocusChangeListener(this);
		mEdcarIntemNum.setOnFocusChangeListener(this);
		mEdcarNumber.setOnFocusChangeListener(this);
		mEdtestPoint.setOnFocusChangeListener(this);
		mEdcRoadNum.setOnFocusChangeListener(this);
		mEdtestDate.setOnFocusChangeListener(this);
		mEdtestSpeed.setOnFocusChangeListener(this);
		mEdtestDistance.setOnFocusChangeListener(this);
		mEdspinAfmileage.setOnFocusChangeListener(this);
		mEdwheelDiameter.setOnFocusChangeListener(this);

	}

	@Override
	public void onFocusChange(View v, boolean flag) {
		switch (v.getId()) {
		case R.id.testmessage_testNum:
			presenter.setTestNum(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_content:
			presenter.setContent(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_instruction:
			presenter.setInstruction(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_name:
			presenter.setName(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_carItemNum:
			presenter.setCarIntemnum(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_carNum:
			presenter.setCarNumber(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_testPoint:
			presenter.setTestPoint(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_cRoadNum:
			presenter.setcRoad(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_testDate:
			presenter.setTestDate(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_testSpeed:
			presenter.setTestSpeed(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_distance:
			presenter.setTestDistance(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_SpinAfmileage:
			presenter.setSpinAfmileage(((EditText) v).getText().toString());
			break;
			
		case R.id.testmessage_Wheeldiameter:
			presenter.setWheelDiameter(((EditText) v).getText().toString());
			break;

		}

	}

	@Override
	public TestMessgaePresenter initPresenter() {
		// TODO Auto-generated method stub
		return new TestMessgaePresenter();
	}
}
