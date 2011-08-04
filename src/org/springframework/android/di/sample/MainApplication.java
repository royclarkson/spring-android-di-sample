package org.springframework.android.di.sample;

import org.springframework.android.di.SpringApplication;
import org.springframework.web.client.RestTemplate;

public class MainApplication extends SpringApplication {

	@Override
	public void onCreate () {
		super.onCreate();
		
		this.addDependency(RestTemplate.class, new RestTemplate());
	}
}
