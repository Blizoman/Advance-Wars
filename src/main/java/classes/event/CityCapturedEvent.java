package classes.event;

import classes.board.Position;
import classes.board.Terrain;
import classes.board.Tile;
import classes.game.Game;
import classes.player.Player;
import classes.unit.Unit;

public class CityCapturedEvent implements GameEvent {
	private final Position position;
	private final Terrain previousTerrain;
	private final Player newOwner;
	private final Player previousOwner;
	private final Unit capturingUnit;

	public CityCapturedEvent(Position position, Terrain previousTerrain,
			Player newOwner, Player previousOwner, Unit capturingUnit) {
		this.position = position;
		this.previousTerrain = previousTerrain;
		this.newOwner = newOwner;
		this.previousOwner = previousOwner;
		this.capturingUnit = capturingUnit;
	}

	public GameEventType type() {
		return GameEventType.CITY_CAPTURED;
	}

	public void execute(Game game) {
		Tile tile = game.getGameBoard().getTile(position);
		if (capturingUnit != null) {
			game.capture(tile, capturingUnit);
		} else {
			if (previousTerrain == Terrain.HQ)
				tile.setTerrain(Terrain.CITY);
			tile.setOwner(newOwner);
		}
	}

	public void undo(Game game) {
		Tile tile = game.getGameBoard().getTile(position);
		tile.setTerrain(previousTerrain);
		if (previousOwner == null)
			tile.unsetOwner();
		else
			tile.setOwner(previousOwner);
	}
}
