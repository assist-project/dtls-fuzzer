package se.uu.it.modeltester.mutate;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;

import se.uu.it.modeltester.mutate.fragment.ReorderingMutation;
import se.uu.it.modeltester.mutate.fragment.SplittingMutation;

/**
 * A class for parsing mutations from Json strings/serializing them to Json strings.
 */
public class JsonMutationParser {
	
	private static JsonMutationParser INSTANCE = null;
	public static JsonMutationParser getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JsonMutationParser();
		}
		return INSTANCE;
	}
	
	private Gson gson;

	public JsonMutationParser() {
		gson = new GsonBuilder()
				.registerTypeAdapter(Mutation.class, new MutationTypeAdapter())
				.create();
	}
	
	/**
	 * Serializes mutations into an one line json string
	 */
	public String serialize(Mutation<?> [] mutations) {
		return gson.toJson(mutations, Mutation [].class);
	}
	
	/**
	 * Deserializes mutations from a json String
	 */
	public Mutation<?> [] deserialize(String mutationsJsonString) {
		return gson.fromJson(mutationsJsonString, Mutation [].class);
	}
	
	
	static class MutationTypeAdapter implements JsonSerializer<Mutation<?>>, JsonDeserializer<Mutation<?>> {
		private static String TYPE_FIELD="@@type"; 
		@Override
		public Mutation<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			MutationType mType = MutationType.valueOf(jsonObject.get(TYPE_FIELD).getAsString());
			return context.deserialize(jsonObject, mType.getMutationClass());
		}
		
		@Override
		public JsonElement serialize(Mutation<?> src, Type typeOfSrc, JsonSerializationContext context) {
			JsonElement element;
			element = context.serialize(src, src.getType().getClass());
			return element;
		}
	} 
}
