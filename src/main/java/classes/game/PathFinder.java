package classes.game;

import java.util.*;
import classes.board.GameBoard;
import classes.board.Position;
import classes.board.Tile;
import classes.player.Player;
import classes.unit.Unit;

public class PathFinder {
	private static final int NOT_PASSABLE = 1_000_000;
	private final GameBoard board;

	public PathFinder(GameBoard board) {
		this.board = board;
	}

	public Set<Position> findReachableTiles(Unit unit) {
		Position start = unit.getPosition();
		int maxRange = unit.getMovesLeft();
		Player player = unit.getPlayer();

		PriorityQueue<Node> openSet = new PriorityQueue<>();
		Map<Position, Integer> costAt = new HashMap<>();

		openSet.add(new Node(start, 0));
		costAt.put(start, 0);

		while (!openSet.isEmpty()) {
			Node current = openSet.poll();
			if (current.cost > costAt.getOrDefault(current.pos, Integer.MAX_VALUE))
				continue;

			for (Position neighbor : getNeighbors(current.pos)) {
				Unit unitAtNeighbor = board.getUnit(neighbor);
				if (unitAtNeighbor != null && unitAtNeighbor.getPlayer() != player)
					continue;

				int newCost = current.cost + getMovementCost(unit, neighbor);
				if (newCost <= maxRange && newCost < costAt.getOrDefault(neighbor, Integer.MAX_VALUE)) {
					costAt.put(neighbor, newCost);
					openSet.add(new Node(neighbor, newCost));
				}
			}
		}

		Set<Position> result = new HashSet<>();
		for (Position pos : costAt.keySet()) {
			Unit unitAtPos = board.getUnit(pos);
			if (unitAtPos == null || pos.equals(start))
				result.add(pos);
		}
		return result;
	}

	private List<Position> getNeighbors(Position pos) {
		List<Position> neighbors = new ArrayList<>();
		int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
		for (int[] d : dirs) {
			Position neighbor = new Position(pos.x() + d[0], pos.y() + d[1]);
			if (board.isValidPosition(neighbor))
				neighbors.add(neighbor);
			neighbors.add(neighbor);
		}
		return neighbors;
	}

	private int getMovementCost(Unit unit, Position pos) {
		Tile tile = board.getTile(pos);
		if (tile == null)
			return NOT_PASSABLE;
		if (!tile.getTerrain().isPassable(unit.getType().getMovementType()))
			return NOT_PASSABLE;
		return tile.getTerrain().getMovementCost(unit.getType().getMovementType());
	}

	private record Node(Position pos, int cost) implements Comparable<Node> {
		public int compareTo(Node other) {
			return Integer.compare(cost, other.cost);
		}
	}
}
