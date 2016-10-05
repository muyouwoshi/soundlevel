package com.tstech.soundlevelinstrument.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.tstech.soundlevelinstrument.bean.ExpParameter;

import android.content.SharedPreferences;
import android.os.Environment;

public class InfoUtil {
	private static final String SP_NAME = "config";

	/** 采样率 名称 */
	public static final String ACQUI = "acquiFreq";

	/** 频率范围 名称 */
	public static final String FREQRANGE = "freqRange";

	/** 频率分辨率 名称 */
	public static final String FREQRES = "freqRes";

	/** 重叠率名称 */
	public static final String OVERLAP = "overlap";

	/** 平均次数 名称 */
	public static final String AVERAGE = "averageCount";

	/** 加窗名称 */
	public static final String WINDOW = "windowType";

	/** 记权名称 */
	public static final String WEIGHT = "weighting";

	/** 快慢档名称 */
	public static final String SELECT = "select";

	/** 试验信息 */
	/** 检测编号 */
	public static final String SP_TestNumber = "testNumber";
	/** 内容 */
	public static final String SP_Content = "content";
	/** 说明 */
	public static final String SP_Instruction = "instruction";
	/** 姓名 */
	public static final String SP_Name = "name";
	/** 车组号 */
	public static final String SP_CarIntemNum = "carIntemnum";
	/** 车辆号 */
	public static final String SP_CarNumber = "carNumber";
	/** 测点 */
	public static final String SP_TestPoint = "testPoint";
	/** 运营交路及车次 */
	public static final String SP_CRoadNum = "cRoad";
	/** 试验日期 */
	public static final String SP_TestDate = "testDate";
	/** 试验速度 */
	public static final String SP_TestSpeed = "testSpeed";
	/** 运营总里程 */
	public static final String SP_TestDistance = "testDistance";
	/** 旋后里程 */
	public static final String SP_SpinAfmileage = "spinAfmileage";
	/** 车轮直径 */
	public static final String SP_WheelDiameter = "wheelDiameter";
	/** 车箱号 */
	public static final String SP_CarriageNum = "carriage";
	/** 端位号 */
	public static final String SP_DWNum = "duanwei";

	/** 预警设置 */
	/** 总声压级 安全区 */
	public static final String SP_Safearea = "safearea";
	/** 总声压级 预警区 */
	public static final String SP_Warningarea = "warningarea";
	/** 总声压级 高危区 */
	public static final String SP_Dangerarea = "dangerarea";
	/** 主频声压级 安全区 */
	public static final String SP_Frequencysafe = "frequencysafe";
	/** 主频声压级 预警区 */
	public static final String SP_Frequencywarning = "frequencywarning";
	/** 主频声压级 高危区 */
	public static final String SP_Frequencydanger = "frequencydanger";
	/** 主频能量占比 安全区 */
	public static final String SP_Dangersafe = "dangersafe";
	/** 主频能量占比 预警区 */
	public static final String SP_Dangerwarning = "dangerwarning";
	/** 主频能量占比 高危区 */
	public static final String SP_Dangerdanger = "dangerdanger";
//	public static final String SP_SafeCondition = "safeCondition";
	/** 预警条件*/
	public static final String SP_WarningCondition = "warningCondition";
	/** 报警条件*/
	public static final String SP_DangerCondition = "dangerCondition";
	
	/** 监测界面*/
//	/** 显示的分贝数 display*/
//	public static final String SPLDISPLAY = "display";

	public static SharedPreferences getSharedPreferences() {
		return CtxApp.context.getSharedPreferences(SP_NAME, 0);
	}

	public static String getSaveFilePath() { // 获取绝对路径
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/soundlevel";

	}

	/**
	 * 时间需要修改
	 * 
	 * @param flag
	 * @return 数据存储路径
	 */
	public static String getSaveDataPath(String carDw) {
		SharedPreferences sp = getSharedPreferences();
		String parentPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/soundlevel";

		SimpleDateFormat forStr = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CHINA);
		String dateStr = forStr.format(new Date());
		String date = "/" + sp.getString(SP_TestDate, dateStr);
		String car = "/" + sp.getString(SP_CarNumber, "001A");
		String path = parentPath + date + car + carDw + "/";// + "data.pcm";
		File file = new File(path);
		if (!file.exists())file.mkdirs(); // 建立文件夹

		return path;
	}

	public static void saveInfo(String key, String value) {
		getSharedPreferences().edit().putString(key, value).commit();
	}

	public static int getSimpleRate() {
		int simpleRate = Integer.parseInt(getSharedPreferences().getString(
				ACQUI, "" + ExpParameter.DEFAULT_ACQUI));
		return simpleRate;
	}
}
