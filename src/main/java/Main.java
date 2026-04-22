import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import classes.board.GameBoard;
import classes.board.GameBoardLoader;
import classes.player.Player;
import classes.unit.UnitFactory;


public class Main {
	UnitFactory unitFactory = new UnitFactory();

	public static void main(String[] args) throws IOException {
		Main app = new Main();
		app.start();

		// Game game = new Game(unitFactory);
		// game.start();
	}

	private void start() throws IOException {
		List<Player> players = List.of(
				new Player("Alfa"),
				new Player("Beta"));
		String mapName = "map1.json";
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(mapName)) {
			if (inputStream == null)
				throw new FileNotFoundException("Map file " + mapName + " not found");

			GameBoard gb = GameBoardLoader.loadFromStream(inputStream, players);
			gb.printMap();
		}
	}
}
