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
import game.Game;
import gamer.Player;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.MouseButton;
import unit.UnitType;
import tools.LogFiler;

public class GameView extends HBox {
	private final Renderer renderer;
	private final Label playerLabel = new Label();
	private final Label moneyLabel = new Label();

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

		canvas.setOnMouseClicked(e -> {
			if (e.getButton() != MouseButton.PRIMARY)
				return;
			Position pos = renderer.screenToGrid(e.getX(), e.getY());
			controller.onTileClicked(pos);
		});

		ContextMenu mapMenu = new ContextMenu();
		MenuItem exportSessionItem = new MenuItem("Export Session...");
		exportSessionItem.setOnAction(e -> {
			Path exportPath = app.chooseSaveReplayFile();
			if (exportPath == null)
				return;
			try {
				controller.onSave(exportPath, finalMap);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to export replay", ex);
			}
		});
		mapMenu.getItems().add(exportSessionItem);
		canvas.setOnContextMenuRequested(e -> mapMenu.show(canvas, e.getScreenX(), e.getScreenY()));

		Runnable refresh = () -> {
			Player active = controller.getActivePlayer();
			playerLabel.setText("Turn: " + active.getName());
			moneyLabel.setText("Money: $" + active.getMoney());
			renderer.render();
		};
		controller.setOnStateChanged(refresh);

		game.getSession().setOnGameEnd(winner -> app.showGameEnd(winner));

		getChildren().addAll(canvas, buildSidebar(app, controller));

		if (replayLog == null)
			controller.startGame();
		else
			refresh.run();
	}

	private VBox buildSidebar(App app, GameController controller) {
		VBox sidebar = new VBox(10);
		sidebar.setPadding(new Insets(10));
		sidebar.setPrefWidth(200);

		playerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		moneyLabel.setStyle("-fx-font-size: 14px;");

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
		endTurnBtn.setPrefWidth(180);
		endTurnBtn.setOnAction(e -> controller.onEndTurn());

		Button stepBackBtn = new Button("◀ Step Back");
		stepBackBtn.setPrefWidth(180);
		stepBackBtn.setOnAction(e -> controller.onStepBackward());

		Button stepFwdBtn = new Button("Step Forward ▶");
		stepFwdBtn.setPrefWidth(180);
		stepFwdBtn.setOnAction(e -> controller.onStepForward());

		Button menuBtn = new Button("Back to Menu");
		menuBtn.setPrefWidth(180);
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

		sidebar.getChildren().addAll(
				playerLabel, moneyLabel,
				new Separator(),
				actionMenu,
				new Separator(),
				endTurnBtn,
				new Separator(),
				stepBackBtn, stepFwdBtn,
				new Separator(),
				menuBtn);
		return sidebar;
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
