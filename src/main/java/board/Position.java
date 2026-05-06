/**
 * Represents a two-dimensional coordinate on the game board. The grid uses a top-left origin
 * system, where [0,0] is the top-left corner. Provides utility methods for string formatting and
 * calculating the Manhattan distance between two positions.
 *
 * @author xpruzir00
 */

package board;

// Starting at [0,0] in TOP-LEFT corner
public record Position(int x, int y) {
	@Override
	public final String toString() {
		return "[" + x + "," + y + "]";
	}

	public int distanceTo(Position to) {
		return Math.abs(this.x - to.x()) + Math.abs(this.y - to.y());
	}
}
