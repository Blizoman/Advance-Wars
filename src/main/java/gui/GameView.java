package gui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import board.AvailableMaps;
import board.GameBoard;
import board.GameBoardLoader;
import board.Position;
import controllers.GameController;
import com.google.gson.JsonObject;
import event.GameEvent;
import game.Game;
import game.Session;
import gamer.Player;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import unit.Unit;
import unit.UnitType;
import tools.LogFiler;

public class GameView extends HBox {
	private static final String UI_FONT = "Manrope";
	private final Renderer renderer;
	private final Canvas canvas;
	private final AvailableMaps.MapMetadata mapMetadata;
	private final Label playerLabel = new Label();
	private final Label moneyLabel = new Label();
	private final Label statusLabel = new Label();
	private final Label infoLabel = new Label();
	private final ListView<GameEvent> eventLogView = new ListView<>();
	private int currentLogCursor = 0;

	private enum UiState {
		IDLE,
		UNIT_SELECTED,
		UNIT_MOVED,
		ATTACK_MODE,
		FACTORY_SELECTED
	}

	public GameView(App app, AvailableMaps.MapMetadata map, List<Player> players) {
		this(app, map, players, null);
	}

	public GameView(App app, AvailableMaps.MapMetadata map, List<Player> players, Path replayLog) {
		AvailableMaps.MapMetadata effectiveMap = map;
		List<Player> effectivePlayers = players;
		JsonObject replayData = null;
		if (replayLog != null) {
			try {
				replayData = LogFiler.loadReplay(replayLog);
				LogFiler.ReplayHeader replayHeader = LogFiler.loadHeader(replayData);
				effectiveMap = replayHeader.map();
				effectivePlayers = new java.util.ArrayList<>();
				for (String playerName : replayHeader.playerNames())
					effectivePlayers.add(new Player(playerName, false));
			} catch (IOException e) {
				throw new RuntimeException("Failed to load replay header", e);
			}
		}
		final AvailableMaps.MapMetadata finalMap = effectiveMap;
		final List<Player> finalPlayers = effectivePlayers;
		this.mapMetadata = finalMap;
		getStyleClass().add("root-view");
		setSpacing(12);
		setPadding(new Insets(12));
		Game game;
		try {
			game = new Game(loadMap(finalMap, finalPlayers), finalPlayers);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load map", e);
		}
		Session session;
		if (replayLog != null)
			session = new Session(game, LogFiler.loadEvents(replayData, finalPlayers));
		else
			session = new Session(game);
		GameController controller = new GameController(session, game);
		this.canvas = new Canvas(880, 700);
		this.renderer = new Renderer(canvas, controller);
		renderer.resizeCanvasToBoard();

		ContextMenu contextMenu = new ContextMenu();
		contextMenu.getStyleClass().add("action-menu");

		canvas.setOnMouseClicked(e -> {
			contextMenu.hide();

			if (e.getButton() != MouseButton.PRIMARY)
				return;
			Position pos = renderer.screenToGrid(e.getX(), e.getY());

			if (!controller.getGame().getGameBoard().isValidPosition(pos))
				return;

			UiState before = getUiState(controller);
			Unit selected = controller.getSelectedUnit();
			if (before == UiState.UNIT_MOVED) {
				showActionMenu(contextMenu, controller, e.getScreenX(), e.getScreenY());
				return;
			}
			if (before == UiState.ATTACK_MODE
					&& selected != null
					&& pos.equals(selected.getPosition())) {
				if (before == UiState.ATTACK_MODE)
					controller.onTileClicked(pos);
				showActionMenu(contextMenu, controller, e.getScreenX(), e.getScreenY());
				return;
			}

			controller.onTileClicked(pos);
			UiState after = getUiState(controller);

			if (after == UiState.UNIT_MOVED) {
				showActionMenu(contextMenu, controller, e.getScreenX(), e.getScreenY());
			}
		});

		Group mapGroup = new Group(canvas);
		ScrollPane mapScrollPane = new ScrollPane(mapGroup);
		mapScrollPane.setPannable(true);
		mapScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		mapScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		mapScrollPane.setFitToWidth(true);
		mapScrollPane.setFitToHeight(true);
		Slider zoomSlider = new Slider(0.3, 2.0, renderer.getZoom());
		zoomSlider.setPrefWidth(160);
		Label zoomValueLabel = new Label("100%");
		zoomValueLabel.setMinWidth(48);
		zoomValueLabel.setAlignment(Pos.CENTER_RIGHT);
		Runnable applyZoom = () -> {
			renderer.setZoom(zoomSlider.getValue());
			zoomValueLabel.setText((int) Math.round(renderer.getZoom() * 100) + "%");
			renderer.render();
		};
		mapScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
			if (!e.isControlDown())
				return;
			double step = 0.05;
			double raw = e.getDeltaY();
			if (raw == 0)
				raw = e.getTextDeltaY();
			if (raw == 0)
				raw = e.getDeltaX();
			if (raw == 0)
				return;
			double delta = Math.signum(raw) * step;
			if (e.isShiftDown())
				delta = -delta;
			double next = Math.max(zoomSlider.getMin(),
					Math.min(zoomSlider.getMax(), zoomSlider.getValue() + delta));
			zoomSlider.setValue(next);
			e.consume();
		});
		zoomSlider.valueProperty().addListener((obs, oldValue, newValue) -> applyZoom.run());
		Button zoomOutBtn = new Button("-");
		zoomOutBtn.getStyleClass().add("btn-icon");
		zoomOutBtn.setTooltip(new Tooltip("Zoom out"));
		zoomOutBtn.setOnAction(
				e -> zoomSlider.setValue(Math.max(zoomSlider.getMin(), zoomSlider.getValue() - 0.05)));
		Button zoomInBtn = new Button("+");
		zoomInBtn.getStyleClass().add("btn-icon");
		zoomInBtn.setTooltip(new Tooltip("Zoom in"));
		zoomInBtn.setOnAction(
				e -> zoomSlider.setValue(Math.min(zoomSlider.getMax(), zoomSlider.getValue() + 0.05)));
		Button resetZoomBtn = new Button("100%");
		resetZoomBtn.getStyleClass().add("btn-ghost");
		resetZoomBtn.setTooltip(new Tooltip("Reset zoom"));
		resetZoomBtn.setOnAction(e -> zoomSlider.setValue(1.0));
		ToolBar mapToolbar =
				new ToolBar(zoomOutBtn, zoomSlider, zoomValueLabel, zoomInBtn, resetZoomBtn);
		mapToolbar.getStyleClass().add("map-toolbar");
		mapToolbar.setMinHeight(36);
		mapToolbar.setPrefHeight(36);
		mapToolbar.setMaxWidth(Double.MAX_VALUE);
		VBox mapPanel = new VBox(8, mapToolbar, mapScrollPane);
		mapPanel.getStyleClass().add("map-panel");
		mapPanel.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(mapPanel, Priority.ALWAYS);
		VBox.setVgrow(mapScrollPane, Priority.ALWAYS);
		mapScrollPane.getStyleClass().add("map-scroll");
		canvas.setOnContextMenuRequested(null);
		Runnable refresh = () -> {
			Player active = controller.getActivePlayer();
			playerLabel.setText("Turn: " + active.getName());
			javafx.scene.paint.Color c = active.getColor();
			playerLabel.setTextFill(c);
			moneyLabel.setText("Money: $" + active.getMoney());
			updateStatusAndInfo(controller);
			refreshEventLog(controller);
			renderer.render();
		};
		controller.setOnStateChanged(refresh);
		session.setOnGameEnd(winner -> app.showGameEnd(winner, exportPath -> {
			try {
				controller.onSave(exportPath, finalMap);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to export replay", ex);
			}
		}));
		VBox sidebar = buildSidebar(app, controller);
		getChildren().addAll(mapPanel, sidebar);
		playIntro(mapPanel, sidebar);
		if (replayLog == null)
			controller.startGame();
		else
			refresh.run();
	}

	private UiState getUiState(GameController controller) {
		if (controller.isAttacking())
			return UiState.ATTACK_MODE;
		if (controller.getSelectedFactory() != null)
			return UiState.FACTORY_SELECTED;
		Unit selected = controller.getSelectedUnit();
		if (selected != null) {
			var moves = controller.getAbailableMoveCosts();
			if (moves != null && moves.isEmpty())
				return UiState.UNIT_MOVED;
			return UiState.UNIT_SELECTED;
		}
		return UiState.IDLE;
	}

	private void showActionMenu(ContextMenu contextMenu, GameController controller,
			double screenX, double screenY) {
		contextMenu.getItems().clear();

		if (controller.canAttack()) {
			MenuItem attackItem = new MenuItem("Attack");
			attackItem.setOnAction(ev -> controller.beginAttackMode());
			contextMenu.getItems().add(attackItem);
		}

		if (controller.canCaptureSelected()) {
			MenuItem captureItem = new MenuItem("Capture");
			captureItem.setOnAction(ev -> controller.onCapture());
			contextMenu.getItems().add(captureItem);
		}

		MenuItem waitItem = new MenuItem("Wait");
		waitItem.setOnAction(ev -> controller.onWait());
		contextMenu.getItems().add(waitItem);

		MenuItem cancelItem = new MenuItem("Cancel");
		cancelItem.setOnAction(ev -> controller.onStepBackward());
		contextMenu.getItems().add(cancelItem);

		contextMenu.show(canvas, screenX, screenY);
	}

	private void updateStatusAndInfo(GameController controller) {
		UiState state = getUiState(controller);
		switch (state) {
			case IDLE -> statusLabel.setText("Select a unit or factory.");
			case UNIT_SELECTED -> statusLabel.setText("Select a destination tile.");
			case UNIT_MOVED -> statusLabel.setText("Choose an action: Attack, Capture, or Wait.");
			case ATTACK_MODE -> statusLabel.setText("Select an enemy to attack.");
			case FACTORY_SELECTED -> statusLabel.setText("Choose a unit to buy.");
		}

		Unit selected = controller.getSelectedUnit();
		if (selected != null) {
			String info = "Type: " + selected.getType().name()
					+ "\nHP: " + selected.getHp()
					+ "\nMoves left: " + selected.getMovesLeft();
			if (selected.isUsed())
				info += "\nState: Used";
			infoLabel.setText(info);
			return;
		}
		if (controller.getSelectedFactory() != null) {
			infoLabel.setText("Factory selected.\nPick a unit to buy.");
			return;
		}
		infoLabel.setText("No selection.");
	}

	private void playIntro(Node... nodes) {
		for (int i = 0; i < nodes.length; i++) {
			Node node = nodes[i];
			node.setOpacity(0);
			FadeTransition ft = new FadeTransition(Duration.millis(220), node);
			ft.setFromValue(0);
			ft.setToValue(1);
			ft.setDelay(Duration.millis(70L * i));
			ft.setInterpolator(Interpolator.EASE_OUT);
			ft.play();
		}
	}

	private VBox buildSidebar(App app, GameController controller) {
		VBox sidebar = new VBox(12);
		sidebar.getStyleClass().add("sidebar");
		sidebar.setPadding(new Insets(10));
		sidebar.setPrefWidth(420);
		sidebar.setMinWidth(360);
		playerLabel.setFont(Font.font(UI_FONT, FontWeight.BOLD, 16));
		playerLabel.setTextFill(Color.BLACK);
		playerLabel.getStyleClass().add("player-label");
		moneyLabel.setFont(Font.font(UI_FONT, 14));
		moneyLabel.setTextFill(Color.BLACK);
		moneyLabel.getStyleClass().add("money-label");
		statusLabel.setFont(Font.font(UI_FONT, 12));
		statusLabel.setTextFill(Color.BLACK);
		statusLabel.setWrapText(true);
		infoLabel.setFont(Font.font(UI_FONT, 12));
		infoLabel.setTextFill(Color.BLACK);
		infoLabel.setWrapText(true);
		VBox actionPanel = new VBox(8);
		actionPanel.getStyleClass().add("panel-card");
		actionPanel.setMaxWidth(Double.MAX_VALUE);
		VBox logPanel = new VBox(8);
		logPanel.getStyleClass().add("panel-card");
		logPanel.setMaxWidth(Double.MAX_VALUE);

		Label statusTitle = new Label("Status");
		statusTitle.setFont(Font.font(UI_FONT, FontWeight.BOLD, 12));
		statusTitle.setTextFill(Color.BLACK);
		statusTitle.getStyleClass().add("section-title");
		VBox statusPanel = new VBox(4, statusTitle, statusLabel);
		statusPanel.getStyleClass().add("panel-card");

		Label infoTitle = new Label("Selection");
		infoTitle.setFont(Font.font(UI_FONT, FontWeight.BOLD, 12));
		infoTitle.setTextFill(Color.BLACK);
		infoTitle.getStyleClass().add("section-title");
		VBox infoPanel = new VBox(4, infoTitle, infoLabel);
		infoPanel.getStyleClass().add("panel-card");
		Label historyLabel = new Label("Event History");
		historyLabel.setFont(Font.font(UI_FONT, FontWeight.BOLD, 12));
		historyLabel.setTextFill(Color.BLACK);
		historyLabel.getStyleClass().add("section-title");
		eventLogView.setPrefHeight(300);
		eventLogView.setFocusTraversable(false);
		eventLogView.getStyleClass().add("log-list");
		Label emptyPlaceholder = new Label("No events yet");
		emptyPlaceholder.setTextFill(Color.BLACK);
		eventLogView.setPlaceholder(emptyPlaceholder);
		eventLogView.setCellFactory(list -> new ListCell<>() {
			@Override
			protected void updateItem(GameEvent event, boolean empty) {
				super.updateItem(event, empty);
				setOpacity(1.0);
				setTextFill(Color.BLACK);
				setBackground(null);
				setFont(Font.font(UI_FONT, 12));
				if (empty || event == null) {
					setText(null);
					setGraphic(null);
					setBackground(null);
					setFont(Font.font(UI_FONT, 12));
					setOpacity(1.0);
					return;
				}
				int index = getIndex();
				String marker =
						index == currentLogCursor - 1 ? "▶ " : index >= currentLogCursor ? "↷ " : "  ";
				String text = marker + (index + 1) + ". " + formatEvent(event);
				if (event instanceof event.TurnChangedEvent tce && tce.getPlayerBefore() != null
						&& tce.getPlayerAfter() != null) {
					javafx.scene.Group splitDot = createSplitColorDot(tce.getPlayerBefore().getColor(),
							tce.getPlayerAfter().getColor(), 6);
					Label lbl = new Label(text);
					lbl.setTextFill(Color.BLACK);
					HBox hb = new HBox(8, splitDot, lbl);
					hb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
					setGraphic(hb);
					setText(null);
				} else {
					gamer.Player evPlayer = null;
					if (event instanceof event.UnitBoughtEvent ube)
						evPlayer = ube.getPlayer();
					else if (event instanceof event.UnitAttackEvent uae)
						evPlayer = uae.getPlayer();
					else if (event instanceof event.UnitMovedEvent ume)
						evPlayer = ume.getPlayer();
					else if (event instanceof event.UnitDiedEvent ude)
						evPlayer = ude.getUnit().getPlayer();
					else if (event instanceof event.CaptureProgressEvent cpe)
						evPlayer = controller.getGame().getGameBoard().getUnit(cpe.getPosition()) == null
								? null
								: controller.getGame().getGameBoard().getUnit(cpe.getPosition())
										.getPlayer();
					else if (event instanceof event.CityCapturedEvent cce)
						evPlayer = cce.getPlayer();
					else if (event instanceof event.MultipleGameEvent mge)
						evPlayer = mge.player();
					if (evPlayer != null) {
						javafx.scene.shape.Circle dot =
								new javafx.scene.shape.Circle(6, evPlayer.getColor());
						Label lbl = new Label(text);
						lbl.setTextFill(Color.BLACK);
						HBox hb = new HBox(8, dot, lbl);
						hb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
						setGraphic(hb);
						setText(null);
					} else {
						setText(text);
						setGraphic(null);
					}
				}
				if (index == currentLogCursor - 1) {
					setFont(Font.font(UI_FONT, FontWeight.BOLD, getFont().getSize()));
				} else if (index >= currentLogCursor) {
					setTextFill(Color.GRAY);
					setOpacity(0.72);
				} else {
					setFont(Font.font(UI_FONT, 12));
				}
			}
		});
		
		VBox actionMenu = new VBox(5);
		actionMenu.getStyleClass().add("panel-card");
		
		Label buyLabel = new Label("Buy Unit:");
		buyLabel.setFont(Font.font(UI_FONT, FontWeight.BOLD, 12));
		buyLabel.setTextFill(Color.BLACK);
		buyLabel.getStyleClass().add("section-title");
		
		Button buyInfantryBtn = new Button("Infantry");
		buyInfantryBtn.setPrefWidth(160);
		buyInfantryBtn.setDisable(true);
		buyInfantryBtn.getStyleClass().add("btn-secondary");
		buyInfantryBtn.setTooltip(new Tooltip("Buy Infantry"));
		
		Button buyTankBtn = new Button("Tank");
		buyTankBtn.setPrefWidth(160);
		buyTankBtn.setDisable(true);
		buyTankBtn.getStyleClass().add("btn-secondary");
		buyTankBtn.setTooltip(new Tooltip("Buy Tank"));
		
		Button buyCannonBtn = new Button("Cannon");
		buyCannonBtn.setPrefWidth(160);
		buyCannonBtn.setDisable(true);
		buyCannonBtn.getStyleClass().add("btn-secondary");
		buyCannonBtn.setTooltip(new Tooltip("Buy Cannon"));
		
		buyInfantryBtn.setOnAction(e -> controller.onBuyUnit(UnitType.INFANTRY));
		buyTankBtn.setOnAction(e -> controller.onBuyUnit(UnitType.TANK));
		buyCannonBtn.setOnAction(e -> controller.onBuyUnit(UnitType.CANNON));
		
		actionMenu.getChildren().addAll(
				buyLabel,
				buyInfantryBtn,
				buyTankBtn,
				buyCannonBtn);
				
		Button endTurnBtn = new Button("End Turn");
		endTurnBtn.setPrefWidth(200);
		endTurnBtn.setOnAction(e -> controller.onEndTurn());
		endTurnBtn.getStyleClass().add("btn-primary");
		endTurnBtn.setTooltip(new Tooltip("End current turn"));
		
		Button stepBackBtn = new Button("◀ Step Back");
		stepBackBtn.setPrefWidth(160);
		stepBackBtn.setOnAction(e -> controller.onStepBackward());
		stepBackBtn.getStyleClass().add("btn-secondary");
		stepBackBtn.getStyleClass().add("btn-compact");
		stepBackBtn.setTooltip(new Tooltip("Step back in replay"));
		
		Button stepFwdBtn = new Button("Step Forward ▶");
		stepFwdBtn.setPrefWidth(160);
		stepFwdBtn.setOnAction(e -> controller.onStepForward());
		stepFwdBtn.getStyleClass().add("btn-secondary");
		stepFwdBtn.getStyleClass().add("btn-compact");
		stepFwdBtn.setTooltip(new Tooltip("Step forward in replay"));

		HBox stepRow = new HBox(8, stepBackBtn, stepFwdBtn);
		stepRow.setAlignment(Pos.CENTER);
		HBox.setHgrow(stepBackBtn, Priority.ALWAYS);
		HBox.setHgrow(stepFwdBtn, Priority.ALWAYS);
		stepBackBtn.setMaxWidth(Double.MAX_VALUE);
		stepFwdBtn.setMaxWidth(Double.MAX_VALUE);
		
		Button exportBtn = new Button("Export Session...");
		exportBtn.setPrefWidth(160);
		exportBtn.setOnAction(e -> {
			Path exportPath = app.chooseSaveReplayFile();
			if (exportPath == null)
				return;
			try {
				controller.onSave(exportPath, mapMetadata);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to export replay", ex);
			}
		});
		exportBtn.getStyleClass().add("btn-secondary");
		exportBtn.getStyleClass().add("btn-compact");
		exportBtn.setTooltip(new Tooltip("Export replay log"));
		
		Button menuBtn = new Button("Back to Menu");
		menuBtn.setPrefWidth(160);
		menuBtn.setOnAction(e -> app.showMapSelect());
		menuBtn.getStyleClass().add("btn-ghost");
		menuBtn.getStyleClass().add("btn-compact");
		menuBtn.setTooltip(new Tooltip("Return to map select"));

		HBox bottomRow = new HBox(8, exportBtn, menuBtn);
		bottomRow.setAlignment(Pos.CENTER);
		HBox.setHgrow(exportBtn, Priority.ALWAYS);
		HBox.setHgrow(menuBtn, Priority.ALWAYS);
		exportBtn.setMaxWidth(Double.MAX_VALUE);
		menuBtn.setMaxWidth(Double.MAX_VALUE);
		
		controller.setOnStateChanged(() -> {
			boolean factorySelected = controller.getSelectedFactory() != null;
			buyInfantryBtn.setDisable(!factorySelected || !controller.canBuyUnit(UnitType.INFANTRY));
			buyTankBtn.setDisable(!factorySelected || !controller.canBuyUnit(UnitType.TANK));
			buyCannonBtn.setDisable(!factorySelected || !controller.canBuyUnit(UnitType.CANNON));
		});
		
		actionPanel.getChildren().addAll(
				playerLabel,
				moneyLabel,
				statusPanel,
				infoPanel,
				new Separator(),
				actionMenu,
				new Separator(),
				endTurnBtn,
				new Separator(),
				stepRow,
				new Separator(),
				bottomRow);
				
		VBox.setVgrow(actionMenu, Priority.NEVER);
		logPanel.getChildren().addAll(historyLabel, eventLogView);
		VBox.setVgrow(eventLogView, Priority.ALWAYS);
		VBox.setVgrow(logPanel, Priority.ALWAYS);
		sidebar.getChildren().addAll(actionPanel, logPanel);
		return sidebar;
	}

	private void refreshEventLog(GameController controller) {
		currentLogCursor = controller.getSession().getLogCursor();
		eventLogView.getItems().setAll(controller.getSession().getEventLog());
		eventLogView.refresh();
		if (currentLogCursor > 0 && currentLogCursor - 1 < eventLogView.getItems().size())
			eventLogView.scrollTo(currentLogCursor - 1);
		else if (!eventLogView.getItems().isEmpty())
			eventLogView.scrollTo(0);
	}

	private String formatEvent(GameEvent event) {
		return switch (event.type()) {
			case UNIT_BOUGHT -> "Unit bought";
			case UNIT_MOVED -> "Unit moved";
			case UNIT_ATTACKED -> "Unit attacked";
			case UNIT_DIED -> "Unit died";
			case CITY_CAPTURED -> "City captured";
			case CAPTURE_PROGRESS -> "Capture progress";
			case PLAYER_ELIMINATED -> "Player eliminated";
			case TURN_CHANGED -> "Turn changed";
		};
	}

	private GameBoard loadMap(AvailableMaps.MapMetadata map, List<Player> players)
			throws IOException {
		return GameBoardLoader.loadMap(map, players);
	}

	private javafx.scene.Group createSplitColorDot(javafx.scene.paint.Color colorLeft,
			javafx.scene.paint.Color colorRight, double radius) {
		javafx.scene.Group group = new javafx.scene.Group();
		javafx.scene.shape.Arc arcLeft =
				new javafx.scene.shape.Arc(0, 0, radius * 2, radius * 2, 90, 180);
		arcLeft.setFill(colorLeft);
		arcLeft.setStroke(javafx.scene.paint.Color.TRANSPARENT);
		javafx.scene.shape.Arc arcRight =
				new javafx.scene.shape.Arc(0, 0, radius * 2, radius * 2, -90, 180);
		arcRight.setFill(colorRight);
		arcRight.setStroke(javafx.scene.paint.Color.TRANSPARENT);
		group.getChildren().addAll(arcLeft, arcRight);
		return group;
	}
}