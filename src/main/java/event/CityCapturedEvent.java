package event;

import board.Position;
import board.Terrain;
import board.Tile;
import game.Game;
import player.Player;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CityCapturedEvent implements GameEvent {
	private final Position position;
	private final Terrain previousTerrain;
	private final Player newOwner;
	private final Player previousOwner;
	private final int previousCaptureHp;

	public GameEventType type() {
		return GameEventType.CITY_CAPTURED;
	}

	public void execute(Game game) {
		Tile tile = game.getGameBoard().getTile(position);
		if (previousTerrain == Terrain.HQ)
			tile.setTerrain(Terrain.CITY);
		if (newOwner != null)
			tile.setOwner(newOwner);
		else
			tile.unsetOwner();
		tile.resetCapturableHp();
	}

	public void undo(Game game) {
		Tile tile = game.getGameBoard().getTile(position);
		tile.setTerrain(previousTerrain);
		if (previousOwner == null)
			tile.unsetOwner();
		else
			tile.setOwner(previousOwner);
		tile.setCaptureHp(previousCaptureHp);
	}
}
