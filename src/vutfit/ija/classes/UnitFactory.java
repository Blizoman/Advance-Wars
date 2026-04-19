package vutfit.ija.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vutfit.ija.classes.player.Player;
import vutfit.ija.tools.consts;

public class UnitFactory {
	private final int DEFAULT_TANK_SPEED = 6;
	private final int DEFAULT_INFANTRY_SPEED = 3;

	private List<UnitType> unitTypes = new ArrayList<>();

	public UnitFactory() {
		Map<String, Integer> tankCosts = Map.of("P", 1, "F", 2);
		Map<String, Integer> infantryCosts = Map.of("P", 1, "F", 1, "M", 2);

		unitTypes.add(new UnitType("Tank", consts.MAX_HP, DEFAULT_TANK_SPEED, tankCosts, Map.of()));
		unitTypes.add(new UnitType("Infantry", consts.MAX_HP, DEFAULT_INFANTRY_SPEED, infantryCosts,
				Map.of()));
	}

	public Unit createUnit(String typeName, Player player, int posX, int posY) {
		UnitType unitType = getTypeByName(typeName);
		if (unitType == null)
			return null;

		Unit unit = new Unit(player, unitType, new Position(posX, posY));

		return unit;
	}

	private UnitType getTypeByName(String typeName) {
		for (UnitType type : this.unitTypes) {
			if (type.name().equals(typeName))
				return type;
		}
		return null;
	}
}
