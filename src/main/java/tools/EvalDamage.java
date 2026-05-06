/**
 * Utility class responsible for calculating combat damage between units. The damage calculation
 * logic accounts for the base damage specific to the unit types, the attacker's current health
 * percentage, and the defensive bonus provided by the defender's terrain. It ensures that combat
 * outcomes are consistent with the game's tactical balance.
 *
 * @author xpruzir00
 */

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
