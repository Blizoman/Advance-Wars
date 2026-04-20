package classes.board.terrain;

import java.util.Map;
import classes.unit.MovementType;

public class HQ extends TerrainType {
	public HQ() {
		super(TerrainTypeName.HQ,
				4,
				Map.of(
						MovementType.HUMAN, 1,
						MovementType.VEHICLE, 1));
	}
}
