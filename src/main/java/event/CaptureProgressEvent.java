/**
 * Represents a game event where a unit makes partial progress in capturing a tile (e.g., City,
 * Factory, HQ). This event records the capture health points (HP) before and after the action,
 * allowing the game session to execute or undo the partial capture progress and update the involved
 * unit's state.
 *
 * @author xpruzir00
 */

package event;

import board.Position;
import game.Game;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import unit.Unit;

@RequiredArgsConstructor
public class CaptureProgressEvent implements GameEvent {
	@Getter
	private final Position position;
	private final int progressBefore;
	private final int progressAfter;

	public GameEventType type() {
		return GameEventType.CAPTURE_PROGRESS;
	}

	public void execute(Game game) {
		setValues(game, progressAfter, true);
	}

	public void undo(Game game) {
		setValues(game, progressBefore, false);
	}

	private void setValues(Game game, int progress, boolean capturing) {
		game.getGameBoard().getTile(position).setCaptureHp(progress);
		Unit capturousUnit = game.getGameBoard().getUnit(position);
		if (capturousUnit != null) {
			capturousUnit.setCaptured(capturing);
			capturousUnit.setUsed(capturing);
		}
	}
}
