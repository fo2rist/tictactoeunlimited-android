package com.github.fo2rist.tictactoeunlimited.controls;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Extended view Pager, that allows adding buttons to "swipe" left/right.  
 */
public class HorizontalFlipperWidget extends ViewPager {

	public HorizontalFlipperWidget(Context context, AttributeSet attrs) {
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
}
