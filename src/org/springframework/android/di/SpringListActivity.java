package org.springframework.android.di;

import android.app.ListActivity;
import android.os.Bundle;

public class SpringListActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		SpringApplication application = (SpringApplication) getApplication();
		application.injectDependencies(this);
		super.onCreate(savedInstanceState);
	}
}
