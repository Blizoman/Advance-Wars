package gui;

import java.util.List;
import board.AvailableMaps;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import player.Player;

public class App extends Application {
	private Stage stage;

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		stage.setTitle("Advance Wars");
		stage.setResizable(false);
		showMapSelect();
		stage.show();
	}

	public void showMapSelect() {
		stage.setScene(new Scene(new MapSelectView(this), 500, 400));
	}

	public void showGame(AvailableMaps.MapMetadata map, List<Player> players) {
		stage.setScene(new Scene(new GameView(this, map, players), 1100, 700));
	}

	public void showGameEnd(Player winner) {
		stage.setScene(new Scene(new GameEndView(this, winner), 400, 300));
	}

	public static void main(String[] args) {
		launch(args);
	}
}
