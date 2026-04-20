package classes.unit.type;

public enum UnitTypeName {
	INFANTRY,
	TANK,
	CANNON;

	public String getValue() {
		String name = name();
		return name;
		// return name.charAt(0) + name.substring(1).toLowerCase(); 
	}

	public static UnitTypeName fromValue(String value) {
		return valueOf(value.toUpperCase());
	}
}
