package classes.board;

import classes.player.Player;
import classes.unit.Unit;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tools.Consts;

@RequiredArgsConstructor
public class Tile {

    @Getter
    @NonNull
    private Terrain terrain;
    @Getter
    private Player owner = null;
    @Getter
    private Unit unit = null;
    @Getter
    private int captureHp = Consts.CAPTURE_HP;

    public boolean isEmpty() { return this.unit == null; }

    public void placeUnit(Unit unit) {
        this.unit = unit;
    }

    public void removeUnit() {
        this.unit = null;
    }

    public void setOwner(Player player) {
        if (!terrain.isCapturable())
            throw new IllegalStateException("Cannot own non-capturable terrain");
        this.owner = player;
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

    public void convertHqToCity() {
        if (this.terrain == Terrain.HQ)
            this.terrain = Terrain.CITY;
    }
}
