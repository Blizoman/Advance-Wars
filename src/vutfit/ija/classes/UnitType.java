package vutfit.ija.classes;

import java.util.Map;

public record UnitType(
		String name,
		int maxHp,
		int movementSpeed,
		Map<String, Integer> terrainCosts,
		Map<String, Integer> damageAgainst) {

	public int getDamageAgainst(UnitType target) {
		return getDamageAgainst(target.name);
	}

	private int getDamageAgainst(String targetName) {
		return damageAgainst.getOrDefault(targetName, 0);
	}
}
