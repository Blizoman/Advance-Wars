package gui;

import gamer.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameEndView extends VBox {
	public GameEndView(App app, Player winner) {
		setSpacing(20);
		setPadding(new Insets(40));
		setAlignment(Pos.CENTER);

		Label winnerLabel = new Label("🏆 " + winner.getName() + " Wins!");
		winnerLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

		Button menuBtn = new Button("Back to Menu");
		menuBtn.setOnAction(e -> app.showMapSelect());

		getChildren().addAll(winnerLabel, menuBtn);
	}
}
