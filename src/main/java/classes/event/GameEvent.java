package classes.event;

import classes.game.Game;

public interface GameEvent {
	GameEventType type();

	void execute(Game game);

	void undo(Game game);
}
