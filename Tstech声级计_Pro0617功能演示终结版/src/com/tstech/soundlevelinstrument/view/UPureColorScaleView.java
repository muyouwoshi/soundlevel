package com.tstech.soundlevelinstrument.view;

import com.tstech.soundlevelinstrument.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class UPureColorScaleView extends View {

	/** 全局画笔 */
	private Paint paint;
	/** 本view宽高 */
	private int height, width;
	/** 边框容器 */
	private Bitmap bmp;
	/** 进度色块 */
	private Bitmap bmpRed, bmpYellow, bmpGreen;
	/** 多元色块 */
	private Bitmap multiColorBmp;
	/** 单色缩放器 */
	private Matrix matrixRed,matrixYellow,matrixGreen;
	
	/** bmp叠加器 */
	private PorterDuffXfermode mFermode;

	public UPureColorScaleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		
	}

	private void init() {
		paint = new Paint();
		paint.setStrokeWidth(1f);
		
		bmp = ((BitmapDrawable) getResources().getDrawable(R.drawable.fmg_record_rate_scale_mark)).getBitmap();

		bmpRed = ((BitmapDrawable) getResources().getDrawable(R.drawable.red)).getBitmap();
		bmpYellow = ((BitmapDrawable) getResources().getDrawable(R.drawable.yellow)).getBitmap();
		bmpGreen = ((BitmapDrawable) getResources().getDrawable(R.drawable.green)).getBitmap();
		
		mFermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int width;
		int height;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width = (int) (getPaddingLeft() + getPaddingRight() + bmp.getWidth());
		}
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = (int) (getPaddingTop() + getPaddingBottom() + bmp.getHeight());
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		width = getWidth();
		height = getHeight();
//		Log.e("ard", "height：" + height);
		
		setMultiColorBmp(10, 10, 10);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	
		int sc = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
	
		Rect rect = new Rect(0, 0, width, height);
		canvas.drawBitmap(bmp, rect, rect, paint); // 已绘制的为dst
	
		paint.setXfermode(mFermode);   // 图像叠加方式
	
	//		Log.e("ard", "彩虹顶x：" + (height - multiColorBmp.getHeight()));
		
		if(null != multiColorBmp){
			canvas.drawBitmap(multiColorBmp, 0, height - multiColorBmp.getHeight(), paint);
		}
		
		paint.setXfermode(null);
	
		canvas.restoreToCount(sc); // 去除黑边
	}

	/**
	 * <b>功能</b>: setMultiColorBmp，<br/>
	 * <b>说明</b>:  <br/>
	 * <b>创建</b>: 2016-1-15_下午4:34:15 <br/>
	 * @param redHeight    像素
	 * @param yellowHeight 像素
	 * @param greenHeight  像素
	 * 
	 * @author : weiyou.cui@ts-tech.com.cn <br/>
	 * @version 1 <br/>
	 */
	private void setMultiColorBmp(float redHeight, float yellowHeight, float greenHeight) {
		Bitmap bmp_red = null, bmp_yellow = null, bmp_green = null;
		int multiHeight = 0;
		
		if(redHeight / 10 > 1){
			matrixRed = new Matrix();
			matrixRed.postScale(50, redHeight / 10); // 水平、垂直，缩放倍数
			bmp_red = Bitmap.createBitmap(bmpRed, 0, 0, 10, 10, matrixRed, true); // (源bmp，裁剪startx，starty，裁剪的宽，高)
			
			multiHeight = multiHeight + bmp_red.getHeight();
		}
		if(yellowHeight / 10 > 1){
			matrixYellow = new Matrix();
			matrixYellow.postScale(50, yellowHeight / 10);
			bmp_yellow = Bitmap.createBitmap(bmpYellow, 0, 0, 10, 10, matrixYellow, true);
			
			multiHeight = multiHeight + bmp_yellow.getHeight();
		}
		if(greenHeight / 10 > 1){
			matrixGreen = new Matrix();
			matrixGreen.postScale(50, greenHeight / 10);
			bmp_green = Bitmap.createBitmap(bmpGreen, 0, 0, 10, 10, matrixGreen, true);
			
			multiHeight = multiHeight + bmp_green.getHeight();
		}

//		Log.e("ard", "总高：" + multiHeight);
		
		if(multiHeight > 0){
			if(null != bmp_green && null == bmp_yellow && null == bmp_red){
				multiColorBmp = Bitmap.createBitmap(width, multiHeight, Config.ARGB_8888);
				Canvas ccc = new Canvas(multiColorBmp);  // 把需要显示的，一起画在画布上
		
				ccc.drawBitmap(bmp_green, 0, 0, null);    // （ 图，绘制位置startx，starty，第四个可以不用管）
			}
			if(null != bmp_yellow && null != bmp_green && null == bmp_red){
				multiColorBmp = Bitmap.createBitmap(width, multiHeight, Config.ARGB_8888);
				Canvas ccc = new Canvas(multiColorBmp); 
		
				ccc.drawBitmap(bmp_yellow, 0, 0, null);
				ccc.drawBitmap(bmp_green, 0, bmp_yellow.getHeight(), null);
			}
			if(null != bmp_green && null != bmp_yellow && null != bmp_red){
				multiColorBmp = Bitmap.createBitmap(width, multiHeight, Config.ARGB_8888);
				Canvas ccc = new Canvas(multiColorBmp); 
		
				ccc.drawBitmap(bmp_red, 0, 0, null);
				ccc.drawBitmap(bmp_yellow, 0, bmp_red.getHeight(), null);
				ccc.drawBitmap(bmp_green, 0, bmp_yellow.getHeight() + bmp_red.getHeight(), null);
			}
			
	//		Log.e("ard", "3色合高：" + multiHeight);
	
			if(null != bmp_red)
				bmp_red.recycle();
			if(null != bmp_yellow)
				bmp_yellow.recycle();
			if(null != bmp_green)
				bmp_green.recycle();
			
		} else {
			multiColorBmp = null;
		}
		
		postInvalidate(); // onDraw();
	}
	
	/**
	 * <b>功能</b>: setMultiColorHeitht，<br/>
	 * <b>说明</b>:  <br/>
	 * <b>创建</b>: 2016-1-15_下午4:32:24 <br/>
	 * @param peak     刻度值。峰值
	 * @param high     刻度值。高频边界
	 * @param center   刻度值。中频边界
	 * @author : weiyou.cui@ts-tech.com.cn <br/>
	 * @version 1 <br/>
	 */
	public void setMultiColorHeitht(float peak, float high, float center){
//		Log.e("ard", "刻度：peak:" + peak + ", high:" + high + ", center:" + center);
		float red = 0, yellow = 0, green = 0;
		
		if(peak < center){
			green = peak;
			yellow = 0;
			red = 0;
		}
		else if(peak > center && peak < high){
			green = center;
			yellow = peak - center;
			red = 0;
		}
		else if(peak > high){
			green = center;
			yellow = high - center;
			red = peak - high;
		}
		
		red = red * height / 100;
		yellow = yellow * height / 100;
		green = green * height / 100;

//		Log.e("ard", "像素：red:" + red + ", yellow:" + yellow + ", green:" + green);
		
		setMultiColorBmp(red, yellow, green);
	}
	
	public void setMultiColorHeitht(float[] peak, float high, float center){
//		Log.e("ard", "自定义控件拿到数组，长度：" + peak.length);
//		for (int i = 0; i < peak.length; i++) {
//			Log.e("ard", "数组元素：" + peak[i]);
//			setMultiColorHeitht(peak[i], high, center);
//		}
		setMultiColorHeitht(peak[1], high, center);
	}
}
