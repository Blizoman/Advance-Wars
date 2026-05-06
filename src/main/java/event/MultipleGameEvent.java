/**
 * Represents a composite game event that groups multiple sub-events together, 
 * primarily used to handle the complex process of player elimination.
 * It executes a sequence of events (e.g., destroying all owned units, losing captured cities) 
 * and removes the player from the active game. When undone, it restores the player to their 
 * original turn index and reverts all sub-events in reverse order to maintain state consistency.
 *
 * @author xpruzir00
 */

package event;

import game.Game;
import gamer.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record MultipleGameEvent(List<GameEvent> events, Player player, int eliminatedIndex)
		implements GameEvent {
	public GameEventType type() {
		return GameEventType.PLAYER_ELIMINATED;
	}

	public void execute(Game game) {
		events.forEach(e -> e.execute(game));

		game.eliminatePlayer(player);
	}

	public void undo(Game game) {
		game.restorePlayer(player, eliminatedIndex);

		List<GameEvent> reversed = new ArrayList<>(events);
		Collections.reverse(reversed);
		reversed.forEach(e -> e.undo(game));
	}
}
