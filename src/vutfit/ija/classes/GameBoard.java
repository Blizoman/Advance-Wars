package vutfit.ija.classes;

import java.util.Map;
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

		if (fromTile == null || toTile == null || fromTile.isEmpty())
			return false;

		Unit unit = fromTile.removeUnit();
		toTile.placeUnit(unit);
		unit.setPosition(to);

		return true;
	}
}
