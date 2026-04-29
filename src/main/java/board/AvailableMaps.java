package board;

import java.util.List;
import lombok.Getter;

public class AvailableMaps {
	public record MapMetadata(
			String title,
			String fileName,
			int players
	) {
	}

	@Getter
	private static final List<MapMetadata> availableMaps = List.of(
			new MapMetadata("SIMPLE", "simple", 2),
			new MapMetadata("ASYMETRIC", "asymetric", 3),
			new MapMetadata("BOT - ADVANTAGE", "botAdvantage", 2),
			new MapMetadata("2 BOTS - ADVANTAGE", "2botsAdvantage", 3),
			new MapMetadata("tactical", "tactical", 3)//
	);

	public static String getFilename(MapMetadata map) {
		return "lib/maps/" + map.fileName() + ".json";
	}

	public static MapMetadata getMapByName(String fileName) {
		return availableMaps.stream()
				.filter(map -> map.fileName().equals(fileName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown map: " + fileName));
	}
}
