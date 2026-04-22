package classes.unit;

import classes.board.Position;
import classes.player.Player;

public class UnitFactory {
	public Unit createUnit(UnitType type, Player player, Position position) {
		return new Unit(player, type, position);
	}
}
