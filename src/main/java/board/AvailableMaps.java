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
			new MapMetadata("SIMPLE", "map0", 2),
			new MapMetadata("SIMPLE larger", "map1", 2),
			new MapMetadata("ASYMETRIC", "map2", 3));

	public static String getFilename(MapMetadata map) {
		return map.fileprefix + ".json";
	}

	public static MapMetadata findByFileprefix(String fileprefix) {
		return availableMaps.stream()
				.filter(map -> map.fileprefix().equals(fileprefix))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown map: " + fileprefix));
	}
}
