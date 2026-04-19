package classes.unit;

public record AttackRange(
		int min,
		int max
) {
	public boolean canReach(int distance) {
		return distance >= min && distance <= max;
	}
}
