package event;

import board.Position;
import game.Game;
import unit.Unit;

public record CaptureProgressEvent(Position position, int progressBefore, int progressAfter, Unit unit)
		implements GameEvent {
	public GameEventType type() {
		return GameEventType.CAPTURE_PROGRESS;
	}

	public void execute(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressAfter);
		if (unit != null)
			unit.setCaptured(true);
	}

	public void undo(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressBefore);
		if (unit != null)
			unit.setCaptured(false);
	}
}
