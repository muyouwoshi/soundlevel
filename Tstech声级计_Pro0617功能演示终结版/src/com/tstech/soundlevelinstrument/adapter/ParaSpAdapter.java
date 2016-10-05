package com.tstech.soundlevelinstrument.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tstech.soundlevelinstrument.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * spinner adapter
 */
public class ParaSpAdapter extends BaseAdapter{
	
	private List<String> list;
	private Context context;
	private String key;
	

	public ParaSpAdapter(Context context, List<String> list, String key) {
		this.context = context;
		this.list = list;
		this.key = key;
		
//		Map<String, String> map = new HashMap<String, String>();
//		map.put(key, "请选择");
//		list.add("请选择");
	}

	@Override
	public int getCount() {
		return null == list || list.size() < 1 ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(null == convertView){
			convertView = View.inflate(context, R.layout.spinner_item, null);
		}
		
//		Map<String, String> map = list.get(position);
//		String name = map.get(key);
		String name =list.get(position);
		
		TextView tv = (TextView) convertView.findViewById(R.id.sp_text);
		tv.setText(name);
//		tv.setTextSize(15);		//TODO Spinner适配器设置spinner字体大小

		return convertView;
	}
}
