package vutfit.ija.classes;

public class Tile {

    //////////////////////////////
    /////////// VALUES ///////////

    /** Type of terrain */
    private final Terrain terrain;

    /** Unit at this tile, can be null */
    private Unit currentUnit;

    /////////// VALUES ///////////
    //////////////////////////////
    ////////// GETTERS ///////////

    /**
     * Obtains type of terrain at this tile
     * 
     * @return Type of terrain at this tile
     */
    public Terrain getTerrain() {
        return this.terrain;
    }

    /**
     * Obtains Unit at this tile
     * 
     * @return Unit at this tile
     */
    public Unit getUnit() {
        return this.currentUnit;
    }

    /**
     * Obtains info whether there is any Unit at this tile
     * 
     * @return True if Unit is at this tile, false otherwise
     */
    public boolean isEmpty() {
        return this.currentUnit == null;
    }

    ////////// GETTERS ///////////
    //////////////////////////////
    ////////// SETTERS ///////////

    /**
     * Constructor
     * 
     * @param terrain Type of terrain
     */
    public Tile(Terrain terrain) {
        this.terrain = terrain;
        this.currentUnit = null;
    }

    /**
     * Assign unit to this tile
     * 
     * @param unit Unit to assign
     */
    public void placeUnit(Unit unit) {
        this.currentUnit = unit;
    }

    /**
     * Remove unit from this tile
     * 
     * @return Removed unit
     */
    public Unit removeUnit() {
        Unit removed = this.currentUnit;
        this.currentUnit = null;
        return removed;
    }

    ////////// SETTERS ///////////
    //////////////////////////////

}