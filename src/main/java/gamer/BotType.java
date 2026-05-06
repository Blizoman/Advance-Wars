/**
 * Defines the available types of AI bots and their difficulty levels.
 *
 * @author xblizna00
 */

package gamer;

public enum BotType {
	NONE("Human"),
	WEAK("Easy"),
	STRONG("Medium");

	private final String label;

	BotType(String label) {
		this.label = label;
	}

	public String getLabel() { return label; }
}
