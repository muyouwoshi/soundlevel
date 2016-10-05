package com.tstech.soundlevelinstrument.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.adapter.UViewPagerAdapter;
import com.tstech.soundlevelinstrument.util.AudioUtil;
import com.tstech.soundlevelinstrument.view.NumberPickerDialog;
import com.tstech.soundlevelinstrument.view.UPureColorScaleView;

public class MainActivity extends Activity implements View.OnClickListener {
	/** bar */
	private ImageView mBar, mRecord, mCalibration;
	/** 检测和校对view页面存储器，用于vp */
	private List<View> list;
	/** 主视区vp */
	private ViewPager mPage;
	/** vp的子view。vRec：检测，vCal：校对 */
	private View vRec, vCal;
	
	/** 检测按钮 */
	private Button mBtnRecord;
	/** 校对按钮 */
	private Button mBtnCalibrate;
	/** 校对值存储 */
	private Button mSaveCalibration;
	/** 校对界面发对数值实时显示 */
	private TextView mTxtStatus;
	private ScrollView mScrollStatus;
	/** 使用上次的校对值 */
	private ToggleButton mLast;
	
	/** 监测界面 center:中频边界，high:高频边界 */
	private TextView mCenterRate, mHighRate;
	/** 监测界面即时显示dB*/
	private TextView mFlashRate;
	/** 校对界面结果校对值 */
	private TextView mTxtCalibrate;
	/** 监测界面刻度控件 */
	private UPureColorScaleView mUCSV;
	
	private AudioUtil audioUtil;
	
	/** isRecording 是否检测中 */
	private boolean isRecording = false;
	/** isCalibrating 是否校对中 */
	private boolean isCalibrating = false;
	/** 校对值 */
	private String valueCalibration;
	
	/** 中频边界，高频边界 */
	private int centerRate, highRate;
	
	/** 消息处理器  */
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			String s = msg.obj.toString();

			// 绘制监测界面刻度，并展示dB值。每次消息传递一个dB值
			if("status".equals(s)){
				String volume = msg.getData().getString("volume");// 用string传递是为了防止NaN
				float db = 0f;
				
				try {
					db = Float.valueOf(volume);
				} catch (Exception e) {
					Toast.makeText(MainActivity.this, "监测错误:" + e, 0).show();
				}
	
				if(db != 0)
					mFlashRate.setText(String.format("%.1f", db));
				
				mUCSV.setMultiColorHeitht(db, highRate, centerRate);
			}
			
			// 校准界面的5s内反对数计算
			else if("proofread".equals(s)){
				String volume = msg.getData().getString("volume");// 用string传递是为了防止NaN
				double ddb = 0d;
				
				try {
					ddb = Double.valueOf(volume);
				} catch (Exception e) {
					Toast.makeText(MainActivity.this, "校对错误:" + e, 0).show();
				}
	
				mTxtStatus.append(ddb + "\n");

				post(new Runnable() {
				    @Override
				    public void run() {
				    	mScrollStatus.fullScroll(ScrollView.FOCUS_DOWN);
				    }
				});
				
			}
			
