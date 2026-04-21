package tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonSimples {
	public static String getString(JsonObject data, String key) {
		return data.has(key) ? data.get(key).getAsString() : null;
	}

	public static Integer getInt(JsonObject data, String key) {
		return data.has(key) ? data.get(key).getAsInt() : null;
	}

	public static JsonArray getArray(JsonObject data, String key) {
		return data.has(key) ? data.get(key).getAsJsonArray() : null;
	}
}
