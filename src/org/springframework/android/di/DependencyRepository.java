package org.springframework.android.di;

public interface DependencyRepository {

	void addDependency(Class<?> clazz, Dependency dependency);
	
	Dependency getDependency(Class<?> clazz, String Name);
}
