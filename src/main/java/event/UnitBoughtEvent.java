package event;

import board.Position;
import game.Game;
import gamer.Player;
import lombok.Getter;
import unit.Unit;
import unit.UnitType;

public class UnitBoughtEvent implements GameEvent {
	private final Position position;
	private final UnitType unitType;
	@Getter
	private final Player player;
	private Unit createdUnit;

	public UnitBoughtEvent(Position position, UnitType unitType, Player player) {
		this.position = position;
		this.unitType = unitType;
		this.player = player;
	}

	public GameEventType type() {
		return GameEventType.UNIT_BOUGHT;
	}

	public void execute(Game game) {
		game.buyUnit(position, unitType);
		createdUnit = game.getGameBoard().getUnit(position);
	}

	public void undo(Game game) {
		game.getGameBoard().removeUnit(createdUnit);
		player.addMoney(unitType.getCost());
	}
}
