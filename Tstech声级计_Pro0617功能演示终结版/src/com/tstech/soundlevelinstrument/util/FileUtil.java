package com.tstech.soundlevelinstrument.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.FieldsDocumentPart;
import org.apache.poi.hwpf.usermodel.Field;
import org.apache.poi.hwpf.usermodel.Fields;
import org.apache.poi.hwpf.usermodel.Range;

import com.tstech.soundlevelinstrument.R;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * 本地（SD卡）文件操作工具<br/>
 * &lt;uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" /&gt;<br/>
 * &lt;uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /&gt;<br/>
 * &lt;uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /&gt;<br/>
 * @author Administrator
 */
public class FileUtil {
	
	private FileUtil(){}
	
	/**
	 * 检查SD卡是否可用
	 * @return
	 */
	private static boolean isSDCardOK(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}
		return false;
	}

	/**
	 * 存储字符串到内存卡
	 */
	public static void writeTime(String timevalue, String filename){
		if(timevalue == null)
			return;
		if(filename == null)
			return;
		if(!isSDCardOK())
			return;
		
		String jsonPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
//		Log.e("ard", "写路径：" + jsonPath);
		try {
			FileOutputStream out = new FileOutputStream(jsonPath);
			byte[]buffer = timevalue.getBytes();
			out.write(buffer);
			out.flush();
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 提取数据
	 */
	public static String readTime(String filename){
		if(filename == null)
			return null;
		if(!isSDCardOK())
			return null;
		
		String result = null;
		
		String jsonPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
		Log.e("ard", "读路径：" + jsonPath);
		if(new File(jsonPath).exists()){
			try {
				FileInputStream in = new FileInputStream(jsonPath);
				InputStreamReader ir = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(ir);
				StringBuffer sb = new StringBuffer();
				String buffer = null;
				while((buffer = br.readLine()) != null){
					sb.append(buffer);
				}
				result = sb.toString();
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}

}