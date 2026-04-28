package gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import board.AvailableMaps;
import board.GameBoard;
import board.GameBoardLoader;
import board.Position;
import controllers.GameController;
import game.Game;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import player.Player;
import unit.UnitType;

public class GameView extends HBox {
	private final Renderer renderer;
	private final Label playerLabel = new Label();
	private final Label moneyLabel = new Label();

	public GameView(App app, AvailableMaps.MapMetadata map, List<Player> players) {
		Game game;
		try {
			game = new Game(loadMap(map, players), players);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load map", e);
		}
		game.initSession();

		GameController controller = new GameController(game.getSession(), game);

		Canvas canvas = new Canvas(880, 700);
		this.renderer = new Renderer(canvas, controller);

		canvas.setOnMouseClicked(e -> {
			Position pos = renderer.screenToGrid(e.getX(), e.getY());
			controller.onTileClicked(pos);
		});

		controller.setOnStateChanged(() -> {
			Player active = controller.getActivePlayer();
			playerLabel.setText("Turn: " + active.getName());
			moneyLabel.setText("Money: $" + active.getMoney());
			renderer.render();
		});

		game.getSession().setOnGameEnd(winner -> app.showGameEnd(winner));

		getChildren().addAll(canvas, buildSidebar(app, controller));

		controller.startGame();
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
