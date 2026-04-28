package gui;

import java.nio.file.Path;
import java.util.function.Consumer;
import gamer.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class GameEndView extends VBox {
	public GameEndView(App app, Player winner, Consumer<Path> onExport) {
		setSpacing(20);
		setPadding(new Insets(40));
		setAlignment(Pos.CENTER);

		Label winnerLabel = new Label("🏆 " + winner.getName() + " Wins!");
		winnerLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

		Button exportBtn = new Button("Export Session...");
		exportBtn.setPrefWidth(180);
		exportBtn.setOnAction(e -> {
			if (onExport == null)
				return;
			Path exportPath = app.chooseSaveReplayFile();
			if (exportPath != null)
				onExport.accept(exportPath);
		});

		Button menuBtn = new Button("Back to Menu");
		menuBtn.setPrefWidth(180);
		menuBtn.setOnAction(e -> app.showMapSelect());

		getChildren().addAll(winnerLabel, new Separator(), exportBtn, menuBtn);
	}
}
