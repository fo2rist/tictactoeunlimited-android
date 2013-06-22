package com.github.fo2rist.tictactoeunlimited.game;

import android.graphics.Point;

/**
 * Game itself.
 * Contains all game logic.
 */
public class GameLogic {
	//Length of the line to win
	private static final int WIN_LINE_LENGTH = 4;
	
	//Game coefficients
	private static final int C_userWin_ = 10;
	private static final int C_enemyWin_ = 9;
	private static final int C_turnsLeftToWin_ = 100;
	private static final int C_freeLinesAroundCell_ = 1;
	
	//Point that mean not more turns possible
	private static final Point impossiblePoint = new Point(-1, -1);
	
	//Directions to walk through cells
	private static final Point directionN = new Point(0, -1);
	private static final Point directionS = new Point(0, 1);
	private static final Point directionW = new Point(-1, 0);
	private static final Point directionE = new Point(1, 0);
	private static final Point directionNW = new Point(-1, -1);
	private static final Point directionNE = new Point(1, -1);
	private static final Point directionSW = new Point(-1, 1);
	private static final Point directionSE = new Point(1, 1);
	
	/**
	 * Game view link.
	 * Listener for all changes.
	 */
	private GameView gameView_;
	
	private static GameLogic gameInstance_;
	public static synchronized GameLogic getInstance() {
		if (gameInstance_ == null) {
			gameInstance_ = new GameLogic();
		}
		return gameInstance_;
	}

	/**
	 * Game view interface.
	 * For those who wants to display game state.
	 */
	public interface GameView {
		void numberOfWinsChanged(int numberOfWins);
		void numberOfDefeatsChanged(int numberOfDefeats);
		void onCellFilled(int x, int y, CellState state);
		void onFieldErased();
		void onShowDialog(String text);
	}

	/**
	 * Possible state for game cell.
	 */
	public enum CellState {
		CellStateEmpty,
		CellStateX,
		CellStateO
	}

	enum TurnType {
		TurnX, //just make turn and state binary compatible
		TurnO
	};

	public int gameWidth;
	public int gameHeight;

	private CellState gameField_[][];
	int gameNumber_;
	int numberOfWins_;
	int numberOfDefeats_;

	private GameLogic() {
	}
	
	public void initializeGame(int width, int height) {
		//Clean old game
		cleanGameField();

		//Init game field
		gameWidth = width;
		gameHeight = height;
		initGameField();
	}

	public void onButtonClicked(boolean checked, Point position) {
		if (checked == false) { //Ignore game reset
			return;
		}

//		ImageToggleButton* clickedButton = qobject_cast<ImageToggleButton*>(QObject::sender());
//		Point position = clickedButton->property(POSITION_PROPERTY).toPoint();

		makeTurn(position, TurnType.TurnX);

		//Check for palyer's win
		if (checkForWin(position)) {
			return;
		}

		//Calculate computer's turn
		Point computersTurnPosition = bestTurnFor(TurnType.TurnO);
		//Go ahead
		if (computersTurnPosition.equals(impossiblePoint)) {
			showGameOverDialog("asset:///images/dialog_no_turns.png");
			return;
		}
		makeTurn(computersTurnPosition, TurnType.TurnO);

		//Check for computer's win
		if(checkForWin(computersTurnPosition)) {
			return;
		}
	}

	public void resetGame() {
		cleanGameField();
		initGameField();
//		QList<ImageToggleButton*> buttons = currentGameFieldContainer_->findChildren<ImageToggleButton*>(BUTTONS_NAME);
//		foreach(ImageToggleButton *button, buttons) {
//			button->setEnabled(true);
//			button->setChecked(false);
//		}
		emit_onFieldErased();

		//increase game number to switch first player
		gameNumber_++;
		//Make AI's turn every second time
		if (gameNumber_ % 2 == 1) {
			Point computersTurnPosition = bestTurnFor(TurnType.TurnO);
			makeTurn(computersTurnPosition, TurnType.TurnO);
		}

	}

	public int getNumberOfWins() {
		return numberOfWins_;
	}

	public int getNumberOfDefeats() {
		return numberOfDefeats_;
	}


