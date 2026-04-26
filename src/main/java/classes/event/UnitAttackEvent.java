package classes.event;

import classes.game.Game;
import classes.unit.Unit;

public record UnitAttackEvent(
		Unit attacker, int attackerHpBefore,
		Unit defender, int defenderHpBefore,
		int damageDealt, int damageReceived
) implements GameEvent {
	public GameEventType type() {
		return GameEventType.UNIT_ATTACKED;
	}

	public void execute(Game game) {
		Unit atk = game.getGameBoard().getUnit(attacker.getPosition());
		Unit def = game.getGameBoard().getUnit(defender.getPosition());
		game.dealDamage(atk, def);
		if (def.isDead())
			game.removeUnit(def);
		else if (def.canAttackTo(atk)) {
			game.dealDamage(def, atk);
			if (atk.isDead())
				game.removeUnit(atk);
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
}
