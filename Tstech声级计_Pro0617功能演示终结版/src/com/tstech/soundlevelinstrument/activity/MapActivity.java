package com.tstech.soundlevelinstrument.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tstech.soundlevelinstrument.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapActivity extends Activity {

	// 定位相关的
	private TextView address;
	private LocationClient locationClient = null;
	private ImageView refurbish;
	private EditText write_in;
	private LinearLayout lng_city_lay;
	private ImageView imgback;
	private String lngCityName = "";// 存放返回的城市名
	private String addString = "";	//存放手动输入的地址
	private Button write_but;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		initView();
		initEvent();
		initGps();

	}

	private void initView() {
		imgback = (ImageView) findViewById(R.id.imgback);
		write_in = (EditText) findViewById(R.id.intern);
		lng_city_lay = (LinearLayout) findViewById(R.id.lng_city_lay);
		address = (TextView) findViewById(R.id.lng_city);
		write_but = (Button) findViewById(R.id.write_button);
	}

	private void initEvent() {
		//gps定位获取地址
		lng_city_lay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("addressName", lngCityName);
				setResult(666, intent);
				finish();
			}
		});
		
		//返回上一个界面
		imgback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		//手动输入地址
		write_but.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addString = write_in.getText().toString();
				Intent intent = new Intent();
				intent.putExtra("addressName", addString);
				setResult(666, intent);
				finish();
			}
		});

	}

	/**
	 * gps定位
	 */
	private void initGps() {
//		try {
			MyLocationListenner myListener = new MyLocationListenner();
			locationClient = new LocationClient(this);
			locationClient.registerLocationListener(myListener);
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);
			option.setAddrType("all");
			option.setCoorType("bd09ll");
			option.setScanSpan(5000);
			option.disableCache(true);
			// option.setPoiNumber(5);
			// option.setPoiDistance(1000);
			// option.setPoiExtraInfo(true);
			option.setPriority(LocationClientOption.GpsFirst);
			locationClient.setLocOption(option);
			locationClient.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		locationClient.stop();
	}

	private class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {

			if (location == null)
				return;
			StringBuffer sb = new StringBuffer(256);
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				 sb.append(location.getAddrStr());
//				sb.append(location.getCity());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append(location.getAddrStr());
				// sb.append(location.getLocType());
			}
			if (sb.toString() != null && sb.toString().length() > 0) {
				lngCityName = sb.toString();
				address.setText(lngCityName);
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {

		}
	}

}
