package com.tstech.soundlevelinstrument.task;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimeZone;

import android.os.AsyncTask;

import com.tstech.soundlevelinstrument.back.ITimeBack;

public class TimeTask extends AsyncTask<Void, Void, String>{
	
	private ITimeBack back;

	public TimeTask(ITimeBack back) {
		this.back = back;
	}

	@Override
	protected String doInBackground(Void... params) {
		
		String time = "";
		
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
			
			URL url = new URL("http://www.bjtime.cn");
			URLConnection uc = url.openConnection(); 
			uc.connect(); 
			long ld = uc.getDate(); 

			time = ld + "";
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return time;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		back.getTime(result);
	}
}
