package classes.board;

import java.util.Map;
import classes.board.terrain.City;
import classes.board.terrain.Factory;
import classes.board.terrain.Forest;
import classes.board.terrain.HQ;
import classes.board.terrain.Mountain;
import classes.board.terrain.Plain;
import classes.board.terrain.TerrainType;
import classes.board.terrain.TerrainTypeName;
import classes.board.terrain.Water;

public class TerrainFactory {
    private final Map<TerrainTypeName, TerrainType> terrainTypes = Map.of(
            TerrainTypeName.PLAIN, new Plain(),
            TerrainTypeName.FOREST, new Forest(),
            TerrainTypeName.MOUNTAIN, new Mountain(),
            TerrainTypeName.WATER, new Water(),
            TerrainTypeName.CITY, new City(),
            TerrainTypeName.FACTORY, new Factory(),
            TerrainTypeName.HQ, new HQ());

    public TerrainType getTerrain(TerrainTypeName typeName) {
        return terrainTypes.get(typeName);
    }
}
