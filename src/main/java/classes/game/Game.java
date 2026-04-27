package classes.game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import classes.board.GameBoard;
import classes.board.Position;
import classes.board.Terrain;
import classes.board.Tile;
import classes.event.GameEvent;
import classes.player.Player;
import classes.unit.Unit;
import classes.unit.UnitFactory;
import classes.unit.UnitType;
import lombok.Getter;
import tools.Consts;
import tools.EvalDamage;
import tools.LogFiler;

public class Game {
	@Getter
	private final GameBoard gameBoard;

	@Getter
	private final ArrayList<Player> players;

	@Getter
	private int currentPlayerIndex = 0;

	private final UnitFactory unitFactory = new UnitFactory();

	public Game(GameBoard gameBoard, List<Player> players) {
		this.gameBoard = gameBoard;
		this.players = new ArrayList<>(players);

		List<Tile> hqs = this.gameBoard.getAllTiles().stream()
				.filter(t -> t.getTerrain() == Terrain.HQ)
				.toList();

		for (Player player : this.players) {
			Tile hqTile = hqs.stream()
					.filter(t -> t.getOwner() == player)
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("No HQ for " + player.getName()));
			Position hqPosition = this.gameBoard.getPosition(hqTile);
			Unit startingUnit = unitFactory.createUnit(UnitType.INFANTRY, player, hqPosition);
			hqTile.placeUnit(startingUnit);
		}
	}

	public Player getActive() { return players.get(currentPlayerIndex); }

	public void processIncome() {
		Player player = getActive();
		gameBoard.getTilesOf(player).stream()
				.filter(t -> t.getTerrain().isGenerateIncome())
				.forEach(t -> player.addMoney(Consts.CITY_INCOME));
	}

	public void processUnits() {
		Player player = getActive();
		List<Unit> playerUnits = gameBoard.getUnitsOf(player);
		playerUnits.forEach(Unit::resetMovement);
		healUnits(playerUnits, player);
	}

	private void healUnits(List<Unit> units, Player player) {
		units.stream()
				.sorted(Comparator.comparingInt((Unit u) -> u.getType().getCost()).reversed())
				.forEach(unit -> {
					Tile tile = gameBoard.getTile(unit.getPosition());
					if (!tile.getTerrain().isHeals() || tile.getOwner() != player)
						return;

					int toHeal = Math.min(Consts.MAX_HEAL, Consts.MAX_HP - unit.getHp());
					if (toHeal == 0)
						return;

					int moneyToHeal = (toHeal / 10) * (unit.getType().getCost() / 10);
					if (player.getMoney() >= moneyToHeal) {
						unit.heal(toHeal);
						player.removeMoney(moneyToHeal);
					}
				});
	}

	public void moveUnit(Unit unit, Position to) {
		unit.moveBy(unit.getPosition().distanceTo(to));
		gameBoard.moveUnit(unit.getPosition(), to);
	}

	public int dealDamage(Unit attacker, Unit defender) {
		int defenseBonus = gameBoard.getTile(defender.getPosition()).getTerrain().getDefenseBonus();
		int damage = EvalDamage.evalDamage(attacker, defender, defenseBonus);
		defender.takeDamage(damage);
		return damage;
	}

	public void removeUnit(Unit unit) {
		gameBoard.removeUnit(unit);
	}

	public void placeUnit(Unit unit) {
		gameBoard.placeUnit(unit);
	}

	public void buyUnit(Position position, UnitType unitType) {
		Player player = getActive();
		Unit unit = unitFactory.createUnit(unitType, player, position);
		gameBoard.getTile(position).placeUnit(unit);
		player.removeMoney(unitType.getCost());
	}

	public void capture(Tile tile, Unit unit) {
		tile.evalCapture(unit);
	}

	public void eliminatePlayer(Player player) {
		int eliminatedIndex = players.indexOf(player);
		player.kill();
		players.remove(player);

		if (eliminatedIndex < currentPlayerIndex)
			currentPlayerIndex--;
		else if (eliminatedIndex == currentPlayerIndex)
			currentPlayerIndex = currentPlayerIndex % players.size();
	}

	public void restorePlayer(Player player) {
		player.realive();
		players.add(player);
	}

	public void forwardTurn() {
		currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
	}

	public void previousTurn() {
		currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
	}

	@Getter
	private Session session;

	public void initSession() {
		this.session = new Session(this);
	}

	public void loadSession(Path path) throws IOException {
		List<GameEvent> events = LogFiler.load(path, this.players);
		this.session = new Session(this, events);
	}

	public void saveSession(Path path) throws IOException {
		LogFiler.save(session.getEventLog(), path, this.players);
	}
}
