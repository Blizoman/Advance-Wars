package tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import board.AvailableMaps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import event.*;
import gamer.Player;

public class LogFiler {
	private static final String MAP_KEY = "map";
	private static final String PLAYERS_KEY = "players";
	private static final String EVENTS_KEY = "events";
	private static final String TYPE_KEY = "type";

	private static final class GameEventAdapter
			implements JsonSerializer<GameEvent>, JsonDeserializer<GameEvent> {
		@Override
		public JsonElement serialize(GameEvent event, java.lang.reflect.Type type,
				JsonSerializationContext ctx) {
			JsonObject obj = ctx.serialize(event, event.getClass()).getAsJsonObject();
			obj.addProperty(TYPE_KEY, event.type().name());
			return obj;
		}

		@Override
		public GameEvent deserialize(JsonElement json, java.lang.reflect.Type type,
				JsonDeserializationContext ctx) {
			JsonObject obj = json.getAsJsonObject();
			return switch (GameEventType.valueOf(obj.get(TYPE_KEY).getAsString())) {
				case UNIT_MOVED -> ctx.deserialize(obj, UnitMovedEvent.class);
				case UNIT_ATTACKED -> ctx.deserialize(obj, UnitAttackEvent.class);
				case UNIT_DIED -> ctx.deserialize(obj, UnitDiedEvent.class);
				case UNIT_BOUGHT -> ctx.deserialize(obj, UnitBoughtEvent.class);
				case CITY_CAPTURED -> ctx.deserialize(obj, CityCapturedEvent.class);
				case CAPTURE_PROGRESS -> ctx.deserialize(obj, CaptureProgressEvent.class);
				case TURN_CHANGED -> ctx.deserialize(obj, TurnChangedEvent.class);
				case PLAYER_ELIMINATED -> ctx.deserialize(obj, MultipleGameEvent.class);
			};
		}
	}

	private static final class PlayerAdapter
			implements JsonSerializer<Player>, JsonDeserializer<Player> {
		private final List<Player> players;

		private PlayerAdapter(List<Player> players) {
			this.players = players;
		}

		@Override
		public JsonElement serialize(Player player, java.lang.reflect.Type type,
				JsonSerializationContext ctx) {
			return new JsonPrimitive(player.getName());
		}

		@Override
		public Player deserialize(JsonElement json, java.lang.reflect.Type type,
				JsonDeserializationContext ctx) {
			return players.stream()
					.filter(p -> p.getName().equals(json.getAsString()))
					.findFirst()
					.orElse(null);
		}
	}

	public record ReplayHeader(AvailableMaps.MapMetadata map, List<String> playerNames) {
	}

	public static JsonObject readReplay(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}

	public static ReplayHeader loadHeader(JsonObject obj) {
		if (!obj.has(MAP_KEY) || !obj.has(PLAYERS_KEY))
			throw new IllegalArgumentException("Replay log is missing map/player header");

		List<String> playerNames = new ArrayList<>();
		obj.getAsJsonArray(PLAYERS_KEY).forEach(e -> playerNames.add(e.getAsString()));
		return new ReplayHeader(AvailableMaps.findByFileprefix(obj.get(MAP_KEY).getAsString()),
				playerNames);
	}

	public static List<GameEvent> loadEvents(JsonObject obj, List<Player> players) {
		if (!obj.has(EVENTS_KEY))
			throw new IllegalArgumentException("Replay log is missing events list");

		return buildGson(players).fromJson(
				obj.get(EVENTS_KEY),
				new TypeToken<List<GameEvent>>() {}.getType());
	}

	private static Gson buildGson(List<Player> players) {
		return new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(GameEvent.class, new GameEventAdapter())
				.registerTypeAdapter(Player.class, new PlayerAdapter(players))
				.create();
	}

	public static void save(List<GameEvent> eventLog, Path path, AvailableMaps.MapMetadata map,
			List<Player> players) throws IOException {
		Gson gson = buildGson(players);
		JsonObject root = new JsonObject();
		root.addProperty(MAP_KEY, map.fileprefix());
		root.add(PLAYERS_KEY, gson.toJsonTree(players));
		root.add(EVENTS_KEY,
				gson.toJsonTree(eventLog, new TypeToken<List<GameEvent>>() {}.getType()));
		Files.writeString(path, gson.toJson(root));
	}

	public static ReplayHeader loadHeader(Path path) throws IOException {
		return loadHeader(readReplay(path));
	}

	public static List<GameEvent> loadEvents(Path path, List<Player> players) throws IOException {
		return loadEvents(readReplay(path), players);
	}
}
