/**
 * Player state
 * 
 * @author: xpruzir00
 */

package gamer;

import tools.Consts;
import tools.PlayerColor;
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
	private Color color = PlayerColor.randomColor();
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
