package com.github.fo2rist.tictactoeunlimited.controls;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
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

	/**
	 * Default view constructor for editing mode, should not be used in code. 
	 */
	public ImageTextView(Context context, AttributeSet attributes) {
		this(context, attributes, null);
		
		if (isInEditMode()) {
			ImageView image = new ImageView(context);
			image.setImageResource(R.drawable.numder_game_0);
			this.addView(image);
		}
	}
	
	/**
	 * Create image view to display given set of letters.
	 */
	public ImageTextView(Context context, HashMap<Character, Integer> charDrawableMap) {
		this(context, null, charDrawableMap);
	}
	
	private ImageTextView(Context context, AttributeSet attributes, HashMap<Character, Integer> charDrawableMap) {
		super(context, attributes);
		
		this.setOrientation(LinearLayout.HORIZONTAL);
		this.setGravity(Gravity.CENTER_VERTICAL); 
		
		context_ = context;
		setCharDrawableMap(charDrawableMap);
	}

	/**
	 * Set mapping from character to image resource.
	 */
	public void setCharDrawableMap(HashMap<Character, Integer> charDrawableMap) {
		charDrawableMap_ = charDrawableMap;
	}
	
	public void setText(String text) {
		this.removeAllViews();
		for (char c: text.toCharArray()) {
			ImageView image = new ImageView(context_);
			image.setImageResource(charDrawableMap_.get(c));
			this.addView(image);			
		}
	}
}
