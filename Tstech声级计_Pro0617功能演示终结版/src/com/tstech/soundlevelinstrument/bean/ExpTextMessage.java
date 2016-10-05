package com.tstech.soundlevelinstrument.bean;

public class ExpTextMessage {
	/** 默认*/
	public final static String date = "2016-08-23";
	public final static String car = "其他";
	public final static String def ="";
	
	
	/** 检测编号 */
	private String testNum = def;
	/** 内容 */
	private String content = def;
	/** 说明 */
	private String instruction = def;
	/** 姓名 */
	private String name = def;
	/** 车组号 */
	private String carIntemnum = def;
	/** 车辆号 */
	private String carNumber = car;
	/** 测点 */
	private String testPoint = def;
	/** 运营交路及车次 */
	private String cRoad = def;
	/** 试验日期 */
	private String testDate = date;
	/** 试验速度 */
	private String testSpeed = def;
	/** 运营总里程 */
	private String testDistance = def;
	/** 旋后里程 */
	private String spinAfmileage = def;
	/** 车轮直径 */
	private String wheelDiameter = def;
	


	public String getTestNum() {
		return testNum;
	}

	public void setTestNum(String testNum) {
		this.testNum = testNum;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCarIntemnum() {
		return carIntemnum;
	}

	public void setCarIntemnum(String carIntemnum) {
		this.carIntemnum = carIntemnum;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public String getTestPoint() {
		return testPoint;
	}

	public void setTestPoint(String testPoint) {
		this.testPoint = testPoint;
	}

	public String getcRoad() {
		return cRoad;
	}

	public void setcRoad(String cRoad) {
		this.cRoad = cRoad;
	}

	public String getTestDate() {
		return testDate;
	}

	public void setTestDate(String testDate) {
		this.testDate = testDate;
	}

	public String getTestSpeed() {
		return testSpeed;
	}

	public void setTestSpeed(String testSpeed) {
		this.testSpeed = testSpeed;
	}

	public String getTestDistance() {
		return testDistance;
	}

	public void setTestDistance(String testDistance) {
		this.testDistance = testDistance;
	}

	public String getSpinAfmileage() {
		return spinAfmileage;
	}

	public void setSpinAfmileage(String spinAfmileage) {
		this.spinAfmileage = spinAfmileage;
	}

	public String getWheelDiameter() {
		return wheelDiameter;
	}

	public void setWheelDiameter(String wheelDiameter) {
		this.wheelDiameter = wheelDiameter;
	}

}
