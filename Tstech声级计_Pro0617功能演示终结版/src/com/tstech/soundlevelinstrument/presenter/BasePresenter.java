package com.tstech.soundlevelinstrument.presenter;


public abstract class BasePresenter<T> {
    public T mView;

    /**
     * present 与view 绑定时调用
     */
    public void attach(T mView){
        this.mView = mView;
    }

    /**
     * present 与view 解绑时调用
     */
    public void dettach(){
        mView = null;
    }
}
