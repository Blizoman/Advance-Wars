package controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import board.AvailableMaps;
import board.Position;
import board.Tile;
import game.Game;
import game.PathFinder;
import game.Session;
import gamer.Player;
import gamer.GeminiBot;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import tools.LogFiler;
import unit.Unit;
import unit.UnitType;

public class GameController {
	@Getter
	private final Session session;
	@Getter
	private final Game game;
	private final GeminiBot bot;
	private final PathFinder pathFinder;
	private final List<Runnable> onStateChangedListeners = new ArrayList<>();
	@Getter
	private Unit selectedUnit;
	@Getter
	private Position selectedFactoryTile;
	@Getter
	private boolean attackMode;
	@Getter
	private Map<Position, Integer> moveCosts = Collections.emptyMap();
	private List<Unit> attackTargets;

	public GameController(Session session, Game game) {
		this.session = session;
		this.game = game;
		this.pathFinder = new PathFinder(game.getGameBoard());
		this.bot = new GeminiBot(pathFinder);
		session.setOnGameEnd($_ -> stateChanged());
	}

	private void stateChanged() {
		for (Runnable listener : onStateChangedListeners)
			listener.run();
	}

	public void setOnStateChanged(Runnable onStateChanged) {
		if (onStateChanged != null)
			onStateChangedListeners.add(onStateChanged);
	}

	public void onTileClicked(Position position) {
		Unit clickedUnit = game.getGameBoard().getUnit(position);
		Tile clickedTile = game.getGameBoard().getTile(position);

		if (isSelectableFactoryTile(clickedTile, position)) {
			selectedFactoryTile = position;
			selectedUnit = null;
			attackMode = false;
			moveCosts = Collections.emptyMap();
			attackTargets = null;
			stateChanged();
			return;
		}

		if (selectedUnit == null) {
			if (clickedUnit != null && clickedUnit.getPlayer() == session.getActive()) {
				selectedUnit = clickedUnit;
				attackMode = false;
				moveCosts = pathFinder.findReachableTiles(clickedUnit);
				attackTargets = null;
				stateChanged();
			}
			return;
		}

		if (attackMode) {
			if (clickedUnit != null && getAttackTargets().contains(clickedUnit)) {
				session.attack(selectedUnit, clickedUnit);
				deselect();
				return;
			}
			attackMode = false;
		}

		if (position.equals(selectedUnit.getPosition()) && canCapture()) {
			session.tryCapture(selectedUnit, game.getGameBoard().getTile(position));
			deselect();
			return;
		}

		if (clickedUnit != null && clickedUnit.getPlayer() != selectedUnit.getPlayer()
				&& canSelectedUnitAttackNow()
				&& selectedUnit.canAttackTo(clickedUnit)) {
			session.attack(selectedUnit, clickedUnit);
			deselect();
			return;
		}

		if (clickedUnit != null && clickedUnit.getPlayer() == selectedUnit.getPlayer()) {
			selectedUnit = clickedUnit;
			attackMode = false;
			moveCosts = pathFinder.findReachableTiles(clickedUnit);
			attackTargets = null;
			stateChanged();
			return;
		}

		if (moveCosts.containsKey(position)) {
			session.moveUnit(selectedUnit, position, moveCosts.get(position));
			moveCosts = pathFinder.findReachableTiles(selectedUnit);
			attackTargets = null;
			stateChanged();
			return;
		}

		deselect();
	}

	public void onAttack(Unit target) {
		session.attack(selectedUnit, target);
		deselect();
	}

	public void beginAttackMode() {
		if (selectedUnit != null && canAttack()) {
			attackMode = true;
			stateChanged();
		}
	}

	public void onCapture() {
		session.tryCapture(selectedUnit, game.getGameBoard().getTile(selectedUnit.getPosition()));
		deselect();
	}

	public void onWait() {
		deselect();
	}

