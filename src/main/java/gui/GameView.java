package gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

public class GameView extends HBox {
	private final GameController controller;
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
		this.controller = controller;

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

		sidebar.getChildren().addAll(
				playerLabel, moneyLabel,
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
