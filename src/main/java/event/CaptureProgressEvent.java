package event;

import board.Position;
import game.Game;
import unit.Unit;

public record CaptureProgressEvent(Position position, int progressBefore, int progressAfter)
		implements GameEvent {
	public GameEventType type() {
		return GameEventType.CAPTURE_PROGRESS;
	}

	public void execute(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressAfter);
		Unit liveUnit = game.getGameBoard().getUnit(position);
		if (liveUnit != null)
			liveUnit.setCaptured(true);
	}

	public void undo(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressBefore);
		Unit liveUnit = game.getGameBoard().getUnit(position);
		if (liveUnit != null)
			liveUnit.setCaptured(false);
	}
}
