package classes.unit.type;

import java.util.Map;
import classes.unit.AttackRange;
import classes.unit.MovementType;

public class Infantry extends UnitType {
	public Infantry() {
		super(
				UnitTypeName.INFANTRY,
				1000,
				MovementType.HUMAN,
				3,
				new AttackRange(1, 1),
				true,
				true,
				Map.of(
						UnitTypeName.INFANTRY, 55,
						UnitTypeName.TANK, 5,
						UnitTypeName.CANNON, 15));
	}
}
