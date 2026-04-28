package event;

import game.Game;
import gamer.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record MultipleGameEvent(List<GameEvent> events, Player player) implements GameEvent {
	public GameEventType type() {
		return GameEventType.PLAYER_ELIMINATED;
	}

	public void execute(Game game) {
		events.forEach(e -> e.execute(game));
		game.eliminatePlayer(player);
	}

	public void undo(Game game) {
		game.restorePlayer(player);

		List<GameEvent> reversed = new ArrayList<>(events);
		Collections.reverse(reversed);
		reversed.forEach(e -> e.undo(game));
	}
}
