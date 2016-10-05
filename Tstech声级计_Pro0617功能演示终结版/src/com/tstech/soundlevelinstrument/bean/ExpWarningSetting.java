package com.tstech.soundlevelinstrument.bean;

public class ExpWarningSetting {
	public final static String str = "";
	public final static String safe = "45";
	public final static String warn = "80";
	public final static String danger = "95";

	/** 总声压级 */
	private String safearea = str;
	private String warningarea = str;
	private String dangerarea = str;

	/** 主频声压级 */
	private String frequencysafe = str;
	private String frequencywarning = str;
	private String frequencydanger = str;

	/** 主频能量占比 */
	private String dangersafe = str;
	private String dangerwarning = str;
	private String dangerdanger = str;
	
//	private String safeCondition = "A|(B&C)";
	private String warningCondition = "A|(B&C)";
	private String dangerCondition ="A|(B&C)";

	public String getSafearea() {
		return safearea;
	}

	public void setSafearea(String safearea) {
		this.safearea = safearea;
	}

	public String getWarningarea() {
		return warningarea;
	}

	public void setWarningarea(String warningarea) {
		this.warningarea = warningarea;
	}

	public String getDangerarea() {
		return dangerarea;
	}

	public void setDangerarea(String dangerarea) {
		this.dangerarea = dangerarea;
	}

	public String getFrequencysafe() {
		return frequencysafe;
	}

	public void setFrequencysafe(String frequencysafe) {
		this.frequencysafe = frequencysafe;
	}

	public String getFrequencywarning() {
		return frequencywarning;
	}

	public void setFrequencywarning(String frequencywarning) {
		this.frequencywarning = frequencywarning;
	}

	public String getFrequencydanger() {
		return frequencydanger;
	}

	public void setFrequencydanger(String frequencydanger) {
		this.frequencydanger = frequencydanger;
	}

	public String getDangersafe() {
		return dangersafe;
	}

	public void setDangersafe(String dangersafe) {
		this.dangersafe = dangersafe;
	}

	public String getDangerwarning() {
		return dangerwarning;
	}

	public void setDangerwarning(String dangerwarning) {
		this.dangerwarning = dangerwarning;
	}

	public String getDangerdanger() {
		return dangerdanger;
	}

	public void setDangerdanger(String dangerdanger) {
		this.dangerdanger = dangerdanger;
	}

//	public String getSafeCondition() {
//		return safeCondition;
//	}
//
//	public void setSafeCondition(String safeCondition) {
//		this.safeCondition = safeCondition;
//	}

	public String getWarningCondition() {
		return warningCondition;
	}

	public void setWarningCondition(String warningCondition) {
		this.warningCondition = warningCondition;
	}

	public String getDangerCondition() {
		return dangerCondition;
	}

	public void setDangerCondition(String dangerCondition) {
		this.dangerCondition = dangerCondition;
	}



}
