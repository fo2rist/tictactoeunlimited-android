package com.weezlabs.tictactoeunlimited.controls;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Extended view Pager, that allows adding buttons to "swipe" left/right.  
 */
public class HorizontalPagerWidget extends ViewPager {

	public interface OnPageSelectedListener {
		public void onPageSelected(int position);
	}
	
	public HorizontalPagerWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setButtons(View leftClickable, View rightClickable) {
		leftClickable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PagerAdapter adapter = getAdapter();
				if (adapter == null || adapter.getCount() <= 1) {
					return;
				}
				int current = getCurrentItem();
				if (current > 0) {
					setCurrentItem(current - 1);
				}
			}
		});
		
		rightClickable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PagerAdapter adapter = getAdapter();
				if (adapter == null || adapter.getCount() <= 1) {
					return;
				}
				int current = getCurrentItem();
				if (current < adapter.getCount()-1) {
					setCurrentItem(current + 1);
				}
			}
		});
	}
	
	/**
	 * Set listener to be notified about page change.
	 */
	public void setOnPageSelectedListener(final OnPageSelectedListener listener) {
		this.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				listener.onPageSelected(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
}
