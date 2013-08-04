package com.github.fo2rist.tictactoeunlimited.controls;

import java.util.HashMap;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.fo2rist.tictactoeunlimited.R;

/**
 * View that display bunch of images in single line to draw words and numbers. 
 */
public class ImageTextView extends LinearLayout {
	private Context context_;
	private HashMap<Character, Integer> charDrawableMap_;
	
	public ImageTextView(Context context, HashMap<Character, Integer> charDrawableMap) {
		super(context);
		this.setOrientation(LinearLayout.HORIZONTAL);
		this.setGravity(Gravity.CENTER_VERTICAL); 
		
		context_ = context;
		charDrawableMap_ = charDrawableMap;
		
		if (isInEditMode()) {
			ImageView image = new ImageView(context);
			image.setImageResource(R.drawable.numder_game_0);
			this.addView(image);
		}
	}
	
	public void setText(String text) {
		for (char c: text.toCharArray()) {
			ImageView image = new ImageView(context_);
			image.setImageResource(charDrawableMap_.get(c));
			this.addView(image);			
		}
	}
}
