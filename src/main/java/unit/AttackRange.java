/**
 * Defines the operational distance within which a unit can engage an enemy. This record stores the
 * minimum and maximum attack range, allowing the game to distinguish between direct combat units
 * (e.g., Infantry) and long-range artillery that may have a minimum range requirement.
 *
 * @author xpruzir00
 */

package unit;

public record AttackRange(
		int min,
		int max
) {
	public boolean canReach(int distance) {
		return distance >= min && distance <= max;
	}
}
