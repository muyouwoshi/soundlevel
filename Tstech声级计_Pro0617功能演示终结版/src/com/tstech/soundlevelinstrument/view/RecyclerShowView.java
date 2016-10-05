package com.tstech.soundlevelinstrument.view;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.adapter.RecyclerViewAdapter;
import com.tstech.soundlevelinstrument.adapter.RecyclerViewAdapter.OnItemClickLitener;
import com.tstech.soundlevelinstrument.util.CtxApp;
import com.tstech.soundlevelinstrument.util.DividerItemDecoration;
import com.tstech.soundlevelinstrument.util.InfoUtil;
import com.tstech.soundlevelinstrument.util.ToastUtil;

public class RecyclerShowView extends LinearLayout {
	private FilenameFilter mFilter;
	private String fileDescription = ".doc";
	/** 报告显示列表 */
	private RecyclerView mRecycleView;
	/** RecycleView适配器 */
	private RecyclerViewAdapter mAdapter;
	/** 数据集 */
	private List<String> list; // item名称
	private List<Integer> data; // 图标
	private List<Integer> cbShow;// checkbox是否可见
	private List<Boolean> mStatus;
	/** 全选 */
	private CheckBox mSelectAll;
	/** 返回上一目录 */
	private ImageButton mButton;
	/** 记录当前的父文件夹 */
	private File currentParent;
	/** 记录当前路径下的所有文件的文件数组 */
	private File[] currentFiles;
	/** 记录全选中的条目数量 */
	private int checkNum;
	private boolean isDoc = false;
	private List<String> repList;

	// public RecyclerShowView(Context context, AttributeSet attrs, int
	// defStyle) {
	// super(context, attrs, defStyle);
	//
	// }

