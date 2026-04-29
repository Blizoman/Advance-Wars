/**
 * Factory for Unit
 * 
 * @author: xpruzir00
 */

package unit;

import board.Position;
import gamer.Player;

public class UnitFactory {
	public Unit createUnit(UnitType type, Player player, Position position) {
		Unit unit = new Unit(player, type, position);
		unit.resetMovement();
		return unit;
	}
}
