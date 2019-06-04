package se.uu.it.modeltester.mutate;

import java.util.LinkedList;
import java.util.List;

public class Helper {
	public static <T> List<T> reorder(List<T> elements, Integer [] mapping) {
		List<T> orderedList = new LinkedList<T>();
		for (Integer i=0; i<mapping.length; i++) {
			if (mapping[i] != null) {
				orderedList.add(elements.get(mapping[i]));
			}
		}
		return orderedList;
	}

}
