package classes.board.terrain;

import java.util.Map;
import classes.unit.MovementType;

public class Factory extends TerrainType {
	public Factory() {
		super(TerrainTypeName.FACTORY,
				3,
				Map.of(
						MovementType.HUMAN, 1,
						MovementType.VEHICLE, 1));
	}
}
