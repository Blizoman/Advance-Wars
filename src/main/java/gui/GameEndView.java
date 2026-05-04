package gui;

import java.nio.file.Path;
import java.util.function.Consumer;
import gamer.Player;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class GameEndView extends VBox {
	private static final String UI_FONT = "Noto Sans";
	public GameEndView(App app, Player winner, Consumer<Path> onExport) {
		getStyleClass().add("root-view");
		setSpacing(20);
		setPadding(new Insets(40));
		setAlignment(Pos.CENTER);

		Label winnerLabel = new Label(winner.getName() + " Wins!");
		winnerLabel.setFont(Font.font(UI_FONT, FontWeight.BOLD, 32));
		winnerLabel.setTextFill(Color.BLACK);
		winnerLabel.getStyleClass().add("title-label");

		ImageView trophyView = null;
		var trophyStream = getClass().getResourceAsStream("/gui/trophy.png");
		if (trophyStream != null) {
			Image trophy = new Image(trophyStream);
			trophyView = new ImageView(trophy);
			trophyView.setFitWidth(44);
			trophyView.setFitHeight(44);
			trophyView.setPreserveRatio(true);
		}
		HBox header = new HBox(12);
		header.setAlignment(Pos.CENTER);
		header.getStyleClass().add("winner-header");
		if (trophyView != null)
			header.getChildren().add(trophyView);
		header.getChildren().add(winnerLabel);

		Button exportBtn = new Button("Export Session...");
		exportBtn.setPrefWidth(180);
		exportBtn.getStyleClass().add("btn-secondary");
		exportBtn.setOnAction(e -> {
			if (onExport == null)
				return;
			Path exportPath = app.chooseSaveReplayFile();
			if (exportPath != null)
				onExport.accept(exportPath);
		});

		Button menuBtn = new Button("Back to Menu");
		menuBtn.setPrefWidth(180);
		menuBtn.getStyleClass().add("btn-primary");
		menuBtn.setOnAction(e -> app.showMapSelect());

		getChildren().addAll(header, new Separator(), exportBtn, menuBtn);
		playIntro(header, exportBtn, menuBtn);
	}

	private void playIntro(Node... nodes) {
		for (int i = 0; i < nodes.length; i++) {
			Node node = nodes[i];
			node.setOpacity(0);
			FadeTransition ft = new FadeTransition(Duration.millis(220), node);
			ft.setFromValue(0);
			ft.setToValue(1);
			ft.setDelay(Duration.millis(80L * i));
			ft.setInterpolator(Interpolator.EASE_OUT);
			ft.play();
		}
	}
}
