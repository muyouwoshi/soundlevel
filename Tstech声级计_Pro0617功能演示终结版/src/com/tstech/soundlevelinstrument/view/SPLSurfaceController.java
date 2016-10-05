package com.tstech.soundlevelinstrument.view;

import java.util.ArrayList;

import com.tstech.soundlevelinstrument.algorithm.SPLHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;

public class SPLSurfaceController extends SurfaceController<ArrayList<Double>> {

	public SPLSurfaceController(Context context) {
		super(context);
	}
	
	public SPLSurfaceController(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	
	@Override
	protected void init() {
		ymultiple = 0.15f;
		xmultiple = 2f;
	}
	
	@Override
	protected void bindHelper() {
		SPLHelper.getInstance().addViewQueue(this);
	}

	
	
	@Override
	protected void drawXLable(Canvas canvas) {
		changeX = SPLHelper.getInstance().getChangeX();
		super.drawXLable(canvas);
	}

	@Override
	protected void drawLines(Canvas canvas) {
		canvas.save();
		canvas.clipRect(xbaseLine, 0, WIDTH, HEIGHT - 50);

		int size = dataArray.size();
		float px, py, x, y;
		
		canvas.save();
		canvas.clipRect(xbaseLine, 0, WIDTH, HEIGHT - 50);

		mPaint.setStyle(Style.STROKE);

		Path p = new Path();
		if (size > 0) {
			px = xbaseLine;
			py = (float) (ybaseLine - dataArray.get(0) / ymultiple);
			p.moveTo(px, py);
		}

		for (int i = 1; i < size; i++) {
			Log.e("bug11", "data:"+dataArray.get(i));
			x = xbaseLine + i * offset;
			y = (float) (ybaseLine - dataArray.get(i) / ymultiple);
			p.lineTo(x, y);
		}
		canvas.drawPath(p, mPaint);
		canvas.restore();
		
		canvas.restore();
	}

	@Override
	public void setData(ArrayList<Double> data) {
		dataArray = data;
		refresh();
	}

	@Override
	protected ArrayList<Double> initDataArray() {
		return new ArrayList<Double>();
	}

	@Override
	public ArrayList<Double> getData() {
		return dataArray;
	}
	
}