	public void onBuyUnit(UnitType type, Position position) {
		if (position != null && game.getActive().canAfford(type.getCost())
				&& game.getGameBoard().getTile(position).isEmpty())
			session.buyUnit(position, type);
		deselect();
	}

	public void startGame() {
		session.startTurn();
		runTurnLoop();
	}

	private void runTurnLoop() {
		if (session.getActive().isBot()) {
			// Using an indefinite timeline to tick actions one by one
			Timeline botTimeline = new Timeline();
			botTimeline.setCycleCount(Timeline.INDEFINITE);

			KeyFrame frame = new KeyFrame(Duration.millis(500), e -> {
				boolean hasMoreActions = bot.performNextAction(session);
				stateChanged(); // Redraw screen after every single move

				if (!hasMoreActions) {
					botTimeline.stop();
					session.endTurn();
					stateChanged();
					runTurnLoop(); // Move to the next player
				}
			});

			botTimeline.getKeyFrames().add(frame);
			botTimeline.play();
		} else {
			stateChanged();
		}
	}

	public void onEndTurn() {
		deselect();
		session.endTurn();
		runTurnLoop();
	}

	public void onStepForward() {
		session.stepForward();
		attackTargets = null;
		stateChanged();
	}

	public void onStepBackward() {
		session.stepBackward();
		attackTargets = null;
		stateChanged();
	}

	public void onSave(Path path, AvailableMaps.MapMetadata map) throws IOException {
		game.saveSession(path, map);
	}

	public void onLoad(Path path) throws IOException {
		game.loadSession(LogFiler.loadEvents(path, game.getPlayers()));
		attackTargets = null;
		stateChanged();
	}

	private void deselect() {
		selectedUnit = null;
		selectedFactoryTile = null;
		attackMode = false;
		moveCosts = Collections.emptyMap();
		attackTargets = null;
		stateChanged();
	}

	public boolean canAttack() {
		return canSelectedUnitAttackNow() && !getAttackTargets().isEmpty();
	}

	public boolean canCapture() {
		if (selectedUnit == null)
			return false;
		Tile tile = game.getGameBoard().getTile(selectedUnit.getPosition());
		return !selectedUnit.isCaptured()
				&& selectedUnit.getType().isCanCapture()
				&& tile.getTerrain().isCapturable()
				&& (tile.getOwner() == null || tile.getOwner() != selectedUnit.getPlayer());
	}

	public boolean canBuyUnit() {
		return findBuyPosition() != null;
	}

	public boolean canBuyUnit(UnitType type) {
		return findBuyPosition() != null && game.getActive().canAfford(type.getCost());
	}

	public Position findBuyPosition() {
		if (selectedFactoryTile == null)
			return null;

		Tile tile = game.getGameBoard().getTile(selectedFactoryTile);
		if (tile == null)
			return null;
		if (!isSelectableFactoryTile(tile, selectedFactoryTile))
			return null;
		return selectedFactoryTile;
	}

	private boolean isSelectableFactoryTile(Tile tile, Position position) {
		return tile != null
				&& tile.getTerrain().isProduceUnits()
				&& tile.getOwner() == session.getActive()
				&& tile.isEmpty()
				&& position != null;
	}

	public Player getActivePlayer() { return session.getActive(); }

	private boolean canSelectedUnitAttackNow() {
		return selectedUnit != null
				&& !selectedUnit.isAttacked()
				&& (selectedUnit.getType().isCanAttackAfterMove()
						|| selectedUnit.getMovesLeft() == selectedUnit.getType().getMoveRange());
	}

	public List<Unit> getAttackTargets() {
		if (attackTargets == null)
			attackTargets = computeAttackTargets();
		return attackTargets;
	}

	private List<Unit> computeAttackTargets() {
		if (!canSelectedUnitAttackNow())
			return List.of();

		return game.getGameBoard().getAllUnits().stream()
				.filter(target -> target.getPlayer() != selectedUnit.getPlayer()
						&& selectedUnit.canAttackTo(target))
				.toList();
	}
}
