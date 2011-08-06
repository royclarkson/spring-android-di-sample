package org.springframework.android.di;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.util.LinkedMultiValueMap;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

public abstract class SpringApplication extends Application implements DependencyRepository {
	
	private static final String TAG = SpringApplication.class.getSimpleName();
	
	private Map<Class<?>, List<Dependency>> dependencies = new LinkedMultiValueMap<Class<?>, Dependency>();
	
	
	//***************************************
	// Application Methods
	//***************************************
	@Override
	public void onCreate () {
		Log.d(TAG, "onCreate()");
		super.onCreate();
	}
	
	
	//***************************************
	// Methods
	//***************************************
	void injectDependencies(Activity activity) {
		Class<?> clazz = activity.getClass();
		Log.d(TAG, "Injecting dependencies in " + clazz.getSimpleName());
		String component = null;
		if (clazz.isAnnotationPresent(Component.class)) {
			component = clazz.getAnnotation(Component.class).value();
		}
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Inject.class)) {
				field.setAccessible(true);
				try {
//					if (field.get(activity) == null) {
						Class<?> classToInject = field.getType();
						if (dependencies.containsKey(classToInject)) {
							String name = null;
							if (field.isAnnotationPresent(Named.class)) {
								name = field.getAnnotation(Named.class).value();
							}
							Dependency dependency = getDependency(classToInject, name);
							if (dependency != null && 
									(component ==  null || dependency.getComponentName().equals(component))) {
								Log.d(TAG, "Injecting value for field: " + dependency.getObject().getClass().getSimpleName());
								field.set(activity, dependency.getObject());
							}
						}
//					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Unable to access field.", e);
				}
			}
		}	
	}
	
	
	//***************************************
	// DependencyRepository Methods
	//***************************************
	public void addDependency(Class<?> clazz, Dependency dependency) {
		Log.d(TAG, "addDependency()");
		List<Dependency> l = dependencies.get(clazz);
		if (l == null) {
			l = new ArrayList<Dependency>();
		} else {
//			dependencies.remove(clazz);
			for (int i = 0; i < l.size(); i++) {
				Dependency d = l.get(i);
				if (d.getName() == dependency.getName()) {
					l.remove(i);
					Log.d(TAG, "Updating existing dependency");
					break;
				}
			}	
		}
		l.add(dependency);
		dependencies.put(clazz, l);
	}
	
	public Dependency getDependency(Class<?> clazz, String name) {
		Log.d(TAG, "getDependency()");
		List<Dependency> l = dependencies.get(clazz);
		for (Dependency d : l) {
			if (d.getName() == name) {
				return d;
			}
		}
		return null;
	}

}
