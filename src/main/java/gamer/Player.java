/**
 * Represents a player in the game, tracking their current state and resources. It manages the
 * player's identity, financial balance, survival status, assigned color, and whether they are
 * controlled by a human or an AI bot.
 *
 * @author xpruzir00
 * @author xblizna00
 */

package gamer;

import tools.Consts;
import lombok.Getter;
import lombok.Setter;
import javafx.scene.paint.Color;

@Getter
public class Player {

	private final String name;
	@Setter
	private int money = Consts.STARTING_MONEY;
	private boolean isAlive = true;
	private final boolean isBot;
	@Setter
	private Color color;
	@Setter
	private BotType botType = BotType.NONE;

	public Player(String name, boolean isBot) {
		this.name = name;
		this.isBot = isBot;
		this.botType = isBot ? BotType.WEAK : BotType.NONE;
	}

	public void addMoney(int amount) {
		this.money += amount;
	}

	public void removeMoney(int amount) {
		this.money -= amount;
	}

	public boolean canAfford(int cost) {
		return this.money >= cost;
	}

	public void kill() {
		this.isAlive = false;
	}

	public void realive() {
		this.isAlive = true;
	}
}
