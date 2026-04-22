package classes.game;

import java.util.Comparator;
import java.util.List;
import classes.board.GameBoard;
import classes.board.Position;
import classes.board.Tile;
import classes.player.Player;
import classes.unit.Unit;
import classes.unit.UnitFactory;
import classes.unit.UnitType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tools.Consts;
import tools.EvalDamage;

@RequiredArgsConstructor
public class Game {
	@Getter
	private final GameBoard gameBoard;

	@Getter
	private final List<Player> players;

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
		gameBoard.getAllTiles().stream()
				.filter(t -> t.getOwner() == player && t.getTerrain().isGenerateIncome())
				.forEach(t -> player.addMoney(Consts.CITY_INCOME));
	}

	private void processUnits() {
		List<Unit> allUnits = gameBoard.getAllUnits();

		Player player = getActive();
		List<Unit> playerUnits = allUnits.stream()
				.filter(u -> u.getPlayer() == player)
				.toList();

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

		if (defender.isDead())
			gameBoard.removeUnit(defender);
	}

	public void tryCapture(Unit unit, Tile tile) {
		tile.evalCapture(unit);
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
}
