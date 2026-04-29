/**
 * Tracks that unit has been bought
 * 
 * @author: xpruzir00
 */

package event;

import board.Position;
import game.Game;
import gamer.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import unit.Unit;
import unit.UnitType;

@RequiredArgsConstructor
public class UnitBoughtEvent implements GameEvent {
	private final Position position;
	private final UnitType unitType;
	@Getter
	private final Player player;
	private Unit createdUnit;

	public GameEventType type() {
		return GameEventType.UNIT_BOUGHT;
	}

	public void execute(Game game) {
		createdUnit = game.buyUnit(position, unitType);
	}

	public void undo(Game game) {
		game.getGameBoard().removeUnit(createdUnit);
		player.addMoney(unitType.getCost());
	}
}
