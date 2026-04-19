package vutfit.ija.classes.unit;

import java.util.Map;
import vutfit.ija.classes.board.Position;
import vutfit.ija.classes.player.Player;

public class UnitFactory {
	private final Map<UnitTypeName, UnitType> unitTypes = Map.of(
			UnitTypeName.INFANTRY, new Infantry(),
			UnitTypeName.TANK, new Tank(),
			UnitTypeName.CANNON, new Cannon());

	public Unit createUnit(UnitTypeName typeName, Player player, Position position) {
		return new Unit(player, unitTypes.get(typeName), position);
	}
}
