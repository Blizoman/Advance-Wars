/**
 * Represents a game event where a unit moves from one position to another on the board. This event
 * manages the physical relocation of the unit, updates its remaining movement points, and handles
 * the resetting of capture progress if a unit moves away from a partially captured structure. It
 * stores the necessary state (previous position, movement points, and capture progress) to allow
 * for a seamless reversal of the move during an undo operation.
 *
 * @author xpruzir00
 */

package event;

import board.Position;
import board.Tile;
import game.Game;
import gamer.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import unit.Unit;

@RequiredArgsConstructor
public class UnitMovedEvent implements GameEvent {
	private final Position from;
	private final Position to;
	private final int movesLeftBefore;
	private Integer fromCaptureHpBeforeReset;
	@Getter
	private final Player player;

	public GameEventType type() {
		return GameEventType.UNIT_MOVED;
	}

	public void execute(Game game) {
		Unit unit = game.getGameBoard().getUnit(from);
		if (from.equals(to))
			return;
		Tile fromTile = game.getGameBoard().getTile(from);
		fromCaptureHpBeforeReset = null;
		if (fromTile.getTerrain().isCapturable() && fromTile.getOwner() != unit.getPlayer()) {
			fromCaptureHpBeforeReset = fromTile.getCaptureHp();
			fromTile.resetCapturableHp();
		}
		unit.setMovesLeft(0);
		game.moveUnit(unit, to);
	}

	public void undo(Game game) {
		if (from.equals(to))
			return;
		Unit unit = game.getGameBoard().getUnit(to);
		game.moveUnit(unit, from);
		if (fromCaptureHpBeforeReset != null)
			game.getGameBoard().getTile(from).setCaptureHp(fromCaptureHpBeforeReset);
		unit.setMovesLeft(movesLeftBefore);
	}
}
