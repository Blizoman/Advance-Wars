package event;

import java.util.ArrayList;
import java.util.List;
import board.Position;
import game.Game;
import gamer.Player;
import lombok.Getter;
import unit.Unit;

public class TurnChangedEvent implements GameEvent {
	private final List<PlayerMoney> moneyBefore = new ArrayList<>();
	private final List<UnitHp> hpBefore = new ArrayList<>();
	@Getter
	private gamer.Player playerBefore;
	@Getter
	private gamer.Player playerAfter;

	private record PlayerMoney(Player player, int money) {}
	private record UnitHp(Position position, int hp) {}

	public GameEventType type() {
		return GameEventType.TURN_CHANGED;
	}

	public void execute(Game game) {
		// snapshot all players money before
		moneyBefore.clear();
		game.getPlayers().forEach(p -> moneyBefore.add(new PlayerMoney(p, p.getMoney())));
		// snapshot HP for all units before changing turn
		hpBefore.clear();
		game.getGameBoard().getAllUnits().forEach(u -> hpBefore.add(new UnitHp(u.getPosition(), u.getHp())));

		this.playerBefore = game.getActive();
		game.forwardTurn();
		game.processIncome();
		game.processUnits();
		this.playerAfter = game.getActive();
	}

	public void undo(Game game) {
		// revert turn, money and HP to state before this event
		game.previousTurn();
		moneyBefore.forEach(pm -> pm.player().setMoney(pm.money()));
		hpBefore.forEach(uhp -> {
			Unit unit = game.getGameBoard().getUnit(uhp.position());
			if (unit != null)
				unit.setHp(uhp.hp());
		});
	}
}
