package controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import classes.board.Position;
import classes.game.Game;
import classes.game.Session;
import classes.unit.Unit;
import classes.unit.UnitType;
import lombok.Getter;
import lombok.Setter;

public class GameController {
	@Getter
	private final Session session;
	@Getter
	private final Game game;

	@Setter
	private Runnable onStateChanged;
	@Getter
	private Unit selectedUnit;
	@Getter
	private Set<Position> validMoves = new HashSet<>();

	public GameController(Session session, Game game) {
		this.session = session;
		this.game = game;
		session.setOnGameEnd($_ -> stateChanged());
	}

	private void stateChanged() {
		if (onStateChanged != null)
			onStateChanged.run();
		// TODO: on View set this:
		// controller.setOnStateChanged(() -> repaint());
	}

	public void onTileClicked(Position position) {
		Unit unit = game.getGameBoard().getUnit(position);
		if (selectedUnit == null) {
			if (unit != null && unit.getPlayer() == session.getActive()) {
				selectedUnit = unit;
				validMoves = new HashSet<>(); // TODO: ADO pathfinder
				stateChanged();
			}
		} else {
			if (validMoves.contains(position))
				session.moveUnit(selectedUnit, position);
			deselect();
		}
	}

	public void onAttack(Unit target) {
		session.attack(selectedUnit, target);
		deselect();
	}

	public void onCapture() {
		session.tryCapture(selectedUnit, game.getGameBoard().getTile(selectedUnit.getPosition()));
		deselect();
	}

	public void onWait() {
		deselect();
	}

	public void onBuyUnit(UnitType type, Position position) {
		session.buyUnit(position, type);
	}

	public void onEndTurn() {
		deselect();
		session.endTurn();
		stateChanged();
	}

	public void onStepForward() {
		session.stepForward();
		stateChanged();
	}

	public void onStepBackward() {
		session.stepBackward();
		stateChanged();
	}

	public void onSave(Path path) throws IOException {
		game.saveSession(path);
	}

	public void onLoad(Path path) throws IOException {
		game.loadSession(path);
		stateChanged();
	}

	private void deselect() {
		selectedUnit = null;
		validMoves.clear();
		stateChanged();
	}
}
