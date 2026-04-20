package classes.unit;

import java.util.Map;
import classes.player.Player;
import classes.unit.type.Cannon;
import classes.unit.type.Infantry;
import classes.unit.type.Tank;
import classes.unit.type.UnitType;
import classes.unit.type.UnitTypeName;

public class UnitFactory {
	private final Map<UnitTypeName, UnitType> unitTypes = Map.of(
			UnitTypeName.INFANTRY, new Infantry(),
			UnitTypeName.TANK, new Tank(),
			UnitTypeName.CANNON, new Cannon());

	public Unit createUnit(UnitTypeName typeName, Player player) {
		return new Unit(player, unitTypes.get(typeName));
	}
}
