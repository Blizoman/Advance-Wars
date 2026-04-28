package gui;

import java.util.ArrayList;
import java.util.List;
import board.AvailableMaps;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import player.Player;

public class MapSelectView extends VBox {
	public MapSelectView(App app) {
		setSpacing(12);
		setPadding(new Insets(30));
		setAlignment(Pos.CENTER);

		Label title = new Label("Advance Wars");
		title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

		// player name fields
		List<TextField> nameFields = List.of(
				new TextField("Player 1"),
				new TextField("Player 2"),
				new TextField("Player 3"),
				new TextField("Player 4"));


		List<Player> players = new ArrayList<>();
		players.add(new Player(nameFields.get(0).getText(), false));
		players.add(new Player(nameFields.get(1).getText(), false));

		VBox namesBox = new VBox(5);
		namesBox.getChildren().add(new Label("Player Names:"));
		namesBox.getChildren().addAll(nameFields);

		// map list
		ListView<AvailableMaps.MapMetadata> mapList = new ListView<>();
		mapList.getItems().addAll(AvailableMaps.getAvailableMaps());
		mapList.setPrefHeight(120);
		mapList.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(AvailableMaps.MapMetadata item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null
						: item.title() + " (" + item.players() + " players)");
			}
		});
		mapList.getSelectionModel().selectFirst();

		Button startBtn = new Button("Start Game");
		startBtn.setStyle("-fx-font-size: 16px;");
		startBtn.setOnAction(e -> {
			AvailableMaps.MapMetadata selected = mapList.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;
			app.showGame(selected, players);
		});

		getChildren().addAll(
				title,
				new Label("Select Map:"),
				mapList,
				namesBox,
				startBtn);
	}
}
