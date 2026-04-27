package board;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import player.Player;
import tools.Consts;
import tools.JsonSimples;

public class GameBoardLoader {
	public static GameBoard loadFromStream(InputStream inputStream, List<Player> players)
			throws JsonIOException, FileNotFoundException {
		InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
		return loadFromJson(json, players);
	}

	public static GameBoard loadFromFile(Path path, List<Player> players)
			throws JsonIOException, FileNotFoundException {
		JsonObject json = JsonParser.parseReader(new FileReader(path.toFile())).getAsJsonObject();
		return loadFromJson(json, players);
	}

	private static GameBoard loadFromJson(JsonObject json, List<Player> players) {
		Map<Position, Tile> map = new HashMap<>();

		int width = JsonSimples.requireInt(json, Consts.GameBoard.WIDTH);
		int height = JsonSimples.requireInt(json, Consts.GameBoard.HEIGHT);
		JsonArray rows = JsonSimples.requireArray(json, Consts.GameBoard.MAP);

		for (int y = 0; y < height; y++) {
			JsonArray cols = rows.get(y).getAsJsonArray();
			for (int x = 0; x < width; x++) {
				JsonObject data = cols.get(x).getAsJsonObject();

				Tile tile = new Tile(
						Terrain.valueOf(JsonSimples.requireString(data, Consts.GameBoard.TERRAIN)));

				Player owner = null;
				Integer ownerIndex = JsonSimples.getInt(data, Consts.GameBoard.OWNER);
				if (ownerIndex != null) {
					if (ownerIndex >= players.size())
						throw new IllegalArgumentException(
								"Player index " + ownerIndex +
										" too high at [" + x + "," + y + "]" +
										"\nMybe map doesn't support only " + players.size()
										+ " players game");
										//TODO: fix low index too

					owner = players.get(ownerIndex);
					tile.setOwner(owner);
				}

				map.put(new Position(x, y), tile);
			}
		}
		return new GameBoard(map, width, height);
	}
}
