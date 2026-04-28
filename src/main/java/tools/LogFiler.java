package tools;

import java.io.IOException;
import java.lang.reflect.Type;
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
	public record ReplayHeader(AvailableMaps.MapMetadata map, List<String> playerNames) {}

	private static Gson buildGson(List<Player> players) {
		return new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(GameEvent.class, new GameEventAdapter())
				.registerTypeAdapter(Player.class, new PlayerAdapter(players))
				.create();
	}

	public static void save(List<GameEvent> eventLog, Path path, AvailableMaps.MapMetadata map, List<Player> players)
			throws IOException {
		Gson gson = buildGson(players);
		JsonObject root = new JsonObject();
		root.addProperty("map", map.fileprefix());
		JsonArray playerArray = new JsonArray();
		players.forEach(player -> playerArray.add(player.getName()));
		root.add("players", playerArray);
		root.add("events", gson.toJsonTree(eventLog, new TypeToken<List<GameEvent>>() {}.getType()));
		Files.writeString(path, gson.toJson(root));
	}

	public static ReplayHeader loadHeader(Path path) throws IOException {
		JsonObject object = readRoot(path);
		if (!object.has("map") || !object.has("players"))
			throw new IllegalArgumentException("Replay log is missing map/player header");

		AvailableMaps.MapMetadata map = AvailableMaps.findByFileprefix(object.get("map").getAsString());
		List<String> playerNames = new ArrayList<>();
		object.getAsJsonArray("players").forEach(element -> playerNames.add(element.getAsString()));
		return new ReplayHeader(map, playerNames);
	}

	public static List<GameEvent> loadEvents(Path path, List<Player> players) throws IOException {
		Gson gson = buildGson(players);
		JsonObject root = readRoot(path);
		Type listType = new TypeToken<List<GameEvent>>() {}.getType();
		if (!root.has("events"))
			throw new IllegalArgumentException("Replay log is missing events list");
		return gson.fromJson(root.get("events"), listType);
	}

	private static JsonObject readRoot(Path path) throws IOException {
		String json = Files.readString(path);
		return JsonParser.parseString(json).getAsJsonObject();
	}

	private static class GameEventAdapter
			implements JsonSerializer<GameEvent>, JsonDeserializer<GameEvent> {

		@Override
		public JsonElement serialize(GameEvent event, Type type, JsonSerializationContext ctx) {
			JsonObject object = new JsonObject();
			object.addProperty("type", event.type().name());

			JsonObject rest = ctx.serialize(event, event.getClass()).getAsJsonObject();
			rest.entrySet().forEach(e -> object.add(e.getKey(), e.getValue()));

			return object;
		}

		@Override
		public GameEvent deserialize(JsonElement json, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			GameEventType eventType = GameEventType.valueOf(obj.get("type").getAsString());
			return switch (eventType) {
				case UNIT_MOVED -> context.deserialize(obj, UnitMovedEvent.class);
				case UNIT_ATTACKED -> context.deserialize(obj, UnitAttackEvent.class);
				case UNIT_DIED -> context.deserialize(obj, UnitDiedEvent.class);
				case UNIT_BOUGHT -> context.deserialize(obj, UnitBoughtEvent.class);
				case CITY_CAPTURED -> context.deserialize(obj, CityCapturedEvent.class);
				case CAPTURE_PROGRESS -> context.deserialize(obj, CaptureProgressEvent.class);
				case TURN_CHANGED -> context.deserialize(obj, TurnChangedEvent.class);
				case PLAYER_ELIMINATED -> context.deserialize(obj, MultipleGameEvent.class);
			};
		}
	}


	private static class PlayerAdapter implements JsonSerializer<Player>, JsonDeserializer<Player> {
		private final List<Player> players;

		PlayerAdapter(List<Player> players) {
			this.players = players;
		}

		@Override
		public JsonElement serialize(Player player, Type type, JsonSerializationContext ctx) {
			return new JsonPrimitive(player.getName());
		}

		@Override
		public Player deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
			String name = json.getAsString();
			return players.stream()
					.filter(player -> player.getName().equals(name))
					.findFirst()
					.orElse(null);
		}
	}
}
