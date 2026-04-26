package classes.event;

import classes.board.Position;
import classes.board.Terrain;
import classes.board.Tile;
import classes.game.Game;
import classes.player.Player;

public record CityCapturedEvent(
		Position position, Terrain previousTerrain, Player newOwner, Player previousOwner
) implements GameEvent {
	public GameEventType type() {
		return GameEventType.CITY_CAPTURED;
	}

	public void execute(Game game) {
		Tile tile = game.getGameBoard().getTile(position);
		if (previousTerrain == Terrain.HQ)
			tile.setTerrain(Terrain.CITY);
		tile.setOwner(newOwner);
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
