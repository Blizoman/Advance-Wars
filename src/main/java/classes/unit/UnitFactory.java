package classes.unit;

import classes.player.Player;

public class UnitFactory {
	public Unit createUnit(UnitType type, Player player) {
		if (!player.canAfford(type.getCost()))
			return null;

		player.removeMoney(type.getCost());
		return new Unit(player, type);
	}
}
