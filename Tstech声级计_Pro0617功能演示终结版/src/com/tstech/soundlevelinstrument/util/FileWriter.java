package com.tstech.soundlevelinstrument.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

public class FileWriter {
	private OutputStream mOutputStream = null;
	private BufferedOutputStream mBufferedOutputStream = null;
	private DataOutputStream mDataOutputStream = null;

	/**
	 * <功能> 准备写入文件
	 * 
	 * @param FilePath
	 *            文件名称
	 */
	public void prepare(@NonNull String FilePath) {
		File file = new File(FilePath);
		if (file.exists())
			file.delete();
		try {
			file.createNewFile();
			mOutputStream = new FileOutputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBufferedOutputStream = new BufferedOutputStream(mOutputStream);
		mDataOutputStream = new DataOutputStream(mBufferedOutputStream);
	}

	/**
	 * 存储文件
	 */
	public void writeToFile(short data) {
		try {
			mDataOutputStream.writeShort(data);
		} catch (IOException e) {
			release();
			e.printStackTrace();
		}
	}

	/**
	 * 关闭输出流
	 */
	public void release() {
		try {
			if (mDataOutputStream != null) {
				mDataOutputStream.close();
				mDataOutputStream = null;
			}
			if (mBufferedOutputStream != null) {
				mBufferedOutputStream.close();
				mBufferedOutputStream = null;
			}
			if (mOutputStream != null) {
				mOutputStream.close();
				mOutputStream = null;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 生成doc文件
	 * 
	 * @param map
	 */
	public static void writeDoc(Map<String, String> map) {
		try {
			// 读取word模板
			Resources res = CtxApp.context.getResources();
			InputStream in = res.openRawResource(R.raw.dmu); // 加载模板
			HWPFDocument hdt = new HWPFDocument(in);
			Fields fields = hdt.getFields();
			Iterator<Field> it = fields.getFields(FieldsDocumentPart.MAIN)
					.iterator();
			while (it.hasNext()) {
//				Log.e("temp", "it.next().getType()" + it.next().getType());
			}

			// 读取word文本内容
			Range range = hdt.getRange();
//			Log.e("temp", "模板：rang.text():" + range.text());

			// 替换文本内容
			for (Map.Entry<String, String> entry : map.entrySet()) {
				range.replaceText(entry.getKey(), entry.getValue());
			}

			ByteArrayOutputStream ostream = new ByteArrayOutputStream();

			// 获取当前日期----设置doc文件名称
			SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fileName = "" + date.format(System.currentTimeMillis());
			fileName += ".doc";
			FileOutputStream out = new FileOutputStream(
					InfoUtil.getSaveDataPath("")+"/" + fileName, true);
			Toast.makeText(CtxApp.context,
					"成功保存到" + InfoUtil.getSaveDataPath("") + "目录下",
					Toast.LENGTH_LONG).show();
			hdt.write(ostream);
//			Log.e("temp", "写入后：rang.text():" + range.text());

			// 输出字节流
			out.write(ostream.toByteArray());
			out.close();
			ostream.close();
		} catch (IOException e) {
//			Log.e("temp", "--------------");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
}
