package classes.board.terrain;

import java.util.Map;
import classes.unit.MovementType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class TerrainType {
	private final TerrainTypeName name;
	private final int defenseBonus;
	private final Map<MovementType, Integer> movementCosts;

	public int getMoveCost(MovementType type) {
		return movementCosts.getOrDefault(type, null);
	}
}
