/**
 * Tracks that turn changed
 * 
 * @author: xpruzir00
 */

package event;

import java.util.ArrayList;
import java.util.List;
import board.Position;
import game.Game;
import gamer.Player;
import lombok.Getter;

public class TurnChangedEvent implements GameEvent {
	private final List<PlayerMoney> moneyBefore = new ArrayList<>();
	private final List<UnitHp> hpBefore = new ArrayList<>();
	@Getter
	private Player playerBefore;
	@Getter
	private Player playerAfter;

	private record PlayerMoney(Player player, int money) {
	}
	private record UnitHp(Position position, int hp) {
	}

	public GameEventType type() {
		return GameEventType.TURN_CHANGED;
	}

	public void execute(Game game) {
		moneyBefore.clear();
		game.getPlayers().forEach(p -> moneyBefore.add(new PlayerMoney(p, p.getMoney())));
		hpBefore.clear();
		game.getGameBoard().getAllUnits()
				.forEach(u -> hpBefore.add(new UnitHp(u.getPosition(), u.getHp())));

		this.playerBefore = game.getActive();
		game.forwardTurn();
		game.processIncome();
		game.processUnits();
		this.playerAfter = game.getActive();
	}

	public void undo(Game game) {
		game.previousTurn();
		hpBefore.forEach(u -> game.getGameBoard().getUnit(u.position()).setHp(u.hp()));
		moneyBefore.forEach(p -> p.player().setMoney(p.money()));
	}
}
