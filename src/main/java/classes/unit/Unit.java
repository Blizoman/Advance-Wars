package classes.unit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import classes.player.Player;
import tools.Consts;

@RequiredArgsConstructor
public class Unit {

	@Getter
	private final Player player;
	@Getter
	private final UnitType type;
	@Getter
	private int hp = Consts.MAX_HP;

	public boolean isAlive() { return hp > 0; }

	public boolean isDead() { return !isAlive(); }

	public void takeDamage(int damageToTake) {
		this.hp = Math.max(0, this.hp - damageToTake);
	}

	public void heal(int hpToHeal) {
		this.hp = Math.min(this.hp + hpToHeal, Consts.MAX_HP);
	}
}
