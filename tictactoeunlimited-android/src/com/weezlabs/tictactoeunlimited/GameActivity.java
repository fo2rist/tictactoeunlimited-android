package com.weezlabs.tictactoeunlimited;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.weezlabs.tictactoeunlimited.controls.ImageTextView;
import com.weezlabs.tictactoeunlimited.game.GameLogic;
import com.weezlabs.tictactoeunlimited.game.GameLogic.CellState;
import com.weezlabs.tictactoeunlimited.game.GameLogic.GameView;
import com.weezlabs.tictactoeunlimited.game.GameLogic.TurnType;

/**
 * Game field.
 */
public class GameActivity extends Activity implements GameView, OnClickListener {
	public static int HORIZONTAL_SCREEN_PADDING_DP = 12;
	public static int VERTICAL_SCREEN_PADDING_DP = 120;
	
	private static String GAME_SIZE_KEY = "game_size_key";
	private static String GAME_MODE_KEY = "game_mode_key";
	
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
	private ImageButton iconX;
	private ImageButton iconO;
	private ImageTextView scoreO;
	private ImageTextView scoreX;
	private ImageView gameOverView;

	public static void launch(Context context, int width, int height, GameLogic.GameMode gameMode) {
		Intent intent = new Intent(context, GameActivity.class);
		intent.putExtra(GAME_SIZE_KEY, new Point(width, height));
		intent.putExtra(GAME_MODE_KEY, gameMode);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_game);
		
		gameGrid_ = (GridLayout) findViewById(R.id.game_grid);
		iconX = (ImageButton) findViewById(R.id.icon_x);
		iconO = (ImageButton) findViewById(R.id.icon_o);
		scoreO = (ImageTextView) findViewById(R.id.score_o);
		scoreX = (ImageTextView) findViewById(R.id.score_x);
		gameOverView = (ImageView) findViewById(R.id.game_over_view);
		
		scoreO.setCharDrawableMap(SCORES_CHAR_DRAWABLE_MAP);
		scoreX.setCharDrawableMap(SCORES_CHAR_DRAWABLE_MAP);
		
		//Init game
		Point gameSize = (Point) getIntent().getParcelableExtra(GAME_SIZE_KEY);
		GameLogic.GameMode playersMode = (GameLogic.GameMode) getIntent().getSerializableExtra(GAME_MODE_KEY);
		GameLogic.getInstance().registerGameView(this);
		GameLogic.getInstance().initializeGame(gameSize.x, gameSize.y, playersMode);
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
		
		gameOverView.setVisibility(View.GONE);
		gameOverView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		
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
	
	public void onResetGameClick(View sender) {
		GameLogic.getInstance().resetGame();
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
		if (currentPlayer == TurnType.TurnX) {
			iconX.setAlpha(255);
			iconO.setAlpha(100);
		} else {
			iconX.setAlpha(100);
			iconO.setAlpha(255);
		}
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
	public void onGameOver(CellState winnersCell) {
		switch (winnersCell) {
		case CellStateEmpty:
			gameOverView.setBackgroundColor(getResources().getColor(R.color.no_winner));
			gameOverView.setImageResource(R.drawable.game_win_no);
			break;
		case CellStateX:
			gameOverView.setBackgroundColor(getResources().getColor(R.color.win));
			gameOverView.setImageResource(R.drawable.game_win_x);
			break;
		case CellStateO:
			gameOverView.setBackgroundColor(getResources().getColor(R.color.lose));
			gameOverView.setImageResource(R.drawable.game_win_o);
			break;
		default:
			throw new IllegalArgumentException("Not supported winner");
		}
		gameOverView.setVisibility(View.VISIBLE);
	}
}
