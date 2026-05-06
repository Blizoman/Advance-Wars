/**
 * Defines the contract for all trackable events within the game session. Implementations of this
 * interface represent specific actions (e.g., moving, attacking, capturing) and must provide the
 * logic to execute the action, cleanly undo it, and identify their event type. This forms the
 * foundation of the game's undo/redo and replay systems.
 *
 * @author xpruzir00
 */

package event;

import game.Game;

public interface GameEvent {
	GameEventType type();

	void execute(Game game);

	void undo(Game game);
}
