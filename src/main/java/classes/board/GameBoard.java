package classes.board;

import java.util.Map;
import classes.unit.Unit;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameBoard {
	private final Map<Position, Tile> map;

	public Tile getTile(Position position) {
		return this.map.get(position);
	}

	public Unit getUnit(Position position) {
		Tile tile = getTile(position);
		return tile == null ? null : tile.getUnit();
	}

	public boolean moveUnit(Position from, Position to) {
		Tile fromTile = map.get(from);
		Tile toTile = map.get(to);
		if (fromTile == null || toTile == null || !toTile.isEmpty())
			return false;

		Unit unit = fromTile.getUnit();
		if (unit == null)
			return false;

		return toTile.placeUnit(unit);
	}

	public boolean canPlaceUnit(Position wantedPosition) {
		Tile wantedTile = this.getTile(wantedPosition);
		return wantedTile.getTerrain() != Terrain.WATER &&
				wantedTile.isEmpty();
	}
}
