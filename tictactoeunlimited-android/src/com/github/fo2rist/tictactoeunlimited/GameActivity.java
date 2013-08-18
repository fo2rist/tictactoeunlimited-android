package com.github.fo2rist.tictactoeunlimited;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.fo2rist.tictactoeunlimited.controls.ImageTextView;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic.CellState;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic.GameView;

/**
 * Game field.
 */
public class GameActivity extends Activity implements GameView, OnClickListener {
	private static HashMap<Character, Integer> SCORES_CHAR_DRAWABLE_MAP = new HashMap<Character, Integer>();
	static {
		SCORES_CHAR_DRAWABLE_MAP.put('0', R.drawable.numder_game_0);
		SCORES_CHAR_DRAWABLE_MAP.put('1', R.drawable.numder_game_1);
		SCORES_CHAR_DRAWABLE_MAP.put('2', R.drawable.numder_game_2);
		SCORES_CHAR_DRAWABLE_MAP.put('3', R.drawable.numder_game_3);
		SCORES_CHAR_DRAWABLE_MAP.put('4', R.drawable.numder_game_4);
		SCORES_CHAR_DRAWABLE_MAP.put('5', R.drawable.numder_game_5);
		SCORES_CHAR_DRAWABLE_MAP.put('6', R.drawable.numder_game_6);
		SCORES_CHAR_DRAWABLE_MAP.put('7', R.drawable.numder_game_7);
		SCORES_CHAR_DRAWABLE_MAP.put('8', R.drawable.numder_game_8);
		SCORES_CHAR_DRAWABLE_MAP.put('9', R.drawable.numder_game_9);
	}
	
	//Controls
	private GridLayout gameGrid_;
	private ImageTextView scoreO;
	private ImageTextView scoreX;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_game);
		
		gameGrid_ = (GridLayout) findViewById(R.id.game_grid);
		scoreO = (ImageTextView) findViewById(R.id.score_o);
		scoreX = (ImageTextView) findViewById(R.id.score_x);
		
		scoreO.setCharDrawableMap(SCORES_CHAR_DRAWABLE_MAP);
		scoreX.setCharDrawableMap(SCORES_CHAR_DRAWABLE_MAP);
		
		scoreO.setText("0");
		scoreX.setText("0");
		
		//Init game
		GameLogic.getInstance().registerGameView(this);
		fillGameView();
	}
	
	@Override
	protected void onDestroy() {
		GameLogic.getInstance().registerGameView(null);
		super.onDestroy();
	}
	
	private void fillGameView() {
		GameLogic game = GameLogic.getInstance();
		
		gameGrid_.removeAllViews();
		
		gameGrid_.setColumnCount(game.gameWidth);
		gameGrid_.setRowCount(game.gameHeight);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int defaultMargin = 2;
		int size = Math.min(width/game.gameWidth - defaultMargin, height/game.gameHeight - defaultMargin);
		
		for (int y = 0; y < game.gameHeight; y++) {
			for (int x = 0; x < game.gameWidth; x++) {
				ImageButton ImageButton = new ImageButton(this);
				ImageButton.setBackgroundResource(R.drawable.cell_empty);
				gameGrid_.addView(ImageButton, size, size);
				ImageButton.setTag(new Point(x,y));
				ImageButton.setOnClickListener(this);
			}
		}
	}
	
	/**
	 * Game ImageButton clicked listener.
	 */
	@Override
	public void onClick(View sender) {
		Point tagPosition = (Point) sender.getTag();
		GameLogic.getInstance().onButtonClicked(true, tagPosition);
	}

	//Game view implementation
	@Override
	public void numberOfWinsChanged(int numberOfWins) {
		scoreX.setText(String.valueOf(numberOfWins));
	}

	@Override
	public void numberOfDefeatsChanged(int numberOfDefeats) {
		scoreO.setText(String.valueOf(numberOfDefeats));
	}

	@Override
	public void onCellFilled(int x, int y, CellState state) {
		int buttonIndex = x + y * GameLogic.getInstance().gameWidth;
		ImageButton ImageButton = (ImageButton) gameGrid_.getChildAt(buttonIndex);
		
		ImageButton.setEnabled(false);
		switch (state) {
		case CellStateX:
			ImageButton.setImageResource(R.drawable.game_x);
			break;
		case CellStateO:
			ImageButton.setImageResource(R.drawable.game_o);
			break;
		default:
			break;
		}
	}

	@Override
	public void onFieldErased() {
		fillGameView();
	}
	
	@Override
	public void onShowDialog(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		GameLogic.getInstance().resetGame();
	}
}
