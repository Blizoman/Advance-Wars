package board;

import java.util.List;
import lombok.Getter;

public class AvailableMaps {
	public record MapMetadata(
			String title,
			String fileprefix,
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
		return "lib/maps/" + map.fileprefix() + ".json";
	}

	public static MapMetadata findByFileprefix(String fileprefix) {
		return availableMaps.stream()
				.filter(map -> map.fileprefix().equals(fileprefix))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown map: " + fileprefix));
	}
}
