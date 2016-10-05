package com.tstech.soundlevelinstrument.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.back.ITimeBack;
import com.tstech.soundlevelinstrument.task.TimeTask;
import com.tstech.soundlevelinstrument.util.FileUtil;
import com.tstech.soundlevelinstrument.util.NetStateUtil;

public class WelcomeActivity extends Activity implements ITimeBack {

	private Button btn;	//进入按钮
	private long target;
	private TextView warnning;
	private String filename = "tltime";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		String overtiem = "2016-10-21";		//截止日期
		
		warnning = (TextView) findViewById(R.id.warnning);
		warnning.setText("本应用试用期截止到 " + overtiem);
		
		btn = (Button) findViewById(R.id.wel_btn);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(WelcomeActivity.this, MonitorActivity.class);
				startActivity(i);
				finish();
			}
		});

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(overtiem);
			target = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String netState = NetStateUtil.getNetState();
		if (null != netState && !"".equals(netState)) {
			new TimeTask(this).execute();
		} else {
			getLocalTime();
		}
	}

	private void getLocalTime() {
		
		Date date = new Date();
		long time = date.getTime();
		
		String lasttime = FileUtil.readTime(filename);
		long lt = (null == lasttime || "".equals(lasttime) ? 0 : Long.parseLong(lasttime));
		
		if(0 != lt){
			if(lt > time){
				Toast.makeText(WelcomeActivity.this, "异常状况！请检查系统时间是否正常", 0).show();
			} else {
		
				if(target > time){
					btn.setEnabled(true);
					FileUtil.writeTime(time + "", filename);
				} else {
					Toast.makeText(WelcomeActivity.this, "试用结束，请联系Tstech大友科技", 1).show();
					warnning.setText("试用结束，请联系Tstech大友科技");
				}
			}
		} else {
			if(target > time){
				btn.setEnabled(true);
				FileUtil.writeTime(time + "", filename);
			} else {
				Toast.makeText(WelcomeActivity.this, "试用结束，请联系Tstech大友科技", 1).show();
				warnning.setText("试用结束，请联系Tstech大友科技");
			}
		}
	}

	@Override
	public void getTime(String timestmp) {
		
		if (null != timestmp && !"".equals(timestmp)) {
			
			if(target > Long.parseLong(timestmp)){
				btn.setEnabled(true);
				FileUtil.writeTime(timestmp, filename);
			} else {
				Toast.makeText(WelcomeActivity.this, "试用结束，请联系Tstech大友科技", 1).show();
				warnning.setText("试用结束，请联系Tstech大友科技");
			}
			
		} else {
			getLocalTime();
		}
	}
}
