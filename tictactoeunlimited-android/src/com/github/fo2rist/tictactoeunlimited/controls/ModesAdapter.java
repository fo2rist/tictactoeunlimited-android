package com.github.fo2rist.tictactoeunlimited.controls;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.fo2rist.tictactoeunlimited.R;

public class ModesAdapter extends FragmentPagerAdapter {

	private int[] buttons_ = {
		R.layout.btn_vs_cpu,
		R.layout.btn_vs_friend,
		R.layout.btn_via_bt
//		R.layout.btn_via_net
	};
	
	public ModesAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return ButtonFragment.newInstance(buttons_[position]);
	}

	@Override
	public int getCount() {
		return buttons_.length;
	}
}
