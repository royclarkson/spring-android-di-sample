/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.android.di.sample;

import org.springframework.android.di.Dependency;
import org.springframework.android.di.DependencyRepository;
import org.springframework.android.di.SpringApplication;
import org.springframework.security.crypto.encrypt.AndroidEncryptors;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.sqlite.SQLiteConnectionRepository;
import org.springframework.social.connect.sqlite.support.SQLiteConnectionRepositoryHelper;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

import android.util.Log;

/**
 * @author Roy Clarkson
 */
public class MainApplication extends SpringApplication {
	
	private static final String TAG = MainApplication.class.getSimpleName();
	
		
	//***************************************
	// Application Methods
	//***************************************
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
		ConnectionFactoryRegistry connectionFactoryRegistry = new ConnectionFactoryRegistry();
		connectionFactoryRegistry.addConnectionFactory(new TwitterConnectionFactory(getTwitterConsumerToken(), getTwitterConsumerTokenSecret()));
		ConnectionRepository connectionRepository = new SQLiteConnectionRepository(new SQLiteConnectionRepositoryHelper(this), 
				connectionFactoryRegistry, AndroidEncryptors.text("password", "5c0744940b5c369b"));
		TwitterConnectionFactory connectionFactory = (TwitterConnectionFactory) connectionFactoryRegistry.getConnectionFactory(Twitter.class);
		
		addDependency(DependencyRepository.class, new Dependency(this, null, "oauth"));
		addDependency(ConnectionRepository.class, new Dependency(connectionRepository, null, "oauth"));
		addDependency(TwitterConnectionFactory.class, new Dependency(connectionFactory, null, "oauth"));
		
		Boolean isConnected = false;
		Connection<Twitter> connection = connectionRepository.findPrimaryConnection(Twitter.class);
		if (connection != null) {
			isConnected = true;
			addDependency(Twitter.class, new Dependency(connection.getApi()));
		}
		addDependency(Boolean.class, new Dependency(isConnected, "isConnected"));
	}
	
	
	//***************************************
	// Private methods
	//***************************************	
	private String getTwitterConsumerToken() {
		return getString(R.string.twitter_consumer_key);
	}
	
	private String getTwitterConsumerTokenSecret() {
		return getString(R.string.twitter_consumer_key_secret);
	}
	
}
