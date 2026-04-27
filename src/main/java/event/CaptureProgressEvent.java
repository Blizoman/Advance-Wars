package event;

import board.Position;
import game.Game;

public record CaptureProgressEvent(Position position, int progressBefore, int progressAfter)
		implements GameEvent {
	public GameEventType type() {
		return GameEventType.CAPTURE_PROGRESS;
	}

	public void execute(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressAfter);
	}

	public void undo(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressBefore);
	}
}
