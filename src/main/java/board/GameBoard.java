/**
 * Represents the main game board, managing the grid of tiles and the units occupying them.
 * It handles spatial queries, validates positions, executes unit movements, and provides 
 * utility methods to retrieve all tiles or units, as well as filtering them by owner.
 *
 * @author xpruzir00
 */

package board;

import java.util.List;
import java.util.Map;
import gamer.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import unit.Unit;

@RequiredArgsConstructor
public class GameBoard {

	private final Map<Position, Tile> map;
	private List<Tile> allTilesCache;

	@Getter
	private final int width;
	@Getter
	private final int height;

	////////////////////////////////////////////////////
	///////////////////// POSITION /////////////////////

	public Tile getTile(Position position) {
		return this.map.get(position);
	}

	public boolean isValidPosition(Position position) {
		return getTile(position) != null;
	}

	public Position getPosition(Tile tile) {
		return this.map.entrySet().stream()
				.filter(e -> e.getValue() == tile)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Invalid tile"))
				.getKey();
	}

	///////////////////// POSITION /////////////////////
	////////////////////////////////////////////////////
	/////////////////////// UNIT ///////////////////////

	public Unit getUnit(Position position) {
		Tile tile = getTile(position);
		if (tile == null)
			return null;
		return tile.getUnit();
	}

	public void moveUnit(Position from, Position to) {
		Tile fromTile = getTile(from);
		Tile toTile = getTile(to);

		Unit unit = fromTile.getUnit();
		if (unit == null)
			throw new IllegalStateException("No unit found at " + from);

		fromTile.removeUnit();
		toTile.setUnit(unit);
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

	public void placeUnit(Unit unit) {
		getTile(unit.getPosition()).setUnit(unit);
	}

	/////////////////////// UNIT ///////////////////////
	////////////////////////////////////////////////////
	//////////////////// GET ALL/OF ////////////////////

	public List<Tile> getAllTiles() {
		if (this.allTilesCache == null)
			this.allTilesCache = List.copyOf(this.map.values());
		return this.allTilesCache;
	}

	public List<Unit> getAllUnits() {
		return map.values().stream()
				.filter(t -> t.getUnit() != null)
				.map(Tile::getUnit)
				.toList();
	}

	public List<Tile> getTilesOf(Player player) {
		return getAllTiles().stream()
				.filter(t -> t.getOwner() == player)
				.toList();
	}

	public List<Unit> getUnitsOf(Player player) {
		return getAllUnits().stream()
				.filter(u -> u.getPlayer() == player)
				.toList();
	}

	//////////////////// GET ALL/OF ////////////////////
	////////////////////////////////////////////////////
}
