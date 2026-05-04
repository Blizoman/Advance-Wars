package gui;

import board.GameBoard;
import board.Position;
import board.Terrain;
import board.Tile;
import controllers.GameController;
import gamer.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import tools.Consts;
import unit.Unit;

public class Renderer {
	private static final double BASE_TILE_SIZE = 80.0;

	private final Canvas canvas;
	private final GameController controller;
	private double zoom = 1.0;

	public Renderer(Canvas canvas, GameController controller) {
		this.canvas = canvas;
		this.controller = controller;
	}

	public void setZoom(double zoom) {
		this.zoom = Math.max(0.3, Math.min(1.0, zoom));
		applyZoom();
	}

	public double getZoom() { return zoom; }

	private double tileSize() {
		return BASE_TILE_SIZE * zoom;
	}

	public void resizeCanvasToBoard() {
		GameBoard board = controller.getGame().getGameBoard();
		canvas.setWidth(board.getWidth() * BASE_TILE_SIZE);
		canvas.setHeight(board.getHeight() * BASE_TILE_SIZE);
		applyZoom();
	}

	private void applyZoom() {
		canvas.setScaleX(zoom);
		canvas.setScaleY(zoom);
	}

	public void render() {
		resizeCanvasToBoard();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		GameBoard board = controller.getGame().getGameBoard();
		double tileSize = tileSize();

		for (int y = 0; y < board.getHeight(); y++) {
			for (int x = 0; x < board.getWidth(); x++) {
				Position pos = new Position(x, y);
				Tile tile = board.getTile(pos);
				drawTile(gc, tile, x, y, tileSize);
			}
		}

		gc.setFill(Color.color(0, 1, 0, 0.35));
		var moveCosts = controller.getAbailableMoveCosts();
		if (moveCosts != null)
			moveCosts.keySet().forEach(
					pos -> gc.fillRect(pos.x() * tileSize, pos.y() * tileSize, tileSize, tileSize));

		Unit selected = controller.getSelectedUnit();
		if (selected != null) {
			gc.setFill(Color.color(1, 1, 0, 0.5));
			gc.fillRect(
					selected.getPosition().x() * tileSize,
					selected.getPosition().y() * tileSize,
					tileSize, tileSize);
		}

		if (controller.isAttacking()) {
			gc.setFill(Color.color(1, 0, 0, 0.45));
			controller.getAttackTargets().forEach(target -> gc.fillRect(
					target.getPosition().x() * tileSize,
					target.getPosition().y() * tileSize,
					tileSize, tileSize));
		}

		Position selectedFactoryTile = controller.getSelectedFactory();
		if (selectedFactoryTile != null) {
			gc.setFill(Color.color(0.3, 0.6, 1.0, 0.35));
			gc.fillRect(
					selectedFactoryTile.x() * tileSize,
					selectedFactoryTile.y() * tileSize,
					tileSize, tileSize);
		}

		board.getAllUnits().forEach(u -> drawUnit(gc, u));
	}

	private void drawTile(GraphicsContext gc, Tile tile, int x, int y, double tileSize) {
		double px = x * tileSize;
		double py = y * tileSize;

		Image img = AssetLoader.terrain(tile.getTerrain());
		if (img != null) {
			gc.drawImage(img, px, py, tileSize, tileSize);
		} else {
			gc.setFill(fallbackColor(tile.getTerrain()));
			gc.fillRect(px, py, tileSize, tileSize);
		}

		if (tile.getTerrain().isCapturable()) {
			Color foreground =
					tile.getOwner() == null ? Color.LIGHTGRAY : playerColor(tile.getOwner());
			drawBar(gc, px, py + tileSize - 6, tileSize, 6,
					tile.getCaptureHp() / (double) Consts.CAPTURE_HP,
					foreground,
					Color.color(0, 0, 0, 0.35));
		}

		// colored border for buildings owned by a player
		if (tile.getOwner() != null && (tile.getTerrain() == Terrain.CITY
				|| tile.getTerrain() == Terrain.FACTORY || tile.getTerrain() == Terrain.HQ)) {
			Color c = playerColor(tile.getOwner());
			gc.setStroke(c);
			gc.setLineWidth(Math.max(2, tileSize * 0.04));
			gc.strokeRect(px + 2, py + 2, tileSize - 4, tileSize - 4);
		}

		gc.setStroke(Color.color(0, 0, 0, 0.15));
		gc.strokeRect(px, py, tileSize, tileSize);
	}

	private void drawUnit(GraphicsContext gc, Unit unit) {
		double tileSize = tileSize();
		double px = unit.getPosition().x() * tileSize;
		double py = unit.getPosition().y() * tileSize;
		int barHeight = 6;
		int bottomBarsHeight = barHeight * 2;
		double unitBodyHeight = tileSize - bottomBarsHeight;

		if (unit.isUsed()) {
			javafx.scene.effect.ColorAdjust grayscale = new javafx.scene.effect.ColorAdjust();
			grayscale.setSaturation(-1.0);
			grayscale.setBrightness(-0.2); 
			gc.setEffect(grayscale);
		}

		Image img = AssetLoader.unit(unit.getType());
		if (img != null) {
			gc.drawImage(img, px, py, tileSize, unitBodyHeight);
		} else {
			gc.setFill(playerColor(unit.getPlayer()));
			gc.fillOval(px + 4, py + 4, tileSize - 8, unitBodyHeight - 8);
		}
		
		// Frame draw by player color
		gc.setStroke(playerColor(unit.getPlayer()));
		gc.setLineWidth(2);
		gc.strokeRect(px + 1, py + 1, tileSize - 2, unitBodyHeight - 2);
		
		// HP Bar
		drawBar(gc, px, py + unitBodyHeight, tileSize, barHeight,
				unit.getHp() / 100.0,
				playerColor(unit.getPlayer()),
				Color.color(0.45, 0.45, 0.45));
				
		// Show HP
		gc.setFill(Color.WHITE);
		gc.setFont(Font.font(Math.max(9, tileSize / 9)));
		gc.fillText(String.valueOf(unit.getHp()), px + 2, py + unitBodyHeight - 1);

		// Destroy effect contrary for others infantries,tanks,...
		gc.setEffect(null);
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
		return player.getColor();
	}

	private void drawBar(GraphicsContext gc, double x, double y, double width, double height,
			double ratio, Color fillColor, Color backgroundColor) {
		ratio = Math.max(0.0, Math.min(1.0, ratio));
		gc.setFill(backgroundColor);
		gc.fillRect(x, y, width, height);
		gc.setFill(fillColor);
		gc.fillRect(x, y, width * ratio, height);
	}

	public Position screenToGrid(double x, double y) {
		double size = tileSize();
		return new Position((int) (x / size), (int) (y / size));
	}
}
