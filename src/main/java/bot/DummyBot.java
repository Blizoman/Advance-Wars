package bot;

import java.util.*;
import board.Position;
import game.PathFinder;
import game.Session;
import unit.Unit;
import unit.UnitType;

public class DummyBot {
	private final PathFinder pathFinder;
	private final Random random = new Random();

	public DummyBot(PathFinder pathFinder) {
		this.pathFinder = pathFinder;
	}

	public void takeTurn(Session session) {
		List<Unit> units = new ArrayList<>(
				session.getGameBoard().getUnitsOf(session.getActive()));

		for (Unit unit : units) {
			// move to random reachable tile
			Map<Position, Integer> moves = pathFinder.findReachableTiles(unit);
			if (!moves.isEmpty()) {
				List<Position> moveList = new ArrayList<>(moves.keySet());
				Position target = moveList.get(random.nextInt(moveList.size()));
				session.moveUnit(unit, target, moves.get(target));
			}
		}

		// try buying a random unit at each factory
		session.getGameBoard().getTilesOf(session.getActive()).stream()
				.filter(t -> t.getTerrain().isProduceUnits() && t.isEmpty())
				.forEach(t -> {
					UnitType[] types = UnitType.values();
					UnitType type = types[random.nextInt(types.length)];
					if (session.getActive().canAfford(type.getCost()))
						session.buyUnit(session.getGameBoard().getPosition(t), type);
				});
	}
}
