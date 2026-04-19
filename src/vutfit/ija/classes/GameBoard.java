package vutfit.ija.classes;

import java.util.HashMap;
import java.util.Map;

public class GameBoard {

	//////////////////////////////
	/////////// VALUES ///////////

	/** Playable map */
	private Map<Position, Tile> map = new HashMap<>();

	/////////// VALUES ///////////
	//////////////////////////////
	////////// GETTERS ///////////

	/**
	 * Obtain tile at position
	 * 
	 * @param position Wanted position
	 * @return Tile at position
	 */
	public Tile getTile(Position position) {
		return this.map.get(position);
	}

	/**
	 * Obtain Unit at specific position
	 * 
	 * @param position Wanted position
	 * @return Unit at position or null
	 */
	public Unit getUnit(Position position) {
		Tile tile = getTile(position);
		if (tile == null)
			return null;
		else
			return tile.getUnit();
	}

	////////// GETTERS ///////////
	//////////////////////////////
	////////// SETTERS ///////////

	/**
	 * Constructor
	 * 
	 * @param map Map of game
	 */
	public GameBoard(Map<Position, Tile> map) {
		this.map = map;
	}

	/**
	 * CHange Unit's position
	 * 
	 * @param from Original position
	 * @param to   New positioin
	 * @return Successness of operation
	 */
	public boolean moveUnit(Position from, Position to) {
		Tile fromTile = map.get(from);
		Tile toTile = map.get(to);

		if (fromTile == null || toTile == null || fromTile.isEmpty()) {
			return false;
		}

		Unit unit = fromTile.removeUnit();
		toTile.placeUnit(unit);

		unit.setPosition(to);

		return true;
	}

	////////// SETTERS ///////////
	//////////////////////////////
}
