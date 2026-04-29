/**
 * Game API
 * 
 * @author: xpruzir00
 */

package game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import board.GameBoard;
import board.Position;
import board.Terrain;
import board.Tile;
import gamer.Player;
import lombok.Getter;
import tools.Consts;
import tools.EvalDamage;
import unit.Unit;
import unit.UnitFactory;
import unit.UnitType;

public class Game {
	@Getter
	private final GameBoard gameBoard;
	private final UnitFactory unitFactory = new UnitFactory();

	@Getter
	private final ArrayList<Player> players;
	@Getter
	private final List<Player> initialPlayers;
	private int currentPlayerIndex = 0;

	public Game(GameBoard gameBoard, List<Player> players) {
		this.gameBoard = gameBoard;
		this.players = new ArrayList<>(players);
		this.initialPlayers = new ArrayList<>(players);

		List<Tile> hqs = this.gameBoard.getAllTiles().stream()
				.filter(t -> t.getTerrain() == Terrain.HQ)
				.toList();
		for (Player player : this.players) {
			Tile hqTile = hqs.stream()
					.filter(t -> t.getOwner() == player)
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("No HQ for " + player.getName()));
			Position hqPosition = this.gameBoard.getPosition(hqTile);
			Unit startingUnit = this.unitFactory.createUnit(UnitType.INFANTRY, player, hqPosition);
			hqTile.setUnit(startingUnit);
		}
	}

	/////////////////// VALUES ///////////////////
	//////////////////////////////////////////////
	/////////////////// PLAYER ///////////////////

	public void forwardTurn() {
		this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.players.size();
	}

	public void previousTurn() {
		this.currentPlayerIndex =
				(this.currentPlayerIndex - 1 + this.players.size()) % this.players.size();
	}

	public Player getActive() { return this.players.get(this.currentPlayerIndex); }

	public void eliminatePlayer(Player player) {
		int eliminatedIndex = this.players.indexOf(player);
		player.kill();
		this.players.remove(player);

		if (eliminatedIndex < this.currentPlayerIndex)
			this.currentPlayerIndex--;
	}

	public void restorePlayer(Player player, int originalIndex) {
		player.realive();
		players.add(originalIndex, player);
		if (originalIndex < currentPlayerIndex)
			currentPlayerIndex++;
	}

	/////////////////// PLAYER ///////////////////
	//////////////////////////////////////////////
	//////////////////// UNIT ////////////////////

	public void processIncome() {
		Player player = getActive();
		this.gameBoard.getTilesOf(player).stream()
				.filter(t -> t.getTerrain().isGenerateIncome())
				.forEach(t -> player.addMoney(Consts.CITY_INCOME));
	}

	public void processUnits() {
		Player player = getActive();
		List<Unit> playerUnits = this.gameBoard.getUnitsOf(player);
		playerUnits.forEach(Unit::resetMovement);
		playerUnits.forEach(Unit::resetCapture);
		playerUnits.forEach(Unit::resetUsed);
		healUnits(playerUnits, player);
	}

	private void healUnits(List<Unit> units, Player player) {
		units.stream()
				.sorted(Comparator.comparingInt((Unit u) -> u.getType().getCost()).reversed())
				.forEach(unit -> {
					Tile tile = this.gameBoard.getTile(unit.getPosition());
					if (!tile.getTerrain().isHeals() || tile.getOwner() != player)
						return;

					int toHeal = Math.min(Consts.MAX_HEAL, Consts.MAX_HP - unit.getHp());
					if (toHeal == 0)
						return;

					int moneyToHeal = (toHeal / 10) * (unit.getType().getCost() / 10);
					if (player.canAfford(moneyToHeal)) {
						unit.heal(toHeal);
						player.removeMoney(moneyToHeal);
					}
				});
	}

	public void dealDamage(Unit attacker, Unit defender) {
		int defenseBonus =
				this.gameBoard.getTile(defender.getPosition()).getTerrain().getDefenseBonus();
		defender.takeDamage(EvalDamage.evalDamage(attacker, defender, defenseBonus));
	}

	public void moveUnit(Unit unit, Position to) {
		this.gameBoard.moveUnit(unit.getPosition(), to);
	}

	public void removeUnit(Unit unit) {
		this.gameBoard.removeUnit(unit);
	}

	public void placeUnit(Unit unit) {
		this.gameBoard.placeUnit(unit);
	}

	public Unit buyUnit(Position position, UnitType unitType) {
		Player player = getActive();
		Unit newUnit = this.unitFactory.createUnit(unitType, player, position);
		placeUnit(newUnit);
		player.removeMoney(unitType.getCost());
		return newUnit;
	}

	//////////////////// UNIT ////////////////////
	//////////////////////////////////////////////
}
