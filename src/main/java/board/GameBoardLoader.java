/**
 * Loads wanted map
 * 
 * @author: xpruzir00
 */

package board;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gamer.Player;
import tools.JsonSimples;

public class GameBoardLoader {
	private final static String WIDTH = "width";
	private final static String HEIGHT = "height";
	private final static String MAP = "map";
	private final static String TERRAIN = "terrain";
	private final static String OWNER = "owner";

	public static GameBoard loadMap(AvailableMaps.MapMetadata mapMetadata, List<Player> players)
			throws IOException {
		String filename = AvailableMaps.getFilename(mapMetadata);
		Path path = Path.of(filename);

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			return loadFromJson(json, players);
		}
	}

	private static GameBoard loadFromJson(JsonObject json, List<Player> players) {
		Map<Position, Tile> map = new HashMap<>();

		int width = JsonSimples.requireInt(json, WIDTH);
		int height = JsonSimples.requireInt(json, HEIGHT);
		JsonArray rows = JsonSimples.requireArray(json, MAP);

		for (int y = 0; y < height; y++) {
			JsonArray cols = rows.get(y).getAsJsonArray();
			for (int x = 0; x < width; x++) {
				JsonObject data = cols.get(x).getAsJsonObject();

				Tile tile = new Tile(
						Terrain.valueOf(JsonSimples.requireString(data, TERRAIN)));

				Player owner = null;
				Integer ownerIndex = JsonSimples.getInt(data, OWNER);
				if (ownerIndex != null) {
					if (ownerIndex >= players.size())
						throw new IllegalArgumentException(
								"Player index " + ownerIndex +
										" too high at [" + x + "," + y + "]" +
										"\nMybe map doesn't support only " + players.size()
										+ " players game");

					owner = players.get(ownerIndex);
					tile.setOwner(owner);
				}

				map.put(new Position(x, y), tile);
			}
		}
		return new GameBoard(map, width, height);
	}
}