			// 校准界面反对数计算结果
			else if("proofread_result".equals(s)){
				isCalibrating = false;
				
				valueCalibration = msg.getData().getString("volume");

				mTxtCalibrate.setText(valueCalibration.substring(0, 11));
				mBtnCalibrate.setBackgroundResource(R.drawable.selector_record_unrecord);
				mBtnCalibrate.setEnabled(true);
				
				mSaveCalibration.setEnabled(true);
				mLast.setEnabled(true);
				
				mPage.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return false;
					}
				});
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		initAdapter();
		initPageChange();
		initEvent();
	}

	/** 1.初始化bar，vp */
	private void initView() {
		mPage = (ViewPager) findViewById(R.id.containear);

		mBar = (ImageView) findViewById(R.id.bar);
		mRecord = (ImageView) findViewById(R.id.bar_record);
		mCalibration = (ImageView) findViewById(R.id.bar_calibration);
	}

	/** 2.为vp指定适配器。初始化vp子view中的控件。初始化 recordUtil，开始监测dB */
	private void initAdapter() {
		vRec = View.inflate(this, R.layout.page_record, null);
		mBtnRecord = (Button) vRec.findViewById(R.id.record_btn);
		mCenterRate = (TextView) vRec.findViewById(R.id.record_centerrate);
		mHighRate = (TextView) vRec.findViewById(R.id.record_highrate);
		mFlashRate = (TextView) vRec.findViewById(R.id.record_flashrate);
		mUCSV = (UPureColorScaleView) vRec.findViewById(R.id.record_ucsv);

		vCal = View.inflate(this, R.layout.page_calibration, null);
		mTxtStatus = (TextView) vCal.findViewById(R.id.calibration_status);
		mScrollStatus = (ScrollView) vCal.findViewById(R.id.calibration_scroll_status);
		mBtnCalibrate = (Button) vCal.findViewById(R.id.calibration_btn);
		mTxtCalibrate = (TextView) vCal.findViewById(R.id.calibration_flashrate);
		mSaveCalibration = (Button) vCal.findViewById(R.id.calibration_save);
		mLast = (ToggleButton) vCal.findViewById(R.id.calibration_last);
	
		list = new ArrayList<View>();
		list.add(vRec);
		list.add(vCal);

		UViewPagerAdapter mpa = new UViewPagerAdapter(list);

		mPage.setAdapter(mpa);
		
		audioUtil = new AudioUtil(this, handler);
	}

	/** 3.vp添加切换事件 */
	private void initPageChange() {
		mPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			/**
			 * index：被选择的页面索引，即切换后 显示的页面 如果页面只是滚了一下但没有切换，不会调用此方法
			 */
			@Override
			public void onPageSelected(int index) {
				switch (index) {
				case 0:   				// 切换到 监测 界面
					if(isCalibrating){  // 如果校对还在进行中。那么，doCalibrating停止之
						isCalibrating = false;
						doCalibrating();
					}
					
					mBar.setImageResource(R.drawable.bar_record);
					break;
				case 1:                 // 切换到 校对界面
					if(isRecording){    // 如果还在监测中，doRecording，停止之
						isRecording = false;
						doRecording();
					}
					
					mBar.setImageResource(R.drawable.bar_calibration);
					break;
				}
			}

			@Override
			public void onPageScrolled(int index, float scale, int pixel) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
	}

	/** 4. bar控件添加事件。vp子view添加事件 */
	private void initEvent() {
		mRecord.setOnClickListener(this);
		mCalibration.setOnClickListener(this);
		
		mBtnRecord.setOnClickListener(this);
		mBtnCalibrate.setOnClickListener(this);
		mSaveCalibration.setOnClickListener(this);
		
		mCenterRate.setOnClickListener(this);
		mHighRate.setOnClickListener(this);
		
		mLast.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					
				}else{
					
				} 
			}
		});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.bar_record:                   // bar，监测
			if(!isCalibrating){
				mPage.setCurrentItem(0, false);
				mBar.setImageResource(R.drawable.bar_record);
			}
			break;
		case R.id.bar_calibration:              // bar，校对
			mPage.setCurrentItem(1, false);
			mBar.setImageResource(R.drawable.bar_calibration);
			break;

		///////////////////////////////-----------------------------------
			
		case R.id.record_btn:                 // 监测界面，开始监测
			Log.e("ard", "开始监测...");
			isRecording = isRecording == true ? false : true;
			
			doRecording();
			break;
		case R.id.calibration_btn:            // 校对界面，开始校对
			
			createAlterdialog();

			break;
		case R.id.calibration_save:            // 校对界面，保存校对值
			
			if(mLast.isChecked()){
				audioUtil.saveProofreadValue(1);
//				Toast.makeText(MainActivity.this, "上次校对值：" + valueCalibration, 0).show();
			}else{ 
				CharSequence text = mTxtCalibrate.getText();
				valueCalibration = null == text ? "" : text.toString();
				
				if("".equals(text) || "校对".equals(text)){
					Toast.makeText(MainActivity.this, "请重新校对或使用以往校对值", 0).show();
				} else {
					audioUtil.saveProofreadValue(0);
					Toast.makeText(MainActivity.this, "新校对值：" + valueCalibration, 0).show();
				}
			}
			
			break;

		///////////////////////////////-----------------------------------
		
		case R.id.record_centerrate:
			createNumberPicker(1);
			break;
			
		case R.id.record_highrate:
			createNumberPicker(0);
			break;
			
		}
	}
	
	private void createAlterdialog() {
	    // 1.构造器
		AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
	    builder.setTitle("提示");                      // 标题
	    builder.setIcon(R.drawable.iconx72);           // 图标
	    builder.setMessage(Html.fromHtml("请确认已经插接好<b><font color=red>标定器</font></b>和<b><font color=red>MIC</font></b>。"));   // 信息
	    // 确定按钮
	    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
				
				audioUtil.clearProofreadValue();
				
				isCalibrating = isCalibrating == true ? false : true;

				doCalibrating();
				
				mLast.setChecked(false);
	        }
	    });
	    // 取消按钮
	    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        }
	    });
	    // 2.创建对话框
	    AlertDialog dialog = builder.create();
	    // 3.显示对话框
	    dialog.show();
	}

	/**
	 * <b>功能</b>: doRecording，监测界面处理<br/>
	 */
	public void doRecording(){
//		Toast.makeText(this, "isRecording " + isRecording, 0).show();
		
		if(isRecording){ // 如果正在监测
			CharSequence textCenter = mCenterRate.getText();
			CharSequence textHigh = mHighRate.getText();
			
			Double dCenter = Double.valueOf(textCenter == null || textCenter.toString().equals("") ? 60+"" : textCenter.toString());
			centerRate = dCenter.intValue();
			Double dHigh = Double.valueOf(textHigh == null || textHigh.toString().equals("") ? 35+"" : textHigh.toString());
			highRate = dHigh.intValue();
			
			if(highRate > 90){
				highRate = 90;
				mHighRate.setText("90");
				Toast.makeText(MainActivity.this, "高频边界最大预设为90", 0).show();
				return;
			}
			
			if(centerRate < 20){
				centerRate = 20;
				mCenterRate.setText("20");
				Toast.makeText(MainActivity.this, "中频边界最小预设为20", 0).show();
				return;
			}
			
			if(centerRate >= highRate){
				highRate = 90;
				mHighRate.setText("90");
				centerRate = 20;
				mCenterRate.setText("20");
				Toast.makeText(MainActivity.this, "高频边界必须大于中频边界", 0).show();
				return;
			}
			
			if(null == valueCalibration || "".equals(valueCalibration)){
				Toast.makeText(MainActivity.this, "尚未校对，请侧滑到校对界面执行", 0).show();
				return;
			}
			
			mCenterRate.setEnabled(false);
			mHighRate.setEnabled(false);
			
			mBtnRecord.setBackgroundResource(R.drawable.selector_record_record);
			
			Log.e("ard", "执行监测");
			
			audioUtil.setFlag(true, 0);
		}
		else{ 
			mCenterRate.setEnabled(true);
			mHighRate.setEnabled(true);

			mBtnRecord.setBackgroundResource(R.drawable.selector_record_unrecord);
			
			audioUtil.setFlag(false, 0);
			
			
		}
	}
	
	/**
	 * <b>功能</b>: doCalibrating，校对界面处理 <br/>
	 * <b>说明</b>:  <br/>
	 * <b>创建</b>: 2016-1-18_下午2:25:43 <br/>
	 * @author : weiyou.cui@ts-tech.com.cn <br/>
	 * @version 1 <br/>
	 */
	public void doCalibrating(){
		if(isCalibrating){ 
			mTxtCalibrate.setText("校对中...");
			mTxtStatus.setText("");
			mBtnCalibrate.setBackgroundResource(R.drawable.selector_record_record);
			mBtnCalibrate.setEnabled(false);

			mBtnCalibrate.setBackgroundResource(R.drawable.selector_record_record);
			
			mSaveCalibration.setEnabled(false);
			valueCalibration = null;

			mPage.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});
			
			mLast.setEnabled(false);
			
			audioUtil.setFlag(true, 1);
		}
	}

	/** 创建数字选择器
	 * @param i 0=高频，1=中频 */
	private void createNumberPicker(final int i) {
		
		int oldVal = 0;
		
		switch(i){
		case 0:
			oldVal = Integer.valueOf(mHighRate.getText().toString());
			break;
		case 1:
			oldVal = Integer.valueOf(mCenterRate.getText().toString());
			break;
		}
		
		new NumberPickerDialog(
				this, 
				new OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						
						switch(i){
						case 0:
							mHighRate.setText(String.valueOf(picker.getValue()));
							break;
						case 1:
							mCenterRate.setText(String.valueOf(picker.getValue()));
							break;
						}
					}
				}, 
				90, // 最大值
				20, // 最小值
				40) // 默认值
		.setCurrentValue(oldVal) // 更新默认值
		.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		audioUtil.stop();
	}

}
