package se.uu.it.modeltester;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

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
import com.google.gson.reflect.TypeToken;

import se.uu.it.modeltester.mutate.Fragment;
import se.uu.it.modeltester.mutate.Fragmentation;
import se.uu.it.modeltester.mutate.SplittingMutation;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;
import se.uu.it.modeltester.mutate.ReorderingMutation;

public class Test {
	
	public static void main(String args[]) {
		Gson g = new GsonBuilder()
				.registerTypeAdapter(Mutation.class, new MutationTypeAdapter())
				.create();
		Fragmentation fragmentation = new Fragmentation(new Fragment(0, 5), new Fragment(5, 5));
		SplittingMutation splittingMutation = new SplittingMutation(fragmentation);
		ReorderingMutation mappingMutation = new ReorderingMutation(new Integer [] {1,0});
		Mutation [] mutArr = new Mutation [] {splittingMutation, mappingMutation};
		List<Mutation<?>> mutations = new LinkedList<>();
		mutations.add(splittingMutation);
		mutations.add(mappingMutation);
//		Mutation [] mutationarr = new Mutation [] {};
		Type type = new TypeToken<LinkedList<Mutation<?>>>(){}.getType();
		String jsonString = g.toJson(mutations, type);
		com.google.gson.JsonSerializer< ?> a;
		
		System.out.println(jsonString);
//		System.out.println(g.toJson(mutations, typeOfSrc));
		System.out.println(g.toJson(mutArr, Mutation [].class));
		String arrString = g.toJson(mutArr, Mutation [].class);
		Mutation [] arrFromJson = g.fromJson(arrString, Mutation [].class);
		
		
//		System.out.println(g.fromJson(jsonString, mutations.getClass()).get(0));
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
	
	static class MutationSerializer implements JsonSerializer<Mutation<?>> {

		@Override
		public JsonElement serialize(Mutation<?> src, Type typeOfSrc, JsonSerializationContext context) {
			switch(src.getType()) {
			case FRAGMENT_REORDERING:
				return context.serialize(src, ReorderingMutation.class);
			case MESSAGE_SPLITTING:
				return context.serialize(src, SplittingMutation.class);
			default:
				return JsonNull.INSTANCE;
			}
		}
		
	}
	
	static class MutationDeserializer implements JsonDeserializer<Mutation<?>> {

		@Override
		public Mutation<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			Mutation<?> mutation;
			
			if (jsonObject.has("mapping")) {
				mutation = context.deserialize(json, ReorderingMutation.class);
			} else {
				if (jsonObject.has("fragmentation")) {
					mutation = context.deserialize(json, SplittingMutation.class);
				}
			}
			return null;
		}
	}
		 

}
