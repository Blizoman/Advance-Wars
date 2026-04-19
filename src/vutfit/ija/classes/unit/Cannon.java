package vutfit.ija.classes.unit;

import java.util.Map;

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
