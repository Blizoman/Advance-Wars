import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import classes.board.AvailableMaps;
import classes.board.GameBoard;
import classes.board.GameBoardLoader;
import classes.game.Game;
import classes.player.Player;
import tools.P;


public class Main {
	public static void main(String[] args) throws IOException {
		Main app = new Main();

		List<Player> players = List.of(
				new Player("Alfa"),
				new Player("Beta"));

		Game game = new Game(app.loadMap(players), players);
		game.initSession();
		game.getSession().setOnGameEnd(winner -> P.println("Winner: " + winner.getName()));
		game.getSession().startTurn();
	}

	public void saveSession(Game game) throws IOException {
		game.saveSession(Path.of("gamelog.json"));
	}

	public void loadSession(Game game) throws IOException {
		game.loadSession(Path.of("gamelog.json"));
		game.getSession().stepForward();
	}

	private GameBoard loadMap(List<Player> players) throws IOException {
		AvailableMaps.MapMetadata map = AvailableMaps.getAvailableMaps().get(1);
		String mapName = AvailableMaps.getFilename(map);

		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(mapName)) {
			if (inputStream == null)
				throw new FileNotFoundException("Map file " + mapName + " not found");
			return GameBoardLoader.loadFromStream(inputStream, players);
		}
	}
}