	///Set check-mark in given position.
	private void makeTurn(Point position, TurnType turn) {
		switch (turn) {
			case TurnX:
				gameField_[position.x][position.y] = CellState.CellStateX;
				emit_onCellFilled(position.x, position.y, CellState.CellStateX);
				break;
			case TurnO:
				gameField_[position.x][position.y] = CellState.CellStateO;
				emit_onCellFilled(position.x, position.y, CellState.CellStateO);
				break;
		}
	}
	///Check whether current step is the last.
	///@return true if it's a last step.
	private boolean checkForWin(Point position) {
		if (getLineLength(position, directionE) + getLineLength(position, directionW) > WIN_LINE_LENGTH
				|| getLineLength(position, directionN) + getLineLength(position, directionS) > WIN_LINE_LENGTH
				|| getLineLength(position, directionNE) + getLineLength(position, directionSW) > WIN_LINE_LENGTH
				|| getLineLength(position, directionNW) + getLineLength(position, directionSE) > WIN_LINE_LENGTH) {

			CellState cellState = gameField_[position.x][position.y];
			String background;
			if (cellState == CellState.CellStateX) {
				background= "asset:///images/dialog_win.png";
				numberOfWins_++;
				emit_numberOfWinsChanged();
			} else {
				background = "asset:///images/dialog_lose.png";
				numberOfDefeats_++;
				emit_numberOfDefeatsChanged();
			}

			showGameOverDialog(background);
			return true;
		}
		return false;
	}

	///Calculate best turn position for given gamer(X/O).
	//Will return point (-1,-1) if there are no possible turns.
	private Point bestTurnFor(TurnType turnType) {
		CellState desiredState = null;
		CellState enemysState = null;
		switch (turnType) {
			case TurnX:
				desiredState = CellState.CellStateX;
				enemysState = CellState.CellStateO;
				break;
			case TurnO:
				desiredState = CellState.CellStateO;
				enemysState = CellState.CellStateX;
				break;
		}

		//Go through matrix an look for best point
		int bestWeight = 0;
		Point bestPoint = impossiblePoint; //impossible point
		for (int x = 0; x < gameWidth; ++x) {
			for (int y = 0; y < gameHeight; ++y) {
				Point position = new Point(x, y);
				int playersWeight = calculateWeightFor(position, desiredState);
				int enemysWeight = calculateWeightFor(position, enemysState);

				int total = playersWeight*C_userWin_ + enemysWeight*C_enemyWin_;
				if (total > bestWeight) {
					bestWeight = total;
					bestPoint = position;
				}
			}
		}

		return bestPoint;
	}

	///Get length of the line started by this point in given direction.
	private int getLineLength(Point initialPoint, Point direction) {
		int x = initialPoint.x;
		int y = initialPoint.y;

		int result = 0;
		CellState desiredCellState = gameField_[x][y]; //Will calculate cell of same type as the initial
		do {
			result++; //Take into account current cell and move forward in given direction.
			x += direction.x;
			y += direction.y;
		} while(x < gameWidth
				&& x >= 0
				&& y < gameHeight
				&& y >= 0
				&& gameField_[x][y] == desiredCellState);

		return result;
	}
	
