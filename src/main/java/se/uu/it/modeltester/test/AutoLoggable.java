package se.uu.it.modeltester.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Stack;

public abstract class AutoLoggable {
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName()).append(": {");
		Class<?> cls = this.getClass();

		// we print fields from the top down
		Stack<Class<?>> clsStack = new Stack<>();
		while (cls != AutoLoggable.class) {
			clsStack.push(cls);
			cls = cls.getSuperclass();
		}

		while (!clsStack.isEmpty()) {
			cls = clsStack.pop();
			for (Field f : cls.getDeclaredFields()) {
				// ignore java runtime-related fields or static fields
				if (f.isSynthetic() || Modifier.isStatic(f.getModifiers())) {
					continue;
				}
				f.setAccessible(true);
				try {
					sb.append("   ").append(f.getName()).append(": ")
							.append(f.get(this)).append("\n");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			cls = cls.getSuperclass();
		}
		sb.append("}");
		return sb.toString();
	}
}
