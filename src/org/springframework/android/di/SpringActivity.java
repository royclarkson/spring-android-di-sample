package org.springframework.android.di;

import android.app.Activity;
import android.os.Bundle;


public abstract class SpringActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	SpringApplication application = (SpringApplication) getApplication();
        application.injectDependencies(this);
        super.onCreate(savedInstanceState);
    }

}
