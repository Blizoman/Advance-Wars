package classes.board;

import java.nio.channels.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
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

	public void moveUnit(Position from, Position to)
			throws IndexOutOfBoundsException, NoSuchObjectException, IllegalAccessException {
		Tile fromTile = map.get(from);
		Tile toTile = map.get(to);

		if (fromTile == null || toTile == null)
			throw new IndexOutOfBoundsException("Invalid location: " + from + " -> " + to);
		if (!toTile.isEmpty())
			throw new IllegalAccessException("Tile " + to + " already occupied");

		Unit unit = fromTile.getUnit();
		if (unit == null)
			throw new NoSuchObjectException("No unit found at " + from);

		fromTile.removeUnit();
		toTile.placeUnit(unit);
	}

	public boolean canPlaceUnit(Position wantedPosition) {
		Tile wantedTile = this.getTile(wantedPosition);
		return wantedTile.getTerrain() != Terrain.WATER &&
				wantedTile.isEmpty();
	}
}
