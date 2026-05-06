/**
 * Represents a game event where a player purchases a new unit from a factory. This event handles
 * the instantiation of the unit at a specific position and the deduction of the purchase cost from
 * the player's balance. It supports the undo operation by removing the created unit from the board
 * and refunding the full cost to the player.
 *
 * @author xpruzir00
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
