package gamer;

public enum BotType {
	NONE("Human"),
	WEAK("Easy"),
	STRONG("Medium");

	private final String label;

	BotType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