	///Calculate weight for this point for given cell state.
	///@return 0 - useless point, -1 - impossible point, >0 - good point.
	private int calculateWeightFor(Point position, CellState desiredState) {
		//Impossible point
		if (gameField_[position.x][position.y] != CellState.CellStateEmpty) {
			return -1;
		}

		//1 Maximal possible line * lines count (low priority factor)
		int cumulativeLinesLength = 0;
		//2 Turns left to win if win possible (high priority)
		int turnsLeftToWin = WIN_LINE_LENGTH;

		int lineN_S = calculateMaxLineLength(position, directionN, desiredState)
				+ calculateMaxLineLength(position, directionS, desiredState);
		if (lineN_S > WIN_LINE_LENGTH) { //Take into account only directions that may win
			cumulativeLinesLength += lineN_S;

			int cellsFilled = calculateFilledCells(position, directionN, desiredState)
						+ calculateFilledCells(position, directionS, desiredState);
			turnsLeftToWin = Math.min(turnsLeftToWin, WIN_LINE_LENGTH - cellsFilled);
		}

		int lineE_W = calculateMaxLineLength(position, directionE, desiredState)
				+ calculateMaxLineLength(position, directionW, desiredState);
		if (lineE_W > WIN_LINE_LENGTH) { //Take into account only directions that may win
			cumulativeLinesLength += lineE_W;

			int cellsFilled = calculateFilledCells(position, directionE, desiredState)
						+ calculateFilledCells(position, directionW, desiredState);
			turnsLeftToWin = Math.min(turnsLeftToWin, WIN_LINE_LENGTH - cellsFilled);
		}

		int lineNE_SW = calculateMaxLineLength(position, directionNE, desiredState)
				+ calculateMaxLineLength(position, directionSW, desiredState);
		if (lineNE_SW > WIN_LINE_LENGTH) { //Take into account only directions that may win
			cumulativeLinesLength += lineNE_SW;

			int cellsFilled = calculateFilledCells(position, directionNE, desiredState)
						+ calculateFilledCells(position, directionSW, desiredState);
			turnsLeftToWin = Math.min(turnsLeftToWin, WIN_LINE_LENGTH - cellsFilled);
		}

		int lineNW_SE = calculateMaxLineLength(position, directionNW, desiredState)
				+ calculateMaxLineLength(position, directionSE, desiredState);
		if (lineNW_SE > WIN_LINE_LENGTH) { //Take into account only directions that may win
			cumulativeLinesLength += lineNW_SE;

			int cellsFilled = calculateFilledCells(position, directionNW, desiredState)
						+ calculateFilledCells(position, directionSE, desiredState);
			turnsLeftToWin = Math.min(turnsLeftToWin, WIN_LINE_LENGTH - cellsFilled);
		}

		//If win impossible
		if (cumulativeLinesLength == 0) {
			return 0;
		}

		//If win possible
		return (int)(C_turnsLeftToWin_ * Math.pow((WIN_LINE_LENGTH - turnsLeftToWin), 2))
				+ C_freeLinesAroundCell_ * cumulativeLinesLength;
	}
	
	///Calculate number of cells, non blocked by enemy or border in given direction.
	private int calculateMaxLineLength(Point initialPoint, Point direction, CellState desiredState) {
		int x = initialPoint.x;
		int y = initialPoint.y;

		int result = 0;
		//Calculate our own and empty cells
		while (x < gameWidth
				&& x >= 0
				&& y < gameHeight
				&& y >= 0
				&& (gameField_[x][y] == desiredState || gameField_[x][y] == CellState.CellStateEmpty)) {
			result++; //Take into account current cell and move forward in given direction.
			x += direction.x;
			y += direction.y;
		}

		return result;
	}
	
	///Calculate number of cells, with desired stated in given direction (before first border).
	///Except initial point itself.
	private int calculateFilledCells(Point initialPoint, Point direction, CellState desiredState) {
		//Start from "next" cell
		int x = initialPoint.x + direction.x;
		int y = initialPoint.y + direction.y;

		int result = 0;
		//Go through own cells
		while (x < gameWidth
				&& x >= 0
				&& y < gameHeight
				&& y >= 0
				&& (gameField_[x][y] == desiredState)) {
			result++;
			x += direction.x;
			y += direction.y;
		}

		return result;
	}

	private void initGameField() {
		gameField_ = new CellState[gameWidth][gameHeight];
		for (int x = 0; x < gameWidth; ++x) {
			//gameField_[x] = new CellState[gameHeight_];
			for (int y = 0; y < gameHeight; ++y) {
				gameField_[x][y] = CellState.CellStateEmpty;
			}
		}
	}
	
	private void cleanGameField() {
		if (gameField_ != null) {
			//for (int x = 0; x < gameWidth_; ++x) {
			//	delete[] gameField_[x];
			//}
			//delete[] gameField_;
			gameField_ = null;
		}
	}

	private void showGameOverDialog(String background) {
		if (gameView_ == null) {
			return;
		}
		gameView_.onShowDialog(background);
	}

	/**
	 * Register game-view which will draw the game.
	 */
	public void registerGameView(GameView gameView) {
		gameView_ = gameView;
	}
	
	void emit_numberOfWinsChanged() {
		if (gameView_ == null) {
			return;
		}
		gameView_.numberOfWinsChanged(numberOfWins_);
	}
	
	void emit_numberOfDefeatsChanged() {
		if (gameView_ == null) {
			return;
		}
		gameView_.numberOfDefeatsChanged(numberOfDefeats_);
	}
	
	void emit_onCellFilled(int x, int y, CellState state) {
		if (gameView_ == null) {
			return;
		}
		gameView_.onCellFilled(x, y, state);
	}
	
	void emit_onFieldErased() {
		if (gameView_ == null) {
			return;
		}
		gameView_.onFieldErased();
	}
}
