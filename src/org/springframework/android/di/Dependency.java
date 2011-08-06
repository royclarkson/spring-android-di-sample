package org.springframework.android.di;

public class Dependency {
	
	private Object object;
	
	private String name;
	
	private String componentName;
	
	public Dependency(Object object) {
		this(object, null, null);
	}
	
	public Dependency(Object object, String name) {
		this(object, name, null);
	}
		
	public Dependency(Object object, String name, String componentName) {
		this.componentName = componentName;
		this.name = name;
		this.object = object;
	}

	public Object getObject() {
		return object;
	}
	
	public String getName() {
		return name;
	}
	
	public String getComponentName() {
		return componentName;
	}

}
