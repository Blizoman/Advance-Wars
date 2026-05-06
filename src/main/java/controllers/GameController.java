/**
 * Acts as the central bridge between the graphical user interface (GUI) and the backend game logic.
 * It manages user interactions such as selecting units, moving, attacking, capturing, and
 * purchasing from factories. Additionally, it controls the turn cycle, manages UI states, handles
 * AI bot execution, and notifies the view listeners of any state changes to trigger re-rendering.
 *
 * @author xpruzir00
 */

package controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import board.AvailableMaps;
import board.Position;
import board.Tile;
import game.Game;
import game.PathFinder;
import game.Session;
import gamer.BotType;
import gamer.DummyBot;
import gamer.GeminiBot;
import gamer.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import tools.Consts;
import tools.LogFiler;
import unit.Unit;
import unit.UnitType;

public class GameController {

	@Getter
	private final Session session;
	@Getter
	private final Game game;

	private final DummyBot weakBot;
	private final GeminiBot strongBot;
	private final PathFinder pathFinder;

	private final List<Runnable> stateListeners = new ArrayList<>();

	@Getter
	private Position selectedFactory;
	@Getter
	private Unit selectedUnit;

	@Getter
	private boolean isAttacking;
	private boolean unitMoved = false;

	private List<Unit> attackTargets;
	private Map<Position, Integer> abailableMoveCosts = null;
	//TODO: ADO can simplify to List<Position> (availablePositions) if needed

	public GameController(Session session, Game game) {
		this.session = session;
		this.game = game;
		this.pathFinder = new PathFinder(game.getGameBoard());
		this.weakBot = new DummyBot(this.pathFinder);
		this.strongBot = new GeminiBot(this.pathFinder);
		this.session.setOnGameEnd(ignored -> stateChanged());
	}

	/////////////////// VALUES ///////////////////
	//////////////////////////////////////////////
	/////////////////// STATE ////////////////////

	private void stateChanged() {
		for (Runnable listener : this.stateListeners)
			listener.run();
	}

	public void setOnStateChanged(Runnable onStateChanged) {
		if (onStateChanged != null)
			this.stateListeners.add(onStateChanged);
	}

	private void deselect() {
		selectedUnit = null;
		selectedFactory = null;
		isAttacking = false;
		unitMoved = false;
		abailableMoveCosts = null;
		attackTargets = null;
		stateChanged();
	}

	private void deselectAsWait() {
		if (selectedUnit != null)
			selectedUnit.setUsed(true);
		deselect();
	}

	private void deselectExceptUnit() {
		selectedFactory = null;
		isAttacking = false;
		unitMoved = false;
		abailableMoveCosts = null;
		attackTargets = null;
		stateChanged();
	}

	/////////////////// STATE ////////////////////
	//////////////////////////////////////////////
	//////////////////// TURN ////////////////////

	public void startGame() {
		this.session.startTurn();
		runTurnLoop();
	}

	private void runTurnLoop() {
		if (getActivePlayer().isBot()) {
			Timeline botTimeline = new Timeline();
			botTimeline.setCycleCount(Timeline.INDEFINITE);

			KeyFrame frame = new KeyFrame(Duration.millis(Consts.BOT_ACTION_DELAY), e -> {
				boolean hasMoreActions = performBotAction(getActivePlayer());
				stateChanged();

				if (!hasMoreActions) {
					botTimeline.stop();
					deselect();
					this.session.endTurn();
					runTurnLoop(); // Move to next player
				}
			});

			botTimeline.getKeyFrames().add(frame);
			botTimeline.play();
		} else {
			stateChanged();
		}
	}

	private boolean performBotAction(Player player) {
		BotType type = player.getBotType();
		if (type == null || type == BotType.NONE)
			type = BotType.WEAK;
		if (type == BotType.STRONG)
			return strongBot.performNextAction(this.session);
		return weakBot.performNextAction(this.session);
	}

	public void onEndTurn() {
		deselect();
		this.session.endTurn();
		runTurnLoop();
	}

	public void onStepForward() {
		this.session.stepForward();
		deselect();
	}

	public void onStepBackward() {
		this.session.stepBackward();
		deselect();
	}

	//////////////////// TURN ////////////////////
	//////////////////////////////////////////////
	////////////////// CAPTURE ///////////////////

	public void onCapture() {
		this.session.tryCapture(selectedUnit,
				this.game.getGameBoard().getTile(selectedUnit.getPosition()));
		deselect();
	}

	public boolean canCaptureSelected() {
		if (selectedUnit == null)
			return false;
		Tile tile = this.game.getGameBoard().getTile(selectedUnit.getPosition());
		return !selectedUnit.isCaptured()
				&& selectedUnit.getType().isCanCapture()
				&& tile.getTerrain().isCapturable()
				&& (tile.getOwner() == null || tile.getOwner() != selectedUnit.getPlayer());
	}

	////////////////// CAPTURE ///////////////////
	//////////////////////////////////////////////
	//////////////////// BUY /////////////////////

