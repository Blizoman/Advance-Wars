package gui;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import board.AvailableMaps;
import gamer.Player;
import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
	private Stage stage;

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		stage.setTitle("Advance Wars");
		stage.setResizable(true);
		stage.setMinWidth(700);
		stage.setMinHeight(600);
		showMapSelect();
		stage.show();
	}

	public void showMapSelect() {
		stage.setScene(new Scene(new MapSelectView(this), 760, 620));
	}

	public void showGame(AvailableMaps.MapMetadata map, List<Player> players) {
		stage.setScene(new Scene(new GameView(this, map, players), 1500, 860));
	}

	public void showGame(AvailableMaps.MapMetadata map, List<Player> players, Path replayLog) {
		stage.setScene(new Scene(new GameView(this, map, players, replayLog), 1500, 860));
	}

	public void showGameEnd(Player winner, Consumer<Path> onExport) {
		stage.setScene(new Scene(new GameEndView(this, winner, onExport), 400, 340));
	}

	public Path chooseLoadReplayFile() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Load Replay Log");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
		var file = chooser.showOpenDialog(stage);
		return file == null ? null : file.toPath();
	}

	public Path chooseSaveReplayFile() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export Replay Log");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
		chooser.setInitialFileName("gamelog.json");
		var file = chooser.showSaveDialog(stage);
		return file == null ? null : file.toPath();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
