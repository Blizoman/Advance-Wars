package gamer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import board.GameBoard;
import board.Position;
import board.Tile;
import game.PathFinder;
import game.Session;
import unit.AttackRange;
import unit.Unit;
import unit.UnitType;

public class DummyBot {
    private final PathFinder pathFinder;
    private Iterator<Unit> unitIterator;
    private Iterator<Tile> factoryIterator;
    private boolean isTurnActive = false;

    public DummyBot(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    // Using iterators (unitIterator, factoryIterator) instead of a while
    // This ensures that bot make only one action per tick, preventing UI freezes.
    public boolean performNextAction(Session session) {
        if (!isTurnActive)
            startTurn(session);

        Player me = session.getActive();
        GameBoard board = session.getGameBoard();
        List<Unit> enemies = board.getAllUnits().stream()
                .filter(u -> u.getPlayer() != me && u.isAlive())
                .toList();

        if (unitIterator != null && unitIterator.hasNext()) {
            Unit unit = unitIterator.next();
            if (!unit.isAlive() || unit.isUsed())
                return true;

            Position startPos = unit.getPosition();
            Map<Position, Integer> reachable = pathFinder.findReachableTiles(unit);
            Position target = chooseTarget(unit, board, enemies, me);

            Position bestMove = startPos;
            boolean canShootNow =
                    !unit.getType().isCanAttackAfterMove()
                            && canAttackFromPosition(startPos, unit, enemies);
            if (!canShootNow)
                bestMove = chooseBestMove(unit, reachable, target, board, enemies);

            boolean moved = !bestMove.equals(startPos);
            if (moved && board.getTile(bestMove).isEmpty())
                session.moveUnit(unit, bestMove);
            else
                moved = false;

            Tile tile = board.getTile(unit.getPosition());
            if (canCapture(unit, tile, me)) {
                session.tryCapture(unit, tile);
                return true;
            }

            if (canAttackNow(unit, moved)) {
                Unit targetEnemy = chooseAttackTarget(unit, enemies);
                if (targetEnemy != null && unit.canAttackTo(targetEnemy)) {
                    session.attack(unit, targetEnemy);
                    return true;
                }
            }

            unit.setUsed(true);
            return true;
        }

        if (factoryIterator != null && factoryIterator.hasNext()) {
            Tile factory = factoryIterator.next();
            if (factory.isEmpty())
                buyUnit(session, board, factory, me, enemies);
            return true;
        }

        isTurnActive = false;
        return false;
    }

    private void startTurn(Session session) {
        Player me = session.getActive();
        GameBoard board = session.getGameBoard();

        unitIterator = new ArrayList<>(board.getUnitsOf(me)).iterator();
        factoryIterator = board.getTilesOf(me).stream()
                .filter(t -> t.getTerrain().isProduceUnits() && t.isEmpty())
                .toList()
                .iterator();
        isTurnActive = true;
    }

    // If the unit is Infantry (can capture), target the closest neutral or enemy building.
    // For vehicles (Tank, Cannon), target the closest enemy unit.
    private Position chooseTarget(Unit unit, GameBoard board, List<Unit> enemies, Player me) {
        if (unit.getType().isCanCapture()) {
            Position best = null;
            int bestDist = Integer.MAX_VALUE;
            for (Tile tile : board.getAllTiles()) {
                if (!tile.getTerrain().isCapturable() || tile.getOwner() == me)
                    continue;
                Position pos = board.getPosition(tile);
                int dist = unit.getPosition().distanceTo(pos);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos;
                }
            }
            if (best != null)
                return best;
        }

        Position enemyPos = null;
        int bestDist = Integer.MAX_VALUE;
        for (Unit enemy : enemies) {
            int dist = unit.getPosition().distanceTo(enemy.getPosition());
            if (dist < bestDist) {
                bestDist = dist;
                enemyPos = enemy.getPosition();
            }
        }
        return enemyPos;
    }

    private Position chooseBestMove(Unit unit, Map<Position, Integer> reachable, Position target,
            GameBoard board, List<Unit> enemies) {
        Position best = unit.getPosition();
        int bestScore = Integer.MAX_VALUE;
        for (Position pos : reachable.keySet()) {
            Tile tile = board.getTile(pos);
            if (tile == null)
                continue;
            if (!pos.equals(unit.getPosition()) && tile.getUnit() != null)
                continue;

            int dist = target == null ? 0 : pos.distanceTo(target);
            int score = dist;
            if (unit.getType().isCanAttackAfterMove()
                    && canAttackFromPosition(pos, unit, enemies))
                score -= 2;

            if (score < bestScore) {
                bestScore = score;
                best = pos;
            }
        }
        return best;
    }

    private boolean canCapture(Unit unit, Tile tile, Player me) {
        return !unit.isCaptured()
                && unit.getType().isCanCapture()
                && tile.getTerrain().isCapturable()
                && (tile.getOwner() == null || tile.getOwner() != me);
    }

    private boolean canAttackNow(Unit unit, boolean moved) {
        return !unit.isUsed()
                && (unit.getType().isCanAttackAfterMove() || !moved);
    }

    private boolean canAttackFromPosition(Position pos, Unit unit, List<Unit> enemies) {
        AttackRange range = unit.getType().getAttackRange();
        for (Unit enemy : enemies) {
            if (!enemy.isAlive())
                continue;
            int dist = pos.distanceTo(enemy.getPosition());
            if (range.canReach(dist))
                return true;
        }
        return false;
    }

    private Unit chooseAttackTarget(Unit attacker, List<Unit> enemies) {
        Unit best = null;
        int bestHp = Integer.MAX_VALUE;
        for (Unit enemy : enemies) {
            if (!enemy.isAlive() || !attacker.canAttackTo(enemy))
                continue;
            if (enemy.getHp() < bestHp) {
                bestHp = enemy.getHp();
                best = enemy;
            }
        }
        return best;
    }

    // Basic buying heuristic:
    // If there are multiple enemies (> 2) and enough money, buy a CANNON for ranged defense.
    // Otherwise, prioritize TANKs if affordable, falling back to INFANTRY.
    private void buyUnit(Session session, GameBoard board, Tile factory, Player me,
            List<Unit> enemies) {
        int money = me.getMoney();
        UnitType toBuy = null;

        if (money >= UnitType.TANK.getCost())
            toBuy = UnitType.TANK;
        else if (money >= UnitType.CANNON.getCost() && enemies.size() > 2)
            toBuy = UnitType.CANNON;
        else if (money >= UnitType.INFANTRY.getCost())
            toBuy = UnitType.INFANTRY;

        if (toBuy != null && factory.isEmpty()) {
            Position pos = board.getPosition(factory);
            session.buyUnit(pos, toBuy);
        }
    }
}
