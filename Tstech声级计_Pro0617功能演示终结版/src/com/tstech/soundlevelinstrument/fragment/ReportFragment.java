package com.tstech.soundlevelinstrument.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.adapter.UViewPagerAdapter;
import com.tstech.soundlevelinstrument.back.FileCallBack;
import com.tstech.soundlevelinstrument.back.IKeyBack;
import com.tstech.soundlevelinstrument.util.FileWriter;
import com.tstech.soundlevelinstrument.util.InfoUtil;
import com.tstech.soundlevelinstrument.util.PullConfigXmlUtil;
import com.tstech.soundlevelinstrument.util.ToastUtil;
import com.tstech.soundlevelinstrument.view.RecyclerShowView;
import com.tstech.soundlevelinstrument.viewinterface.BackHandledFragment;

public class ReportFragment extends BackHandledFragment implements IKeyBack,
		OnClickListener {

	private View view, mView1, mView2;
	/** button ImageView 切换 */
	private ImageView mBar;
	/** 查看报告 */
	private ImageButton mSetReport;
	/** 生成报告 */
	private ImageButton mReadReport;
	/** popuWindow 控件 */
	private PopupWindow repPopuWindow;

	private SharedPreferences sp;
	private ViewPager mViewPager;
	/** recyclerView列表 */
	private RecyclerShowView mRecy1, mRecy2;
	private List<View> viewList;
	/** isDocument 是否为生成报告界面 */
	private boolean isDocument = false;
//	private List<String> repList;
	private String itName;
	private String path;
	private Context ctx;

	public ReportFragment() {

	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.report_fragment, container, false);

		mView1 = inflater.inflate(R.layout.recyclershowview_pcm, null);
		mView2 = inflater.inflate(R.layout.recyclershowview_doc, null);
		initView(); // 初始化界面
		initEvent(); // 交互事件处理
		return view;
	}

	/** 实例化界面 */
	private void initView() {

		viewList = new ArrayList<View>();
//		repList = new ArrayList<String>();
		mBar = (ImageView) view.findViewById(R.id.btn_rep);
		// 生成报告
		mSetReport = (ImageButton) view.findViewById(R.id.set_report);
		// 查看报告
		mReadReport = (ImageButton) view.findViewById(R.id.read_report);
		// viewPager
		mViewPager = (ViewPager) view.findViewById(R.id.rep_viewpager);
		mRecy1 = (RecyclerShowView) mView1.findViewById(R.id.repShowView_pcm);
		mRecy2 = (RecyclerShowView) mView2.findViewById(R.id.repShowView_doc);
		// mRecy1.setContentDescription(".pcm");
		// mRecy2.setContentDescription(".doc");
		mRecy1.readDocument(0);
		mRecy2.readDocument(1);
		viewList.add(mRecy1);
		viewList.add(mRecy2);
		UViewPagerAdapter rev = new UViewPagerAdapter(viewList);
		mViewPager.setAdapter(rev);
	}

	/**
	 * 交互事件处理
	 */
	private void initEvent() {
		mSetReport.setOnClickListener(this);
		mReadReport.setOnClickListener(this);
		mRecy2.setOnClickListener(this);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int index) {
				switch (index) {
				case 0:
					if (isDocument)
						isDocument = false;
					mBar.setImageResource(R.drawable.rep_set);
					break;

				case 1:
					mBar.setImageResource(R.drawable.rep_read);
					break;

				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.read_report: // 查看报告
			mViewPager.setCurrentItem(1, false);
			mBar.setImageResource(R.drawable.rep_read);
			break;

		case R.id.set_report: // 生成报告
				mViewPager.setCurrentItem(0, false);
				mBar.setImageResource(R.drawable.rep_set);
			if (!isDocument && mRecy1.isCheckBox()){
				showReportStyle();
				repPopuWindow.showAtLocation(mSetReport, Gravity.CENTER, 0, 0);
			}
			break;

		case R.id.pop_text_report:
			// TODO　生成测试报告
			getDatasFromConfig();
			generateReport();
			repPopuWindow.dismiss();
			break;

		case R.id.pop_warning_report:
			// TODO 生成预警报告
			//
			if(mRecy1.checkBoxLength())
				repPopuWindow.dismiss();
			else
				ToastUtil.showToast(getActivity(), "点击无效");
			break;

		default:
			break;
		}
	}


	/**
	 * 从config.xml获取数据
	 */
	private void getDatasFromConfig() {
		if(mRecy1.isCheckBox()){
			path = mRecy1.getFile()+"/config.xml";
			PullConfigXmlUtil templateParser = new PullConfigXmlUtil(path);
			SharedPreferences preferences = getActivity().getSharedPreferences("config", 0);
			reLoadPreferences(templateParser,preferences);
		}
		
	}

	private void reLoadPreferences(PullConfigXmlUtil templateParser,
			SharedPreferences preferences) {
//		Log.e("ard", "=======templateParser======="+templateParser.getValueMap());
		SharedPreferences.Editor editor = preferences.edit();
		Map<String, String> map = templateParser.getValueMap();
		Set<String> keySet = map.keySet();

		for (String key : keySet) {
			String value = map.get(key);
//			Log.e("ard", "=======key======="+key);
			editor.putString(key, value);
		}
		editor.commit();
	}

	/**
	 * 生成报告
	 * @param datas 
	 */
	private void generateReport() {

		sp = getActivity().getSharedPreferences("config", 0);
		Map<String, String> map = new HashMap<String, String>();
		Log.e("ard", "sp.getString:"+sp.getString("carNumber",""));
//		map.put("&carIntemNum", sp.getString(InfoUtil.SP_CarIntemNum, "null"));
//		map.put("&cRoadNum", sp.getString(InfoUtil.SP_CRoadNum, "null"));
//		// map.put("&startStation", "北京西");
//		// map.put("&endStation", "北京西");
//		// Log.e("temp","InfoUtil.SP_CarNumber:"+
//		// sp.getString(InfoUtil.SP_CarNumber, "null"));
//		map.put("&carNum", sp.getString(InfoUtil.SP_CarNumber, "001A"));
//		map.put("&date", sp.getString(InfoUtil.SP_TestDate, "null"));
//		map.put("&testDistance", sp.getString(InfoUtil.SP_TestDistance, "null"));
//		map.put("&spinAfmileage",
//				sp.getString(InfoUtil.SP_SpinAfmileage, "null"));
//		map.put("&testSpeed", sp.getString(InfoUtil.SP_TestSpeed, "null"));
		map.put("&carIntemNum", sp.getString("carIntemnum",""));
		map.put("&cRoadNum", sp.getString("cRoad", ""));
		map.put("&carNum", sp.getString("carNumber", "001A"));
		map.put("&date", sp.getString("testDate", ""));
		map.put("&testDistance", sp.getString("testDistance", ""));
		map.put("&spinAfmileage",
				sp.getString("spinAfmileage", ""));
		map.put("&testSpeed", sp.getString("testSpeed", ""));

		for (int c = 1; c < 17; c++) {
			map.put("&Number" + c, "" + c);
		}

		for (int i = 1; i < 6; i++) {
			map.put("&f" + i, "1000" + i);
		}

		FileWriter.writeDoc(map); // TODO 修改生成报告填入的数据

	}

	/**
	 * 生成报告类型
	 * @param report 
	 */
	private void showReportStyle() {
		if (null != repPopuWindow && repPopuWindow.isShowing()) {
			repPopuWindow.dismiss();
		}

		// 获取屏幕的宽高
		WindowManager wm = (WindowManager) getActivity().getSystemService(
				Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		// 加载一个自定义的布局
		View contentView = View.inflate(getActivity(), R.layout.popwindow_show,
				null);
		// 设置布局的宽高
		repPopuWindow = new PopupWindow(contentView, (width / 4) * 3, // 400
																		// 必须是具体数字，bug
				height / 5,// LinearLayout.LayoutParams.WRAP_CONTENT,
				true); // 获取焦点

		TextView tv1 = (TextView) contentView
				.findViewById(R.id.pop_text_report);
		TextView tv2 = (TextView) contentView
				.findViewById(R.id.pop_warning_report);

		tv1.setOnClickListener(this);
		tv2.setOnClickListener(this);

		// 不setBackgroundDrawable,点击repPopuWindow外部区域或Back键都无法dismiss
		repPopuWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.table_bg));
		// 设置动画
		repPopuWindow.setAnimationStyle(R.style.AnimationPreview);
		// 设置显示的位置
		repPopuWindow.showAtLocation(mSetReport, Gravity.CENTER, 0, 0);

	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doKeyBack() {
		getActivity().getSupportFragmentManager().beginTransaction().hide(this)
				.commit();
	}

}
