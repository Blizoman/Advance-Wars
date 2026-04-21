package classes.board;

import classes.unit.Unit;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Tile {

    @Getter
    @NonNull
    private final TerrainType terrain;

    @Getter
    private Unit unit = null;

    public boolean isEmpty() { return this.unit == null; }

    public boolean placeUnit(Unit unit) {
        if (!isEmpty())
            return false;

        this.unit = unit;
        return true;
    }
}
