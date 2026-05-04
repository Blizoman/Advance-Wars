package gui;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import board.AvailableMaps;
import gamer.Player;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class MapSelectView extends VBox {
	private static final String UI_FONT = "Noto Sans";
	private final VBox playersBox = new VBox(8);
	private List<PlayerInputRow> playerRows = new ArrayList<>();

	public MapSelectView(App app) {
		getStyleClass().add("root-view");
		setSpacing(12);
		setPadding(new Insets(20));
		setAlignment(Pos.CENTER);
		setFillWidth(true);

		Label title = new Label("Advance Wars");
		title.setFont(Font.font(UI_FONT, FontWeight.BOLD, 32));
		title.setTextFill(Color.BLACK);
		title.getStyleClass().add("title-label");

		// map list
		ListView<AvailableMaps.MapMetadata> mapList = new ListView<>();
		mapList.getItems().addAll(AvailableMaps.getAvailableMaps());
		mapList.setPrefHeight(220);
		mapList.setPrefWidth(420);
		mapList.getStyleClass().add("map-list");
		mapList.setCellFactory(lv -> new ListCell<AvailableMaps.MapMetadata>() {
			@Override
			protected void updateItem(AvailableMaps.MapMetadata item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null
						: item.title() + " (" + item.players() + " players)");
			}
		});
		mapList.getSelectionModel().selectFirst();

		Label playersLabel = new Label("Players:");
		playersLabel.setTextFill(Color.BLACK);
		playersLabel.getStyleClass().add("section-title");
		playersBox.getChildren().add(playersLabel);
		playersBox.setPadding(new Insets(10));
		playersBox.getStyleClass().add("panel-card");
		mapList.getSelectionModel().selectedItemProperty().addListener((obs, oldMap, selectedMap) -> {
			if (selectedMap != null)
				refreshPlayerRows(selectedMap.players());
		});
		refreshPlayerRows(mapList.getSelectionModel().getSelectedItem().players());

		Button startBtn = new Button("Start Game");
		startBtn.setFont(Font.font(UI_FONT, 16));
		startBtn.getStyleClass().add("btn-primary");
		startBtn.setOnAction(e -> {
			AvailableMaps.MapMetadata selected = mapList.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;
			app.showGame(selected, buildPlayers());
		});

		Button loadReplayBtn = new Button("Load Replay");
		loadReplayBtn.setFont(Font.font(UI_FONT, 16));
		loadReplayBtn.getStyleClass().add("btn-secondary");
		loadReplayBtn.setOnAction(e -> {
			AvailableMaps.MapMetadata selected = mapList.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;
			Path replayLog = app.chooseLoadReplayFile();
			if (replayLog == null)
				return;
			app.showGame(selected, buildPlayers(), replayLog);
		});

		Label selectMapLabel = new Label("Select Map:");
		selectMapLabel.setTextFill(Color.BLACK);
		selectMapLabel.getStyleClass().add("section-title");
		getChildren().addAll(
				title,
				selectMapLabel,
				mapList,
				playersBox,
				startBtn,
				loadReplayBtn);

		setMinWidth(520);
		playIntro(title, selectMapLabel, mapList, playersBox, startBtn, loadReplayBtn);
	}

	private void playIntro(Node... nodes) {
		for (int i = 0; i < nodes.length; i++) {
			Node node = nodes[i];
			node.setOpacity(0);
			FadeTransition ft = new FadeTransition(Duration.millis(200), node);
			ft.setFromValue(0);
			ft.setToValue(1);
			ft.setDelay(Duration.millis(70L * i));
			ft.setInterpolator(Interpolator.EASE_OUT);
			ft.play();
		}
	}

	private void refreshPlayerRows(int count) {
		List<String> previousNames = playerRows.stream()
				.map(row -> row.nameField().getText())
				.toList();
		List<Boolean> previousBots = playerRows.stream()
				.map(row -> row.botCheckBox().isSelected())
				.toList();

		playerRows = new ArrayList<>();
		Label refreshedPlayersLabel = new Label("Players:");
		refreshedPlayersLabel.setTextFill(Color.BLACK);
		refreshedPlayersLabel.getStyleClass().add("section-title");
		playersBox.getChildren().setAll(refreshedPlayersLabel);
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
		label.setTextFill(Color.BLACK);
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
