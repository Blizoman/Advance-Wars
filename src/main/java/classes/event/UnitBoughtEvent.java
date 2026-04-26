package classes.event;

import classes.game.Game;
import classes.unit.Unit;

public record UnitBoughtEvent(Unit unit) implements GameEvent {
	public GameEventType type() {
		return GameEventType.UNIT_BOUGHT;
	}

	public void execute(Game game) {
		game.getGameBoard().getTile(unit.getPosition()).placeUnit(unit);
		unit.getPlayer().removeMoney(unit.getType().getCost());
	}

	public void undo(Game game) {
		game.getGameBoard().removeUnit(unit);
		unit.getPlayer().addMoney(unit.getType().getCost());
	}
}
