package vutfit.ija.classes.unit;

import java.util.Map;

public class Tank extends UnitType {
	public Tank() {
		super(
				UnitTypeName.TANK,
				7000,
				MovementType.VEHICLE,
				6,
				new AttackRange(1, 1),
				false,
				true,
				Map.of(
						UnitTypeName.INFANTRY, 75,
						UnitTypeName.TANK, 55,
						UnitTypeName.CANNON, 70));
	}
}
