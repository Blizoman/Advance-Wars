package vutfit.ija.classes;

import java.util.Map;

/**
 * Type of Unit
 * 
 * @param name          Name
 * @param maxHp         Maximum and initial Hp
 * @param movementSpeed Allowed speed
 * @param terrainCosts  What each possible terrain type costs to move
 * @param damageAgainst WHat damage it deals against other Units
 */
public record UnitType(
		String name,
		int maxHp,
		int movementSpeed,
		Map<String, Integer> terrainCosts,
		Map<String, Integer> damageAgainst) {

	/**
	 * Get damage against Unit
	 * 
	 * @param target Unit to target
	 * @return Dealable damage
	 */
	public int getDamageAgainst(UnitType target) {
		return getDamageAgainst(target.name);
	}

	/**
	 * Get damage against Unit
	 * 
	 * @param targetName Unit to target
	 * @return Dealable damage
	 */
	private int getDamageAgainst(String targetName) {
		return damageAgainst.getOrDefault(targetName, 0);
	}
}