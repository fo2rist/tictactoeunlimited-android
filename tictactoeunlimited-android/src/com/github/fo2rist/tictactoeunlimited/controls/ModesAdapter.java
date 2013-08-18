package com.github.fo2rist.tictactoeunlimited.controls;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.fo2rist.tictactoeunlimited.R;

public class ModesAdapter extends FragmentPagerAdapter {

	public enum GameMode {
		VsCpu,
		VsFriend,
		ViaBluetooth,
		ViaNetwork
	}
	
	private class AdapterItem {
		GameMode mode;
		int buttonId;
		public AdapterItem(GameMode mode, int buttonId) {
			this.mode = mode;
			this.buttonId = buttonId;
		}
	}
	
	private ArrayList<AdapterItem> items_;
	
	public ModesAdapter(FragmentManager fm) {
		super(fm);
		items_ = new ArrayList<AdapterItem>();
		
		items_.add( new AdapterItem(GameMode.VsCpu, R.layout.btn_vs_cpu));
		items_.add( new AdapterItem(GameMode.VsFriend, R.layout.btn_vs_friend));
		items_.add( new AdapterItem(GameMode.ViaBluetooth, R.layout.btn_via_bt));
		items_.add( new AdapterItem(GameMode.ViaNetwork, R.layout.btn_via_net));
	}

	@Override
	public Fragment getItem(int position) {
		return ButtonFragment.newInstance(items_.get(position).buttonId);
	}

	@Override
	public int getCount() {
		return items_.size();
	}
	
	/**
	 * Get game mode by position in adapter.
	 * @throws IndexOutOfBoundsException
	 */
	public GameMode getMode(int position) {
		return items_.get(position).mode;
	}
	
	/**
	 * Remove item corresponding to given game mode from adapter.
	 */
	public void disableMode(GameMode mode) {
		for (int i = 0; i < items_.size(); i++) {
			if (items_.get(i).mode == mode) {
				items_.remove(i);
				return;
			}
		}
	}
}
