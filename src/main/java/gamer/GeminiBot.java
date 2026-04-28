package gamer;

import java.util.*;

import board.GameBoard;
import board.Position;
import board.Terrain;
import board.Tile;
import game.PathFinder;
import game.Session;
import unit.Unit;
import unit.UnitType;

public class GeminiBot {
    private final PathFinder pathFinder;
    private final Random random = new Random();

    public GeminiBot(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    public void takeTurn(Session session) {
        Player me = session.getActive();
        GameBoard board = session.getGameBoard();
        List<Unit> myUnits = new ArrayList<>(board.getUnitsOf(me));
        List<Unit> enemies = board.getAllUnits().stream()
                .filter(u -> u.getPlayer() != me && u.isAlive())
                .toList();

        // 1. UNIT ACTIONS (Move -> Capture/Attack)
        for (Unit unit : myUnits) {
            Position startPos = unit.getPosition();
            Map<Position, Integer> reachableMoves = pathFinder.findReachableTiles(unit);

            Position bestTarget = startPos;

            // Special rule for CANNON: if we can already hit someone, don't move.
            boolean shouldStayToShoot = false;
            if (!unit.getType().isCanAttackAfterMove()) {
                for (Unit enemy : enemies) {
                    if (unit.canAttackTo(enemy)) {
                        shouldStayToShoot = true;
                        break;
                    }
                }
            }

            if (!shouldStayToShoot && !reachableMoves.isEmpty()) {
                bestTarget = getBestMove(unit, reachableMoves.keySet(), board, enemies, me);
            }

            // Move the unit (Session ignores if start == target)
            int cost = reachableMoves.getOrDefault(bestTarget, 0);
            session.moveUnit(unit, bestTarget, cost);

            // Determine what to do after moving
            boolean moved = !startPos.equals(unit.getPosition());
            Tile currentTile = board.getTile(unit.getPosition());

            // Try to Capture
            boolean onEnemyOrNeutralBuilding =
                    currentTile.getTerrain().isCapturable() && currentTile.getOwner() != me;
            if (unit.getType().isCanCapture() && onEnemyOrNeutralBuilding) {
                session.tryCapture(unit, currentTile);
            }
            // Try to Attack
            else if (!moved || unit.getType().isCanAttackAfterMove()) {
                Unit targetEnemy = getBestEnemyToAttack(unit, enemies);
                if (targetEnemy != null) {
                    session.attack(unit, targetEnemy);
                }
            }
        }

        // 2. PRODUCTION PHASE
        // Get all UnitTypes sorted by cost descending (buy the strongest we can)
        List<UnitType> availableTypes = Arrays.asList(UnitType.values());
        availableTypes.sort((a, b) -> Integer.compare(b.getCost(), a.getCost()));

        // Find empty factories we own
        board.getTilesOf(me).stream()
                .filter(t -> t.getTerrain().isProduceUnits() && t.isEmpty())
                .forEach(t -> {
                    for (UnitType type : availableTypes) {
                        if (me.canAfford(type.getCost())) {
                            session.buyUnit(board.getPosition(t), type);
                            break; // Stop checking types once we bought one for this factory
                        }
                    }
                });
    }

    // --- AI Heuristics ---

    private Position getBestMove(Unit unit, Set<Position> reachable, GameBoard board,
            List<Unit> enemies, Player me) {
        Position bestMove = unit.getPosition();
        int bestScore = Integer.MIN_VALUE;

        for (Position pos : reachable) {
            int score = evaluatePosition(unit, pos, board, enemies, me);
            if (score > bestScore) {
                bestScore = score;
                bestMove = pos;
            }
        }
        return bestMove;
    }

    private int evaluatePosition(Unit unit, Position pos, GameBoard board, List<Unit> enemies,
            Player me) {
        int score = 0;
        Tile tile = board.getTile(pos);

        // 1. Capturable Target Evaluation (Only Infantry care about this)
        if (unit.getType().isCanCapture() && tile.getTerrain().isCapturable()
                && tile.getOwner() != me) {
            if (tile.getTerrain() == Terrain.HQ)
                score += 1000;
            else if (tile.getTerrain() == Terrain.FACTORY)
                score += 500;
            else if (tile.getTerrain() == Terrain.CITY)
                score += 300;

            // Bonus if it's already partially captured
            if (tile.getCaptureHp() < 20)
                score += 50;
        }

        // 2. Defensive Terrain Bonuses (Everyone likes cover)
        score += tile.getTerrain().getDefenseBonus() * 10;

        // 3. Combat Positioning (Can we hit an enemy from here?)
        // (Assuming we simulate being at 'pos' to check distances)
        if (unit.getType().isCanAttackAfterMove()) {
            for (Unit enemy : enemies) {
                int distance = pos.distanceTo(enemy.getPosition());
                if (unit.getType().getAttackRange().canReach(distance)) {
                    // Bonus points for being able to shoot something
                    score += 150;
                    // Extra bonus if we deal good damage against this type
                    score += unit.getType().getDamageAgainst(enemy.getType());
                }
            }
        } else {
            // If it's a Cannon and it moves, it can't attack this turn. 
            // So we just want to move closer to enemies without getting into range 1.
            for (Unit enemy : enemies) {
                int distance = pos.distanceTo(enemy.getPosition());
                if (distance == 2 || distance == 3) {
                    score += 100; // Good positioning for next turn
                }
            }
        }

        // 4. Random noise to break ties and prevent infinite loops
        score += random.nextInt(10);

        return score;
    }

    private Unit getBestEnemyToAttack(Unit attacker, List<Unit> enemies) {
        Unit bestTarget = null;
        int maxDamage = -1;

        for (Unit enemy : enemies) {
            if (enemy.isAlive() && attacker.canAttackTo(enemy)) {
                // Determine base damage against this specific target
                int damage = attacker.getType().getDamageAgainst(enemy.getType());

                // Prioritize lower HP enemies to finish them off, or high damage targets
                if (damage > maxDamage || (damage == maxDamage && bestTarget != null
                        && enemy.getHp() < bestTarget.getHp())) {
                    maxDamage = damage;
                    bestTarget = enemy;
                }
            }
        }
        return bestTarget;
    }
}
