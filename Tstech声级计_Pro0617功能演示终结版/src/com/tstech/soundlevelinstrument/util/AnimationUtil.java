package com.tstech.soundlevelinstrument.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import com.tstech.soundlevelinstrument.R;
import com.tstech.soundlevelinstrument.fragment.BaseMvpFragment;

public class AnimationUtil {

	public static void showViewLeft(Context context,final View view){
		AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.scroll_scale_left_in);
		set.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
			}
		});
		view.startAnimation(set);
	}
	public static void hideViewLeft(Context context,final View view){
		AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.scroll_scale_left_hide);
		set.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		view.startAnimation(set);
	}
	
	public static void showViewRight(Context context,final View view){
		AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.scroll_scale_right_in);
		set.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
			}
		});
		view.startAnimation(set);
	}
	
	public static void hideViewRight(Context context,final View view){
		AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.scroll_scale_right_hide);
		set.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		view.startAnimation(set);
	}
	
	
	public static void hideFragmentLeft(Context context,final Fragment fragment){
		AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.scroll_right_hide);
		set.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				fragment.getView().setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				fragment.getView().setVisibility(View.GONE);
			}
		});
		fragment.getView().startAnimation(set);
	}
	public static void showFragmentLeft(Context context,final  Fragment fragment){
		AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.scroll_left_in);
		set.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				fragment.getView().setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		fragment.getView().startAnimation(set);
	}
	class MyListener implements Animation.AnimationListener{

		@Override
		public void onAnimationStart(Animation animation) {
			
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
	}
}
