package classes.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import classes.board.Position;
import classes.board.Terrain;
import classes.board.Tile;
import classes.event.*;
import classes.player.Player;
import classes.unit.Unit;
import classes.unit.UnitType;
import lombok.Getter;
import lombok.Setter;

public class Session {
	private final Game game;

	@Getter
	private final List<GameEvent> eventLog = new ArrayList<>();
	private int logCursor = 0;

	@Setter
	private Consumer<Player> onGameEnd;

	public Session(Game game) {
		this.game = game;
	}

	public Session(Game game, List<GameEvent> loadedLog) {
		this.game = game;
		this.eventLog.addAll(loadedLog);
	}

	public void stepForward() {
		if (logCursor < eventLog.size())
			eventLog.get(logCursor++).execute(game);
	}

	public void stepBackward() {
		if (logCursor > 0)
			eventLog.get(--logCursor).undo(game);
	}

	public void switchToPlayMode() {
		eventLog.subList(logCursor, eventLog.size()).clear();
	}

	private void log(GameEvent event) {
		switchToPlayMode();
		eventLog.add(event);
		logCursor++;
	}

	public void startTurn() {
		game.processIncome();
		game.processUnits();
	}

	public void endTurn() {
		game.forwardTurn();
		log(new TurnChangedEvent());
		startTurn();
	}

	public void moveUnit(Unit unit, Position to) {
		Position from = unit.getPosition();
		game.moveUnit(unit, to);
		log(new UnitMovedEvent(from, to));
	}

	public void attack(Unit attacker, Unit defender) {
		int attackerHpBefore = attacker.getHp();
		int defenderHpBefore = defender.getHp();

		game.dealDamage(attacker, defender);
		if (defender.isDead()) {
			game.removeUnit(defender);
		}

		if (defender.isAlive() && defender.canAttackTo(attacker)) {
			game.dealDamage(defender, attacker);
			if (attacker.isDead()) {
				game.removeUnit(attacker);
			}
		}

		log(new UnitAttackEvent(
				attacker, attackerHpBefore,
				defender, defenderHpBefore,
				attackerHpBefore - attacker.getHp(),
				defenderHpBefore - defender.getHp()));
	}

	public void tryCapture(Unit unit, Tile tile) {
		Player originalOwner = tile.getOwner();
		Terrain previousTerrain = tile.getTerrain();
		boolean wasHq = previousTerrain == Terrain.HQ;

		game.capture(tile, unit);

		if (tile.getOwner() != originalOwner) {
			log(new CityCapturedEvent(
					game.getGameBoard().getPosition(tile),
					previousTerrain, tile.getOwner(), originalOwner));

			if (wasHq) {
				tile.convertHqToCity();
				eliminatePlayer(originalOwner);
			}
		}
	}

	public void buyUnit(Position position, UnitType unitType) {
		game.buyUnit(position, unitType);
		Unit unit = game.getGameBoard().getUnit(position);
		log(new UnitBoughtEvent(unit));
	}

	private void eliminatePlayer(Player player) {
		List<GameEvent> subEvents = new ArrayList<>();

		game.getGameBoard().getUnitsOf(player).forEach(u -> subEvents.add(new UnitDiedEvent(u)));
		game.getGameBoard().getTilesOf(player).forEach(t -> subEvents.add(new CityCapturedEvent(
				game.getGameBoard().getPosition(t),
				t.getTerrain(), null, player)));

		game.eliminatePlayer(player);
		log(new MultipleGameEvent(subEvents, player));

		if (game.getPlayers().size() == 1 && onGameEnd != null)
			onGameEnd.accept(game.getPlayers().get(0));
	}

	public Player getActive() { return game.getActive(); }

	public List<Player> getPlayers() { return game.getPlayers(); }

	public classes.board.GameBoard getGameBoard() { return game.getGameBoard(); }
}
