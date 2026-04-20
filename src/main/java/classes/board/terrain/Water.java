package classes.board.terrain;

import java.util.Map;

public class Water extends TerrainType {
	public Water() {
		super(TerrainTypeName.WATER,
				0,
				Map.of());
	}
}
