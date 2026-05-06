/**
 * Represents the initial setup screen where users can select a map and configure players.
 * It provides UI controls to assign player names, choose between human or AI control 
 * (along with bot difficulty), and select unique colors for each player. It also handles 
 * validation for color uniqueness before allowing the user to start a new game or load a replay.
 *
 * @author xblizna00
 */

package gui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import board.AvailableMaps;
import gamer.BotType;
import gamer.Player;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class MapSelectView extends VBox {
	private static final String UI_FONT = "Manrope";
	private static final String TITLE_FONT = loadFontFamily(
			"/gui/fonts/Manrope-Black.ttf", 46,
			loadFontFamily("/gui/fonts/Manrope-ExtraBold.ttf", 46, "Arial Black"));
	private static final List<ColorOption> COLOR_OPTIONS = List.of(
			new ColorOption("Red", Color.web("#e74c3c")),
			new ColorOption("Blue", Color.web("#3498db")),
			new ColorOption("Green", Color.web("#2ecc71")),
			new ColorOption("Yellow", Color.web("#f1c40f")));
	private final VBox playersBox = new VBox(8);
	private List<PlayerInputRow> playerRows = new ArrayList<>();
	private Button startBtn;
	private Button loadReplayBtn;

	public MapSelectView(App app) {
		getStyleClass().add("root-view");
		setSpacing(12);
		setPadding(new Insets(20));
		setAlignment(Pos.CENTER);
		setFillWidth(true);

		Text title = new Text("Advance Wars");
		title.setFont(Font.font(TITLE_FONT, FontWeight.BLACK, 46));
		title.setStyle("-fx-font-weight: 900;");
		title.setFill(Color.web("#1f2a37"));
		title.setStroke(Color.web("#1f2a37"));
		title.setStrokeWidth(0.6);
		title.getStyleClass().add("title-text");
		ImageView leftIcon = loadHeaderIcon("/gui/tank_left.png", 36);
		ImageView rightIcon = loadHeaderIcon("/gui/tank_right.png", 36);
		HBox titleRow = new HBox(12);
		titleRow.setAlignment(Pos.CENTER);
		titleRow.getStyleClass().add("title-row");
		if (leftIcon != null)
			titleRow.getChildren().add(leftIcon);
		titleRow.getChildren().add(title);
		if (rightIcon != null)
			titleRow.getChildren().add(rightIcon);

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

		startBtn = new Button("Start Game");
		startBtn.setFont(Font.font(UI_FONT, 16));
		startBtn.getStyleClass().add("btn-primary");
		startBtn.getStyleClass().add("btn-fixed");
		startBtn.setMinWidth(220);
		startBtn.setPrefWidth(220);
		startBtn.setMaxWidth(220);
		startBtn.setMinHeight(38);
		startBtn.setPrefHeight(38);
		startBtn.setMaxHeight(38);
		startBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");
		startBtn.setOnAction(e -> {
			if (!isColorSelectionValid())
				return;
			AvailableMaps.MapMetadata selected = mapList.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;
			app.showGame(selected, buildPlayers());
		});

		loadReplayBtn = new Button("Load Replay");
		loadReplayBtn.setFont(Font.font(UI_FONT, 16));
		loadReplayBtn.getStyleClass().add("btn-secondary");
		loadReplayBtn.getStyleClass().add("btn-fixed");
		loadReplayBtn.setMinWidth(220);
		loadReplayBtn.setPrefWidth(220);
		loadReplayBtn.setMaxWidth(220);
		loadReplayBtn.setMinHeight(38);
		loadReplayBtn.setPrefHeight(38);
		loadReplayBtn.setMaxHeight(38);
		loadReplayBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;");
		loadReplayBtn.setOnAction(e -> {
			if (!isColorSelectionValid())
				return;
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
				titleRow,
				selectMapLabel,
				mapList,
				playersBox,
				startBtn,
				loadReplayBtn);

		setMinWidth(520);
		playIntro(titleRow, selectMapLabel, mapList, playersBox, startBtn, loadReplayBtn);
		updateActionButtons();
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
		List<BotType> previousBotTypes = playerRows.stream()
				.map(row -> row.botChoice().getValue())
				.toList();
		List<Color> previousColors = playerRows.stream()
				.map(row -> row.colorChoice().getValue() == null ? null : row.colorChoice().getValue().color())
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
			BotType defaultBotType = i < previousBotTypes.size() ? previousBotTypes.get(i) : null;
			Color defaultColor = i < previousColors.size() ? previousColors.get(i) : null;
			PlayerInputRow row =
					createPlayerRow(i + 1, defaultName, defaultBot, defaultColor, defaultBotType);
			playerRows.add(row);
			playersBox.getChildren().add(row.container());
		}
		updateActionButtons();
	}

	private PlayerInputRow createPlayerRow(int index, String defaultName, boolean defaultBot,
			Color defaultColor, BotType defaultBotType) {
		Label label = new Label("Player " + index + ":");
		label.setMinWidth(70);
		label.setTextFill(Color.BLACK);
		TextField nameField = new TextField(defaultName);
		nameField.setPrefWidth(160);
		CheckBox botCheckBox = new CheckBox("Bot");
		botCheckBox.setSelected(defaultBot);
		ComboBox<BotType> botChoice = buildBotChoice(defaultBotType, defaultBot);
		botCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
			botChoice.setDisable(!newValue);
			if (newValue && botChoice.getValue() == null)
				botChoice.setValue(BotType.WEAK);
		});
		ComboBox<ColorOption> colorChoice = buildColorChoice(defaultColor);
		colorChoice.valueProperty().addListener((obs, oldValue, newValue) -> updateActionButtons());
		HBox row = new HBox(10, label, nameField, botCheckBox, botChoice, colorChoice);
		row.setAlignment(Pos.CENTER_LEFT);
		return new PlayerInputRow(row, nameField, botCheckBox, botChoice, colorChoice);
	}

	private List<Player> buildPlayers() {
		List<Player> players = new ArrayList<>();
		for (int i = 0; i < playerRows.size(); i++) {
			PlayerInputRow row = playerRows.get(i);
			String name = row.nameField().getText();
			if (name == null || name.isBlank())
				name = "Player " + (i + 1);
			boolean isBot = row.botCheckBox().isSelected();
			Player player = new Player(name, isBot);
			if (isBot) {
				BotType botType = row.botChoice().getValue();
				player.setBotType(botType == null ? BotType.WEAK : botType);
			}
			ColorOption option = row.colorChoice().getValue();
			if (option != null)
				player.setColor(option.color());
			players.add(player);
		}
		return players;
	}

	private ComboBox<BotType> buildBotChoice(BotType defaultType, boolean isBot) {
		ComboBox<BotType> box = new ComboBox<>();
		box.getItems().setAll(BotType.WEAK, BotType.STRONG);
		box.getStyleClass().add("bot-choice");
		box.setPrefWidth(140);
		box.setMaxWidth(140);
		box.setMinHeight(28);
		box.setPrefHeight(28);
		box.setMaxHeight(28);
		box.setButtonCell(createBotCell());
		box.setCellFactory(list -> createBotCell());
		BotType value = defaultType != null && defaultType != BotType.NONE
				? defaultType
				: BotType.WEAK;
		box.setValue(value);
		box.setDisable(!isBot);
		return box;
	}

	private ListCell<BotType> createBotCell() {
		return new ListCell<>() {
			@Override
			protected void updateItem(BotType item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				setText(formatBotLabel(item));
				setGraphic(null);
			}
		};
	}

	private String formatBotLabel(BotType type) {
		return switch (type) {
			case STRONG -> "Easy Bot";
			case WEAK -> "Medium Bot";
			default -> "Human";
		};
	}

	private ComboBox<ColorOption> buildColorChoice(Color defaultColor) {
		ComboBox<ColorOption> box = new ComboBox<>();
		box.getItems().setAll(COLOR_OPTIONS);
		box.getStyleClass().add("color-choice");
		box.setPrefWidth(140);
		box.setMaxWidth(140);
		box.setMinHeight(28);
		box.setPrefHeight(28);
		box.setMaxHeight(28);
		box.setPromptText("Select color");
		box.setButtonCell(createColorCell(false));
		box.setCellFactory(list -> createColorCell(true));
		box.setValue(findMatchingColor(defaultColor));
		return box;
	}

	private ListCell<ColorOption> createColorCell(boolean useColorText) {
		return new ListCell<>() {
			@Override
			protected void updateItem(ColorOption item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				Rectangle swatch = new Rectangle(12, 12, item.color());
				swatch.setArcWidth(4);
				swatch.setArcHeight(4);
				Label label = new Label(item.label());
				if (useColorText)
					label.setTextFill(item.color());
				else
					label.setTextFill(Color.web("#1f2a37"));
				HBox box = new HBox(6, swatch, label);
				box.setAlignment(Pos.CENTER_LEFT);
				setText(null);
				setGraphic(box);
			}
		};
	}

	private ColorOption findMatchingColor(Color color) {
		if (color == null)
			return null;
		for (ColorOption option : COLOR_OPTIONS) {
			if (colorsEqual(option.color(), color))
				return option;
		}
		return null;
	}

	private boolean isColorSelectionValid() {
		Set<String> seen = new HashSet<>();
		for (PlayerInputRow row : playerRows) {
			ColorOption option = row.colorChoice().getValue();
			if (option == null)
				return false;
			String key = colorKey(option.color());
			if (!seen.add(key))
				return false;
		}
		return true;
	}

	private void updateActionButtons() {
		boolean enabled = isColorSelectionValid();
		updateDuplicateIndicators();
		if (startBtn != null) {
			startBtn.setDisable(!enabled);
			startBtn.setVisible(true);
			startBtn.setManaged(true);
			startBtn.setOpacity(1.0);
		}
		if (loadReplayBtn != null) {
			loadReplayBtn.setDisable(!enabled);
			loadReplayBtn.setVisible(true);
			loadReplayBtn.setManaged(true);
			loadReplayBtn.setOpacity(1.0);
		}
	}

	private void updateDuplicateIndicators() {
		Map<String, Integer> counts = new HashMap<>();
		for (PlayerInputRow row : playerRows) {
			ColorOption option = row.colorChoice().getValue();
			if (option == null)
				continue;
			String key = colorKey(option.color());
			counts.put(key, counts.getOrDefault(key, 0) + 1);
		}
		for (PlayerInputRow row : playerRows) {
			ComboBox<ColorOption> box = row.colorChoice();
			ColorOption option = box.getValue();
			boolean duplicate = option != null && counts.getOrDefault(colorKey(option.color()), 0) > 1;
			if (duplicate) {
				if (!box.getStyleClass().contains("color-duplicate"))
					box.getStyleClass().add("color-duplicate");
			} else {
				box.getStyleClass().remove("color-duplicate");
			}
		}
	}

	private String colorKey(Color color) {
		return String.format("%02x%02x%02x",
				(int) Math.round(color.getRed() * 255.0),
				(int) Math.round(color.getGreen() * 255.0),
				(int) Math.round(color.getBlue() * 255.0));
	}

	private boolean colorsEqual(Color a, Color b) {
		if (a == null || b == null)
			return false;
		return Math.abs(a.getRed() - b.getRed()) < 0.001
				&& Math.abs(a.getGreen() - b.getGreen()) < 0.001
				&& Math.abs(a.getBlue() - b.getBlue()) < 0.001;
	}

	private ImageView loadHeaderIcon(String resourcePath, double size) {
		try (var stream = getClass().getResourceAsStream(resourcePath)) {
			if (stream == null)
				return null;
			Image image = new Image(stream);
			ImageView view = new ImageView(image);
			view.setFitWidth(size);
			view.setFitHeight(size);
			view.setPreserveRatio(true);
			return view;
		} catch (Exception e) {
			return null;
		}
	}

	private static String loadFontFamily(String resourcePath, double size, String fallback) {
		try (var stream = MapSelectView.class.getResourceAsStream(resourcePath)) {
			if (stream == null)
				return fallback;
			Font font = Font.loadFont(stream, size);
			return font == null ? fallback : font.getFamily();
		} catch (Exception e) {
			return fallback;
		}
	}

	private record PlayerInputRow(HBox container, TextField nameField, CheckBox botCheckBox,
			ComboBox<BotType> botChoice, ComboBox<ColorOption> colorChoice) {}

	private record ColorOption(String label, Color color) {}
}
