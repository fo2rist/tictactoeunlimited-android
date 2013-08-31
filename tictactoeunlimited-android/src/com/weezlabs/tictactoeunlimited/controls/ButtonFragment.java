package com.weezlabs.tictactoeunlimited.controls;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ButtonFragment extends Fragment {
	private static final String LAYOUT_ID_KEY = "layout_id";
	
	private int layoutId_;

	/**
	 * Create a new instance of ButtonFragment, providing button layout id
	 * as an argument.
	 */
	static ButtonFragment newInstance(int layoutId) {
		ButtonFragment result = new ButtonFragment();

		Bundle args = new Bundle();
		args.putInt(LAYOUT_ID_KEY, layoutId);
		result.setArguments(args);

		return result;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layoutId_ = getArguments().getInt(LAYOUT_ID_KEY);
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutId_, container, false);
	}
}
