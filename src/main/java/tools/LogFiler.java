package tools;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import classes.event.*;
import classes.game.Game;
import classes.game.Session;

public class LogFiler {

	private static final Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(GameEvent.class, new GameEventAdapter())
			.create();

	public static void save(List<GameEvent> eventLog, Path path) throws IOException {
		Type listType = new TypeToken<List<GameEvent>>() {}.getType();
		Files.writeString(path, gson.toJson(eventLog, listType));
	}

	public static List<GameEvent> load(Path path) throws IOException {
		String json = Files.readString(path);
		Type listType = new TypeToken<List<GameEvent>>() {}.getType();
		return gson.fromJson(json, listType);
	}

	public static Session loadFromFile(Path path, Game game) throws IOException {
		List<GameEvent> events = LogFiler.load(path);
		return new Session(game, events);
	}

	private static class GameEventAdapter
			implements JsonSerializer<GameEvent>, JsonDeserializer<GameEvent> {

		@Override
		public JsonElement serialize(GameEvent event, Type type, JsonSerializationContext context) {
			JsonObject jo = context.serialize(event, event.getClass()).getAsJsonObject();
			jo.addProperty("type", event.type().name());
			return jo;
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
}
