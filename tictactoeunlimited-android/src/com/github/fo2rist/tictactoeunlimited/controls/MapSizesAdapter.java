package com.github.fo2rist.tictactoeunlimited.controls;

import java.util.HashMap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.fo2rist.tictactoeunlimited.R;

public class MapSizesAdapter extends FragmentPagerAdapter {

	private static HashMap<Character, Integer> CHAR_DRAWABLE_MAP = new HashMap<Character, Integer>();
	static {
		CHAR_DRAWABLE_MAP.put('0', R.drawable.numder_0);
		CHAR_DRAWABLE_MAP.put('1', R.drawable.numder_1);
		CHAR_DRAWABLE_MAP.put('2', R.drawable.numder_2);
		CHAR_DRAWABLE_MAP.put('3', R.drawable.numder_3);
		CHAR_DRAWABLE_MAP.put('4', R.drawable.numder_4);
		CHAR_DRAWABLE_MAP.put('5', R.drawable.numder_5);
		CHAR_DRAWABLE_MAP.put('6', R.drawable.numder_6);
		CHAR_DRAWABLE_MAP.put('7', R.drawable.numder_7);
		CHAR_DRAWABLE_MAP.put('8', R.drawable.numder_8);
		CHAR_DRAWABLE_MAP.put('9', R.drawable.numder_9);
		CHAR_DRAWABLE_MAP.put('x', R.drawable.numder_x);	
	}
	
	private static String[] buttons = {
		"6x6",
		"8x8",
		"8x10",
		"8x14"
	};
	
	public MapSizesAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return ImageTextFragment.newInstance(CHAR_DRAWABLE_MAP, buttons[position]);
	}

	@Override
	public int getCount() {
		return buttons.length;
	}
}
