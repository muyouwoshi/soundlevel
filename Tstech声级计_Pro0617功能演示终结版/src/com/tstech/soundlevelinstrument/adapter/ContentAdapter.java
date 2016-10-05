package com.tstech.soundlevelinstrument.adapter;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.bean.ContentModel;

/**
 * 侧滑菜单
 */
public class ContentAdapter extends BaseAdapter  {
	private Context context;
	private List<ContentModel> list;
//	private FragmentManager fm;


	public ContentAdapter(Context context) {
		super();
		this.context = context.getApplicationContext();

//		fm = ((FragmentActivity)context).getSupportFragmentManager();
		initData();
	}

	@Override
	public int getCount() {

		if (list != null) {
			return list.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (list != null) {
			return list.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold hold;
		if (convertView == null) {
			hold = new ViewHold();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.menu_item, null);
			convertView.setTag(hold);
		} else {
			hold = (ViewHold) convertView.getTag();
		}

//		hold.imageView = (ImageView) convertView
//				.findViewById(R.id.item_imageview);
		hold.textView = (TextView) convertView.findViewById(R.id.item_textview);

//		hold.imageView.setImageResource(list.get(position).getImgeView());
		hold.textView.setText(list.get(position).getText());

		return convertView;
	}

	static class ViewHold {
//		public ImageView imageView;
		public TextView textView;
	}

//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position,
//			long id) {
//		FragmentTransaction fragmentTransation = fm.beginTransaction();
//		// 根据item点击行号判断启用指定Fragment
//		switch (position) {
//		case 0: // 标定
//			
//			fm.beginTransaction().show(fm.findFragmentByTag("calibration")).commit();
//			break;
//
//		case 1:	//实验信息
//			fm.beginTransaction().show(fm.findFragmentByTag("testinfo")).commit();
//			break;
//			
//		case 2: // 采集设置
//			fm.beginTransaction().show(fm.findFragmentByTag("collectinfo")).commit();
//			break;
//			
//		case 3: // 预警设置
//			fm.beginTransaction().show(fm.findFragmentByTag("warninginfo")).commit();
//			break;
//			
//		case 4: // 测试报告
//			fm.beginTransaction().show(fm.findFragmentByTag("report")).commit();
//			break;
//		}
//		
//		fragmentTransation.commit();
//	}



	private void initData() {
		list = new ArrayList<ContentModel>();
		list.add(new ContentModel("标         定"));	//, R.drawable.channel));
		list.add(new ContentModel("实验信息"));	//, R.drawable.acquisition));
		list.add(new ContentModel("采集设置"));	//, R.drawable.analysis));
		list.add(new ContentModel("预警设置"));	//, R.drawable.display));
		list.add(new ContentModel("测试报告"));	//, R.drawable.display));	
	}
}
