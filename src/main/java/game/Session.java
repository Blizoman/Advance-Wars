/**
 * Manages the current game session, acting as an event-driven controller for the core game logic.
 * It handles the execution and logging of game events (allowing for undo/redo functionality),
 * manages turn progression, processes interactions like movement, combat, and capturing, 
 * and evaluates player eliminations and win conditions.
 *
 * @author xpruzir00
 */

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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import unit.Unit;
import unit.UnitType;

@RequiredArgsConstructor
public class Session {
	private final Game game;

	@Getter
	private final List<GameEvent> eventLog = new ArrayList<>();
	@Getter
	private int logCursor = 0;

	@Setter
	private Consumer<Player> onGameEnd;

	public Session(Game game, List<GameEvent> loadedLog) {
		this.game = game;
		this.eventLog.addAll(loadedLog);
	}

	/////////////////// VALUES ///////////////////
	//////////////////////////////////////////////
	//////////////////// TURN ////////////////////

	public void stepForward() {
		if (this.logCursor < this.eventLog.size())
			this.eventLog.get(this.logCursor++).execute(this.game);
	}

	public void stepBackward() {
		if (this.logCursor > 0)
			this.eventLog.get(--this.logCursor).undo(this.game);
	}

	private void execute(GameEvent event) {
		if (this.logCursor < this.eventLog.size()) // Clear future
			this.eventLog.subList(this.logCursor, this.eventLog.size()).clear();
		event.execute(this.game);
		this.eventLog.add(event);
		this.logCursor++;
	}

	public void startTurn() {
		this.game.processIncome();
		this.game.processUnits();
	}

	public void endTurn() {
		execute(new TurnChangedEvent());
		evaluateEliminations();
	}

	//////////////////// TURN ////////////////////
	//////////////////////////////////////////////
	/////////////////// EVENTS ///////////////////

	public void moveUnit(Unit unit, Position to) {
		execute(new UnitMovedEvent(unit.getPosition(), to, unit.getMovesLeft(), unit.getPlayer()));
	}

	public void attack(Unit attacker, Unit defender) {
		execute(new UnitAttackEvent(attacker, defender));
		evaluateEliminations();
	}

	public void tryCapture(Unit unit, Tile tile) {
		Player originalOwner = tile.getOwner();
		Terrain previousTerrain = tile.getTerrain();
		int captureHpBefore = tile.getCaptureHp();
		boolean wasHq = previousTerrain == Terrain.HQ;

		tile.evalCapture(unit); // Execute action firstly
		// Then set states
		if (tile.getOwner() != originalOwner) { // Captured
			execute(new CityCapturedEvent(
					this.game.getGameBoard().getPosition(tile),
					previousTerrain, tile.getOwner(), originalOwner, captureHpBefore));
			if (wasHq)
				eliminatePlayer(originalOwner);
		} else { // Progress only
			execute(new CaptureProgressEvent(
					this.game.getGameBoard().getPosition(tile),
					captureHpBefore, tile.getCaptureHp()));
		}
		evaluateEliminations();
	}

	public void buyUnit(Position position, UnitType unitType) {
		execute(new UnitBoughtEvent(position, unitType, this.game.getActive()));
	}

	public void eliminatePlayer(Player player) {
		int eliminatedIndex = game.getPlayers().indexOf(player);
		List<GameEvent> subEvents = new ArrayList<>();

		this.game.getGameBoard().getUnitsOf(player)
				.forEach(u -> subEvents.add(new UnitDiedEvent(u)));
		this.game.getGameBoard().getTilesOf(player)
				.forEach(t -> subEvents.add(new CityCapturedEvent(
						this.game.getGameBoard().getPosition(t),
						t.getTerrain(), null, player, t.getCaptureHp())));

		execute(new MultipleGameEvent(subEvents, player, eliminatedIndex));

		if (this.game.getPlayers().size() == 1 && onGameEnd != null)
			onGameEnd.accept(this.game.getPlayers().get(0));
	}

	private void evaluateEliminations() {
		List<Player> playersSnapshot = new ArrayList<>(this.game.getPlayers());
		for (Player player : playersSnapshot) {
			if (!this.game.getGameBoard().getUnitsOf(player).isEmpty())
				continue;
			boolean hasFactory = this.game.getGameBoard().getTilesOf(player).stream()
					.anyMatch(t -> t.getTerrain().isProduceUnits());
			if (!hasFactory)
				eliminatePlayer(player);
		}
	}

	/////////////////// EVENTS ///////////////////
	//////////////////////////////////////////////
	//////////////// GAME GETTERS ////////////////

	public Player getActive() { return this.game.getActive(); }

	public List<Player> getPlayers() { return this.game.getPlayers(); }

	public GameBoard getGameBoard() { return this.game.getGameBoard(); }

	//////////////// GAME GETTERS ////////////////
	//////////////////////////////////////////////
}
