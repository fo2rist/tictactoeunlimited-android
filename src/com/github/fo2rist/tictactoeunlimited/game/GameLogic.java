package com.github.fo2rist.tictactoeunlimited.game;

/**
 * Game itself.
 * Contains all game logic.
 */
public class GameLogic {

	/**
	 * Game view interface.
	 * For those who wants to display game state.
	 */
	public interface GameView {
		void numberOfWinsChanged(int numberOfWins);
		void numberOfDefeatsChanged(int numberOfDefeats);
		void onCellFilled(int x, int y, CellState state);
		void onFieldErased();
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

	int gameWidth_;
	int gameHeight_;

	private CellState gameField_[][];
	int gameNumber_;
	int numberOfWins_;
	int numberOfDefeats_;

	public void initializeGame(int width, int height) {
	}

	public void onButtonClicked(bool checked);
	public void resetGame();

	public int getNumberOfWins() {

	}
	public int getNumberOfDefeats() {

	}


	///Set check-mark in given position.
	///Setup UI cell if necessary.
	private void makeTurn(QPoint position, TurnType turn);
	///Check whether current step is the last.
	///@return true if it's a last step.
	private bool checkForWin(QPoint position);
	///Calculate best turn position for given gamer(X/O).
	//Will return point (-1,-1) if there are no possible turns.
	private QPoint bestTurnFor(TurnType turnType);

	///Get length of the line started by this point in given direction.
	private int getLineLength(QPoint initialPoint, QPoint direction);
	///Calculate weight for this point for given cell state.
	///@return 0 - useless point, -1 - impossible point, >0 - good point.
	private int calculateWeightFor(QPoint position, CellState desiredState);
	///Calculate number of cells, non blocked by enemy or border in given direction.
	private int calculateMaxLineLength(QPoint initialPoint, QPoint direction, CellState desiredState);
	///Calculate number of cells, with desired stated in given direction (before first border).
	///Except initial point itself.
	private int calculateFilledCells(QPoint initialPoint, QPoint direction, CellState desiredState);

	private void initGameField();
	private void cleanGameField();

	private void showGameOverDialog(const QString& background);

//signals:
//	void numberOfWinsChanged(int);
//	void numberOfDefeatsChanged(int);
}
