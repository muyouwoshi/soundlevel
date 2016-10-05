package com.tstech.soundlevelinstrument.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.adapter.ContentAdapter;
import com.tstech.soundlevelinstrument.adapter.UViewPagerAdapter;
import com.tstech.soundlevelinstrument.fragment.CalibrationFragment;
import com.tstech.soundlevelinstrument.fragment.ParameterSettingFragment;
import com.tstech.soundlevelinstrument.fragment.ReportFragment;
import com.tstech.soundlevelinstrument.fragment.TestMessageFragment;
import com.tstech.soundlevelinstrument.fragment.WarningSettingFragment;
import com.tstech.soundlevelinstrument.picker.CustomNumberPicker;
import com.tstech.soundlevelinstrument.presenter.MonitorPresenter;
import com.tstech.soundlevelinstrument.util.AnimationUtil;
import com.tstech.soundlevelinstrument.util.AudioController;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.view.FFTSurfaceController;
import com.tstech.soundlevelinstrument.view.SPLSurfaceController;
import com.tstech.soundlevelinstrument.viewinterface.BackHandledFragment;
import com.tstech.soundlevelinstrument.viewinterface.BackHandledInterface;
import com.tstech.soundlevelinstrument.viewinterface.MonitorView;
import common.check.unit.VoiceCheckerInterface;

public class MonitorActivity extends

