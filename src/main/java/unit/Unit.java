package unit;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import player.Player;
import board.Position;
import tools.Consts;

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
	@Setter
	private int movesLeft;
	@Getter
	@Setter
	private int hp = Consts.MAX_HP;

	public boolean isAlive() { return hp > 0; }

	public boolean isDead() { return !isAlive(); }

	public void takeDamage(int damageToTake) {
		this.hp = Math.max(0, this.hp - damageToTake);
	}

	public void heal(int hpToHeal) {
		this.hp = Math.min(this.hp + hpToHeal, Consts.MAX_HP);
	}

	public boolean canAttackTo(Unit defender) {
		int distance = this.position.distanceTo(defender.getPosition());
		AttackRange range = this.type.getAttackRange();
		return range.min() <= distance && distance <= range.max();
	}

	public void resetMovement() {
		this.movesLeft = this.type.getMoveRange();
	}
}
