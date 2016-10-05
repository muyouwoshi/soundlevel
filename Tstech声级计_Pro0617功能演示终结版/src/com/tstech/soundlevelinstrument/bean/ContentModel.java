package com.tstech.soundlevelinstrument.bean;


public class ContentModel {

//	private int imgeView;
	private String text;

	public ContentModel(String text) {
		super();
//		this.imgeView = imgeView;
		this.text = text;
	}

//	public int getImgeView() {
//		return imgeView;
//	}

//	public void setImgeView(int imgeView) {
//		this.imgeView = imgeView;
//	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
