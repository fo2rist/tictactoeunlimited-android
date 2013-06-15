package com.github.fo2rist.tictactoeunlimited;

import com.github.fo2rist.tictactoeunlimited.game.GameLogic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

/**
 * Main screen.
 * Game type chooser.
 */
public class MainActivity extends Activity {

	private GameLogic game_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void startGameVsCpu(View sender) {
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		startActivity(new Intent(this, GameActivity.class));
	}

	public void startGameHotSeat(View sender) {
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		startActivity(new Intent(this, GameActivity.class));
	}

	public void startGameViaBluetooth(View sender) {
		Toast.makeText(this, "Not ready", 0).show();
	}

	public void showAbout(View sender) {
		startActivity(new Intent(this, AboutActivity.class));
	}
}