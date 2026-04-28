package gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import board.AvailableMaps;
import board.GameBoard;
import board.GameBoardLoader;
import board.Position;
import controllers.GameController;
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

		if (replayLog != null) {
			try {
				LogFiler.ReplayHeader replayHeader = LogFiler.loadHeader(replayLog);
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
			try {
				game.loadSession(replayLog);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load replay", e);
			}
		}

		GameController controller = new GameController(game.getSession(), game);

		Canvas canvas = new Canvas(880, 700);
		this.renderer = new Renderer(canvas, controller);
		renderer.resizeCanvasToBoard();

		canvas.setOnMouseClicked(e -> {
			if (e.getButton() != MouseButton.PRIMARY)
				return;
			Position pos = renderer.screenToGrid(e.getX(), e.getY());
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
		zoomOutBtn.setOnAction(e -> zoomSlider.setValue(Math.max(zoomSlider.getMin(), zoomSlider.getValue() - 0.05)));

		Button zoomInBtn = new Button("+");
		zoomInBtn.setOnAction(e -> zoomSlider.setValue(Math.min(zoomSlider.getMax(), zoomSlider.getValue() + 0.05)));

		Button resetZoomBtn = new Button("100%");
		resetZoomBtn.setOnAction(e -> zoomSlider.setValue(1.0));

		ToolBar mapToolbar = new ToolBar(zoomOutBtn, zoomSlider, zoomValueLabel, zoomInBtn, resetZoomBtn);
		mapToolbar.setMinHeight(36);
		mapToolbar.setPrefHeight(36);
		mapToolbar.setMaxWidth(Double.MAX_VALUE);

		VBox mapPanel = new VBox(8, mapToolbar, mapScrollPane);
		VBox.setVgrow(mapScrollPane, Priority.ALWAYS);

		canvas.setOnContextMenuRequested(null);

		Runnable refresh = () -> {
			Player active = controller.getActivePlayer();
			playerLabel.setText("Turn: " + active.getName());
			moneyLabel.setText("Money: $" + active.getMoney());
			refreshEventLog(controller);
			renderer.render();
		};
		controller.setOnStateChanged(refresh);

		game.getSession().setOnGameEnd(winner -> app.showGameEnd(winner));

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

		playerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		moneyLabel.setStyle("-fx-font-size: 14px;");

		VBox actionPanel = new VBox(8);
		actionPanel.setPrefWidth(230);
		actionPanel.setMinWidth(210);

		VBox logPanel = new VBox(8);
		logPanel.setPrefWidth(250);
		logPanel.setMinWidth(230);

		Label historyLabel = new Label("Event History");
		historyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
		eventLogView.setPrefHeight(540);
		eventLogView.setFocusTraversable(false);
		eventLogView.setPlaceholder(new Label("No events yet"));
		eventLogView.setCellFactory(list -> new ListCell<>() {
			@Override
			protected void updateItem(GameEvent event, boolean empty) {
				super.updateItem(event, empty);
				if (empty || event == null) {
					setText(null);
					setStyle("");
					return;
				}

				int index = getIndex();
				String marker = index == currentLogCursor - 1 ? "▶ " : index >= currentLogCursor ? "↷ " : "  ";
				setText(marker + (index + 1) + ". " + formatEvent(event));

				if (index == currentLogCursor - 1) {
					setStyle("-fx-background-color: rgba(64, 128, 255, 0.18); -fx-font-weight: bold;");
				} else if (index >= currentLogCursor) {
					setStyle("-fx-text-fill: #808080; -fx-opacity: 0.72;");
				} else {
					setStyle("");
				}
			}
		});

		// Action Menu (for selected unit)
		VBox actionMenu = new VBox(5);
		actionMenu.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");

		Label actionLabel = new Label("Unit Actions:");
		actionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

		Button attackBtn = new Button("Attack");
		attackBtn.setPrefWidth(160);
		attackBtn.setDisable(true);

		Button captureBtn = new Button("Capture");
		captureBtn.setPrefWidth(160);
		captureBtn.setDisable(true);

		Label buyLabel = new Label("Buy Unit:");
		buyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

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
		buyInfantryBtn.setOnAction(e -> controller.onBuyUnit(UnitType.INFANTRY, controller.findBuyPosition()));
		buyTankBtn.setOnAction(e -> controller.onBuyUnit(UnitType.TANK, controller.findBuyPosition()));
		buyCannonBtn.setOnAction(e -> controller.onBuyUnit(UnitType.CANNON, controller.findBuyPosition()));
        

		actionMenu.getChildren().addAll(
				actionLabel,
				attackBtn,
				captureBtn,
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
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
			if (is == null)
				throw new FileNotFoundException(filename);
			return GameBoardLoader.loadFromStream(is, players);
		}
	}
}
