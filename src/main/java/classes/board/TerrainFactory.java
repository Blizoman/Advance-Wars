package classes.board;

import java.util.HashMap;
import java.util.Map;

public class TerrainFactory {
    private static final Map<String, Terrain> knownTerrainTypes = new HashMap<>();

    public static Terrain getTerrain(String typeName) {
        return knownTerrainTypes.computeIfAbsent(typeName, Terrain::new);
    }
}
