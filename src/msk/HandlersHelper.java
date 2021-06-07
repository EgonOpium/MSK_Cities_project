package msk;

import java.util.HashMap;
import java.util.Map;

public class HandlersHelper {

	private static Map<String, Integer> interactionClassMapping;
	private static Map<Integer, Integer> objectClassMapping;


	static {
		interactionClassMapping = new HashMap<String, Integer>();
		objectClassMapping = new HashMap<Integer, Integer>();
	}
	
	public static void addInteractionClassHandler(String interactionName, Integer handle) {
		interactionClassMapping.put(interactionName, handle);
	}
	
	public static int getInteractionHandleByName(String name) {
		return interactionClassMapping.get(name).intValue();
	}

	public static void addObjectClassHandler(Integer objectName, Integer handle) {
		objectClassMapping.put(objectName, handle);
	}

	public static int getObjectClassHandleByName(Integer name) {
		return objectClassMapping.get(name).intValue();
	}

}