	public RecyclerShowView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.recycler_view, this);

		mRecycleView = (RecyclerView) findViewById(R.id.recyclerView);
		mSelectAll = (CheckBox) findViewById(R.id.select_all);
		mButton = (ImageButton) findViewById(R.id.to_back);

		// 创建一个线性布局管理器
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
				context);
		// 默认是Vertical
		linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecycleView.setLayoutManager(linearLayoutManager);
		// 添加分割线
		mRecycleView.addItemDecoration(new DividerItemDecoration(context,
				DividerItemDecoration.VERTICAL_LIST));
		// 设置Item项的不同操作的动画
		mRecycleView.setItemAnimator(new DefaultItemAnimator());
		// 创建数据集
		list = new ArrayList<String>(); // TODO 更换数据 放入获取的word资源
		data = new ArrayList<Integer>();
		cbShow = new ArrayList<Integer>();
		repList = new ArrayList<String>();
		mFilter = new DirectoryFilter();

		// 判断/mnt/sdcard目录是否存在
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) { // SD卡正常挂载，可读写改，新建文件夹
			// 获得文件路径
			currentParent = new File(InfoUtil.getSaveFilePath());
			currentFiles = currentParent.listFiles();// 读取
			getFileName(currentFiles);

		}

		// 创建Adapter，并指定数据集
		mAdapter = new RecyclerViewAdapter(context, list, // 文字
				data, // 图标
				cbShow); // 是否显示checkedbox
		// 设置Adapter
		mRecycleView.setAdapter(mAdapter);
		initEvent();
	}

	private void initEvent() {
		mAdapter.setOnItemClickLitener(new OnItemClickLitener() {

			@Override
			public void onItemClick(View view, int position) {
				String parm = null;
				TextView text = (TextView) view.findViewById(R.id.rep_item_textview);
				if(text!=null) parm = text.getText().toString();
				// parm = currentFiles[position].getAbsolutePath();
				parm = currentParent.getAbsolutePath() + "/"+ parm;
				File file  = new File(parm);
				
				if (file.isFile()) {
					if (file.getName().endsWith(".doc")) {
						// Log.e("ard",
						// "=============="+currentFiles[position].getAbsolutePath());
						// TODO 打开文件
						Intent intent = getWordFileIntent(parm);
						if (isIntentAvailable(CtxApp.context, intent))
							CtxApp.context.startActivity(intent);
						else
							ToastUtil.showToast(CtxApp.context, "请安装WPSoffice");

					}
					return;
				}

				File[] temp = file.listFiles();

				if (temp == null || temp.length == 0) {
					ToastUtil.showToast(CtxApp.context, "当前路径不可访问或该路径下没有文件");

				} else {
					// 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
					currentParent = file;
					// 保存当前父文件夹内的全部文件和文件夹
					currentFiles = temp;
					// 再次更新recyclerView
					clearList();
					if (!isDoc)
						getFileName(currentFiles);
					else
						getDocument(currentFiles);

					mAdapter.initData();
					mAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onItemLongClick(View view, int position) {

			}

		});

		// 返回上一目录
		mButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (!currentParent.getCanonicalPath().equals(
							InfoUtil.getSaveFilePath())) {
						// 获取上一级目录
						currentParent = currentParent.getParentFile();
						// 列出当前目录下的所有文件
						currentFiles = currentParent.listFiles();
						// 清空RecyclerView
						clearList();
						// 重新填充RecyclerView
						if (!isDoc)
							getFileName(currentFiles);
						else
							getDocument(currentFiles);
						// 重置checkbox
						resetCheckbox();
						mAdapter.notifyDataSetChanged();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// 全选监听事件
		mSelectAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (list.size() != 0) { // 判断列表中是否有数据
					if (isChecked) {
						for (int i = 0; i < list.size(); i++) {
							mAdapter.getmIsChecked().put(i, true);
						}
					} else {
						for (int i = 0; i < list.size(); i++) {
							mAdapter.getmIsChecked().put(i, false);
						}
					}
				} else {
					mSelectAll.setVisibility(View.GONE);
				}
				// 数量设为list的长度
				checkNum = list.size();
				mAdapter.notifyDataSetChanged();
			}
		});

	}

	private void getDocument(File[] files) {
		if (files != null) {// 先判断目录是否为空，否则会报空指针
			for (File file : files) {
				String fileName = file.getName();
				// 设置图标,checkbox是否显示
				if (file.isDirectory() && !fileName.contains("车")) {
					data.add(R.drawable.rep_folder); // TODO 改图标
					mSelectAll.setVisibility(View.INVISIBLE);
					cbShow.add(View.INVISIBLE); // 不可见
					list.add(fileName);
				} else {
					if (fileName.endsWith("doc")) {// TODO Word过滤 bug
						data.add(R.drawable.doc);
						list.add(fileName);
						// Log.e("ard", "list:"+list);
						// } else {
						// data.add(R.drawable.text);
					}
					mSelectAll.setVisibility(View.INVISIBLE);
					cbShow.add(View.INVISIBLE); // 不可见
				}
			}
		}
	}

	/**
	 * 获取"日期/车号"目录下的所有"--车--端位"文件名称
	 * 
	 * @param files
	 */
	private void getFileName(File[] files) {
		if (files != null) {// 先判断目录是否为空，否则会报空指针
			for (File file : files) {
				String fileName = file.getName();
				// Log.e("show", "fileName :" + fileName);
				// 设置图标,checkbox是否显示
				if (file.isDirectory() && !fileName.contains("车")) {
					data.add(R.drawable.rep_folder); // TODO 改图标
					mSelectAll.setVisibility(View.INVISIBLE);
					cbShow.add(View.INVISIBLE); // 不可见
					list.add(fileName);
				} else {
					if (fileName.endsWith(".doc")) {
						// data.add(R.drawable.doc);
					} else {
						data.add(R.drawable.text); // TODO 改图标
						list.add(fileName);
					}
					mSelectAll.setVisibility(View.VISIBLE);
					cbShow.add(View.VISIBLE); // 可见
				}
				// // if(fileName.contains(fileDescription))
				// // list.add(fileName);
				// // if (mFilter.accept(file, fileDescription))
				// list.add(fileName);
				// // file.list(mFilter);
			}
		}
	}

	/** clear RecyclerView */
	private void clearList() {
		list.clear();
		data.clear();
		cbShow.clear();
	}

	/** 重置checkbox状态 */
	private void resetCheckbox() {
		mSelectAll.setChecked(false);
		for (int i = 0; i < list.size(); i++) {
			mAdapter.getmIsChecked().put(i, false);
		}
	}

	/** 获取一个用于打开Word文件的intent */
	public Intent getWordFileIntent(String param) {
		Intent intent = null;
		try {
			// Log.e("ard", "param" + param);
			intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Uri uri = Uri.fromFile(new File(param));
			intent.setDataAndType(uri, "application/msword");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return intent;

	}

	/**
	 * 判断intent是否存在
	 * 
	 * @param context
	 * @param intent
	 * @return
	 */
	private boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.GET_ACTIVITIES);
		return list.size() > 0;
	}

	/** 获取当前的item的文件名称 */
	public String getFile() {
		String cbPath = null;
		for (int i = 0; i < cbShow.size(); i++) {
//			 cbPath = currentFiles[cbShow.get(i)].getAbsolutePath();
			cbPath = currentParent.getAbsolutePath() + "/"
					+ list.get(cbShow.get(i));		//TODO　bug　空针
		}
		return cbPath;
	}

	/** 获取当前选择的checkbox*/
	public boolean isCheckBox() {
		for (int i = 0; i < mAdapter.getmIsChecked().size(); i++) {
			Log.e("ard", "tag:"+mAdapter.getmIsChecked().get(i));
			if(mAdapter.getmIsChecked().get(i)){
				repList.add(String.valueOf(mAdapter.getmIsChecked().get(i)));
				return true;
			}
		}
		return false;
		
	}
	
	public boolean checkBoxLength() {
		for (int i = 0; i < repList.size(); i++) {
			Log.e("ard", "repList:"+repList.get(i));
			if(repList.size() == 1){
				return true;
			}
		}
		return false;
	}
	
	
	public boolean readDocument(int flag) {
		if (flag == 0) {
			isDoc = false;
		} else if (flag == 1) {
			isDoc = true;
		}
		return false;

	}

	class DirectoryFilter implements FilenameFilter {

		@Override
		public boolean accept(File file, String description) {
			if (file.getName().endsWith(".doc"))
				return true;
			return false;
		}

	}

	public void setFilter(FilenameFilter filter) {
		mFilter = filter;
	}
}
