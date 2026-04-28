package gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
import gamer.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import unit.UnitType;
import tools.LogFiler;

public class GameView extends HBox {
	private final Renderer renderer;
	private final AvailableMaps.MapMetadata mapMetadata;
	private final Label playerLabel = new Label();
	private final Label moneyLabel = new Label();
	private final ListView<GameEvent> eventLogView = new ListView<>();
	private int currentLogCursor = 0;

	public GameView(App app, AvailableMaps.MapMetadata map, List<Player> players) {
		this(app, map, players, null);
	}

	public GameView(App app, AvailableMaps.MapMetadata map, List<Player> players, Path replayLog) {
		AvailableMaps.MapMetadata effectiveMap = map;
		List<Player> effectivePlayers = players;
		JsonObject replayData = null;

		if (replayLog != null) {
			try {
				replayData = LogFiler.readReplay(replayLog);
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

		Game game;
		try {
			game = new Game(loadMap(finalMap, finalPlayers), finalPlayers);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load map", e);
		}
		game.initSession();
		if (replayLog != null) {
			List<GameEvent> events = LogFiler.loadEvents(replayData, finalPlayers);
			game.loadSession(events);
		}

		GameController controller = new GameController(game.getSession(), game);

		Canvas canvas = new Canvas(880, 700);
		this.renderer = new Renderer(canvas, controller);
		renderer.resizeCanvasToBoard();

		canvas.setOnMouseClicked(e -> {
			if (e.getButton() != MouseButton.PRIMARY)
				return;
			Position pos = renderer.screenToGrid(e.getX(), e.getY());
			if (!controller.getGame().getGameBoard().isValidPosition(pos)) {
				if (controller.getSelectedUnit() != null)
					controller.onWait();
				return;
			}
			controller.onTileClicked(pos);
		});

		Group mapGroup = new Group(canvas);
		ScrollPane mapScrollPane = new ScrollPane(mapGroup);
		mapScrollPane.setPannable(true);
		mapScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		mapScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		mapScrollPane.setFitToWidth(false);
		mapScrollPane.setFitToHeight(false);

		Slider zoomSlider = new Slider(0.3, 1.0, renderer.getZoom());
		zoomSlider.setPrefWidth(160);
		Label zoomValueLabel = new Label("100%");
		zoomValueLabel.setMinWidth(48);
		zoomValueLabel.setAlignment(Pos.CENTER_RIGHT);

		Runnable applyZoom = () -> {
			renderer.setZoom(zoomSlider.getValue());
			zoomValueLabel.setText((int) Math.round(renderer.getZoom() * 100) + "%");
			renderer.render();
		};
		zoomSlider.valueProperty().addListener((obs, oldValue, newValue) -> applyZoom.run());

		Button zoomOutBtn = new Button("-");
		zoomOutBtn.setOnAction(
				e -> zoomSlider.setValue(Math.max(zoomSlider.getMin(), zoomSlider.getValue() - 0.05)));

		Button zoomInBtn = new Button("+");
		zoomInBtn.setOnAction(
				e -> zoomSlider.setValue(Math.min(zoomSlider.getMax(), zoomSlider.getValue() + 0.05)));

		Button resetZoomBtn = new Button("100%");
		resetZoomBtn.setOnAction(e -> zoomSlider.setValue(1.0));

		ToolBar mapToolbar =
				new ToolBar(zoomOutBtn, zoomSlider, zoomValueLabel, zoomInBtn, resetZoomBtn);
		mapToolbar.setMinHeight(36);
		mapToolbar.setPrefHeight(36);
		mapToolbar.setMaxWidth(Double.MAX_VALUE);

		VBox mapPanel = new VBox(8, mapToolbar, mapScrollPane);
		VBox.setVgrow(mapScrollPane, Priority.ALWAYS);

		canvas.setOnContextMenuRequested(null);

		Runnable refresh = () -> {
			Player active = controller.getActivePlayer();
			playerLabel.setText("Turn: " + active.getName());
			// color the player name label with player's color
			javafx.scene.paint.Color c = active.getColor();
			playerLabel.setTextFill(c);
			moneyLabel.setText("Money: $" + active.getMoney());
			refreshEventLog(controller);
			renderer.render();
		};
		controller.setOnStateChanged(refresh);

		game.getSession().setOnGameEnd(winner -> app.showGameEnd(winner, exportPath -> {
			try {
				controller.onSave(exportPath, finalMap);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to export replay", ex);
			}
		}));

		getChildren().addAll(mapPanel, buildSidebar(app, controller));

		if (replayLog == null)
			controller.startGame();
		else
			refresh.run();
	}

	private HBox buildSidebar(App app, GameController controller) {
		HBox sidebar = new HBox(12);
		sidebar.setPadding(new Insets(10));
		sidebar.setPrefWidth(520);
		sidebar.setMinWidth(480);

		playerLabel.setFont(Font.font(playerLabel.getFont().getFamily(), FontWeight.BOLD, 16));
		playerLabel.setTextFill(Color.BLACK);
		moneyLabel.setFont(Font.font(moneyLabel.getFont().getFamily(), 14));
		moneyLabel.setTextFill(Color.BLACK);

		VBox actionPanel = new VBox(8);
		actionPanel.setPrefWidth(230);
		actionPanel.setMinWidth(210);

		VBox logPanel = new VBox(8);
		logPanel.setPrefWidth(250);
		logPanel.setMinWidth(230);

		Label historyLabel = new Label("Event History");
		historyLabel.setFont(Font.font(historyLabel.getFont().getFamily(), FontWeight.BOLD, 12));
		historyLabel.setTextFill(Color.BLACK);
		eventLogView.setPrefHeight(540);
		eventLogView.setFocusTraversable(false);
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
				setFont(Font.getDefault());
				if (empty || event == null) {
					setText(null);
					setGraphic(null);
					setBackground(null);
					setFont(Font.getDefault());
					setOpacity(1.0);
					return;
				}

				int index = getIndex();
				String marker =
						index == currentLogCursor - 1 ? "▶ " : index >= currentLogCursor ? "↷ " : "  ";
				String text = marker + (index + 1) + ". " + formatEvent(event);

				// handle split-color dot for TurnChangedEvent
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
					// attempt to extract a player for coloring (single-color dot)
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
					setFont(Font.font(getFont().getFamily(), FontWeight.BOLD, getFont().getSize()));
				} else if (index >= currentLogCursor) {
					setTextFill(Color.GRAY);
					setOpacity(0.72);
				} else {
					setFont(Font.getDefault());
				}
			}
		});

		// Action Menu (for selected unit)
		VBox actionMenu = new VBox(5);
		actionMenu.setPadding(new Insets(8));
		actionMenu.setBorder(new Border(new BorderStroke(Color.web("#ccc"), BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		Label actionLabel = new Label("Unit Actions:");
		actionLabel.setFont(Font.font(actionLabel.getFont().getFamily(), FontWeight.BOLD, 12));
		actionLabel.setTextFill(Color.BLACK);

		Button attackBtn = new Button("Attack");
		attackBtn.setPrefWidth(160);
		attackBtn.setDisable(true);

		Button captureBtn = new Button("Capture");
		captureBtn.setPrefWidth(160);
		captureBtn.setDisable(true);

		Button waitBtn = new Button("Wait");
		waitBtn.setPrefWidth(160);
		waitBtn.setOnAction(e -> controller.onWait());

		Label buyLabel = new Label("Buy Unit:");
		buyLabel.setFont(Font.font(buyLabel.getFont().getFamily(), FontWeight.BOLD, 12));
		buyLabel.setTextFill(Color.BLACK);

		Button buyInfantryBtn = new Button("Infantry");
		buyInfantryBtn.setPrefWidth(160);
		buyInfantryBtn.setDisable(true);

		Button buyTankBtn = new Button("Tank");
		buyTankBtn.setPrefWidth(160);
		buyTankBtn.setDisable(true);

		Button buyCannonBtn = new Button("Cannon");
		buyCannonBtn.setPrefWidth(160);
		buyCannonBtn.setDisable(true);



		attackBtn.setOnAction(e -> {
			controller.beginAttackMode();
		});
		captureBtn.setOnAction(e -> controller.onCapture());
		buyInfantryBtn.setOnAction(
				e -> controller.onBuyUnit(UnitType.INFANTRY, controller.findBuyPosition()));
		buyTankBtn
				.setOnAction(e -> controller.onBuyUnit(UnitType.TANK, controller.findBuyPosition()));
		buyCannonBtn
				.setOnAction(e -> controller.onBuyUnit(UnitType.CANNON, controller.findBuyPosition()));


		actionMenu.getChildren().addAll(
				actionLabel,
				attackBtn,
				captureBtn,
				waitBtn,
				buyLabel,
				buyInfantryBtn,
				buyTankBtn,
				buyCannonBtn);

		Button endTurnBtn = new Button("End Turn");
		endTurnBtn.setPrefWidth(200);
		endTurnBtn.setOnAction(e -> controller.onEndTurn());

		Button stepBackBtn = new Button("◀ Step Back");
		stepBackBtn.setPrefWidth(200);
		stepBackBtn.setOnAction(e -> controller.onStepBackward());

		Button stepFwdBtn = new Button("Step Forward ▶");
		stepFwdBtn.setPrefWidth(200);
		stepFwdBtn.setOnAction(e -> controller.onStepForward());

		Button exportBtn = new Button("Export Session...");
		exportBtn.setPrefWidth(200);
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

		Button menuBtn = new Button("Back to Menu");
		menuBtn.setPrefWidth(200);
		menuBtn.setOnAction(e -> app.showMapSelect());

		// Update action menu on state changes
		controller.setOnStateChanged(() -> {
			boolean unitSelected = controller.getSelectedUnit() != null;
			boolean factorySelected = controller.getSelectedFactoryTile() != null;
			attackBtn.setDisable(!unitSelected || !controller.canAttack());
			captureBtn.setDisable(!unitSelected || !controller.canCapture());
			waitBtn.setDisable(!unitSelected);
			buyInfantryBtn.setDisable(!factorySelected || !controller.canBuyUnit(UnitType.INFANTRY));
			buyTankBtn.setDisable(!factorySelected || !controller.canBuyUnit(UnitType.TANK));
			buyCannonBtn.setDisable(!factorySelected || !controller.canBuyUnit(UnitType.CANNON));

		});

		actionPanel.getChildren().addAll(
				playerLabel,
				moneyLabel,
				new Separator(),
				actionMenu,
				new Separator(),
				endTurnBtn,
				new Separator(),
				stepBackBtn,
				stepFwdBtn,
				new Separator(),
				exportBtn,
				new Separator(),
				menuBtn);
		VBox.setVgrow(actionMenu, Priority.ALWAYS);

		logPanel.getChildren().addAll(historyLabel, eventLogView);
		VBox.setVgrow(eventLogView, Priority.ALWAYS);

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
		String filename = AvailableMaps.getFilename(map);
		Path path = Path.of(filename);
		if (!Files.exists(path))
			throw new FileNotFoundException(filename);
		try (InputStream is = Files.newInputStream(path)) {
			return GameBoardLoader.loadFromStream(is, players);
		}
	}

	private javafx.scene.Group createSplitColorDot(javafx.scene.paint.Color colorLeft,
			javafx.scene.paint.Color colorRight, double radius) {
		javafx.scene.Group group = new javafx.scene.Group();

		// Left half (left color)
		javafx.scene.shape.Arc arcLeft =
				new javafx.scene.shape.Arc(0, 0, radius * 2, radius * 2, 90, 180);
		arcLeft.setFill(colorLeft);
		arcLeft.setStroke(javafx.scene.paint.Color.TRANSPARENT);

		// Right half (right color)
		javafx.scene.shape.Arc arcRight =
				new javafx.scene.shape.Arc(0, 0, radius * 2, radius * 2, -90, 180);
		arcRight.setFill(colorRight);
		arcRight.setStroke(javafx.scene.paint.Color.TRANSPARENT);

		group.getChildren().addAll(arcLeft, arcRight);
		return group;
	}
}
