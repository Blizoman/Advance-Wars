package vutfit.ija.classes.player;

import vutfit.ija.tools.consts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Player {

	@Getter
	private final String name;
	@Getter
	private int money = consts.STARTING_MONEY;
	@Getter
	private boolean isAlive = true;

	public void addMoney(int amount) {
		this.money += amount;
	}

	public void kill() {
		this.isAlive = false;
	}
}
