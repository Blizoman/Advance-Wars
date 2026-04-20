package classes.board.terrain;

import java.util.Map;
import classes.unit.MovementType;

public class Forest extends TerrainType {
	public Forest() {
		super(TerrainTypeName.FOREST,
				2,
				Map.of(
						MovementType.HUMAN, 1,
						MovementType.VEHICLE, 2));
	}
}
