/**
 * Factory class responsible for instantiating and initializing game units.
 * This class centralizes the creation logic for the {@link Unit} objects, ensuring that 
 * whenever a unit is spawned (e.g., purchased at a factory), it is correctly 
 * linked to its owner, placed at the correct position, and has its movement points 
 * fully initialized based on its specific {@link UnitType}.
 *
 * @author xpruzir00
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
