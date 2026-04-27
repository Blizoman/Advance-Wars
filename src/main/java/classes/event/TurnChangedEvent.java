package classes.event;

import java.util.HashMap;
import java.util.Map;
import classes.game.Game;
import classes.player.Player;
import classes.unit.Unit;

public class TurnChangedEvent implements GameEvent {
	private final Player player;
	private int moneyBefore;
	private int moneyAfter;
	private Map<Unit, Integer> hpBefore = new HashMap<>();
	private Map<Unit, Integer> hpAfter = new HashMap<>();

	public TurnChangedEvent(Player player) {
		this.player = player;
	}

	public GameEventType type() {
		return GameEventType.TURN_CHANGED;
	}

	public void execute(Game game) {
		moneyBefore = game.getActive().getMoney();
		game.getGameBoard().getUnitsOf(game.getActive())
				.forEach(u -> hpBefore.put(u, u.getHp()));

		game.forwardTurn();
		game.processIncome();
		game.processUnits();

		moneyAfter = game.getActive().getMoney();
		game.getGameBoard().getUnitsOf(game.getActive())
				.forEach(u -> hpAfter.put(u, u.getHp()));
	}

	public void undo(Game game) {
		hpAfter.forEach((unit, hp) -> unit.setHp(hp));
		game.getActive().setMoney(moneyBefore);
		game.previousTurn();
	}
}
