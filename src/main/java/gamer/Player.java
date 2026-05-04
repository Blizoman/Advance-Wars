/**
 * Player state
 * 
 * @author: xpruzir00
 */

package gamer;

import tools.Consts;
import tools.PlayerColor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import javafx.scene.paint.Color;

@Getter
@RequiredArgsConstructor
public class Player {

	private final String name;
	@Setter
	private int money = Consts.STARTING_MONEY;
	private boolean isAlive = true;
	private final boolean isBot;
	@Setter
	private Color color = PlayerColor.randomColor();

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
