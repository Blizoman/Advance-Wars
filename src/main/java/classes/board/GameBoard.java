package classes.board;

import java.rmi.NoSuchObjectException;
import java.util.Map;
import classes.unit.Unit;
import lombok.RequiredArgsConstructor;
import tools.P;

@RequiredArgsConstructor
public class GameBoard {
	private final Map<Position, Tile> map;
	private final int width;
	private final int height;

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

	public void printMap() {
		P.println("MAP h×w " + height + "×" + width + " :");
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Tile tile = map.get(new Position(x, y));
				Terrain terrain = tile.getTerrain();
				String symbol = terrain.name().substring(0, 1);
				String colorCode = switch (terrain) {
					case WATER -> "\u001B[34m";
					case PLAIN -> "\u001B[92m";
					case FOREST -> "\u001B[32m";
					case MOUNTAIN -> "\u001B[97m";
					case HQ -> "\u001B[38;5;208m";
					case FACTORY -> "\u001B[31m";
					case CITY -> "\u001B[33m";
				};
				String RESET = "\u001B[0m";

				P.print(colorCode + symbol + RESET);
				P.print(" ");
			}
			P.eprintln();
		}
	}
}
