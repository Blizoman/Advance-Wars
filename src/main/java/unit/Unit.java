/**
 * Represents an individual combat or utility unit on the game board. This class maintains the
 * current state of a unit, including its health (HP), position, remaining movement points, and
 * whether it has already performed an action in the current turn. It provides essential methods for
 * combat interaction (taking damage, healing), movement management, and status resets at the
 * beginning of a new turn.
 *
 * @author xpruzir00
 */

package unit;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import board.Position;
import gamer.Player;
import tools.Consts;

@Getter
@Setter
@RequiredArgsConstructor
public class Unit {

	private final Player player;
	private final UnitType type;
	@NonNull
	private Position position;
	private int movesLeft;
	private int hp = Consts.MAX_HP;
	private boolean captured = false;
	private boolean used = false;

	//////////////// VALUES ////////////////
	////////////////////////////////////////
	//////////////// MISCS /////////////////

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
		return this.type.getAttackRange().canReach(distance);
	}

	//////////////// MISCS /////////////////
	////////////////////////////////////////
	//////////////// RESETS ////////////////

	public void resetMovement() {
		this.movesLeft = this.type.getMoveRange();
	}

	public void resetUsed() {
		this.used = false;
	}

	public void resetCapture() {
		this.captured = false;
	}

	//////////////// RESETS ////////////////
	////////////////////////////////////////
}
