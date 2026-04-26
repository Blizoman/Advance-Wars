package classes.event;

import classes.game.Game;
import classes.player.Player;

public record TurnChangedEvent(Player player) implements GameEvent {
	@Override
	public String toString() {
		return "TurnChangedEvent[player=" + player.getName() + "]";
	}

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
