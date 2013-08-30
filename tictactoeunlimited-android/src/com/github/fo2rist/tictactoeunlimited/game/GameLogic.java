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
		void onNumberOfWinsChanged(int numberOfWins);
		void onNumberOfDefeatsChanged(int numberOfDefeats);
		void onCurrentPlayerChanged(TurnType currentPlayer);
		void onCellFilled(int x, int y, CellState state);
		void onFieldErased();
		void onGameOver(CellState winnersCell);
	}

	/**
	 * Possible state for game cell.
	 */
	public enum CellState {
		CellStateEmpty,
		CellStateX,
		CellStateO,
		CellStateV // For third player
	}

	public enum TurnType {
		TurnX,
		TurnO,
		TurnV
	};
	
	public enum GameMode {
		GameModeSinglePlayer,
		GameModeTwoPlayers,
		GameModeThreePlayers
	};
	
	//Properties
	public int gameWidth;
	public int gameHeight;

	//Private fields
	private CellState gameField_[][];
	private GameMode gameMode_;
	private TurnType currentTurn_;
	private int gameNumber_;
	private int numberOfWins_;
	private int numberOfDefeats_;

	private GameLogic() {
	}
	
	public void initializeGame(int width, int height, GameMode gameMode) {
		//Clean old game
		cleanGameField();

		//Clear game state
		currentTurn_ = TurnType.TurnX; //First player starts by default
		gameNumber_ = 0;
		numberOfWins_ = 0;
		numberOfDefeats_ = 0;
		
		//Set new game parameters
		gameWidth = width;
		gameHeight = height;
		gameMode_ = gameMode;
		
		//Init game field
		initGameField();
		
		//Notify UI to redraw everything
		emit_currentPlayerChanged();
		emit_numberOfWinsChanged();
		emit_numberOfDefeatsChanged();
	}

	public int getNumberOfWins() {
		return numberOfWins_;
	}

	public int getNumberOfDefeats() {
		return numberOfDefeats_;
	}
	
	public void onButtonClicked(boolean checked, Point position) {
		makeTurn(position);

		//Check for palyer's win
		if (checkForWin(position)) {
			return;
		}

		//Calculate computer's turn
		Point computersTurnPosition = bestTurnFor(TurnType.TurnO);
		//Go ahead
		if (computersTurnPosition.equals(impossiblePoint)) {
			showGameOverDialog(CellState.CellStateEmpty);
			return;
		}

		//If we play vs CPU make this turn
		if (gameMode_ == GameMode.GameModeSinglePlayer) {
			makeTurn(computersTurnPosition);

			//Check for computer's win
			if(checkForWin(computersTurnPosition)) {
				return;
			}
		} else {
			//Wait for next player turn
			emit_currentPlayerChanged();
		}
	}

	public void resetGame() {
		cleanGameField();
		initGameField();

		emit_onFieldErased();

		//increase game number to switch first player
		gameNumber_++;
		//Switch first player every second time
		switch (gameNumber_ % 2) {
		case 0:
			currentTurn_ = TurnType.TurnX;
			break;
		case 1:
			currentTurn_ = TurnType.TurnO;
			//Make AI's turn automatically
			if (gameMode_ == GameMode.GameModeSinglePlayer) {
				Point computersTurnPosition = bestTurnFor(currentTurn_);
				makeTurn(computersTurnPosition);
			}
			break;
		}
		emit_currentPlayerChanged();
	}

	///Set check-mark in given position.
	private void makeTurn(Point position) {
		switch (currentTurn_) {
			case TurnX:
				gameField_[position.x][position.y] = CellState.CellStateX;
				emit_onCellFilled(position.x, position.y, CellState.CellStateX);
				break;
			case TurnO:
				gameField_[position.x][position.y] = CellState.CellStateO;
				emit_onCellFilled(position.x, position.y, CellState.CellStateO);
				break;
			case TurnV:
				gameField_[position.x][position.y] = CellState.CellStateV;
				emit_onCellFilled(position.x, position.y, CellState.CellStateV);
				break;
		}
		//Figure out who is the next
		switch (gameMode_) {
			case GameModeSinglePlayer:
			case GameModeTwoPlayers:
				if (currentTurn_ == TurnType.TurnX) {
					currentTurn_ = TurnType.TurnO;
				} else {
					currentTurn_ = TurnType.TurnX;
				}
				break;
			case GameModeThreePlayers:
				if (currentTurn_ == TurnType.TurnX) {
					currentTurn_ = TurnType.TurnO;
				} else if (currentTurn_ == TurnType.TurnO) {
					currentTurn_ = TurnType.TurnV;
				} else {
					currentTurn_ = TurnType.TurnX;
				}
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
			if (cellState == CellState.CellStateX) {
				numberOfWins_++;
				emit_numberOfWinsChanged();
				showGameOverDialog(cellState);
			} else {
				numberOfDefeats_++;
				emit_numberOfDefeatsChanged();
				showGameOverDialog(cellState);
			}
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
			case TurnV:
				//not implemented
				desiredState = CellState.CellStateV;
				return impossiblePoint;
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
			gameField_ = null;
		}
	}

	private void showGameOverDialog(CellState winnersCell) {
		if (gameView_ == null) {
			return;
		}
		gameView_.onGameOver(winnersCell);
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
		gameView_.onNumberOfWinsChanged(numberOfWins_);
	}
	
	void emit_numberOfDefeatsChanged() {
		if (gameView_ == null) {
			return;
		}
		gameView_.onNumberOfDefeatsChanged(numberOfDefeats_);
	}
	
	void emit_currentPlayerChanged() {
		if (gameView_ == null) {
			return;
		}
		gameView_.onCurrentPlayerChanged(currentTurn_);
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
