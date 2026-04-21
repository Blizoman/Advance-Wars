package classes.board;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import classes.player.Player;
import tools.Consts;
import tools.JsonSimples;

public class GameBoardLoader {
	public GameBoard loadFromFile(Path path, List<Player> players)
			throws JsonIOException, FileNotFoundException {
		JsonObject json = JsonParser.parseReader(new FileReader(path.toFile())).getAsJsonObject();
		Map<Position, Tile> map = new HashMap<>();

		int width = JsonSimples.getInt(json, Consts.GameBoard.WIDTH);
		int height = JsonSimples.getInt(json, Consts.GameBoard.HEIGHT);
		JsonArray rows = JsonSimples.getArray(json, Consts.GameBoard.MAP);

		for (int y = 0; y < height; y++) {
			JsonArray cols = rows.get(y).getAsJsonArray();
			for (int x = 0; x < width; x++) {
				JsonObject data = cols.get(x).getAsJsonObject();

				Player owner = null;
				String ownerString = JsonSimples.getString(data, Consts.GameBoard.OWNER);
				if (ownerString != null)
					owner = players
							.stream()
							.filter(p -> p.getName() == ownerString)
							.findFirst()
							.orElse(null);

				Tile tile = new Tile(Terrain.valueOf(JsonSimples.getString(data, Consts.GameBoard.TERRAIN)));
				tile.setOwner(owner);

				map.put(new Position(x, y), tile);
			}
		}
		return new GameBoard(map);
	}
}
