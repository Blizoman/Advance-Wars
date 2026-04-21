package classes.board;

// Starting at [0,0] in TOP-LEFT corner
public record Position(int x, int y) {
	@Override
	public final String toString() {
		return "[" + x + "," + y + "]";
	}
}
