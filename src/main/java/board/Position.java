package board;

// Starting at [0,0] in TOP-LEFT corner
public record Position(int x, int y) {
	@Override
	public final String toString() {
		return "[" + x + "," + y + "]";
	}

	public int distanceTo(Position to) {
		return Math.abs(this.x - to.x()) + Math.abs(this.y - to.y());	}
}
