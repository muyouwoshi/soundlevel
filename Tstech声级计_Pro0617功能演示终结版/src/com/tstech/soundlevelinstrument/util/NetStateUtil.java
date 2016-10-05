package com.tstech.soundlevelinstrument.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

/**
 * 网络状态判断工具<br/>
 * &lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/&gt;
 */
public class NetStateUtil {
	private NetStateUtil(){}

	public static String getNetState(){
		
		// 连接管理器-连接状态服务
		ConnectivityManager manager = (ConnectivityManager) CtxApp.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(null == manager)
			return null;
		
		// Wifi网络信息对象
		NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(null != wifiInfo){
			// 状态对象
			State state = wifiInfo.getState();
			if(state != null){
				if(state.equals(State.CONNECTED)){
					//wifi可用
					return "wifi";
				}
			}
		}
		
		// 移动网络
		NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(null != mobileInfo){
			State state = mobileInfo.getState();
			if(null != state){
				if(state.equals(State.CONNECTED)){
					return "mobile";
				}
			}
		}
		
		return null;
	}

}
