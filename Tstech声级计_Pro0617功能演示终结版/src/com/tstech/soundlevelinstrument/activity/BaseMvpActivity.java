package com.tstech.soundlevelinstrument.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.tstech.soundlevelinstrument.presenter.BasePresenter;

public abstract class BaseMvpActivity<V,T extends BasePresenter<V>> extends FragmentActivity  {

    public T presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = initPresenter();
        
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void onResume() {
        super.onResume();
        presenter.attach((V)this);
    }

    @Override
    protected void onDestroy() {
        presenter.dettach();
        super.onDestroy();
    }

	public abstract T initPresenter();

}
