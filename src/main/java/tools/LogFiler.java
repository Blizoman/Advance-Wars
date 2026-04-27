package tools;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import event.*;
import player.Player;

public class LogFiler {

	private static Gson buildGson(List<Player> players) {
		return new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(GameEvent.class, new GameEventAdapter())
				.registerTypeAdapter(Player.class, new PlayerAdapter(players))
				.create();
	}

	public static void save(List<GameEvent> eventLog, Path path, List<Player> players)
			throws IOException {
		Gson gson = buildGson(players);
		Type listType = new TypeToken<List<GameEvent>>() {}.getType();
		Files.writeString(path, gson.toJson(eventLog, listType));
	}

	public static List<GameEvent> load(Path path, List<Player> players) throws IOException {
		Gson gson = buildGson(players);
		String json = Files.readString(path);
		Type listType = new TypeToken<List<GameEvent>>() {}.getType();
		return gson.fromJson(json, listType);
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
				case TURN_CHANGED -> context.deserialize(obj, TurnChangedEvent.class);
				case PLAYER_ELIMINATED -> context.deserialize(obj, MultipleGameEvent.class);
			};
		}
	}


	private static class PlayerAdapter implements JsonSerializer<Player>, JsonDeserializer<Player> {
		private final Map<String, Player> byName;

		PlayerAdapter(List<Player> players) {
			this.byName = players.stream().collect(Collectors.toMap(Player::getName, p -> p));
		}

		@Override
		public JsonElement serialize(Player player, Type type, JsonSerializationContext ctx) {
			return new JsonPrimitive(player.getName());
		}

		@Override
		public Player deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
			return byName.get(json.getAsString());
		}
	}
}
