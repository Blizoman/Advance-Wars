/**
 * Represents a game event where a unit is destroyed and removed from the board.
 * Beyond simply removing the unit, this event also handles the resetting of a tile's 
 * capture progress if the unit was in the middle of capturing a structure. It preserves 
 * the unit's health and the tile's capture status before the death occurred, enabling 
 * a complete restoration of both the unit and the capture state upon an undo operation.
 *
 * @author xpruzir00
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
