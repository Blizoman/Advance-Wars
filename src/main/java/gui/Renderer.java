package gui;

import board.GameBoard;
import board.Position;
import board.Terrain;
import board.Tile;
import controllers.GameController;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import player.Player;
import tools.Consts;
import unit.Unit;

public class Renderer {
	private static final int TILE_SIZE = 80;

	private final Canvas canvas;
	private final GameController controller;

	public Renderer(Canvas canvas, GameController controller) {
		this.canvas = canvas;
		this.controller = controller;
	}

	public void render() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		GameBoard board = controller.getGame().getGameBoard();

		for (int y = 0; y < board.getHeight(); y++) {
			for (int x = 0; x < board.getWidth(); x++) {
				Position pos = new Position(x, y);
				Tile tile = board.getTile(pos);
				drawTile(gc, tile, x, y);
			}
		}

		gc.setFill(Color.color(0, 1, 0, 0.35));
		controller.getMoveCosts().keySet().forEach(
				pos -> gc.fillRect(pos.x() * TILE_SIZE, pos.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE));

		Unit selected = controller.getSelectedUnit();
		if (selected != null) {
			gc.setFill(Color.color(1, 1, 0, 0.5));
			gc.fillRect(
					selected.getPosition().x() * TILE_SIZE,
					selected.getPosition().y() * TILE_SIZE,
					TILE_SIZE, TILE_SIZE);
		}

		if (controller.isAttackMode()) {
			gc.setFill(Color.color(1, 0, 0, 0.45));
			controller.getAttackTargets().forEach(target -> gc.fillRect(
					target.getPosition().x() * TILE_SIZE,
					target.getPosition().y() * TILE_SIZE,
					TILE_SIZE, TILE_SIZE));
		}

		Position selectedFactoryTile = controller.getSelectedFactoryTile();
		if (selectedFactoryTile != null) {
			gc.setFill(Color.color(0.3, 0.6, 1.0, 0.35));
			gc.fillRect(
					selectedFactoryTile.x() * TILE_SIZE,
					selectedFactoryTile.y() * TILE_SIZE,
					TILE_SIZE, TILE_SIZE);
		}

		board.getAllUnits().forEach(u -> drawUnit(gc, u));
	}

	private void drawTile(GraphicsContext gc, Tile tile, int x, int y) {
		int px = x * TILE_SIZE;
		int py = y * TILE_SIZE;

		Image img = AssetLoader.terrain(tile.getTerrain());
		if (img != null) {
			gc.drawImage(img, px, py, TILE_SIZE, TILE_SIZE);
		} else {
			gc.setFill(fallbackColor(tile.getTerrain()));
			gc.fillRect(px, py, TILE_SIZE, TILE_SIZE);
		}

		if (tile.getTerrain().isCapturable()) {
			Color foreground = tile.getOwner() == null ? Color.LIGHTGRAY : playerColor(tile.getOwner());
			drawBar(gc, px, py + TILE_SIZE - 6, TILE_SIZE, 6,
					tile.getCaptureHp() / (double) Consts.CAPTURE_HP,
					foreground,
					Color.color(0, 0, 0, 0.35));
		}

		gc.setStroke(Color.color(0, 0, 0, 0.15));
		gc.strokeRect(px, py, TILE_SIZE, TILE_SIZE);
	}

	private void drawUnit(GraphicsContext gc, Unit unit) {
		int px = unit.getPosition().x() * TILE_SIZE;
		int py = unit.getPosition().y() * TILE_SIZE;
		int barHeight = 6;
		int bottomBarsHeight = barHeight * 2;
		int unitBodyHeight = TILE_SIZE - bottomBarsHeight;

		Image img = AssetLoader.unit(unit.getType());
		if (img != null) {
			gc.drawImage(img, px, py, TILE_SIZE, unitBodyHeight);
		} else {
			gc.setFill(playerColor(unit.getPlayer()));
			gc.fillOval(px + 4, py + 4, TILE_SIZE - 8, unitBodyHeight - 8);
		}

		gc.setStroke(playerColor(unit.getPlayer()));
		gc.setLineWidth(2);
		gc.strokeRect(px + 1, py + 1, TILE_SIZE - 2, unitBodyHeight - 2);

		drawBar(gc, px, py + unitBodyHeight, TILE_SIZE, barHeight,
				unit.getHp() / 100.0,
				Color.LIME,
				Color.RED);

		gc.setFill(Color.WHITE);
		gc.setFont(Font.font(9));
		gc.fillText(String.valueOf(unit.getHp()), px + 2, py + unitBodyHeight - 1);
	}

	private Color fallbackColor(Terrain terrain) {
		return switch (terrain) {
			case PLAIN -> Color.LIGHTGREEN;
			case FOREST -> Color.DARKGREEN;
			case MOUNTAIN -> Color.GRAY;
			case WATER -> Color.CORNFLOWERBLUE;
			case CITY -> Color.LIGHTYELLOW;
			case FACTORY -> Color.ORANGE;
			case HQ -> Color.GOLD;
		};
	}

	private Color playerColor(Player player) {
		return switch (player.getName().hashCode() % 4) {
			case 0 -> Color.DODGERBLUE;
			case 1 -> Color.TOMATO;
			case 2 -> Color.LIMEGREEN;
			default -> Color.MEDIUMPURPLE;
		};
	}

	private void drawBar(GraphicsContext gc, int x, int y, int width, int height,
			double ratio, Color fillColor, Color backgroundColor) {
		ratio = Math.max(0.0, Math.min(1.0, ratio));
		gc.setFill(backgroundColor);
		gc.fillRect(x, y, width, height);
		gc.setFill(fillColor);
		gc.fillRect(x, y, width * ratio, height);
	}

	public Position screenToGrid(double x, double y) {
		return new Position((int) (x / TILE_SIZE), (int) (y / TILE_SIZE));
	}
}
