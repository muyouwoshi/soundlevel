package com.tstech.soundlevelinstrument.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimeZone;

import android.util.Log;

/**
 * 获取网络时间工具类
 */
public class NetTimeUtil {

	public static String getNetTime() {
		String time = "";
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));

			URL url = new URL("http://www.bjtime.cn");// 取得资源对象
			URLConnection uc = url.openConnection();// 生成连接对象
			uc.connect(); // 发出连接
			long ld = uc.getDate(); // 取得网站日期时间
			time = ld + "";
			Log.e("temp", "网络时间" + time);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return time;
	}
}
