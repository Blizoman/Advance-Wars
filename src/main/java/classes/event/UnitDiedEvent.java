package classes.event;

import classes.game.Game;
import classes.unit.Unit;

public record UnitDiedEvent(Unit unit) implements GameEvent {
	public GameEventType type() {
		return GameEventType.UNIT_DIED;
	}

	public void execute(Game game) {
		game.removeUnit(unit);
	}

	public void undo(Game game) {
		game.placeUnit(unit);
	}
}
