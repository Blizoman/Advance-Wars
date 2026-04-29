package game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import board.GameBoard;
import board.Position;
import board.Terrain;
import board.Tile;
import event.*;
import gamer.Player;
import lombok.Getter;
import lombok.Setter;
import unit.Unit;
import unit.UnitType;

public class Session {
	private final Game game;

	@Getter
	private final List<GameEvent> eventLog = new ArrayList<>();
	@Getter
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

	private void execute(GameEvent event) {
		if (logCursor < eventLog.size())
			eventLog.subList(logCursor, eventLog.size()).clear();
		event.execute(game);
		eventLog.add(event);
		logCursor++;
	}

	public void startTurn() {
		game.processIncome();
		game.processUnits();
	}

	public void endTurn() {
		execute(new TurnChangedEvent());
	}

	public void moveUnit(Unit unit, Position to) {
		Position from = unit.getPosition();
		execute(new UnitMovedEvent(from, to, unit.getMovesLeft()));
	}

	public void attack(Unit attacker, Unit defender) {
		UnitAttackEvent event = new UnitAttackEvent(attacker, defender);
		execute(event);
	}

	public void tryCapture(Unit unit, Tile tile) {
		Player originalOwner = tile.getOwner();
		Terrain previousTerrain = tile.getTerrain();
		int captureHpBefore = tile.getCaptureHp();
		boolean wasHq = previousTerrain == Terrain.HQ;

		game.capture(tile, unit); // execute action directly

		if (tile.getOwner() != originalOwner) {
			// capture completed - log final state
			execute(new CityCapturedEvent(
					game.getGameBoard().getPosition(tile),
					previousTerrain, tile.getOwner(), originalOwner, captureHpBefore));

			if (wasHq)
				eliminatePlayer(originalOwner);
		} else {
			// partial capture - log progress only
			execute(new CaptureProgressEvent(
					game.getGameBoard().getPosition(tile),
					captureHpBefore, tile.getCaptureHp()));
		}
	}

	public void buyUnit(Position position, UnitType unitType) {
		execute(new UnitBoughtEvent(position, unitType, game.getActive()));
	}

	public void eliminatePlayer(Player player) {
		List<GameEvent> subEvents = new ArrayList<>();
		game.getGameBoard().getUnitsOf(player)
				.forEach(u -> subEvents.add(new UnitDiedEvent(u)));
		game.getGameBoard().getTilesOf(player)
				.forEach(t -> subEvents.add(new CityCapturedEvent(
						game.getGameBoard().getPosition(t),
						t.getTerrain(), null, player, t.getCaptureHp())));

		execute(new MultipleGameEvent(subEvents, player));

		if (game.getPlayers().size() == 1 && onGameEnd != null)
			onGameEnd.accept(game.getPlayers().get(0));
	}

	public Player getActive() { return game.getActive(); }

	public List<Player> getPlayers() { return game.getPlayers(); }

	public GameBoard getGameBoard() { return game.getGameBoard(); }
}
