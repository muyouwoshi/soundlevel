package com.tstech.soundlevelinstrument.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.adapter.RecyclerViewAdapter;

public class DialogUtil {

	public DialogUtil() {
	}

	/**
	 * {@link #createDialog(Context, String, String, String, String, DialogCallBack)}
	 * 
	 * @param context
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param callback
	 *            确认或取消时的回调
	 */
	public static void createDialog(Context context, CharSequence title,
			CharSequence content, DialogCallBack callback) {
		createDialog(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, title,
				content, "确认", "取消", callback);
	}

	/**
	 * {@link #createDialog(Context, String, String, String, String, DialogCallBack)}
	 * 
	 * @param theme
	 *            主题
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param callback
	 *            确认或取消时的回调
	 */
	public static void createDialog(Context context, int theme,
			CharSequence title, CharSequence content, DialogCallBack callback) {
		createDialog(context, theme, title, content, "确认", "取消", callback);
	}

	/**
	 * <功能> 新建Dialog<br/>
	 * 
	 * @param context
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param positive
	 *            确认文字
	 * @param negative
	 *            取消文字
	 * @param callback
	 *            确认或取消时的回调
	 */
	public static void createDialog(Context context, int Theme,
			CharSequence title, CharSequence content, String positive,
			String negative, final DialogCallBack callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context, Theme);

		builder.setTitle(title); // 标题
		builder.setIcon(R.drawable.iconx72); // 图标
		builder.setMessage(content); // 信息

		// 取消按钮
		builder.setNegativeButton(negative,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
						if (callback != null)
							callback.cancle();
					}
				});
		// 确定按钮
		builder.setPositiveButton(positive,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (callback != null)
							callback.confrim();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();

	}

	/**
	 * <功能>确认或取消时的回调 <br/>
	 * {@link #confrim()} 确认操作<br/>
	 * {@link #cancle()} 取消操作 <br/>
	 */
	public interface DialogCallBack {
		public void cancle();

		public void confrim();
	}

	/** 显示对话框 **/
//	public static void createFileListDialog(Context context) {
//		/** 信息加载窗口 */
//		final Dialog builder = new Dialog(context);
//		// 实例化自定义的对话框主题
//		// final Dialog dialog = new Dialog(context, R.style.Dialog);
//		builder.setTitle("请选择信息：");
//		builder.show();
//
//		LayoutInflater inflater = LayoutInflater.from(context);
//		View viewDialog = inflater.inflate(R.layout.dialog, null);
//
//		WindowManager wm = (WindowManager) context
//				.getSystemService(Context.WINDOW_SERVICE);
//		Display display = wm.getDefaultDisplay();
//		int width = display.getWidth();
//		int height = display.getHeight();
//
//		// TODO 设置对话框的宽高
//		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
//				height * 3/5);
//		// LayoutParams layoutParams = new
//		// LayoutParams(LayoutParams.WRAP_CONTENT,
//		// LayoutParams.WRAP_CONTENT);
//		builder.setContentView(viewDialog, layoutParams);
//
//		Button confirm = (Button) viewDialog.findViewById(R.id.dialog_confirm); // 确定
//		Button cancle = (Button) viewDialog.findViewById(R.id.dialog_cancel); // 取消
//
//		// 确定		TODO dialog确定
//		confirm.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				builder.dismiss();
//			}
//		});
//		// 取消
//		cancle.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				builder.dismiss();
//			}
//		});
//
//	}

}
