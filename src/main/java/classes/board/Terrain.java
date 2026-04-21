package classes.board;

import java.util.Map;
import classes.unit.MovementType;
import lombok.Getter;

public enum Terrain {
	PLAIN(1, Map.of(MovementType.HUMAN, 1, MovementType.VEHICLE, 1)),
	FOREST(2, Map.of(MovementType.HUMAN, 1, MovementType.VEHICLE, 2)),
	MOUNTAIN(4, Map.of(MovementType.HUMAN, 2)),
	WATER(0, Map.of()),
	CITY(3, true, true, false, Map.of(MovementType.HUMAN, 1, MovementType.VEHICLE, 1)),
	FACTORY(3, true, false, true, Map.of(MovementType.HUMAN, 1, MovementType.VEHICLE, 1)),
	HQ(4, true, true, false, Map.of(MovementType.HUMAN, 1, MovementType.VEHICLE, 1));

	@Getter
	private final int defenseBonus;
	@Getter
	private final boolean capturable;
	@Getter
	private final boolean heals;
	@Getter
	private final boolean produceUnits;
	private final Map<MovementType, Integer> movementCosts;

	public Terrain fromString(String from) {
		return Terrain.valueOf(from);
	}

	Terrain(int defenseBonus, Map<MovementType, Integer> movementCosts) {
		this(defenseBonus, false, false, false, movementCosts);
	}

	Terrain(int defenseBonus, boolean capturable, boolean heals,
			boolean produceUnits, Map<MovementType, Integer> movementCosts) {
		this.defenseBonus = defenseBonus;
		this.capturable = capturable;
		this.heals = heals;
		this.produceUnits = produceUnits;
		this.movementCosts = movementCosts;
	}

	public int getCost(MovementType type) {
		return movementCosts.getOrDefault(type, null);
	}

	public boolean isPassable(MovementType type) {
		return movementCosts.containsKey(type);
	}
}
