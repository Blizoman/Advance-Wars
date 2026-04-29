package event;

import game.Game;
import lombok.RequiredArgsConstructor;
import unit.Unit;

@RequiredArgsConstructor
public class UnitAttackEvent implements GameEvent {
	private final Unit attacker;
	private final Unit defender;
	private int attackerHpBefore;
	private int defenderHpBefore;

	public GameEventType type() {
		return GameEventType.UNIT_ATTACKED;
	}

	public void execute(Game game) {
		attackerHpBefore = attacker.getHp();
		defenderHpBefore = defender.getHp();

		game.dealDamage(attacker, defender);
		attacker.setMovesLeft(0);
		attacker.setUsed(true);
		if (defender.isDead()) {
			game.removeUnit(defender);
			return;
		}

		if (defender.canAttackTo(attacker)) {
			game.dealDamage(defender, attacker);
			if (attacker.isDead())
				game.removeUnit(attacker);
		}
	}

	public void undo(Game game) {
		if (defender.isDead())
			game.placeUnit(defender);
		if (attacker.isDead())
			game.placeUnit(attacker);
		attacker.setHp(attackerHpBefore);
		defender.setHp(defenderHpBefore);
		attacker.setMovesLeft(attacker.getType().getMoveRange());
		attacker.setUsed(false);
	}

	public gamer.Player getPlayer() { return attacker.getPlayer(); }
}
