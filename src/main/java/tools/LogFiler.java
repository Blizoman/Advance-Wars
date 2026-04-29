package tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import board.AvailableMaps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import event.*;
import gamer.Player;
import lombok.RequiredArgsConstructor;
import java.lang.reflect.Type;

public class LogFiler {
	private LogFiler() { /* Prevent instantiation */ }

	private static final String MAP_KEY = "map";
	private static final String PLAYERS_KEY = "players";
	private static final String EVENTS_KEY = "events";
	private static final String TYPE_KEY = "type";

	private static Gson buildGson(List<Player> players) {
		return new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Player.class, new PlayerAdapter(players))
				.registerTypeAdapter(GameEvent.class, new GameEventAdapter())
				.create();
	}

	////////////////////////////////////////////
	/////////////////// SAVE ///////////////////

	public static void save(List<GameEvent> eventLog, Path path, AvailableMaps.MapMetadata map,
			List<Player> players) throws IOException {
		Gson gson = buildGson(players);
		JsonObject root = new JsonObject();
		root.addProperty(MAP_KEY, map.fileName());
		root.add(PLAYERS_KEY, gson.toJsonTree(players));
		root.add(EVENTS_KEY,
				gson.toJsonTree(eventLog, new TypeToken<List<GameEvent>>() {}.getType()));

		try (var writer = Files.newBufferedWriter(path)) {
			gson.toJson(root, writer);
		}
	}

	/////////////////// SAVE ///////////////////
	////////////////////////////////////////////
	/////////////////// LOAD ///////////////////

	public static JsonObject loadReplay(Path path) throws IOException {
		try (var reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	public record ReplayHeader(AvailableMaps.MapMetadata map, List<String> playerNames) {
	}

	public static ReplayHeader loadHeader(JsonObject data) {
		if (!data.has(MAP_KEY) || !data.has(PLAYERS_KEY))
			throw new IllegalArgumentException("Log missing header data");

		List<String> playerNames = data
				.getAsJsonArray(PLAYERS_KEY)
				.asList()
				.stream()
				.map(JsonElement::getAsString)
				.toList();

		return new ReplayHeader(AvailableMaps.getMapByName(data.get(MAP_KEY).getAsString()),
				playerNames);
	}

	public static List<GameEvent> loadEvents(JsonObject obj, List<Player> players) {
		if (!obj.has(EVENTS_KEY))
			throw new IllegalArgumentException("Log missing events");

		return buildGson(players).fromJson(
				obj.get(EVENTS_KEY),
				new TypeToken<List<GameEvent>>() {}.getType());
	}

	/////////////////// LOAD ///////////////////
	////////////////////////////////////////////
	///////////////// ADAPTERS /////////////////

	private static final class GameEventAdapter
			implements JsonSerializer<GameEvent>, JsonDeserializer<GameEvent> {
		@Override
		public JsonElement serialize(GameEvent event, Type type, JsonSerializationContext jdc) {
			JsonObject obj = jdc.serialize(event, event.getClass()).getAsJsonObject();
			obj.addProperty(TYPE_KEY, event.type().name());
			return obj;
		}

		@Override
		public GameEvent deserialize(JsonElement json, Type type, JsonDeserializationContext jdc) {
			JsonObject obj = json.getAsJsonObject();
			return switch (GameEventType.valueOf(obj.get(TYPE_KEY).getAsString())) {
				case UNIT_MOVED -> jdc.deserialize(obj, UnitMovedEvent.class);
				case UNIT_ATTACKED -> jdc.deserialize(obj, UnitAttackEvent.class);
				case UNIT_DIED -> jdc.deserialize(obj, UnitDiedEvent.class);
				case UNIT_BOUGHT -> jdc.deserialize(obj, UnitBoughtEvent.class);
				case CITY_CAPTURED -> jdc.deserialize(obj, CityCapturedEvent.class);
				case CAPTURE_PROGRESS -> jdc.deserialize(obj, CaptureProgressEvent.class);
				case TURN_CHANGED -> jdc.deserialize(obj, TurnChangedEvent.class);
				case PLAYER_ELIMINATED -> jdc.deserialize(obj, MultipleGameEvent.class);
			};
		}
	}

	@RequiredArgsConstructor
	private static final class PlayerAdapter
			implements JsonSerializer<Player>, JsonDeserializer<Player> {
		private final List<Player> players;

		@Override
		public JsonElement serialize(Player player, Type type, JsonSerializationContext jdc) {
			return new JsonPrimitive(player.getName());
		}

		@Override
		public Player deserialize(JsonElement json, Type type, JsonDeserializationContext jdc) {
			String playerName = json.getAsString();
			return this.players.stream()
					.filter(p -> p.getName().equals(playerName))
					.findFirst()
					.orElseThrow(() -> new JsonParseException("Player >" + playerName + "< not found"));
		}
	}

	///////////////// ADAPTERS /////////////////
	////////////////////////////////////////////
}
