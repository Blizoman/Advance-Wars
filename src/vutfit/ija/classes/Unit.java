package vutfit.ija.classes;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import vutfit.ija.classes.player.Player;
import vutfit.ija.tools.consts;

@RequiredArgsConstructor
public class Unit {

	@Getter
	private final Player player;
	@Getter
	private final UnitType type;
	@Getter
	@Setter
	@NonNull
	private Position position;
	@Getter
	private int hp = consts.MAX_HP;

	public void takeDamage(int damageToTake) {
		this.hp -= damageToTake;
		if (this.hp <= 0) {
			this.hp = 0;
		}
	}

	public void attack(Unit target) {
		int damageDealt = this.type.getDamageAgainst(target.getType());
		target.takeDamage(damageDealt);
	}
}
