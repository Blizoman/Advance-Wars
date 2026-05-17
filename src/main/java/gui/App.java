/**
 * The main entry point for the JavaFX application. It manages the primary application window
 * (Stage) and handles transitions between different scenes (Map Selection, Game View, and Game End
 * View). Additionally, it handles loading global UI assets (fonts, themes) and provides utility
 * dialogs for loading and saving replay files.
 *
 * @author xblizna00
 */

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
import javafx.scene.text.Font;

public class App extends Application {
	private Stage stage;
	private boolean fontsLoaded = false;

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
		Scene scene = new Scene(new MapSelectView(this), 760, 620);
		applyTheme(scene);
		stage.setScene(scene);
	}

	public void showGame(AvailableMaps.MapMetadata map, List<Player> players) {
		Scene scene = new Scene(new GameView(this, map, players), 1500, 860);
		applyTheme(scene);
		stage.setScene(scene);
	}

	public void showGame(AvailableMaps.MapMetadata map, List<Player> players, Path replayLog) {
		Scene scene = new Scene(new GameView(this, map, players, replayLog), 1500, 860);
		applyTheme(scene);
		stage.setScene(scene);
	}

	public void showGameEnd(Player winner, Consumer<Path> onExport) {
		Scene scene = new Scene(new GameEndView(this, winner, onExport), 400, 340);
		applyTheme(scene);
		stage.setScene(scene);
	}

	private void applyTheme(Scene scene) {
		ensureFontsLoaded();
		scene.getStylesheets().add(getClass().getResource("/gui/styles.css").toExternalForm());
	}

	private void ensureFontsLoaded() {
		if (fontsLoaded)
			return;
		fontsLoaded = true;
		loadFontIfPresent("/gui/fonts/Manrope-Regular.ttf");
		loadFontIfPresent("/gui/fonts/Manrope-SemiBold.ttf");
		loadFontIfPresent("/gui/fonts/Manrope-Bold.ttf");
		loadFontIfPresent("/gui/fonts/Manrope-ExtraBold.ttf");
		loadFontIfPresent("/gui/fonts/Manrope-Black.ttf");
	}

	private void loadFontIfPresent(String resourcePath) {
		try (var stream = getClass().getResourceAsStream(resourcePath)) {
			if (stream != null)
				Font.loadFont(stream, 12);
		} 
		catch (Exception ignored) {
			// Optional font not present.
		}
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
