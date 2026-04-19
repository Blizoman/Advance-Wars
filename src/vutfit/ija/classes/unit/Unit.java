package vutfit.ija.classes.unit;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import vutfit.ija.classes.board.Position;
import vutfit.ija.classes.player.Player;
import vutfit.ija.tools.Consts;

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
	private int hp = Consts.MAX_HP;

	public boolean isAlive() { return hp > 0; }

	public boolean isDead() { return !isAlive(); }

	public void takeDamage(int damageToTake) {
		this.hp = Math.max(0, this.hp - damageToTake);
	}

	public void attack(Unit target) {
		int damageDealt = this.type.getDamageAgainst(target.getType());
		target.takeDamage(damageDealt);
	}
}
