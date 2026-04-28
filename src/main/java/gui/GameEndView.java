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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameEndView extends VBox {
	public GameEndView(App app, Player winner, Consumer<Path> onExport) {
		setSpacing(20);
		setPadding(new Insets(40));
		setAlignment(Pos.CENTER);

		Label winnerLabel = new Label("🏆 " + winner.getName() + " Wins!");
		winnerLabel.setFont(Font.font(winnerLabel.getFont().getFamily(), FontWeight.BOLD, 32));
		winnerLabel.setTextFill(Color.BLACK);

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
