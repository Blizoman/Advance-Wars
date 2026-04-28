package gui;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import board.AvailableMaps;
import gamer.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class MapSelectView extends VBox {
	private final VBox playersBox = new VBox(8);
	private List<PlayerInputRow> playerRows = new ArrayList<>();

	public MapSelectView(App app) {
		setSpacing(12);
		setPadding(new Insets(20));
		setAlignment(Pos.CENTER);
		setFillWidth(true);

		Label title = new Label("Advance Wars");
		title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

		// map list
		ListView<AvailableMaps.MapMetadata> mapList = new ListView<>();
		mapList.getItems().addAll(AvailableMaps.getAvailableMaps());
		mapList.setPrefHeight(220);
		mapList.setPrefWidth(420);
		mapList.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(AvailableMaps.MapMetadata item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null
						: item.title() + " (" + item.players() + " players)");
			}
		});
		mapList.getSelectionModel().selectFirst();

		playersBox.getChildren().add(new Label("Players:"));
		mapList.getSelectionModel().selectedItemProperty().addListener((obs, oldMap, selectedMap) -> {
			if (selectedMap != null)
				refreshPlayerRows(selectedMap.players());
		});
		refreshPlayerRows(mapList.getSelectionModel().getSelectedItem().players());

		Button startBtn = new Button("Start Game");
		startBtn.setStyle("-fx-font-size: 16px;");
		startBtn.setOnAction(e -> {
			AvailableMaps.MapMetadata selected = mapList.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;
			app.showGame(selected, buildPlayers());
		});

		Button loadReplayBtn = new Button("Load Replay");
		loadReplayBtn.setStyle("-fx-font-size: 16px;");
		loadReplayBtn.setOnAction(e -> {
			AvailableMaps.MapMetadata selected = mapList.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;
			Path replayLog = app.chooseLoadReplayFile();
			if (replayLog == null)
				return;
			app.showGame(selected, buildPlayers(), replayLog);
		});

		getChildren().addAll(
				title,
				new Label("Select Map:"),
				mapList,
				playersBox,
				startBtn,
				loadReplayBtn);

		setMinWidth(520);
	}

	private void refreshPlayerRows(int count) {
		List<String> previousNames = playerRows.stream()
				.map(row -> row.nameField().getText())
				.toList();
		List<Boolean> previousBots = playerRows.stream()
				.map(row -> row.botCheckBox().isSelected())
				.toList();

		playerRows = new ArrayList<>();
		playersBox.getChildren().setAll(new Label("Players:"));
		for (int i = 0; i < count; i++) {
			String defaultName = i < previousNames.size() && previousNames.get(i) != null
					&& !previousNames.get(i).isBlank()
							? previousNames.get(i)
							: "Player " + (i + 1);
			boolean defaultBot =
					i < previousBots.size() && previousBots.get(i) != null && previousBots.get(i);
			PlayerInputRow row = createPlayerRow(i + 1, defaultName, defaultBot);
			playerRows.add(row);
			playersBox.getChildren().add(row.container());
		}
	}

	private PlayerInputRow createPlayerRow(int index, String defaultName, boolean defaultBot) {
		Label label = new Label("Player " + index + ":");
		label.setMinWidth(70);
		TextField nameField = new TextField(defaultName);
		nameField.setPrefWidth(160);
		CheckBox botCheckBox = new CheckBox("Bot");
		botCheckBox.setSelected(defaultBot);

		HBox row = new HBox(10, label, nameField, botCheckBox);
		row.setAlignment(Pos.CENTER_LEFT);
		return new PlayerInputRow(row, nameField, botCheckBox);
	}

	private List<Player> buildPlayers() {
		List<Player> players = new ArrayList<>();
		for (int i = 0; i < playerRows.size(); i++) {
			PlayerInputRow row = playerRows.get(i);
			String name = row.nameField().getText();
			if (name == null || name.isBlank())
				name = "Player " + (i + 1);
			players.add(new Player(name, row.botCheckBox().isSelected()));
		}
		return players;
	}

	private record PlayerInputRow(HBox container, TextField nameField, CheckBox botCheckBox) {}
}
