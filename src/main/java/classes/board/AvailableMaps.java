package classes.board;

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
			new MapMetadata("SIMPLE", "map0", 2),
			new MapMetadata("SIMPLE larger", "map1", 2));

	public static String getFilename(MapMetadata map) {
		return map.fileprefix + ".json";
	}
}
