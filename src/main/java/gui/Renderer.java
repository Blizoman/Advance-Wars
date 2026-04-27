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

		// draw terrain
		for (int y = 0; y < board.getHeight(); y++) {
			for (int x = 0; x < board.getWidth(); x++) {
				Position pos = new Position(x, y);
				Tile tile = board.getTile(pos);
				drawTile(gc, tile, x, y);
			}
		}

		// draw valid move highlights
		gc.setFill(Color.color(0, 1, 0, 0.35));
		controller.getValidMoves().forEach(
				pos -> gc.fillRect(pos.x() * TILE_SIZE, pos.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE));

		// draw selected unit highlight
		Unit selected = controller.getSelectedUnit();
		if (selected != null) {
			gc.setFill(Color.color(1, 1, 0, 0.5));
			gc.fillRect(
					selected.getPosition().x() * TILE_SIZE,
					selected.getPosition().y() * TILE_SIZE,
					TILE_SIZE, TILE_SIZE);
		}

		// draw units
		board.getAllUnits().forEach(u -> drawUnit(gc, u));
	}

	private void drawTile(GraphicsContext gc, Tile tile, int x, int y) {
		int px = x * TILE_SIZE;
		int py = y * TILE_SIZE;

		Image img = AssetLoader.terrain(tile.getTerrain());
		if (img != null) {
			gc.drawImage(img, px, py, TILE_SIZE, TILE_SIZE);
		} else {
			// fallback color if image missing
			gc.setFill(fallbackColor(tile.getTerrain()));
			gc.fillRect(px, py, TILE_SIZE, TILE_SIZE);
		}

		// draw owner indicator on capturable tiles
		if (tile.getTerrain().isCapturable() && tile.getOwner() != null) {
			gc.setFill(playerColor(tile.getOwner()));
			gc.fillRect(px, py, 8, 8);
		}

		// grid line
		gc.setStroke(Color.color(0, 0, 0, 0.15));
		gc.strokeRect(px, py, TILE_SIZE, TILE_SIZE);
	}

	private void drawUnit(GraphicsContext gc, Unit unit) {
		int px = unit.getPosition().x() * TILE_SIZE;
		int py = unit.getPosition().y() * TILE_SIZE;

		Image img = AssetLoader.unit(unit.getType());
		if (img != null) {
			gc.drawImage(img, px, py, TILE_SIZE, TILE_SIZE);
		} else {
			// fallback circle if image missing
			gc.setFill(playerColor(unit.getPlayer()));
			gc.fillOval(px + 4, py + 4, TILE_SIZE - 8, TILE_SIZE - 8);
		}

		// player color border
		gc.setStroke(playerColor(unit.getPlayer()));
		gc.setLineWidth(2);
		gc.strokeRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);

		// HP bar
		double hpRatio = unit.getHp() / 100.0;
		gc.setFill(Color.RED);
		gc.fillRect(px, py + TILE_SIZE - 5, TILE_SIZE, 5);
		gc.setFill(Color.LIME);
		gc.fillRect(px, py + TILE_SIZE - 5, TILE_SIZE * hpRatio, 5);

		// HP number
		gc.setFill(Color.WHITE);
		gc.setFont(Font.font(9));
		gc.fillText(String.valueOf(unit.getHp()), px + 2, py + TILE_SIZE - 7);
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
		// cycle through colors by player index
		return switch (player.getName().hashCode() % 4) {
			case 0 -> Color.DODGERBLUE;
			case 1 -> Color.TOMATO;
			case 2 -> Color.LIMEGREEN;
			default -> Color.MEDIUMPURPLE;
		};
	}

	public Position screenToGrid(double x, double y) {
		return new Position((int) (x / TILE_SIZE), (int) (y / TILE_SIZE));
	}
}
