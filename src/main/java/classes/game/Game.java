package classes.game;

import java.util.List;
import classes.board.GameBoard;
import classes.player.Player;
import classes.unit.UnitFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Game {
	@Getter
	private final GameBoard gameBoard;

	@Getter
	private final List<Player> players;

	@Getter
	private int currentPlayerIndex = 0;

	private final UnitFactory unitFactory = new UnitFactory();

	public void startTurn(){
		//TODO: income, heal, index,...
	}
}
