/**
 * Defines the static properties, combat statistics, and strategic capabilities 
 * for each class of unit in the game.
 * Each constant represents a unique unit class with specific values for cost, 
 * mobility, and engagement rules. This enum also encapsulates the game's combat 
 * balance through a damage matrix, defining how much base damage each unit 
 * type inflicts upon others.
 *
 * @author xpruzir00
 */

package unit;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public enum UnitType {
	INFANTRY(1000, MovementType.FOOT, 3, new AttackRange(1, 1), true, true),
	TANK(7000, MovementType.VEHICLE, 6, new AttackRange(1, 1), false, true),
	CANNON(6000, MovementType.VEHICLE, 5, new AttackRange(2, 3), false, false);

	private final int cost;
	private final MovementType movementType;
	private final int moveRange;
	private final AttackRange attackRange;
	private final boolean canCapture;
	private final boolean canAttackAfterMove;
	@Getter(AccessLevel.NONE)
	private Map<UnitType, Integer> damageAgainst;

	UnitType(int cost, MovementType movementType, int moveRange,
			AttackRange attackRange, boolean canCapture, boolean canAttackAfterMove) {
		this.cost = cost;
		this.movementType = movementType;
		this.moveRange = moveRange;
		this.attackRange = attackRange;
		this.canCapture = canCapture;
		this.canAttackAfterMove = canAttackAfterMove;
	}

	static {
		INFANTRY.damageAgainst = Map.of(INFANTRY, 55, TANK, 5, CANNON, 15);
		TANK.damageAgainst = Map.of(INFANTRY, 75, TANK, 55, CANNON, 70);
		CANNON.damageAgainst = Map.of(INFANTRY, 90, TANK, 70, CANNON, 75);
	}

	public int getDamageAgainst(UnitType target) {
		return damageAgainst.getOrDefault(target, 0);
	}
}
