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

import se.uu.it.modeltester.mutate.fragment.ReorderingMutation;
import se.uu.it.modeltester.mutate.fragment.SplittingMutation;

/**
 * A class for parsing mutations from Json strings/serialiazing them to Json strings.
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
			switch(mType) {
			case FRAGMENT_REORDERING:
				return context.deserialize(jsonObject, ReorderingMutation.class);
			case MESSAGE_SPLITTING:
				return context.deserialize(jsonObject, SplittingMutation.class);
			default:
				return null;
			}
		}
		
		@Override
		public JsonElement serialize(Mutation<?> src, Type typeOfSrc, JsonSerializationContext context) {
			JsonElement element;
			switch(src.getType()) {
			case FRAGMENT_REORDERING:
				element = context.serialize(src, ReorderingMutation.class);
				break;
			case MESSAGE_SPLITTING:
				element = context.serialize(src, SplittingMutation.class);
				break;
			default:
				element = JsonNull.INSTANCE;
				break;
			}
			if (element != JsonNull.INSTANCE) {
				element.getAsJsonObject().addProperty(TYPE_FIELD, src.getType().name());
			}
			return element;
		}
	} 
}
