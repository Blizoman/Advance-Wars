/**
 * Defines the various types of terrain available on the game board and their specific properties.
 * It dictates movement costs for different unit types (foot, vehicle), defensive bonuses during combat, 
 * and special structure behaviors such as capturability, unit healing, production capabilities, 
 * and income generation.
 *
 * @author xpruzir00
 */

package board;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import unit.MovementType;

@Getter
public enum Terrain {
	PLAIN(1, false, Map.of(MovementType.FOOT, 1, MovementType.VEHICLE, 1)),
	FOREST(2, false, Map.of(MovementType.FOOT, 1, MovementType.VEHICLE, 2)),
	MOUNTAIN(4, false, Map.of(MovementType.FOOT, 2)),
	WATER(0, false, Map.of()),
	CITY(3, true, true, false, true, Map.of(MovementType.FOOT, 1, MovementType.VEHICLE, 1)),
	FACTORY(3, true, true, true, false, Map.of(MovementType.FOOT, 1, MovementType.VEHICLE, 1)),
	HQ(4, true, true, false, false, Map.of(MovementType.FOOT, 1, MovementType.VEHICLE, 1));

	private final int defenseBonus;
	private final boolean capturable;
	private final boolean heals;
	private final boolean produceUnits;
	private final boolean generateIncome;
	@Getter(AccessLevel.NONE)
	private final Map<MovementType, Integer> movementCosts;

	Terrain(int defenseBonus, boolean generateIncome, Map<MovementType, Integer> movementCosts) {
		this(defenseBonus, false, false, false, generateIncome, movementCosts);
	}

	Terrain(int defenseBonus, boolean capturable, boolean heals,
			boolean produceUnits, boolean generateIncome, Map<MovementType, Integer> movementCosts) {
		this.defenseBonus = defenseBonus;
		this.capturable = capturable;
		this.heals = heals;
		this.produceUnits = produceUnits;
		this.generateIncome = generateIncome;
		this.movementCosts = movementCosts;
	}

	public int getMovementCost(MovementType type) {
		Integer movementCost = movementCosts.get(type);
		if (movementCost == null)
			throw new IllegalStateException(type + " cannot go through " + this);
		return movementCost;
	}

	public boolean isPassable(MovementType type) {
		return movementCosts.containsKey(type);
	}
}
