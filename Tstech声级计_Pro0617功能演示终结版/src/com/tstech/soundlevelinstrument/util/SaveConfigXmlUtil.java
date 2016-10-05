package com.tstech.soundlevelinstrument.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.content.Context;
import android.util.Log;

/**
 * 
 * 信息数据保存SaveConfigXmlUtil
 * 
 */
public class SaveConfigXmlUtil {

	public static void saveTemplate(Context context, String dw) {

		String path = context.getApplicationContext().getFilesDir()
				.getAbsolutePath();
		int num = path.lastIndexOf("/");
		String str = path.substring(0, num + 1);
		String oldPath = str + "shared_prefs/config.xml";
		File pf = new File(str.substring(0, str.length()-1));
		for(File strfile : pf.listFiles()){
			if(strfile.getPath().contains("shared")){
				for(String sstr: strfile.list()){
					Log.e("ard", "shared:"+sstr);
				}
			}
		}
		
		
		String fileName = "config";
		String newPath = InfoUtil.getSaveDataPath(dw) +fileName+ ".xml";
		File newfile = new File(newPath);

		if (!newfile.getParentFile().exists()) {
			newfile.getParentFile().mkdirs();
		}

		if (newfile.exists()) {

			ToastUtil.showToast(context,
					"config保存路径为：" + InfoUtil.getSaveDataPath(dw));
			Log.e("ard", "config1:"+newfile);
			return;

		}
		
		Log.e("ard", "config2:"+newfile);
		SaveTo(oldPath, newPath);

//		ToastUtil.showToast(context, "保存路径为：" + InfoUtil.getSaveDataPath(dw));
		// if (savePopWindow != null) {
		// savePopWindow.dismiss();
		// }
	}

	public static void SaveTo(String oldPath, String newPath) {
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			File newfile = new File(newPath);
//			Log.e("ard", "newfile.getParentFile():"+newfile.getParentFile());
			if (!newfile.getParentFile().exists()) {
				newfile.getParentFile().mkdirs();
				Log.e("ard", "config3:"+newfile);
			}
			
//			Log.e("ard", "=======oldfile====="+oldfile);
//			Log.e("ard", "newPath:"+newPath);
//			if (oldfile.exists()) {
				
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
				Log.e("ard", "config4:"+newfile);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
