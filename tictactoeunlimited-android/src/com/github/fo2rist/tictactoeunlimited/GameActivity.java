package com.github.fo2rist.tictactoeunlimited;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.github.fo2rist.tictactoeunlimited.controls.ImageTextView;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic.CellState;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic.GameView;

/**
 * Game field.
 */
public class GameActivity extends Activity implements GameView, OnClickListener {
	public static int HORIZONTAL_SCREEN_PADDING_DP = 12;
	public static int VERTICAL_SCREEN_PADDING_DP = 120;
	
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
	public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
		View result = super.onCreateView(parent, name, context, attrs);
		
		return result;
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
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		int paddingHorizontal = (int) (HORIZONTAL_SCREEN_PADDING_DP * dm.density);
		int paddingVertical = (int) (VERTICAL_SCREEN_PADDING_DP * dm.density);
		//Calculate min of width and height
		int size = Math.min((screenWidth - paddingHorizontal)/game.gameWidth,
				(screenHeight - paddingVertical)/game.gameHeight);
		//and limit size by necessary value
		size = Math.min(size, (int) (50 * dm.density));
		
		for (int y = 0; y < game.gameHeight; y++) {
			for (int x = 0; x < game.gameWidth; x++) {
				ImageButton cellButton = new ImageButton(this);
				cellButton.setBackgroundResource(R.drawable.bg_cell);
				cellButton.setTag(new Point(x,y));
				cellButton.setOnClickListener(this);
				cellButton.setScaleType(ScaleType.CENTER_INSIDE);
				gameGrid_.addView(cellButton, size, size);
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
	public void onNumberOfWinsChanged(int numberOfWins) {
		scoreX.setText(String.valueOf(numberOfWins));
	}

	@Override
	public void onNumberOfDefeatsChanged(int numberOfDefeats) {
		scoreO.setText(String.valueOf(numberOfDefeats));
	}
	
	@Override
	public void onCurrentPlayerChanged(GameLogic.TurnType currentPlayer) {
		// TODO Auto-generated method stub	
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
