package event;

import game.Game;
import unit.Unit;

public class UnitAttackEvent implements GameEvent {
	private final Unit attacker;
	private final Unit defender;
	private int attackerHpBefore;
	private int defenderHpBefore;

	public UnitAttackEvent(Unit attacker, Unit defender) {
		this.attacker = attacker;
		this.defender = defender;
	}

	public GameEventType type() {
		return GameEventType.UNIT_ATTACKED;
	}

	public void execute(Game game) {
		attackerHpBefore = attacker.getHp();
		defenderHpBefore = defender.getHp();

		game.dealDamage(attacker, defender);
		if (defender.isDead())
			game.removeUnit(defender);

		if (defender.isAlive() && defender.canAttackTo(attacker)) {
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
	}

	public gamer.Player getPlayer() {
		return attacker.getPlayer();
	}
}
