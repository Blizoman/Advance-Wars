package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import unit.UnitType;
import java.util.HashMap;
import java.util.Map;
import board.Terrain;

public class AssetLoader {
	private static final Map<String, Image> loaded = new HashMap<>();

	public static Image terrain(Terrain terrain) {
		return load(terrain.name().toLowerCase());
	}

	public static Image terrain(Terrain terrain, Color ownerColor) {
		if (!isColorableTerrain(terrain) || ownerColor == null)
			return loadTerrainDefault(terrain);
		String colorKey = resolveColorKey(ownerColor);
		if (colorKey != null) {
			String base = terrain.name().toLowerCase();
			Path colored1 = Path.of("lib/assets", colorKey, base + "_" + colorKey + ".png");
			Image colored = loadPath(colored1);
			if (colored != null)
				return colored;
			Path colored2 = Path.of("lib/assets", colorKey, base + ".png");
			colored = loadPath(colored2);
			if (colored != null)
				return colored;
		}
		return loadTerrainDefault(terrain);
	}

	public static Image unit(UnitType type) {
		return load(type.name().toLowerCase());
	}

	public static Image unit(UnitType type, Color playerColor) {
		String colorKey = resolveColorKey(playerColor);
		if (colorKey != null) {
			String fileName = type.name().toLowerCase() + "_" + colorKey + ".png";
			Path path = Path.of("lib/assets", colorKey, fileName);
			Image colored = loadPath(path);
			if (colored != null)
				return colored;
		}
		return unit(type);
	}

	private static Image load(String name) {
		Path path = Path.of("lib/assets/" + name + ".png");
		return loadPath(path);
	}

	private static Image loadTerrainDefault(Terrain terrain) {
		String base = terrain.name().toLowerCase();
		Path grey1 = Path.of("lib/assets", base + "_grey.png");
		Image img = loadPath(grey1);
		if (img != null)
			return img;
		Path grey2 = Path.of("lib/assets", "grey", base + "_grey.png");
		img = loadPath(grey2);
		if (img != null)
			return img;
		Path grey3 = Path.of("lib/assets", "grey", base + ".png");
		img = loadPath(grey3);
		if (img != null)
			return img;
		return load(base);
	}

	private static Image loadPath(Path path) {
		String key = path.toString();
		return loaded.computeIfAbsent(key, ignored -> {
			if (!Files.exists(path))
				return null;
			try (var stream = Files.newInputStream(path)) {
				return new Image(stream);
			} catch (IOException e) {
				return null;
			}
		});
	}

	private static String resolveColorKey(Color color) {
		if (color == null)
			return null;
		if (isClose(color, Color.web("#e74c3c")))
			return "red";
		if (isClose(color, Color.web("#3498db")))
			return "blue";
		if (isClose(color, Color.web("#2ecc71")))
			return "green";
		if (isClose(color, Color.web("#f1c40f")))
			return "yellow";
		return null;
	}

	private static boolean isColorableTerrain(Terrain terrain) {
		return terrain == Terrain.CITY || terrain == Terrain.FACTORY || terrain == Terrain.HQ;
	}

	private static boolean isClose(Color a, Color b) {
		double eps = 0.01;
		return Math.abs(a.getRed() - b.getRed()) < eps
				&& Math.abs(a.getGreen() - b.getGreen()) < eps
				&& Math.abs(a.getBlue() - b.getBlue()) < eps;
	}
}
