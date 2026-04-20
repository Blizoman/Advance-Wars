package classes.board.terrain;

import java.util.Map;
import classes.unit.MovementType;

public class City extends TerrainType {
	public City() {
		super(TerrainTypeName.CITY,
				3,
				Map.of(
						MovementType.HUMAN, 1,
						MovementType.VEHICLE, 1));
	}
}
