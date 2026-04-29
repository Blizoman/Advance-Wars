/**
 * Interface for tracked events
 * 
 * @author: xpruzir00
 */

package event;

import game.Game;

public interface GameEvent {
	GameEventType type();

	void execute(Game game);

	void undo(Game game);
}
