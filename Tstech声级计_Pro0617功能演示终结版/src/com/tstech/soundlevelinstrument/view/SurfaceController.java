package com.tstech.soundlevelinstrument.view;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.tstech.soundlevelinstrument.R;

public abstract class SurfaceController<T> extends LinearLayout implements
		SurfaceHolder.Callback {
	protected T dataArray;
	protected int WIDTH;
	protected int HEIGHT;
	protected SurfaceView mSurfaceView;
	protected SurfaceHolder mSurfaceHolder;
	protected float offset = 2;
	protected Paint mPaint;
	protected float xGrid = 150;
	protected float yGrid = 100;
	protected float xbaseLine = 50;
	protected float ybaseLine;
	protected float yaxisLeft = 10;
	protected DecimalFormat xdf = new DecimalFormat("###0.000");
	protected DecimalFormat ydf = new DecimalFormat("###0.00");
	protected Paint CoordinatePaint;
	protected Paint LablePaint;
	protected int axisTextSize = 18;
	protected float xaxisbottom = 20;
	protected int backColor;
	protected String xUnitLable = "ms";
	protected String yUnitLable = "dB";
	protected boolean isCanvasAlive;

	protected float ymultiple = 0.2f;
	protected float xmultiple = 1f;

	protected float changeX;

	public SurfaceController(Context context) {
		this(context, null);
	}

	public SurfaceController(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.surface_contain, this);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		dataArray = initDataArray();
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2);
		CoordinatePaint = new Paint();
		CoordinatePaint.setColor(Color.rgb(234, 234, 234));
		LablePaint = new Paint();
		LablePaint.setColor(Color.rgb(64, 64, 64));
		backColor = Color.WHITE;
		
		init();
	}
	
	protected abstract void init();

	protected abstract T initDataArray();

	protected void drawYLable(Canvas canvas) {

		int upCount = (int) ((ybaseLine) / yGrid);
		int downCount = (int) ((HEIGHT - 50 - ybaseLine) / yGrid);

		for (float i = 0; i <= upCount; i++) {
			float ypos = ybaseLine - i * yGrid;
			if (ypos > 55 && ypos < HEIGHT - 50) {
				String text = null;
				text = ydf.format(i * yGrid * ymultiple);
				canvas.drawText(text, yaxisLeft, ypos, LablePaint);
			}
		}
		canvas.drawText(yUnitLable, yaxisLeft, 25, LablePaint);
		for (float i = 1; i <= downCount; i++) {

			float ypos = ybaseLine + i * yGrid;
			if (ypos > 55 && ypos <= HEIGHT - axisTextSize) {
				String text = null;

				text = "-" + ydf.format(i * yGrid * ymultiple);
				canvas.drawText(text, yaxisLeft, ypos, LablePaint);
			}
		}

	}

	protected void drawXLable(Canvas canvas) {

		int rightCount = (int) ((WIDTH - 50 - xbaseLine) / xGrid);

		for (float i = 0; i <= rightCount; i++) {
			float xpos = xbaseLine + i * xGrid;
			if (xpos >= xbaseLine && xpos < WIDTH - 60) {
				String text = null;
				text = xdf.format(xGrid * i*xmultiple + changeX);

				canvas.drawText(text, xpos - 25, canvas.getHeight()
						- xaxisbottom, LablePaint);
			}
		}
		canvas.drawText(xUnitLable, WIDTH - 25, HEIGHT - xaxisbottom, LablePaint);
	}

	protected void drawYLine(Canvas canvas) {
		int upCount = (int) ((ybaseLine) / yGrid);
		int downCount = (int) ((HEIGHT - 50 - ybaseLine) / yGrid);

		canvas.drawLine(xbaseLine, 0, xbaseLine, HEIGHT - 50, CoordinatePaint);

		for (float i = 0; i <= upCount; i++) {
			float ypos = ybaseLine - i * yGrid;
			if (ypos > 0 && ypos < HEIGHT - 50) {
				canvas.drawLine(xbaseLine, ypos, WIDTH, ypos, CoordinatePaint);
			}
		}
		for (float i = 1; i <= downCount; i++) {
			float ypos = ybaseLine + i * yGrid;
			if (ypos > 0 && ypos <= HEIGHT - 50) {
				canvas.drawLine(xbaseLine, ypos, WIDTH, ypos, CoordinatePaint);
			}
		}

	}

	protected void drawXLine(Canvas canvas) {
		canvas.drawLine(xbaseLine, HEIGHT - 51, WIDTH, HEIGHT - 51,
				CoordinatePaint);
		int rightCount = (int) ((WIDTH - 50 - xbaseLine) / xGrid);

		for (float i = 0; i <= rightCount + 1; i++) {

			float xpos = xbaseLine + i * xGrid;
			if (xpos >= xbaseLine && xpos < WIDTH) {
				canvas.drawLine(xpos, 0, xpos, HEIGHT - 50, CoordinatePaint);
			}
		}

	}

	protected abstract void drawLines(Canvas canvas);

	protected void drawChart() {
		synchronized (mSurfaceHolder) {
			Canvas canvas = mSurfaceHolder.lockCanvas();

			drawBackground(canvas);
			drawYLable(canvas);
			drawXLable(canvas);
			drawYLine(canvas);
			drawXLine(canvas);
			drawLines(canvas);
			mSurfaceHolder.unlockCanvasAndPost(canvas);
		}
	}

	protected void drawBackground(Canvas canvas) {
		Paint bPaint = new Paint();
		bPaint.setStrokeWidth(2);
		bPaint.setStyle(Style.STROKE);
		bPaint.setColor(Color.GRAY);
		canvas.drawColor(backColor);
		canvas.drawRect(new Rect(0, 0, WIDTH, HEIGHT), bPaint);
	}
	
	/**
	 * 两种设置数据的方式<br/>
	 * 此方法针对绘图数据为数组的情况使用<br>
	 * 针对数据为集合的情况使用{@link #getData()}+{@link #refresh()}
	 * @param data
	 * 设置更新需要的数据<br>
	 */
	public abstract void setData(T data);
	
	/**
	 * 两种设置数据的方式<br/>
	 * 此方法+{@link #refresh()}针对绘图数据为集合的情况使用<br>
	 * 针对数据为数组的情况使用{@link #setData(T data)}
	 * 设置更新需要的数据<br>
	 */
	public abstract T getData();
	public void refresh(){
		
		if(isCanvasAlive) drawChart();
	}
	

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	public int getMaxCount() {
		return (int) ((WIDTH - 50) / offset+0.5);
	}
	protected abstract void bindHelper();

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		isCanvasAlive = false;
		WIDTH = width;
		HEIGHT = height;
		ybaseLine = (height - 50);
		if (width * height > 0) {
			isCanvasAlive = true;
			bindHelper();
			drawChart();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isCanvasAlive = false;
	}
	
}
