package gui;

import javafx.scene.image.Image;
import unit.UnitType;
import java.util.HashMap;
import java.util.Map;
import board.Terrain;

public class AssetLoader {
	private static final Map<String, Image> loaded = new HashMap<>();

	public static Image terrain(Terrain terrain) {
		return load(terrain.name().toLowerCase());
	}

	public static Image unit(UnitType type) {
		return load(type.name().toLowerCase());
	}

	private static Image load(String name) {
		return loaded.computeIfAbsent(name, key -> {
			var stream = AssetLoader.class.getResourceAsStream("/assets/" + key + ".png");
			return (stream == null) ? null : new Image(stream);
		});
	}
}
