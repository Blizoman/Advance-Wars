package player;

import tools.Consts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Player {

	@Getter
	private final String name;
	@Getter
	@Setter
	private int money = Consts.STARTING_MONEY;
	@Getter
	private boolean isAlive = true;
	@Getter
	private final boolean isBot;

	public void addMoney(int amount) {
		this.money += amount;
	}

	public void removeMoney(int amount) {
		this.money -= amount;
	}

	public void kill() {
		this.isAlive = false;
	}

	public boolean canAfford(int cost) {
		return this.money >= cost;
	}

	public void realive() {
		this.isAlive = true;
	}
}
