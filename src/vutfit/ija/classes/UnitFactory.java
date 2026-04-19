package vutfit.ija.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vutfit.ija.classes.player.Player;

public class UnitFactory {
	/** Only for homework2, i guess in project this wont be needed */
	private final int DEFAULT_MAX_HP = 100;
	private final int DEFAULT_TANK_SPEED = 6;
	private final int DEFAULT_INFANTRY_SPEED = 3;

	/** All Unit types in game */
	private List<UnitType> unitTypes = new ArrayList<>();

	public UnitFactory() {
		Map<String, Integer> tankCosts = Map.of("P", 1, "F", 2);
		Map<String, Integer> infantryCosts = Map.of("P", 1, "F", 1, "M", 2);

		unitTypes.add(new UnitType("Tank", DEFAULT_MAX_HP, DEFAULT_TANK_SPEED, tankCosts, Map.of()));
		unitTypes.add(new UnitType("Infantry", DEFAULT_MAX_HP, DEFAULT_INFANTRY_SPEED, infantryCosts,
				Map.of()));
	}

	/**
	 * Creates unit
	 * 
	 * @param typeName Type of unit
	 * @param player Owner
	 * @param posX X position of Unit
	 * @param posY Y position of Unit
	 * @return Created Unit
	 */
	public Unit createUnit(String typeName, Player player, int posX, int posY) {
		UnitType unitType = getTypeByName(typeName);
		if (unitType == null)
			return null;

		Unit unit = new Unit(player, unitType, new Position(posX, posY));

		return unit;
	}

	/**
	 * Obtains type by name
	 * 
	 * @param typeName Type name
	 * @return Type from name
	 */
	private UnitType getTypeByName(String typeName) {
		for (UnitType type : this.unitTypes) {
			if (type.name().equals(typeName)) {
				return type;
			}
		}
		return null;
	}
}
