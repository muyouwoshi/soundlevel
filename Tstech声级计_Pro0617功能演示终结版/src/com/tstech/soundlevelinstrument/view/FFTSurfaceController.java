package com.tstech.soundlevelinstrument.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;

import com.tstech.soundlevelinstrument.algorithm.FFTHelper;

public class FFTSurfaceController extends SurfaceController<float[]> {
	/** 峰值保持 */
	private float[] peakKeepArray;
	private float[] orginalArray;
	private float compareValue = 15.0f;
	private float[] mainFreqs;
	private float[] mainFreqValues;
	private int[] mainFreqIndex;
	private int mainFreqCount = 5;
	private Paint keepPaint,pointPaint;
	private FFTHelper helper;
	
	public FFTSurfaceController(Context context) {
		super(context);
	}

	public FFTSurfaceController(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void init() {
		
		ymultiple = 0.15f;
		xUnitLable = "Hz";
		offset = 2;

		keepPaint = new Paint();
		keepPaint.setStyle(Style.STROKE);
		keepPaint.setColor(Color.YELLOW);
		keepPaint.setStrokeWidth(2);
		pointPaint = new Paint();
		pointPaint.setStrokeWidth(15);
		pointPaint.setColor(Color.GREEN);
		mPaint.setStyle(Style.STROKE);
		
	}

	@Override
	protected void bindHelper() {
		helper = FFTHelper.getInstance();
		helper.addViewQueue(this);
		ybaseLine =(HEIGHT-50)/2;
	}

	@Override
	protected float[] initDataArray() {
		mainFreqs = new float[] { 0, 0, 0, 0, 0 };
		mainFreqValues = new float[] { 0, 0, 0, 0, 0 };
		mainFreqIndex = new int[]{-1,-1,-1,-1,-1};
		return new float[0];
	}

	@Override
	protected void drawLines(Canvas canvas) {
//		 long time = System.currentTimeMillis();
		canvas.save();
		canvas.clipRect(xbaseLine, 0.5f, WIDTH, HEIGHT - 50);

		int length = dataArray.length;
		float px, py, x, y;

		Path p = new Path();
		Path keepP = new Path();

		if (length > 0) {
			getMax(0);
			px = xbaseLine;
			py = ybaseLine - dataArray[0] / ymultiple;
			p.moveTo(px, py);
			py = ybaseLine - peakKeepArray[0] / ymultiple;
			keepP.moveTo(px, py);
		}

		for (int i = 1; i < length - 1; i++) {
			getMainFreq(i);
			getMax(i);
			x = xbaseLine + i * offset;
			y = ybaseLine - dataArray[i] / ymultiple;
			p.lineTo(x, y);

			y = ybaseLine - peakKeepArray[i] / ymultiple;
			keepP.lineTo(x, y);
		}
		canvas.drawPath(keepP, keepPaint);
		drawPoint(canvas);
		canvas.drawPath(p, mPaint);
		canvas.restore();
//		Log.e("bug11", "time:"+(System.currentTimeMillis()-time));
	}

	private void drawPoint(Canvas canvas) {
		for (int i = 0; i < mainFreqCount; i++) {
			if (mainFreqIndex[i] == -1) break;
			int index = mainFreqIndex[i];
			float x = xbaseLine + index * offset;
			float y = ybaseLine - dataArray[index] / ymultiple;
			canvas.drawPoint(x, y, pointPaint);            
		}
	}

	private void getMainFreq(int index) {
		if (index < mainFreqCount - 1)
			return;
		if (dataArray[index - 4] + compareValue > dataArray[index - 2])
			return;
		if (dataArray[index] + compareValue > dataArray[index - 2])
			return;
		if (dataArray[index - 3] > dataArray[index - 2]
				|| dataArray[index - 1] > dataArray[index - 2])
			return;

		int insert = -1;
		for (int k = 0; k < mainFreqCount; k++) {
			if (dataArray[index - 2] > mainFreqValues[k]) {
				insert = k;
				break;
			}
		}

		if (insert != -1) {
			float[] tempValues = new float[mainFreqCount];
			System.arraycopy(mainFreqValues, 0, tempValues, 0, insert);
			System.arraycopy(mainFreqValues, insert, tempValues, insert + 1,
					4 - insert);
			mainFreqValues = tempValues;

			float[] tempFreqs = new float[mainFreqCount];
			System.arraycopy(mainFreqs, 0, tempFreqs, 0, insert);
			System.arraycopy(mainFreqs, insert, tempFreqs, insert + 1,
					4 - insert);
			mainFreqs = tempFreqs;
			
			int[] tempIndex = new int[mainFreqCount];
			System.arraycopy(mainFreqIndex, 0, tempIndex, 0, insert);
			System.arraycopy(mainFreqIndex, insert, tempIndex, insert + 1,
					4 - insert);
			mainFreqIndex = tempIndex;

			mainFreqIndex[insert] = (index - 2);
			mainFreqValues[insert] = dataArray[index - 2];
			mainFreqs[insert] = (index - 2) *offset* xmultiple;
		}
	}

	private void getMax(int index) {
		if (orginalArray[index] > peakKeepArray[index])
			peakKeepArray[index] = orginalArray[index];
		if (dataArray[index] > peakKeepArray[index])
			peakKeepArray[index] = dataArray[index];

	}

	@Override
	public void setData(float[] data) {
		dataArray = data;
		offset = (WIDTH - 50) * 1f / data.length;
		if (isCanvasAlive)
			drawChart();
	}

	public void setOriginalData(float[] data) {
		orginalArray = data;
		if (peakKeepArray == null || peakKeepArray.length == 0)
			peakKeepArray = orginalArray;
	}

	@Override
	public float[] getData() {
		return dataArray;
	}

	public void setXRang(float freqRang) {

		if (freqRang > 24000)
			freqRang = 24000;

		if (freqRang > (WIDTH - xbaseLine)) {
			xmultiple = freqRang / (WIDTH - 50);
			float xgrid = 100 / xmultiple;
			int n = 0;
			while (xmultiple / (int) Math.pow(2, n) >= 1) {
				n += 1;
			}
			xmultiple = (int) Math.pow(2, n);
			xGrid = xmultiple * xgrid;
			xmultiple = freqRang / (WIDTH - 50);
		} else {
			xmultiple = (WIDTH - 50) / freqRang;
			float xgrid = 100 * xmultiple;
			int n = 0;
			while (xmultiple / (int) Math.pow(2, n) > 2) {
				n += 1;
			}
			xmultiple = (int) Math.pow(2, n);

			xGrid = xgrid / xmultiple;
			xmultiple = freqRang / (WIDTH - 50);
		}
		refresh();
	}

	public void clear() {
		peakKeepArray = new float[0];
		dataArray = new float[0];
		orginalArray = new float[0];
		
		mainFreqs = new float[] { 0, 0, 0, 0, 0 };
		mainFreqValues = new float[] { 0, 0, 0, 0, 0 };
		mainFreqIndex = new int[]{-1,-1,-1,-1,-1};
		
		refresh();
	}

	public float[] getMainFreqs() {
		
		float[] freqs = mainFreqs.clone();
		
		mainFreqs = new float[] { 0, 0, 0, 0, 0 };
		return freqs;
		
	}
	
	public float[] getMainFreqValues(){
		float[] values = mainFreqValues.clone();
		mainFreqValues = new float[] { 0, 0, 0, 0, 0 };
		mainFreqIndex = new int[]{-1,-1,-1,-1,-1};
		return values;
	}

	
}
