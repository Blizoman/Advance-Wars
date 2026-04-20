package classes.board.terrain;

import java.util.Map;
import classes.unit.MovementType;

public class Plain extends TerrainType {
	public Plain() {
		super(TerrainTypeName.PLAIN,
				1,
				Map.of(
						MovementType.HUMAN, 1,
						MovementType.VEHICLE, 1));
	}
}
