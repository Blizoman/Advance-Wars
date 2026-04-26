package classes.event;

import classes.game.Game;

public record TurnChangedEvent() implements GameEvent {
	public GameEventType type() {
		return GameEventType.TURN_CHANGED;
	}

	public void execute(Game game) {
		game.forwardTurn();
	}

	public void undo(Game game) {
		game.previousTurn();
	}
}
