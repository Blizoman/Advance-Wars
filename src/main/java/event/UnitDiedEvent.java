/**
 * Tracks that Unit died
 * 
 * @author: xpruzir00
 */

package event;

import board.Tile;
import game.Game;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import unit.Unit;

@RequiredArgsConstructor
public class UnitDiedEvent implements GameEvent {
	@Getter
	private final Unit unit;
	private Integer captureHpBeforeReset = null;
	private Integer hpBeforeDeath = null;

	public GameEventType type() {
		return GameEventType.UNIT_DIED;
	}

	public void execute(Game game) {
		Tile tile = game.getGameBoard().getTile(unit.getPosition());
		hpBeforeDeath = unit.getHp();
		captureHpBeforeReset = null;
		if (tile.getTerrain().isCapturable()) {
			captureHpBeforeReset = tile.getCaptureHp();
			tile.resetCapturableHp();
		}
		unit.setHp(0);
		game.removeUnit(unit);
	}

	public void undo(Game game) {
		unit.setHp(hpBeforeDeath);
		game.placeUnit(unit);
		if (captureHpBeforeReset != null)
			game.getGameBoard().getTile(unit.getPosition()).setCaptureHp(captureHpBeforeReset);
	}
}
