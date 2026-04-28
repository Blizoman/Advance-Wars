package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
			Path path = Path.of("lib/assets/" + key + ".png");
			if (!Files.exists(path))
				return null;
			try (var stream = Files.newInputStream(path)) {
				return new Image(stream);
			} catch (IOException e) {
				return null;
			}
		});
	}
}
