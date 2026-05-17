/**
 * Represents a basic AI opponent that automates a player's turn. It processes actions step-by-step
 * using iterators to prevent UI freezes, handling unit movement, capturing, combat, and basic
 * purchasing heuristics.
 *
 * @author xblizna00
 */

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
        // Collect live enemy units once per tick to avoid redundant board manipulatiopn.
        List<Unit> enemies = board.getAllUnits().stream()
                .filter(u -> u.getPlayer() != me && u.isAlive())
                .toList();

        // [Phase 1] - Process units one by one.
        if (unitIterator != null && unitIterator.hasNext()) {
            Unit unit = unitIterator.next();

            // Skip dead or already moved units
            if (!unit.isAlive() || unit.isUsed())
                return true;

            Position startPos = unit.getPosition();
            Map<Position, Integer> reachable = pathFinder.findReachableTiles(unit);
            Position target = chooseTarget(unit, board, enemies, me);

            // If unit cannot attack after moving (Cannon) and already has a target in range,
            // skip movement to assure the ability to fire this turn.
            Position bestMove = startPos;
            boolean canShootNow =
                    !unit.getType().isCanAttackAfterMove()
                            && canAttackFromPosition(startPos, unit, enemies);
            if (!canShootNow)
                bestMove = chooseBestMove(unit, reachable, target, board, enemies);

            // Only move if the destination tile is free.
            boolean moved = !bestMove.equals(startPos);
            if (moved && board.getTile(bestMove).isEmpty())
                session.moveUnit(unit, bestMove);
            else
                moved = false;

            // Attempt capture before attack – capturing ends the units action for this tick.
            Tile tile = board.getTile(unit.getPosition());
            if (canCapture(unit, tile, me)) {
                session.tryCapture(unit, tile);
                return true;
            }

            // Attack if allowed (respecting 'isCanAttackAfterMove' rule).
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

        // [Phase 2] - Buy units in owned factories after all units have acted.
        if (factoryIterator != null && factoryIterator.hasNext()) {
            Tile factory = factoryIterator.next();
            if (factory.isEmpty())
                buyUnit(session, board, factory, me, enemies);
            return true;
        }
        // Both phases done so it is an end of turn.
        isTurnActive = false;
        return false;
    }
    // Initializes iterators at the start of every turn. 
    // Called only once thanks to isTurnActive guard
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

        // Fallback: closest enemy unit.
        Position enemyPos = null;
        int bestDist = Integer.MAX_VALUE;
        for (Unit enemy : enemies) {
            int dist = unit.getPosition().distanceTo(enemy.getPosition());
            if (dist < bestDist) {
                bestDist = dist;
                enemyPos = enemy.getPosition();
            }
        }

        if (enemyPos == null) {
            for (Tile tile : board.getAllTiles()) {
                if (!tile.getTerrain().isCapturable()) continue;
                if (tile.getOwner() == null || tile.getOwner() == me) continue;
                Position pos = board.getPosition(tile);
                int dist = unit.getPosition().distanceTo(pos);
                if (dist < bestDist) {
                    bestDist = dist;
                    enemyPos = pos;
                }
            }
        }
        return enemyPos;
    }

    // Pick reachable tile that shorten distance to the target.
    // Adds a small bonus (-2) for positions that allow an immediate attack,
    // so the unit prefers an attack-ready tile over one that is closer.
    // If target is null (no enemies, no buildings), the score is 0 for all tiles and
    // the unit stays in place.
    private Position chooseBestMove(Unit unit, Map<Position, Integer> reachable, Position target,
            GameBoard board, List<Unit> enemies) {
        Position best = unit.getPosition();
        int bestScore = Integer.MAX_VALUE;
        for (Position pos : reachable.keySet()) {
            Tile tile = board.getTile(pos);
            if (tile == null)
                continue;
            // Skip tiles occupied by any unit
            if (!pos.equals(unit.getPosition()) && tile.getUnit() != null)
                continue;

            int dist = target == null ? 0 : pos.distanceTo(target);
            int score = dist;

            // Prefer positions that give an immediate attack opportunity.
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

    // Unit can capture if:
    // 1. It has capture ability, the tile is capturable
    // 2. The tile is not already owned by us
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

    // Target the weakest (lowest HP) enemy to finish them off
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

        if (money >= UnitType.CANNON.getCost() && enemies.size() > 2)
            toBuy = UnitType.CANNON;
        else if (money >= UnitType.TANK.getCost())
            toBuy = UnitType.TANK;
        else if (money >= UnitType.INFANTRY.getCost())
            toBuy = UnitType.INFANTRY;

        if (toBuy != null && factory.isEmpty()) {
            Position pos = board.getPosition(factory);
            session.buyUnit(pos, toBuy);
        }
    }
}
