package vutfit.ija.classes.unit;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class UnitType {
	private final UnitTypeName name;
	private final int cost;
	private final MovementType movementType;
	private final int moveRange;
	private final AttackRange attackRange;
	private final boolean canCapture;
	private final boolean canAttackAfterMove;
	private final Map<UnitTypeName, Integer> damageAgainst;

	public int getDamageAgainst(UnitType target) {
		return damageAgainst.getOrDefault(target.name, 0);
	}
}