	public void onBuyUnit(UnitType type) {
		if (selectedFactory != null // Selected factory
				&& getActivePlayer().canAfford(type.getCost()) // Can afford
				&& this.game.getGameBoard().getTile(selectedFactory).isEmpty()) // Can place new unit
			this.session.buyUnit(selectedFactory, type);
		deselect();
	}

	public boolean canBuyUnit(UnitType type) {
		return selectedFactory != null && getActivePlayer().canAfford(type.getCost());
	}

	//////////////////// BUY /////////////////////
	//////////////////////////////////////////////
	/////////////////// ATTACK ///////////////////

	public void beginAttackMode() {
		if (canAttack()) {
			isAttacking = true;
			abailableMoveCosts = null;
			stateChanged();
		}
	}

	public boolean canAttack() {
		return canSelectedDoAttack() && !getAttackTargets().isEmpty();
	}

	private boolean canSelectedDoAttack() {
		return selectedUnit != null
				&& !selectedUnit.isUsed()
				&& (selectedUnit.getType().isCanAttackAfterMove() || selectedUnit.getMovesLeft() > 0);
	}

	/////////////////// ATTACK ///////////////////
	//////////////////////////////////////////////
	/////////////////// CLICK ////////////////////

	public void onTileClicked(Position clickedPosition) {
		if (trySelectFactory(clickedPosition))
			return;
		if (trySelectUnit(clickedPosition))
			return;
		if (selectedUnit == null)
			return; // next calls are unit-only
		if (tryAttack(clickedPosition))
			return;
		if (trySwitchUnit(clickedPosition))
			return;
		if (tryMove(clickedPosition))
			return;
		// deselectAsWait();
		deselect();
	}

	private boolean trySelectFactory(Position position) {
		if (selectedUnit != null && getAbailableMoveCosts().containsKey(position)) // selected unit can move to factory
			return false;

		if (!canSelectFactory(position))
			return false;

		deselectAsWait();
		selectedFactory = position;
		stateChanged();
		return true;
	}

	private boolean canSelectFactory(Position position) {
		Tile tile = this.game.getGameBoard().getTile(position);
		return tile.getTerrain().isProduceUnits()
				&& tile.getOwner() == getActivePlayer()
				&& tile.isEmpty();
	}

	private boolean trySelectUnit(Position position) {
		if (selectedUnit != null)
			return false;

		Unit clicked = this.game.getGameBoard().getUnit(position);
		if (clicked == null || clicked.getPlayer() != getActivePlayer() || clicked.isUsed())
			return false;

		selectedUnit = clicked;
		deselectExceptUnit();
		return true;
	}

	private boolean tryAttack(Position position) {
		if (!isAttacking)
			return false;
		Unit clicked = this.game.getGameBoard().getUnit(position);
		if (getAttackTargets().contains(clicked)) {
			this.session.attack(selectedUnit, clicked);
			deselect();
		} else
			isAttacking = false;
		stateChanged();
		// deselectAsWait();
		return true;
	}

	private boolean trySwitchUnit(Position position) {
		Unit clicked = this.game.getGameBoard().getUnit(position);

		if (clicked == null // Invalid
				|| clicked.getPlayer() != selectedUnit.getPlayer() // Not mine player
				|| clicked == selectedUnit) // Clicked at same unit, do tryMove
			return false;

		// Disable switching to other unit if this already moved
		if (clicked.isUsed() || unitMoved)
			return true;

		selectedUnit = clicked;
		deselectExceptUnit();
		return true;
	}

	private boolean tryMove(Position position) {
		if (!getAbailableMoveCosts().containsKey(position)) // Not reachable
			return false;
		if (unitMoved) // Loglessly allow
			return true;

		this.session.moveUnit(selectedUnit, position);

		selectedFactory = null;
		isAttacking = false;
		unitMoved = true;
		abailableMoveCosts = null;
		attackTargets = null;
		stateChanged();

		return true;
	}

	/////////////////// CLICK ////////////////////
	//////////////////////////////////////////////
	//////////////////// LAZY ////////////////////

	public Map<Position, Integer> getAbailableMoveCosts() {
		if (unitMoved)
			return Map.of();

		if (abailableMoveCosts == null && selectedUnit != null)
			abailableMoveCosts = this.pathFinder.findReachableTiles(selectedUnit);
		return abailableMoveCosts;
	}

	public List<Unit> getAttackTargets() {
		if (attackTargets == null)
			attackTargets = computeAttackTargets();
		return attackTargets;
	}

	private List<Unit> computeAttackTargets() {
		if (!canSelectedDoAttack())
			return List.of();

		return this.game.getGameBoard().getAllUnits().stream()
				.filter(target -> target.getPlayer() != selectedUnit.getPlayer()
						&& selectedUnit.canAttackTo(target))
				.toList();
	}

	//////////////////// LAZY ////////////////////
	//////////////////////////////////////////////
	/////////////////// MISCS ////////////////////

	public void onWait() {
		deselectAsWait();
	}

	public Player getActivePlayer() { return this.session.getActive(); }

	public void onSave(Path path, AvailableMaps.MapMetadata map) throws IOException {
		LogFiler.save(this.session.getEventLog(), path, map, this.game.getInitialPlayers());
	}

	/////////////////// MISCS ////////////////////
	//////////////////////////////////////////////
}
