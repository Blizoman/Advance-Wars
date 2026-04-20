package classes.board.terrain;

public enum TerrainTypeName {
	PLAIN,
	FOREST,
	MOUNTAIN,
	WATER,
	CITY,
	FACTORY,
	HQ;

	public String getValue() {
		String name = name();
		return name;
		// return name.charAt(0) + name.substring(1).toLowerCase(); 
	}

	public static TerrainTypeName fromValue(String value) {
		return valueOf(value.toUpperCase());
	}
}
