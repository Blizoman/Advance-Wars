package classes.unit.type;

import java.util.Map;
import classes.unit.AttackRange;
import classes.unit.MovementType;

public class Cannon extends UnitType {
	public Cannon() {
		super(
				UnitTypeName.CANNON,
				6000,
				MovementType.VEHICLE,
				5,
				new AttackRange(2, 3),
				false,
				false,
				Map.of(
						UnitTypeName.INFANTRY, 90,
						UnitTypeName.TANK, 70,
						UnitTypeName.CANNON, 75));
	}
}
