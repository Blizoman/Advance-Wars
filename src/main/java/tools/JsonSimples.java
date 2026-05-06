/**
 * Provides a simplified interface for reading and validating data from JSON objects. This utility
 * wraps GSON's basic accessors to offer safe extraction of strings, integers, and arrays. It
 * distinguishes between optional fields (returning null if missing) and mandatory fields (throwing
 * an exception if missing), ensuring consistent error handling during the deserialization of game
 * resources.
 *
 * @author xpruzir00
 */

package tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonSimples {
	private JsonSimples() { /* Prevent instantiation */ }

	public static String getString(JsonObject data, String key) {
		return data.has(key) ? data.get(key).getAsString() : null;
	}

	public static Integer getInt(JsonObject data, String key) {
		return data.has(key) ? data.get(key).getAsInt() : null;
	}

	public static JsonArray getArray(JsonObject data, String key) {
		return data.has(key) ? data.get(key).getAsJsonArray() : null;
	}

	public static String requireString(JsonObject data, String key) {
		if (!data.has(key))
			throw new IllegalArgumentException("Missing required field: " + key);
		return getString(data, key);
	}

	public static int requireInt(JsonObject data, String key) {
		if (!data.has(key))
			throw new IllegalArgumentException("Missing required field: " + key);
		return getInt(data, key);
	}

	public static JsonArray requireArray(JsonObject data, String key) {
		if (!data.has(key))
			throw new IllegalArgumentException("Missing required field: " + key);
		return getArray(data, key);
	}
}
