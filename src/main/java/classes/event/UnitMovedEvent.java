package classes.event;

import classes.board.Position;
import classes.game.Game;

public record UnitMovedEvent(Position from, Position to) implements GameEvent {
	public GameEventType type() {
		return GameEventType.UNIT_MOVED;
	}

	public void execute(Game game) {
		game.moveUnit(game.getGameBoard().getUnit(from), to);
	}

	public void undo(Game game) {
		game.moveUnit(game.getGameBoard().getUnit(to), from);
	}
}
