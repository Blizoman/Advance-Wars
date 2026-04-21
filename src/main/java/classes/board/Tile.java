package classes.board;

import classes.player.Player;
import classes.unit.Unit;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Tile {

    @Getter
    @NonNull
    private final Terrain terrain;
    @Getter
    private Player owner = null;
    @Getter
    private Unit unit = null;

    public boolean isEmpty() { return this.unit == null; }

    public void placeUnit(Unit unit) throws IllegalAccessException {
        if (!isEmpty())
            throw new IllegalAccessException("Tile already occupied");
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
}