BaseMvpActivity<MonitorView, MonitorPresenter> implements OnClickListener,
		OnScrollListener, OnValueChangeListener, MonitorView,
		BackHandledInterface {

	private FragmentManager fm;
	/** 侧滑 */
	private DrawerLayout drawerLayout;

	private RelativeLayout leftLayout;
	private ContentAdapter adapter;
	private List<View> surfaceList;

	/** 监测后处理开关 */
	private Switch modeSwitch;
	/** 生成报告 */
	private Button mBtReport;
	/** 监测界面即时显示dB */
	private TextView mFlashRate;

	/** 开始按钮 */
	private ImageButton start;
	/** 侧滑按钮 */
	private ImageButton leftMenuBtn;
	private LinearLayout left_linear;
	/** 车号 */
	private CustomNumberPicker car_picker;
	/** 端位 */
	private CustomNumberPicker dw_picker;
	/** 视图 */
	private ViewPager mViewPager;
	// /** 指示灯 红*/
	// private ImageView redLight;
	// /** 指示灯 黄*/
	// private ImageView yelLight;
	/** 指示灯 绿 */
	private ImageView grelight;

	private Button message_btn;

	/** 提示 */
	private TextView tvShow, f1, f2, f3, f4, f5, dB1, dB2, dB3, dB4, dB5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		addfragment();
		initView(); // 初始化界面
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void addfragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.add(R.id.fragContent, new CalibrationFragment(), "calibration")
				.add(R.id.fragContent, new TestMessageFragment(), "testinfo")
				.add(R.id.fragContent, new ParameterSettingFragment(),
						"collectinfo")
				.add(R.id.fragContent, new WarningSettingFragment(),
						"warninginfo")
				.add(R.id.fragContent, new ReportFragment(), "report").commit();
		// .addToBackStack(null)

	}

	private void initView() {
		f1 = (TextView) findViewById(R.id.f1_text);
		f2 = (TextView) findViewById(R.id.f2_text);
		f3 = (TextView) findViewById(R.id.f3_text);
		f4 = (TextView) findViewById(R.id.f4_text);
		f5 = (TextView) findViewById(R.id.f5_text);
		dB1 = (TextView) findViewById(R.id.db1_text);
		dB2 = (TextView) findViewById(R.id.db2_text);
		dB3 = (TextView) findViewById(R.id.db3_text);
		dB4 = (TextView) findViewById(R.id.db4_text);
		dB5 = (TextView) findViewById(R.id.db5_text);
		tvShow = (TextView) findViewById(R.id.carDw);

		leftMenuBtn = (ImageButton) findViewById(R.id.left_open_drawer); // 侧滑单击按钮
		left_linear = (LinearLayout) findViewById(R.id.left_open_linear); // 侧滑单击按钮

		left_linear.setOnClickListener(this);
		leftMenuBtn.setOnClickListener(this);

		modeSwitch = (Switch) findViewById(R.id.survey_switch); // switch开关
		modeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					presenter.switchMode(0); // 后处理
				} else {
					presenter.switchMode(1); // 监测
				}
			}
		});

		car_picker = (CustomNumberPicker) findViewById(R.id.car_picker);// 车号选择
		car_picker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);// 禁止NumberPicker输入了。
		car_picker.setMaxValue(16);
		car_picker.setMinValue(01);
		car_picker.setValue(01);
		car_picker.setNumberPickerDividerColor(car_picker);
		car_picker.setOnScrollListener(this);
		car_picker.setOnValueChangedListener(this);

		dw_picker = (CustomNumberPicker) findViewById(R.id.dw_picker);// 端位选择
		dw_picker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		dw_picker.setMaxValue(02);
		dw_picker.setMinValue(01);
		dw_picker.setValue(01);
		// dw_picker.setBackgroundColor(Color.LTGRAY);
		dw_picker.setNumberPickerDividerColor(dw_picker);
		dw_picker.setOnScrollListener(this);
		dw_picker.setOnValueChangedListener(this);

		mFlashRate = (TextView) findViewById(R.id.display); // 监测界面显示的dB
		mBtReport = (Button) findViewById(R.id.survey_report); // 生成报告
		start = (ImageButton) findViewById(R.id.survey_start_btn); // 开始按钮
		// redLight = (ImageView) findViewById(R.id.red); //红
		// yelLight = (ImageView) findViewById(R.id.yellow); //黄
		grelight = (ImageView) findViewById(R.id.green); // 绿

		message_btn = (Button) findViewById(R.id.message_loader);

		// tVlight = (TextView) findViewById(R.id.light_text); // 提示字

		surfaceList = new ArrayList<View>();
		surfaceList.add(new SPLSurfaceController(this));
		surfaceList.add(new FFTSurfaceController(this));
		mViewPager = (ViewPager) findViewById(R.id.containear);
		UViewPagerAdapter mpa = new UViewPagerAdapter(surfaceList); // 图
		mViewPager.setAdapter(mpa);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		leftLayout = (RelativeLayout) findViewById(R.id.survey_left_menu);

		// 把侧滑菜单设置为透明，即隐藏右边的阴影部分
		drawerLayout.setScrimColor(Color.TRANSPARENT);

		ListView listView = (ListView) leftLayout
				.findViewById(R.id.left_listview);
		adapter = new ContentAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new DrawerItemClickListener());

		mBtReport.setOnClickListener(this);
		start.setOnClickListener(this);
		// message.setOnClickListener(this); // TODO 信息加载message
		message_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.survey_report:
			ReportFragment repfrag = new ReportFragment();
			loadFragment(repfrag);
			break;

		case R.id.survey_start_btn:
			presenter.onStart();

			break;

		case R.id.left_open_drawer:
			drawerLayout.openDrawer(Gravity.START); // 点击按钮打开侧滑菜单
			break;

		case R.id.left_open_linear:
			drawerLayout.openDrawer(Gravity.START); // 点击按钮附近打开侧滑菜单
			break;

		case R.id.message_loader: // 加载信息
			presenter.showFileList(MonitorActivity.this);
			ToastUtil.showToast(MonitorActivity.this, "信息加载成功");
			break;

		}
	}

	/**
	 * 加载报告界面
	 * 
	 * @param fragment
	 */
	public void loadFragment(BackHandledFragment fragment) {
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().show(fm.findFragmentByTag("report")).commit();
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		getSupportFragmentManager().beginTransaction().hide(fragment).commit();
	}

	@Override
	public void onBackPressed() {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		presenter.onBackPressed(fragments, this);

	}

	@Override
	public MonitorPresenter initPresenter() {
		return new MonitorPresenter();
	}

	@Override
	public void hideFileSelector(int mode) {
		LinearLayout car = (LinearLayout) findViewById(R.id.carText);
		LinearLayout dw = (LinearLayout) findViewById(R.id.dwText);
		LinearLayout message = (LinearLayout) findViewById(R.id.messageText);

		switch (mode) {
		case View.GONE: // 后处理
			AnimationUtil.hideViewLeft(this, car);
			AnimationUtil.hideViewLeft(this, dw);
			AnimationUtil.showViewRight(this, message);
//			car.setVisibility(View.GONE); // 隐藏
//			dw.setVisibility(View.GONE);
//			message.setVisibility(View.VISIBLE); // 显示
			if (tvShow.getText().toString().equals(""))
				tvShow.setVisibility(View.GONE);
			else
				tvShow.setVisibility(View.VISIBLE);
			break;

		case View.VISIBLE: // 监测
			
			AnimationUtil.showViewLeft(this, car);
			AnimationUtil.showViewLeft(this, dw);
			AnimationUtil.hideViewRight(this, message);
//			car.setVisibility(View.VISIBLE);
//			dw.setVisibility(View.VISIBLE);
//			message.setVisibility(View.GONE);
			tvShow.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void finishActivity() {
		finish();
	}

	@Override
	public void hideLeftMenu() {
		drawerLayout.closeDrawers();
	}

	@Override
	public Context getContext() {
		return this;
	}

	/**
	 * <功能> 设置开始按钮的状态
	 * 
	 * @param flag
	 *            0 设置为停止状态 1设置为开始状态
	 */
	@SuppressLint("NewApi")
	@Override
	public void resetStartButton(int flag) {

		// if (flag == AudioController.STOP) {
		// start.setImageResource(
		// R.drawable.fmg_record_stop_normal);
		// } else {
		// start.setImageResource(
		// R.drawable.fmg_record_start_normal);
		// }
		start.setImageResource(flag == AudioController.STOP ? R.drawable.fmg_record_stop_normal
				: R.drawable.fmg_record_start_normal);
	}

	/**
	 * listView 侧滑菜单
	 */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			fm = getSupportFragmentManager();
			FragmentTransaction fragmentTransation = fm.beginTransaction();
			// 根据item点击行号判断启用指定Fragment
			switch (position) {
			case 0: // 标定
				drawerLayout.closeDrawers();
				if (presenter.hasStop())
					fm.beginTransaction()
							.show(fm.findFragmentByTag("calibration")).commit();
				break;

			case 1: // 实验信息
				drawerLayout.closeDrawers();
				fm.beginTransaction().show(fm.findFragmentByTag("testinfo"))
						.commit();
				break;

			case 2: // 采集设置
				drawerLayout.closeDrawers();
				fm.beginTransaction().show(fm.findFragmentByTag("collectinfo"))
						.commit();
				break;

			case 3: // 预警设置
				drawerLayout.closeDrawers();
				fm.beginTransaction().show(fm.findFragmentByTag("warninginfo"))
						.commit();
				break;

			case 4: // 测试报告
				drawerLayout.closeDrawers();
				fm.beginTransaction().show(fm.findFragmentByTag("report"))
						.commit();
				break;
			}

			fragmentTransation.commit();
		}

	}

	@Override
	public void onScrollStateChange(NumberPicker view, int scrollState) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.car_picker:
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_FLING:

				break;
			case OnScrollListener.SCROLL_STATE_IDLE:

				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				break;
			}
			break;

		case R.id.dw_picker:
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_FLING:

				break;
			case OnScrollListener.SCROLL_STATE_IDLE:

				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				break;
			}
			break;
		}

	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		switch (picker.getId()) {
		case R.id.car_picker:
			presenter.setCarNum("" + picker.getValue());
			break;
		case R.id.dw_picker:
			presenter.setDwNum("" + picker.getValue());
			break;
		}
	}

	@Override
	public void setSelectedFragment(BackHandledFragment selectedFragment) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModeSwitch(int mode) {
		modeSwitch.setChecked(mode == MonitorView.MODE_C);

	}

	@Override
	public void updateSPLValue(final String str) {
		// Log.e("ard", "str："+str);
		mFlashRate.setText(str);
	}

	@Override
	public void updateFFTValues(List<String> freqsAndValues) {
		f1.setText(freqsAndValues.get(0));
		dB1.setText(freqsAndValues.get(1));
		f2.setText(freqsAndValues.get(2));
		dB2.setText(freqsAndValues.get(3));
		f3.setText(freqsAndValues.get(4));
		dB3.setText(freqsAndValues.get(5));
		f4.setText(freqsAndValues.get(6));
		dB4.setText(freqsAndValues.get(7));
		f5.setText(freqsAndValues.get(8));
		dB5.setText(freqsAndValues.get(9));
	}

	@Override
	public void setTvShow(String carDwTv) {
		// Log.e("temp", "carDwTv:"+carDwTv);
		tvShow.setVisibility(View.VISIBLE);
		tvShow.setText(carDwTv);
		tvShow.setTypeface(Typeface.DEFAULT);
		tvShow.setTextColor(Color.rgb(156, 46, 96));
		tvShow.setCompoundDrawablePadding(10); // 设置字与图标的距离
	}

	@Override
	public void setLightChange(int state) {
		switch (state) {
		case VoiceCheckerInterface.ERROR:
			grelight.setImageResource(R.drawable.icon_red);
			break;
		case VoiceCheckerInterface.WARINING:
			grelight.setImageResource(R.drawable.icon_yellow);
			break;
		case VoiceCheckerInterface.NORMAL:
			grelight.setImageResource(R.drawable.icon_green);
			break;
		default:
			grelight.setImageResource(R.drawable.icon_black);
			break;
		}
	}
	//获取ViewPager视图中的图片，
	public Bitmap savePagerPicture(){
		mViewPager.setDrawingCacheEnabled(true);
		Bitmap bitmap=mViewPager.getDrawingCache();
		Bitmap result=Bitmap.createBitmap(bitmap);
		mViewPager.setDrawingCacheEnabled(false);
		return result;
	}
	//将图片保存至文件夹中
	public void saveBitmap(Bitmap result,String fileName){
		String path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();      
		
	     File myCaptureFile = new File(path +File.separator+ fileName);    
	     BufferedOutputStream bos=null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
			result.compress(Bitmap.CompressFormat.JPEG, 80, bos);    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			try {
				bos.flush();
				bos.close(); 
			} catch (IOException e) {
				e.printStackTrace();
			}    
		      	
		}     
	}
	
	public MonitorPresenter getPresent() {
		if(presenter != null) return presenter;
		return null;
	}

}
