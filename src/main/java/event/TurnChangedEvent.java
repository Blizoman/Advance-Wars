package event;

import java.util.HashMap;
import java.util.Map;
import game.Game;
import player.Player;
import unit.Unit;

public class TurnChangedEvent implements GameEvent {
	private Map<Player, Integer> moneyBefore = new HashMap<>();
	private Map<Player, Integer> moneyAfter = new HashMap<>();
	private Map<Unit, Integer> hpBefore = new HashMap<>();
	private Map<Unit, Integer> hpAfter = new HashMap<>();

	public GameEventType type() {
		return GameEventType.TURN_CHANGED;
	}

	public void execute(Game game) {
		// snapshot all players money before
		game.getPlayers().forEach(p -> moneyBefore.put(p, p.getMoney()));
		game.getGameBoard().getUnitsOf(game.getActive())
				.forEach(u -> hpBefore.put(u, u.getHp()));

		game.forwardTurn();
		game.processIncome();
		game.processUnits();

		// snapshot all players money after
		game.getPlayers().forEach(p -> moneyAfter.put(p, p.getMoney()));
		game.getGameBoard().getUnitsOf(game.getActive())
				.forEach(u -> hpAfter.put(u, u.getHp()));
	}

	public void undo(Game game) {
		hpAfter.forEach(Unit::setHp);
		moneyAfter.forEach(Player::setMoney);
		game.previousTurn();
		moneyBefore.forEach(Player::setMoney);
		hpBefore.forEach(Unit::setHp);
	}
}
