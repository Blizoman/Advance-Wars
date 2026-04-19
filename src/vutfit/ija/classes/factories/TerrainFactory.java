package vutfit.ija.classes.factories;

import java.util.HashMap;
import java.util.Map;
import vutfit.ija.classes.board.Terrain;

public class TerrainFactory {
    private static final Map<String, Terrain> knownTerrainTypes = new HashMap<>();

    public static Terrain getTerrain(String typeName) {
        return knownTerrainTypes.computeIfAbsent(typeName, Terrain::new);
    }
}
