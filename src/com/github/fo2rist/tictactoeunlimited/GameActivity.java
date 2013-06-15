package com.github.fo2rist.tictactoeunlimited;

import android.app.Activity;
import android.os.Bundle;

import com.github.fo2rist.tictactoeunlimited.game.GameLogic.CellState;
import com.github.fo2rist.tictactoeunlimited.game.GameLogic.GameView;

/**
 * Game field.
 */
public class GameActivity extends Activity implements GameView {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_game);
	}

	//Game view implementation
	@Override
	public void numberOfWinsChanged(int numberOfWins) {
		// TODO Auto-generated method stub

	}

	@Override
	public void numberOfDefeatsChanged(int numberOfDefeats) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCellFilled(int x, int y, CellState state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFieldErased() {
		// TODO Auto-generated method stub

	}
}
