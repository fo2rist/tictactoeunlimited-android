package com.github.fo2rist.tictactoeunlimited.controls;

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

public class ImageTextFragment	 extends Fragment {

	private static final String CHARACTER_DRAWABLE_MAP_KEY = "char_drawable_map";
	private static final String TEXT_KEY = "text";

	private HashMap<Character, Integer> charDrawableMap_;
	private String text_;
	
	/**
	 * Create a new instance of ImageTextFragment with given text.
	 */
	static ImageTextFragment newInstance(HashMap<Character, Integer> charDrawableMap, String text) {
		ImageTextFragment result = new ImageTextFragment();

		Bundle args = new Bundle();
		args.putSerializable(CHARACTER_DRAWABLE_MAP_KEY, charDrawableMap);
		args.putString(TEXT_KEY, text);
		result.setArguments(args);

		return result;
	}

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		charDrawableMap_ = (HashMap<Character, Integer>) getArguments().getSerializable(CHARACTER_DRAWABLE_MAP_KEY);
		text_ = getArguments().getString(TEXT_KEY);
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ImageTextView imageTextView = new ImageTextView(getActivity(), charDrawableMap_);		
		imageTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imageTextView.setGravity(Gravity.CENTER);
		imageTextView.setText(text_);
		return imageTextView;
	}
}
