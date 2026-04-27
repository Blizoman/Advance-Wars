package event;

import board.Position;
import game.Game;
import unit.Unit;

public record UnitMovedEvent(Position from, Position to, int movesLeftBefore) implements GameEvent {
	public GameEventType type() {
		return GameEventType.UNIT_MOVED;
	}

	public void execute(Game game) {
		Unit unit = game.getGameBoard().getUnit(from);
		unit.setMovesLeft(movesLeftBefore - from.distanceTo(to));
		game.moveUnit(unit, to);
	}

	public void undo(Game game) {
		Unit unit = game.getGameBoard().getUnit(to);
		game.moveUnit(unit, from);
		unit.setMovesLeft(movesLeftBefore);
	}
}
