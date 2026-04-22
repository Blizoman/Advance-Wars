package classes.unit;

import java.util.Map;
import lombok.Getter;

public enum UnitType {
	INFANTRY(1000, MovementType.HUMAN, 3, new AttackRange(1, 1), true, true),
	TANK(7000, MovementType.VEHICLE, 6, new AttackRange(1, 1), false, true),
	CANNON(6000, MovementType.VEHICLE, 5, new AttackRange(2, 3), false, false);

	@Getter
	private final int cost;
	@Getter
	private final MovementType movementType;
	@Getter
	private final int moveRange;
	@Getter
	private final AttackRange attackRange;
	@Getter
	private final boolean canCapture;
	@Getter
	private final boolean canAttackAfterMove;
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
