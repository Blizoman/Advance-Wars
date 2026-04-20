package classes.board;

import java.util.Map;
import classes.unit.Unit;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameBoard {
	private Map<Position, Tile> map;

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
		Unit unit = fromTile.getUnit();

		if (fromTile == null || toTile == null || unit == null || !toTile.isEmpty())
			return false;

		return toTile.placeUnit(unit);
	}
}
