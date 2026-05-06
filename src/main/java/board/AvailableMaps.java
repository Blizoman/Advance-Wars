/**
 * Provides a centralized registry of all playable maps available in the game. It stores map
 * metadata (such as display title, internal file name, and required player count) and offers
 * utility methods to retrieve map file paths and metadata by name.
 *
 * @author xpruzir00
 */

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
			new MapMetadata("TESTING", "testing", 2),
			new MapMetadata("SYMETRIC 2", "symetric2", 2),
			new MapMetadata("ASYMETRIC 2", "asymetric2", 2),
			new MapMetadata("ASYMETRIC", "asymetric", 3),
			new MapMetadata("BOT - ADVANTAGE", "botAdvantage", 2),
			new MapMetadata("2 BOTS - ADVANTAGE", "2botsAdvantage", 3),
			new MapMetadata("tactical", "tactical", 3),
			new MapMetadata("Crossfire", "4p_crossfire", 4),
			new MapMetadata("Twin Rivers", "2p_twin_rivers", 2)//
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
