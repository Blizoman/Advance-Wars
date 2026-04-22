import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import classes.board.AvailableMaps;
import classes.board.GameBoard;
import classes.board.GameBoardLoader;
import classes.player.Player;
import classes.unit.UnitFactory;
import tools.P;


public class Main {
	UnitFactory unitFactory = new UnitFactory();

	public static void main(String[] args) throws IOException {
		Main app = new Main();
		app.start();

		// Game game = new Game(unitFactory);
		// game.start();
	}

	private void start() throws IOException {
		for (AvailableMaps.MapMetadata map : AvailableMaps.getAvailableMaps()) {
			String mapName = AvailableMaps.getFilename(map);

			List<Player> players = List.of(
					new Player("Alfa"),
					new Player("Beta"));
			try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(mapName)) {
				if (inputStream == null)
					throw new FileNotFoundException("Map file " + mapName + " not found");

				GameBoard gb = GameBoardLoader.loadFromStream(inputStream, players);
				P.println(map.title());
				gb.printMap();

				P.eprintln();
				P.eprintln();
			}
		}
	}
}
