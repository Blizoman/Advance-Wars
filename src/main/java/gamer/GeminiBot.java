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

    private Iterator<Unit> unitIterator;
    private Iterator<Tile> factoryIterator;
    private boolean isTurnActive = false;

    // Cache for the threat map
    private Set<Position> threatMap = new HashSet<>();

    public GeminiBot(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    private void startTurn(Session session) {
        Player me = session.getActive();
        GameBoard board = session.getGameBoard();

        unitIterator = new ArrayList<>(board.getUnitsOf(me)).iterator();
        factoryIterator = board.getTilesOf(me).stream()
                .filter(t -> t.getTerrain().isProduceUnits() && t.isEmpty())
                .toList()
                .iterator();

        // Calculate threat map at the start of the turn
        threatMap.clear();
        List<Unit> enemies = board.getAllUnits().stream()
                .filter(u -> u.getPlayer() != me && u.isAlive())
                .toList();
        for (Unit enemy : enemies) {
            Map<Position, Integer> enemyReach = pathFinder.findReachableTiles(enemy);
            for (Position reachablePos : enemyReach.keySet()) {
                // Add tiles the enemy can reach to attack
                int attackRange = enemy.getType().getAttackRange().max();
                threatMap.addAll(getTilesInRange(reachablePos, attackRange, board));
            }
        }

        isTurnActive = true;
    }

    public boolean performNextAction(Session session) {
        if (!isTurnActive) {
            startTurn(session);
        }

        Player me = session.getActive();
        GameBoard board = session.getGameBoard();
        List<Unit> enemies = board.getAllUnits().stream()
                .filter(u -> u.getPlayer() != me && u.isAlive())
                .toList();

        // PHASE 1: Move and attack
        if (unitIterator != null && unitIterator.hasNext()) {
            Unit unit = unitIterator.next();
            if (!unit.isAlive())
                return true;

            Position startPos = unit.getPosition();
            Map<Position, Integer> reachableMoves = pathFinder.findReachableTiles(unit);
            Position bestTarget = startPos;
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

            int cost = reachableMoves.getOrDefault(bestTarget, 0);
            session.moveUnit(unit, bestTarget, cost);

            boolean moved = !startPos.equals(unit.getPosition());
            Tile currentTile = board.getTile(unit.getPosition());
            boolean onEnemyOrNeutralBuilding =
                    currentTile.getTerrain().isCapturable() && currentTile.getOwner() != me;

            if (unit.getType().isCanCapture() && onEnemyOrNeutralBuilding) {
                session.tryCapture(unit, currentTile);
            } else if (!moved || unit.getType().isCanAttackAfterMove()) {
                Unit targetEnemy = getBestEnemyToAttack(unit, enemies);
                if (targetEnemy != null) {
                    session.attack(unit, targetEnemy);
                }
            }
            return true;
        }

        // PHASE 2: Buy units
        if (factoryIterator != null && factoryIterator.hasNext()) {
            Tile t = factoryIterator.next();
            if (!t.isEmpty())
                return true;

            buySmartUnit(session, board.getPosition(t), me, enemies);
            return true;
        }

        isTurnActive = false;
        return false;
    }

    private Position getBestMove(Unit unit, Set<Position> reachable, GameBoard board,
            List<Unit> enemies, Player me) {
        Position bestMove = unit.getPosition();
        int bestScore = Integer.MIN_VALUE;

        // Determine if we are in "early game" (greedy phase)
        boolean earlyGame = board.getUnitsOf(me).size() < 6;

        for (Position pos : reachable) {
            int score = evaluatePosition(unit, pos, board, enemies, me, earlyGame);
            if (score > bestScore) {
                bestScore = score;
                bestMove = pos;
            }
        }
        return bestMove;
    }

    private int evaluatePosition(Unit unit, Position pos, GameBoard board, List<Unit> enemies,
            Player me, boolean earlyGame) {
        int score = 0;
        Tile tile = board.getTile(pos);

        // 1. Capture Priority
        if (unit.getType().isCanCapture() && tile.getTerrain().isCapturable()
                && tile.getOwner() != me) {
            if (tile.getTerrain() == Terrain.HQ)
                score += 2000;
            else if (tile.getTerrain() == Terrain.FACTORY)
                score += 800;
            else if (tile.getTerrain() == Terrain.CITY)
                score += earlyGame ? 600 : 300;

            if (tile.getCaptureHp() < 20)
                score += 100;
        }

        // 2. Defense and Healing
        score += tile.getTerrain().getDefenseBonus() * 15;
        if (unit.getHp() <= 60 && tile.getTerrain().isHeals() && tile.getOwner() == me) {
            score += 200; // Prioritize resting if hurt
        }

        // 3. Threat Map Avoidance
        if (threatMap.contains(pos)) {
            // Penalize moving squishy units into danger unless capturing HQ
            if (unit.getType() == UnitType.INFANTRY && tile.getTerrain() != Terrain.HQ) {
                score -= 300;
            } else if (unit.getHp() < 50) {
                score -= 200; // Damaged units avoid danger
            }
        }

        // 4. Attack Positioning
        if (unit.getType().isCanAttackAfterMove()) {
            for (Unit enemy : enemies) {
                int distance = pos.distanceTo(enemy.getPosition());
                if (unit.getType().getAttackRange().canReach(distance)) {
                    int potentialDamage = unit.getType().getDamageAgainst(enemy.getType());
                    score += potentialDamage * 2; // Weight high damage moves
                    if (enemy.getHp() <= potentialDamage) {
                        score += 300; // Bonus for securing a kill
                    }
                }
            }
        } else {
            // For CANNON/Rockets, try to get just outside enemy movement range
            for (Unit enemy : enemies) {
                int distance = pos.distanceTo(enemy.getPosition());
                if (unit.getType().getAttackRange().canReach(distance)) {
                    score += 150; // Good position to shoot next turn
                    if (!threatMap.contains(pos)) {
                        score += 200; // Safe position to shoot next turn!
                    }
                }
            }
        }

        // Move towards the general direction of enemies or neutral cities if nothing else to do
        if (score < 100) {
            score -= closestTargetDistance(pos, board, enemies, unit.getType().isCanCapture(), me);
        }

        score += random.nextInt(10);
        return score;
    }

    private int closestTargetDistance(Position pos, GameBoard board, List<Unit> enemies,
            boolean canCapture, Player me) {
        int minDistance = Integer.MAX_VALUE;
        if (canCapture) {
            for (Tile t : board.getAllTiles()) {
                if (t.getTerrain().isCapturable() && t.getOwner() != me) {
                    minDistance = Math.min(minDistance, pos.distanceTo(board.getPosition(t)));
                }
            }
        } else {
            for (Unit enemy : enemies) {
                minDistance = Math.min(minDistance, pos.distanceTo(enemy.getPosition()));
            }
        }
        return minDistance;
    }

    private Unit getBestEnemyToAttack(Unit attacker, List<Unit> enemies) {
        Unit bestTarget = null;
        int bestScore = -1;

        for (Unit enemy : enemies) {
            if (enemy.isAlive() && attacker.canAttackTo(enemy)) {
                int damage = attacker.getType().getDamageAgainst(enemy.getType());
                int score = damage;

                // Prioritize kills
                if (damage >= enemy.getHp()) {
                    score += 500;
                }
                // Prioritize expensive units
                score += enemy.getType().getCost() / 100;

                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = enemy;
                }
            }
        }
        return bestTarget;
    }

    private void buySmartUnit(Session session, Position pos, Player me, List<Unit> enemies) {
        int money = me.getMoney();

        // Count enemy types
        long enemyInfantry = enemies.stream().filter(u -> u.getType() == UnitType.INFANTRY).count();
        long enemyTanks = enemies.stream()
                .filter(u -> u.getType() == UnitType.TANK || u.getType() == UnitType.TANK)
                .count();

        UnitType toBuy = null;

        // Rock-Paper-Scissors buying logic
        if (money >= UnitType.TANK.getCost() && enemyTanks > 2) {
            toBuy = UnitType.TANK; // Counter heavy armor
        } else if (money >= UnitType.CANNON.getCost() && enemyInfantry > 3) {
            toBuy = UnitType.CANNON; // Good against swarms if protected
        } else if (money >= UnitType.TANK.getCost()) {
            toBuy = UnitType.TANK; // Solid default
        } else if (money >= UnitType.INFANTRY.getCost()) {
            toBuy = UnitType.INFANTRY; // Always buy something if possible
        }

        if (toBuy != null) {
            session.buyUnit(pos, toBuy);
        }
    }

    // Helper to get all tiles within a specific range
    private Set<Position> getTilesInRange(Position center, int range, GameBoard board) {
        Set<Position> tiles = new HashSet<>();
        for (int x = center.x() - range; x <= center.x() + range; x++) {
            for (int y = center.y() - range; y <= center.y() + range; y++) {
                Position p = new Position(x, y);
                if (board.isValidPosition(p) && center.distanceTo(p) <= range) {
                    tiles.add(p);
                }
            }
        }
        return tiles;
    }
}
