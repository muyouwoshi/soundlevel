package com.tstech.soundlevelinstrument.adapter;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.util.ToastUtil;

/**
 * RecyclerView 适配器
 * 
 */

public class RecyclerViewAdapter extends
		RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

	private LayoutInflater mInflater;
	private Context mContext;
	/** 文件 */
	private List<String> mlist;
	/** 图标 */
	private List<Integer> mdata;
	/** 用来控制CheckBox的显示状况 */
	private List<Integer> mCbShow;
	/** 用来控制CheckBox的选中状况 */
	private HashMap<Integer, Boolean> mIsChecked;

	public RecyclerViewAdapter(Context context, List<String> list,
			List<Integer> data, List<Integer> cbShow) {
		super();
		this.mContext = context;
		this.mlist = list;
		this.mdata = data;
		this.mCbShow = cbShow;
		mInflater = LayoutInflater.from(context);
		mIsChecked = new HashMap<Integer, Boolean>();

		initData(); // 初始化数据
	}

	/** 初始化 checkbox的状态 */
	public void initData() {
		for (int i = 0; i < mlist.size(); i++) {
			getmIsChecked().put(i, false);
		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
		// 创建一个View
		View view = mInflater.inflate(R.layout.recycle_item, viewGroup, false);
		// 创建一个ViewHolder
		MyViewHolder viewHolder = new MyViewHolder(view);
		return viewHolder;
	}

	@Override
	public int getItemCount() { // 获取数据中要在recyclerView中显示的条目
		return mlist.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// 点击事件
	public interface OnItemClickLitener {
		void onItemClick(View view, int position); // 点击

		void onItemLongClick(View view, int position); // 长按
	}

	private OnItemClickLitener mOnItemClickLitener;

	public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
		this.mOnItemClickLitener = mOnItemClickLitener;
	}

	// checkbox
	public HashMap<Integer, Boolean> getmIsChecked() {
		return mIsChecked;
	}

	public void setmIsChecked(HashMap<Integer, Boolean> mIsChecked) {
		this.mIsChecked = mIsChecked;
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int pos) {
		// 绑定数据到ViewHolder上
		holder.tv.setText(mlist.get(pos));
		holder.iv.setImageResource(mdata.get(pos));

		if (!mlist.get(pos).contains("车")) {
			holder.cb.setVisibility(mCbShow.get(pos));
		} else {
			holder.cb.setVisibility(View.VISIBLE);
		}

		holder.cb.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// boolean checked = ((CheckBox)v).isChecked();
				// ((CheckBox)v).setChecked(checked);
				// Log.e("ard", "mIsChecked.get(pos):"+mIsChecked.get(pos));
				if (mIsChecked.get(pos)) { // !checked
					mIsChecked.put(pos, false);
					setmIsChecked(mIsChecked);
				} else {
					mIsChecked.put(pos, true);
					mCbShow.add(pos);
					setmIsChecked(mIsChecked);
				}
			}
		});

		// 根据isSelected来设置checkbox的选中状况
		// Log.e("ard", "cb：" + getmIsChecked().get(pos));
		holder.cb.setChecked(getmIsChecked().get(pos));

		// 如果设置了回调，则设置点击事件
		if (mOnItemClickLitener != null) {
			holder.itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// int pos = holder.getLayoutPosition();
					if (!mlist.get(pos).contains("车")) { // 限制指定文件夹不能访问
						mOnItemClickLitener.onItemClick(holder.itemView, pos);
						notifyDataSetChanged();
					}
				}
			});

			holder.itemView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {// TODO 点击
					// int pos = holder.getLayoutPosition();
					if (!mlist.get(pos).contains("车")) { // 限制指定文件夹不能访问
						mOnItemClickLitener.onItemLongClick(holder.itemView,
								pos);
						notifyDataSetChanged();
					}
					return false;
				}
			});
		}

	}

	public static class MyViewHolder extends RecyclerView.ViewHolder {
		ImageView iv;
		TextView tv;
		CheckBox cb;

		public MyViewHolder(View view) {
			super(view);

			iv = (ImageView) view.findViewById(R.id.rep_item_icon);
			tv = (TextView) view.findViewById(R.id.rep_item_textview);
			cb = (CheckBox) view.findViewById(R.id.rep_item_checkbox);
		}

	}

}