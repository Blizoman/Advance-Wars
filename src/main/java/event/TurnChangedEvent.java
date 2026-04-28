package event;

import java.util.HashMap;
import java.util.Map;
import game.Game;
import player.Player;
import unit.Unit;

public class TurnChangedEvent implements GameEvent {
	private Map<Player, Integer> moneyBefore = new HashMap<>();
	private Map<Unit, Integer> hpBefore = new HashMap<>();

	public GameEventType type() {
		return GameEventType.TURN_CHANGED;
	}

	public void execute(Game game) {
		// snapshot all players money before
		game.getPlayers().forEach(p -> moneyBefore.put(p, p.getMoney()));
		// snapshot HP for all units before changing turn
		game.getGameBoard().getAllUnits().forEach(u -> hpBefore.put(u, u.getHp()));

		game.forwardTurn();
		game.processIncome();
		game.processUnits();
	}

	public void undo(Game game) {
		// revert turn, money and HP to state before this event
		game.previousTurn();
		moneyBefore.forEach(Player::setMoney);
		hpBefore.forEach(Unit::setHp);
	}
}
