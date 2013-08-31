package com.weezlabs.tictactoeunlimited.controls;

import java.util.HashMap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.DisplayMetrics;

import com.weezlabs.tictactoeunlimited.GameActivity;
import com.weezlabs.tictactoeunlimited.R;

public class MapSizesAdapter extends FragmentPagerAdapter {

	private static HashMap<Character, Integer> BIGTEXT_CHAR_DRAWABLE_MAP = new HashMap<Character, Integer>();
	static {
		BIGTEXT_CHAR_DRAWABLE_MAP.put('0', R.drawable.numder_0);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('1', R.drawable.numder_1);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('2', R.drawable.numder_2);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('3', R.drawable.numder_3);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('4', R.drawable.numder_4);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('5', R.drawable.numder_5);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('6', R.drawable.numder_6);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('7', R.drawable.numder_7);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('8', R.drawable.numder_8);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('9', R.drawable.numder_9);
		BIGTEXT_CHAR_DRAWABLE_MAP.put('x', R.drawable.numder_x);	
	}
	
	private int[][] maps_;
	
	/**
	 * Construct adapter for given display.
	 * Adapter will calculate suitable map sizes. 
	 */
	public MapSizesAdapter(FragmentManager fm, DisplayMetrics displayMetrics) {
		super(fm);
		int minButtonSizeDp = 30;
		int screenWidthDp = (int) (displayMetrics.widthPixels / displayMetrics.density);
		int screenHeightDp = (int) (displayMetrics.heightPixels / displayMetrics.density);
				
		int maxGameWidth = (screenWidthDp - GameActivity.HORIZONTAL_SCREEN_PADDING_DP) / minButtonSizeDp;
		int maxGameHeight = (screenHeightDp - GameActivity.VERTICAL_SCREEN_PADDING_DP) / minButtonSizeDp;
		
		final int MIN_GAME_SIZE = 6;
		final int STEP = 2;
		int possibleWidths = (maxGameWidth - MIN_GAME_SIZE) / STEP;
		int possibleHeights = (maxGameHeight - MIN_GAME_SIZE) / STEP;
		maps_ = new int[possibleWidths * possibleHeights][];
		for (int w = 0; w < possibleWidths; w++) {
			for (int h = 0; h < possibleHeights; h++) {
				maps_[w * possibleHeights + h] = new int[]{MIN_GAME_SIZE + w * STEP, MIN_GAME_SIZE + h * STEP};
			}
		}
	}

	@Override
	public Fragment getItem(int position) {
		String mapName = maps_[position][0] + "x" + maps_[position][1]; 
		return ImageTextFragment.newInstance(BIGTEXT_CHAR_DRAWABLE_MAP, mapName);
	}

	@Override
	public int getCount() {
		return maps_.length;
	}
	
	/**
	 * Get map size by position in adapter.
	 * @return two dimensional array with {x,y} size.
	 * @throws IndexOutOfBoundsException
	 */
	public int[] getMapSize(int position) {
		return maps_[position];
	}
}
