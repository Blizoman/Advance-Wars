package event;

import game.Game;

public interface GameEvent {
	GameEventType type();

	void execute(Game game);

	void undo(Game game);
}
