package com.tstech.soundlevelinstrument.viewinterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public abstract class BackHandledFragment extends Fragment {

	protected BackHandledInterface mBackHandledInterface;
	
	/**
	 * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
	 * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
	 * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
	 */
	public abstract boolean onBackPressed();
	
	public void hideSelf() {
		this.getFragmentManager().beginTransaction().hide(this).commit();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(!(getActivity() instanceof BackHandledInterface)){
			throw new ClassCastException("Hosting Activity must implement BackHandledInterface");
		}else{
			this.mBackHandledInterface = (BackHandledInterface)getActivity();
		}
	}
	
	@Override
	public void onResume() {
		mBackHandledInterface.setSelectedFragment(this);
		super.onResume();
		//告诉FragmentActivity，当前Fragment在栈顶
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		
		mBackHandledInterface.setSelectedFragment(null);

		super.onPause();
	}
	
	
	
}
