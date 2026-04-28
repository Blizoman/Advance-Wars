package event;

import board.Position;
import board.Terrain;
import board.Tile;
import game.Game;
import gamer.Player;
import lombok.RequiredArgsConstructor;
import unit.Unit;

@RequiredArgsConstructor
public class CityCapturedEvent implements GameEvent {
	private final Position position;
	private final Terrain previousTerrain;
	private final Player newOwner;
	private final Player previousOwner;
	private final int previousCaptureHp;
	private final Unit unit; // nullable - null when tile lost due to player elimination

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
		if (unit != null)
			unit.setCaptured(true);
	}

	public void undo(Game game) {
		Tile tile = game.getGameBoard().getTile(position);
		tile.setTerrain(previousTerrain);
		if (previousOwner == null)
			tile.unsetOwner();
		else
			tile.setOwner(previousOwner);
		tile.setCaptureHp(previousCaptureHp);
		if (unit != null)
			unit.setCaptured(false);
	}

	public Player getPlayer() {
		if (unit != null)
			return unit.getPlayer();
		return previousOwner;
	}
}
