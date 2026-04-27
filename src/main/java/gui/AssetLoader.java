package gui;

import classes.board.Terrain;
import classes.unit.UnitType;
import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

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
