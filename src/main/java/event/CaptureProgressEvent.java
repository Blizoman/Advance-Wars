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
	private Boolean previousUsed = null;

	public GameEventType type() {
		return GameEventType.CAPTURE_PROGRESS;
	}

	public void execute(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressAfter);
		Unit liveUnit = game.getGameBoard().getUnit(position);
		if (liveUnit != null) {
			previousUsed = liveUnit.isUsed();
			liveUnit.setCaptured(true);
			liveUnit.setUsed(true);
		}
	}

	public void undo(Game game) {
		game.getGameBoard().getTile(position).setCaptureHp(progressBefore);
		Unit liveUnit = game.getGameBoard().getUnit(position);
		if (liveUnit != null) {
			liveUnit.setCaptured(false);
			if (previousUsed != null)
				liveUnit.setUsed(previousUsed);
		}
	}
}
