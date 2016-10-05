package com.tstech.soundlevelinstrument.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.animation.Animation;

import com.tstech.soundlevelinstrument.presenter.BasePresenter;
import com.tstech.soundlevelinstrument.util.AnimationUtil;

public abstract class BaseMvpFragment <V,T extends BasePresenter<V>> extends Fragment{

	public BaseMvpFragment() {
		// TODO Auto-generated constructor stub
	}

    public T presenter;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = initPresenter();
        
    }

    /**
     * 用于View和Present绑定<br>
     * 注意：继承该类的fragment必须实现相应View的接口<br/>
     * 	      例：CalibrationFragment implements CalibrationView
     */
	@SuppressWarnings("unchecked")
	@Override
	public void onResume() {
        super.onResume();
        presenter.attach((V)this);
    }

    @Override
	public void onDestroy() {
        presenter.dettach();
        super.onDestroy();
    }

	@Override
	public void onHiddenChanged(boolean hidden) {
//		if (!hidden && isResumed()) {
//			AnimationUtil.showFragmentLeft(getActivity(), this);
//		}else{
//			AnimationUtil.hideFragmentLeft(getActivity(), this);
//		}
	}

	/**
     * 
     * @return 实现BasePresenter的Present实例实现绑定
     */
	public abstract T initPresenter();
}
