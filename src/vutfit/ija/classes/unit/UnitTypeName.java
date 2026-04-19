package vutfit.ija.classes.unit;

import lombok.Getter;
import vutfit.ija.tools.Consts;

public enum UnitTypeName {
	INFANTRY(Consts.UnitNames.INFANTRY),
	TANK(Consts.UnitNames.TANK),
	CANNON(Consts.UnitNames.CANNON);

	@Getter
	private final String value;

	UnitTypeName(String value) {
		this.value = value;
	}

	public static UnitTypeName fromValue(String value) {
		for (UnitTypeName typeName : values()) {
			if (typeName.value.equals(value))
				return typeName;
		}
		throw new IllegalArgumentException("Unknown unit type name >" + value + "<");
	}
}
