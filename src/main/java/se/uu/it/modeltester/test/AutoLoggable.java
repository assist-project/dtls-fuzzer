package se.uu.it.modeltester.test;

import java.lang.reflect.Field;

public abstract class AutoLoggable {
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName()).append(": {");
		for (Field f : this.getClass().getDeclaredFields() ) {
			f.setAccessible(true);
			try {
				sb.append("   ").append(f.getName())
				.append(": ").append(f.get(this)).append("\n");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
