package org.springframework.android.di;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;

public abstract class SpringApplication extends Application {
	
	private Map<Class<?>, Object> dependencies = new HashMap<Class<?>, Object>();
	
	@Override
	public void onCreate () {
		super.onCreate();
	}
	
	void injectDependencies(Activity activity) {
		Class<?> clazz = activity.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Inject.class)) {
				field.setAccessible(true);
				try {
					if (field.get(activity) == null) {
						Class<?> classToInject = field.getType();
						if (dependencies.containsKey(classToInject)) {
							Object object = dependencies.get(classToInject);
							field.set(activity, object);
						}
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Unable to access field.", e);
				}
			}
		}	
	}
	
	protected void addDependency(Class<?> c, Object o) {
		dependencies.put(c, o);
	}

}
