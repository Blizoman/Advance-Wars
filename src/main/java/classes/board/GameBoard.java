package classes.board;

import java.util.List;
import java.util.Map;
import classes.unit.Unit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameBoard {
	private final Map<Position, Tile> map;
	@Getter
	private final int width;
	@Getter
	private final int height;

	public Tile getTile(Position position) {
		Tile tile = this.map.get(position);
		if (tile == null)
			throw new IllegalStateException("Invalid position: " + position);
		return tile;
	}

	public List<Tile> getAllTiles() { return this.map.values().stream().toList(); }

	public List<Unit> getAllUnits() {
		return map.values().stream()
				.filter(t -> t.getUnit() != null)
				.map(p -> p.getUnit())
				.toList();
	}

	public Unit getUnit(Position position) {
		return getTile(position).getUnit();
	}

	public void moveUnit(Position from, Position to) {
		Tile fromTile = getTile(from);
		Tile toTile = getTile(to);

		Unit unit = fromTile.getUnit();
		if (unit == null)
			throw new IllegalStateException("No unit found at " + from);

		fromTile.removeUnit();
		toTile.placeUnit(unit);
		unit.setPosition(to);
	}

	public boolean canPlaceUnit(Position wantedPosition) {
		Tile wantedTile = this.getTile(wantedPosition);
		return wantedTile.getTerrain() != Terrain.WATER &&
				wantedTile.isEmpty();
	}

	public void removeUnit(Unit unit) {
		getTile(unit.getPosition()).removeUnit();
	}
}
