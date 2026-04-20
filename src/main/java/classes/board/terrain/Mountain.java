package classes.board.terrain;

import java.util.Map;
import classes.unit.MovementType;

public class Mountain extends TerrainType {
	public Mountain() {
		super(TerrainTypeName.MOUNTAIN,
				4,
				Map.of(
						MovementType.HUMAN, 2));
	}
}
