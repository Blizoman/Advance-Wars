package tools;

import unit.Unit;

public class EvalDamage {
	private EvalDamage() { /* Prevent instantiation */ }

	public static int evalDamage(Unit attacker, Unit defender, int terrainBonus) {
		int baseDamage = attacker.getType().getDamageAgainst(defender.getType());
		int hpAttacker = attacker.getHp();

		double damageToTake = baseDamage * (hpAttacker / 100.0) * (1 - terrainBonus * 0.1);

		return (int) Math.ceil(damageToTake);
	}
}
