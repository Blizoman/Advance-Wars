package classes.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import classes.board.GameBoard;
import classes.board.Position;
import classes.board.Terrain;
import classes.board.Tile;
import classes.player.Player;
import classes.unit.Unit;
import classes.unit.UnitFactory;
import classes.unit.UnitType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tools.Consts;
import tools.EvalDamage;

@RequiredArgsConstructor
public class Game {
	@Setter
	private Consumer<Player> onGameEnd;

	@Getter
	private final GameBoard gameBoard;

	@Getter
	private final ArrayList<Player> players;

	@Getter
	private int currentPlayerIndex = 0;

	private final UnitFactory unitFactory = new UnitFactory();

	private Player getActive() { return players.get(currentPlayerIndex); }

	public void startTurn() {
		processIncome();
		processUnits();
	}

	private void processIncome() {
		Player player = getActive();
		gameBoard.getTilesOf(player).stream()
				.filter(t -> t.getTerrain().isGenerateIncome())
				.forEach(t -> player.addMoney(Consts.CITY_INCOME));
	}

	private void processUnits() {
		Player player = getActive();
		List<Unit> allUnits = gameBoard.getAllUnits();
		List<Unit> playerUnits = gameBoard.getUnitsOf(player);

		resetMovement(allUnits);
		healUnits(playerUnits);
	}

	private void resetMovement(List<Unit> units) {
		for (Unit unit : units) {
			unit.resetMovement();
		}
	}

	private void healUnits(List<Unit> units) {
		List<Unit> sortedUnits = units.stream()
				.sorted(Comparator.comparingInt((Unit u) -> u.getType().getCost()).reversed())
				.toList();
		Player player = getActive();

		for (Unit unit : sortedUnits) {
			Tile tile = gameBoard.getTile(unit.getPosition());
			if (!tile.getTerrain().isHeals() || tile.getOwner() != player)
				continue;

			int toHeal = Math.min(Consts.MAX_HEAL, Consts.MAX_HP - unit.getHp());
			if (toHeal == 0)
				continue;

			int baseCost = unit.getType().getCost() / 10;
			int money = player.getMoney();
			int moneyToHeal = toHeal / 10 * baseCost;

			if (money >= moneyToHeal) {
				unit.heal(toHeal);
				player.removeMoney(moneyToHeal);
			}
		}
	}

	public void moveUnit(Unit unit, Position to) {
		Position from = unit.getPosition();
		gameBoard.moveUnit(from, to);
	}

	/** Attack + counter-attack */
	public void attack(Unit attacker, Unit defender) {
		performAttack(attacker, defender);
		if (defender.isAlive() && defender.canAttackTo(attacker))
			performAttack(defender, attacker);
	}

	private void performAttack(Unit attacker, Unit defender) {
		int defenseBonus = gameBoard.getTile(defender.getPosition()).getTerrain().getDefenseBonus();
		int damage = EvalDamage.evalDamage(attacker, defender, defenseBonus);
		defender.takeDamage(damage);

		if (defender.isDead()) {
			gameBoard.removeUnit(defender);

			boolean hasUnits = gameBoard.getAllUnits().stream()
					.anyMatch(u -> u.getPlayer() == defender.getPlayer());
			if (!hasUnits)
				eliminatePlayer(defender.getPlayer());
		}
	}

	public void tryCapture(Unit unit, Tile tile) {
		Player originalOwner = tile.getOwner();
		tile.evalCapture(unit);

		if (tile.getTerrain() == Terrain.HQ && tile.getOwner() != originalOwner) {
			eliminatePlayer(originalOwner);
		}
	}

	public void buyUnit(Position position, UnitType unitType) {
		Player player = getActive();
		Unit unit = unitFactory.createUnit(unitType, player, position);
		gameBoard.getTile(position).placeUnit(unit);
		player.removeMoney(unitType.getCost());
	}

	public void endTurn() {
		currentPlayerIndex = ++currentPlayerIndex % players.size();
		startTurn();
	}

	private void eliminatePlayer(Player player) {
		player.kill();
		this.players.remove(player);

		gameBoard.getUnitsOf(player).forEach(gameBoard::removeUnit);
		gameBoard.getTilesOf(player).forEach(t -> {
			if (t.getTerrain() == Terrain.HQ)
				t.convertHqToCity();
			t.unsetOwner();
		});

		if (this.players.size() == 1 && onGameEnd != null) {
			onGameEnd.accept(this.players.get(0));
		}
	}
}
