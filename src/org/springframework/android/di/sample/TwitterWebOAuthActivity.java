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

import javax.inject.Inject;

import org.springframework.android.di.Component;
import org.springframework.android.di.Dependency;
import org.springframework.android.di.DependencyRepository;
import org.springframework.android.di.SpringActivity;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * @author Roy Clarkson
 */
@Component("oauth")
public class TwitterWebOAuthActivity extends SpringActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = TwitterWebOAuthActivity.class.getSimpleName();
	
	private static final String REQUEST_TOKEN_KEY = "request_token";
	
	private static final String REQUEST_TOKEN_SECRET_KEY = "request_token_secret";
	
	private Activity activity;
	
	private WebView webView;
	
	private ProgressDialog progressDialog = null;
	
	private boolean destroyed = false;
	
	@Inject
	private ConnectionRepository connectionRepository;
	
	@Inject
	private TwitterConnectionFactory connectionFactory;
	
	@Inject 
	private DependencyRepository dependencyRepository; 
	
	private SharedPreferences twitterPreferences;
	
	
	//***************************************
	// Activity methods
	//***************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		this.webView = new WebView(this);
		setContentView(webView);
		this.activity = this;
		
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				activity.setTitle("Loading...");
				activity.setProgress(progress * 100);
				if (progress == 100) {
					activity.setTitle(R.string.app_name);
				}
			}
		});
		
		this.twitterPreferences = getSharedPreferences("TwitterConnectPreferences", Context.MODE_PRIVATE);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Uri uri = getIntent().getData();
		if (uri != null) {
			String oauthVerifier = uri.getQueryParameter("oauth_verifier");

			if (oauthVerifier != null) {
				this.webView.clearView();
				new TwitterPostConnectTask().execute(oauthVerifier);
			}
		} else {
			new TwitterPreConnectTask().execute();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.destroyed = true;
	}
	

	//***************************************
	// Private methods
	//***************************************
	private String getOAuthCallbackUrl() {
		return getString(R.string.twitter_oauth_callback_url);
	}
	
	private void displayTwitterAuthorization(OAuthToken requestToken) {
		saveRequestToken(requestToken);
		String authUrl = connectionFactory.getOAuthOperations().buildAuthorizeUrl(requestToken.getValue(), OAuth1Parameters.NONE);
		webView.loadUrl(authUrl);
	}
	
	private void displayTwitterOptions() {
		Intent intent = new Intent();
		intent.setClass(this, TwitterActivity.class);
	    startActivity(intent);
    	finish();
	}
	
	private void saveRequestToken(OAuthToken requestToken) {
		SharedPreferences.Editor editor = twitterPreferences.edit();
		editor.putString(REQUEST_TOKEN_KEY, requestToken.getValue());
		editor.putString(REQUEST_TOKEN_SECRET_KEY, requestToken.getSecret());
		editor.commit();
	}
	
	private OAuthToken retrieveRequestToken() {		
		String token = twitterPreferences.getString(REQUEST_TOKEN_KEY, null);
		String secret = twitterPreferences.getString(REQUEST_TOKEN_SECRET_KEY, null);
		return new OAuthToken(token, secret);
	}
	
	private void deleteRequestToken() {
		twitterPreferences.edit().clear().commit();
	}
	
	private void showProgressDialog(CharSequence message) {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setIndeterminate(true);
		}
		
		progressDialog.setMessage(message);
		progressDialog.show();
	}
		
	private void dismissProgressDialog() {
		if (progressDialog != null && !destroyed) {
			progressDialog.dismiss();
		}
	}
	
	
	//***************************************
	// Private classes
	//***************************************
	private class TwitterPreConnectTask extends AsyncTask<Void, Void, OAuthToken> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Initializing OAuth Connection...");
		}

		@Override
		protected OAuthToken doInBackground(Void... params) {
			return connectionFactory.getOAuthOperations().fetchRequestToken(getOAuthCallbackUrl(), null);
		}

		@Override
		protected void onPostExecute(OAuthToken requestToken) {
			dismissProgressDialog();
			displayTwitterAuthorization(requestToken);
		}
		
	}
	
	private class TwitterPostConnectTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			showProgressDialog("Finalizing OAuth Connection...");
		}
		
		@Override
		protected Void doInBackground(String... params) {
			if (params.length <= 0) {
				return null;
			}

			final String verifier = params[0];

			OAuthToken requestToken = retrieveRequestToken();
			AuthorizedRequestToken authorizedRequestToken = new AuthorizedRequestToken(requestToken, verifier);
			OAuthToken accessToken = connectionFactory.getOAuthOperations().exchangeForAccessToken(authorizedRequestToken, null);
			deleteRequestToken();
			Connection<Twitter> connection = connectionFactory.createConnection(accessToken);

			try {
				connectionRepository.addConnection(connection);
				dependencyRepository.addDependency(Twitter.class, new Dependency(connection.getApi()));
				dependencyRepository.addDependency(Boolean.class, new Dependency(true, "isConnected"));
			} catch (DuplicateConnectionException e) {
				// connection already exists in repository!
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void v){
			dismissProgressDialog();
			displayTwitterOptions();
		}
		
	}
	
}
