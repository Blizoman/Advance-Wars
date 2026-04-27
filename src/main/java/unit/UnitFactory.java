package unit;

import board.Position;
import player.Player;

public class UnitFactory {
	public Unit createUnit(UnitType type, Player player, Position position) {
		return new Unit(player, type, position);
	}
}
