package board;

import gamer.Player;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tools.Consts;
import unit.Unit;

@Getter
@Setter
@RequiredArgsConstructor
public class Tile {

    @NonNull
    private Terrain terrain;
    private Player owner = null;
    private Unit unit = null;
    private int captureHp = Consts.CAPTURE_HP;

    public boolean isEmpty() { return this.unit == null; }

    public void removeUnit() {
        this.unit = null;
    }

    public void unsetOwner() {
        this.owner = null;
    }

    public final void resetCapturableHp() {
        this.captureHp = Consts.CAPTURE_HP;
    }

    public void evalCapture(Unit unit) {
        int captureAmount = (int) Math.floor(unit.getHp() / 10.0);
        this.captureHp = Math.max(0, this.captureHp - captureAmount);
        if (this.captureHp == 0) {
            setOwner(unit.getPlayer());
            resetCapturableHp();
        }
    }
}
