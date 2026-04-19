package vutfit.ija.classes;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Tile {

    @Getter
    @NonNull
    private final Terrain terrain;

    @Getter
    private Unit unit = null;

    public boolean isEmpty() { return this.unit == null; }

    public void placeUnit(Unit unit) {
        if (!isEmpty())
            throw new IllegalStateException("Tile already occupied");
        this.unit = unit;
    }

    public Unit removeUnit() {
        Unit removed = this.unit;
        this.unit = null;
        return removed;
    }
}
