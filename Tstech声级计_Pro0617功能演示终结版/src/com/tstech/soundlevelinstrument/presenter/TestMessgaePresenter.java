package com.tstech.soundlevelinstrument.presenter;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;

import com.tstech.soundlevelinstrument.bean.ExpTextMessage;
import com.tstech.soundlevelinstrument.util.InfoUtil;
import com.tstech.soundlevelinstrument.viewinterface.TestMessageView;

public class TestMessgaePresenter extends BasePresenter<TestMessageView> {

	private ExpTextMessage message;
	private String dd;

	public TestMessgaePresenter() {
		message = new ExpTextMessage();
		readSaveDatas();
	}

	public void restoreMessage() {
		readSaveDatas();
		setTestMessageToUI(message);
	}

	private void setTestMessageToUI(ExpTextMessage mess) {
		mView.initDatas(mess);
	}

	public void saveMessage() {
		saveDatas(message);
	}


	private void readSaveDatas() {
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		dd = date.format(new Date());
		SharedPreferences sp = InfoUtil.getSharedPreferences();

		message.setTestNum(sp.getString(InfoUtil.SP_TestNumber, ""
				+ ExpTextMessage.def));
		message.setContent(sp.getString(InfoUtil.SP_Content, ""
				+ ExpTextMessage.def));
		message.setInstruction(sp.getString(InfoUtil.SP_Instruction, ""
				+ ExpTextMessage.def));
		message.setName(sp.getString(InfoUtil.SP_Name, "" + ExpTextMessage.def));
		message.setCarIntemnum(sp.getString(InfoUtil.SP_CarIntemNum, ""
				+ ExpTextMessage.def));
		message.setCarNumber(sp.getString(InfoUtil.SP_CarNumber, "001A"));
		message.setTestPoint(sp.getString(InfoUtil.SP_TestPoint, ""
				+ ExpTextMessage.def));
		message.setcRoad(sp.getString(InfoUtil.SP_CRoadNum, ""
				+ ExpTextMessage.def));
		message.setTestDate(sp.getString(InfoUtil.SP_TestDate, dd));// ""+ExpTextMessage.date));
		message.setTestSpeed(sp.getString(InfoUtil.SP_TestSpeed, ""
				+ ExpTextMessage.def));
		message.setTestDistance(sp.getString(InfoUtil.SP_TestDistance, ""
				+ ExpTextMessage.def));
		message.setSpinAfmileage(sp.getString(InfoUtil.SP_SpinAfmileage, ""
				+ ExpTextMessage.def));
		message.setWheelDiameter(sp.getString(InfoUtil.SP_WheelDiameter, ""
				+ ExpTextMessage.def));
	}

	public void resetTestMessage() {
		setTestNum("" + ExpTextMessage.def);
		setContent("" + ExpTextMessage.def);
		setInstruction("" + ExpTextMessage.def);
		setName("" + ExpTextMessage.def);
		setCarIntemnum("" + ExpTextMessage.def);
		setCarNumber("001A");
		setTestPoint("" + ExpTextMessage.def);
		setcRoad("" + ExpTextMessage.def);
		setTestDate("" + dd);// ""+ExpTextMessage.date));
		setTestSpeed("" + ExpTextMessage.def);
		setTestDistance("" + ExpTextMessage.def);
		setSpinAfmileage("" + ExpTextMessage.def);
		setWheelDiameter("" + ExpTextMessage.def);

		setTestMessageToUI(message);
	}

	private void saveDatas(ExpTextMessage message) {
		InfoUtil.getSharedPreferences()
				.edit()
				.putString(InfoUtil.SP_TestNumber, "" + message.getTestNum())
				.putString(InfoUtil.SP_Content, "" + message.getContent())
				.putString(InfoUtil.SP_Instruction,
						"" + message.getInstruction())
				.putString(InfoUtil.SP_Name, "" + message.getName())
				.putString(InfoUtil.SP_CarIntemNum,
						"" + message.getCarIntemnum())
				.putString(InfoUtil.SP_CarNumber, "" + message.getCarNumber())
				.putString(InfoUtil.SP_TestPoint, "" + message.getTestPoint())
				.putString(InfoUtil.SP_CRoadNum, "" + message.getcRoad())
				.putString(InfoUtil.SP_TestDate, "" + message.getTestDate())
				.putString(InfoUtil.SP_TestSpeed, "" + message.getTestSpeed())
				.putString(InfoUtil.SP_TestDistance,
						"" + message.getTestDistance())
				.putString(InfoUtil.SP_SpinAfmileage,
						"" + message.getSpinAfmileage())
				.putString(InfoUtil.SP_WheelDiameter,
						"" + message.getWheelDiameter())
						.commit();

	}

	public void setWheelDiameter(String WheelStr) {
		try {
			message.setWheelDiameter(WheelStr);
		} catch (Exception e) {
			message.setWheelDiameter("");
		}

	}

	public void setSpinAfmileage(String spinAfStr) {
		try {
			message.setSpinAfmileage(spinAfStr);
		} catch (Exception e) {
			message.setSpinAfmileage("");
		}

	}

	public void setTestDistance(String distanceStr) {
		try {
			message.setTestDistance(distanceStr);
		} catch (Exception e) {
			message.setTestDistance("");
		}
	}

	public void setTestSpeed(String speedStr) {
		try {
			message.setTestSpeed(speedStr);
		} catch (Exception e) {
			message.setTestSpeed("");
		}
	}

	public void setTestDate(String date) {
		message.setTestDate(date);
	}

	public void setcRoad(String roasStr) {
		try {
			message.setcRoad(roasStr);
		} catch (Exception e) {
			message.setcRoad("");
		}
	}

	public void setTestPoint(String poiStr) {
		try {
			message.setTestPoint(poiStr);
		} catch (Exception e) {
			message.setTestPoint("");
		}
	}

	public void setCarNumber(String carNum) {
//		try {
			message.setCarNumber(carNum);
//		} catch (Exception e) {
//			message.setCarNumber("");
//		}
	}

	public void setCarIntemnum(String carIntNum) {
		try {
			message.setCarIntemnum(carIntNum);
		} catch (Exception e) {
			message.setCarIntemnum("");
		}
	}

	public void setName(String name) {
		try {
			message.setName(name);
		} catch (Exception e) {
			message.setName("");
		}
	}

	public void setInstruction(String instruction) {
		try {
			message.setInstruction(instruction);
		} catch (Exception e) {
			message.setInstruction("");
		}
	}

	public void setContent(String conStr) {
		try {
			message.setContent(conStr);
		} catch (Exception e) {
			message.setContent("");
		}
	}

	public void setTestNum(String testNumStr) {
		try {
			message.setTestNum(testNumStr);
		} catch (Exception e) {
			message.setTestNum("");
		}
	}
}
