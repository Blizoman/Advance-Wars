package event;

import board.Position;
import board.Tile;
import game.Game;
import lombok.RequiredArgsConstructor;
import unit.Unit;

@RequiredArgsConstructor
public class UnitMovedEvent implements GameEvent {
	private final Position from;
	private final Position to;
	private final int movesLeftBefore;
	private final int actualCost;
	private Integer fromCaptureHpBeforeReset;

	public GameEventType type() {
		return GameEventType.UNIT_MOVED;
	}

	public void execute(Game game) {
		Unit unit = game.getGameBoard().getUnit(from);
		Tile fromTile = game.getGameBoard().getTile(from);
		fromCaptureHpBeforeReset = null;
		if (fromTile.getTerrain().isCapturable() && fromTile.getOwner() != unit.getPlayer()) {
			fromCaptureHpBeforeReset = fromTile.getCaptureHp();
			fromTile.resetCapturableHp();
		}
		unit.setMovesLeft(movesLeftBefore - actualCost);
		game.moveUnit(unit, to);
	}

	public void undo(Game game) {
		Unit unit = game.getGameBoard().getUnit(to);
		game.moveUnit(unit, from);
		if (fromCaptureHpBeforeReset != null)
			game.getGameBoard().getTile(from).setCaptureHp(fromCaptureHpBeforeReset);
		unit.setMovesLeft(movesLeftBefore);
	}
}
