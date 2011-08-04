package org.springframework.android.di.sample;

import java.util.Collections;

import org.springframework.android.di.Inject;
import org.springframework.android.di.SpringActivity;
import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends SpringActivity {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	@Inject
	private RestTemplate restTemplate;
	

	//***************************************
    // Activity methods
    //***************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		new TwitterSearchTask().execute();
	}
	
	
	//***************************************
    // Private methods
    //***************************************
	private void refreshResults(String results) {
		if (results == null) {
			return;
		}
				
		TextView textView = (TextView) findViewById(R.id.text);
		textView.setText(results + "\n");
	}
	
	
	//***************************************
    // Private classes
    //***************************************
	private class TwitterSearchTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				// The URL for making the GET request
				final String url = "http://search.twitter.com/search.json?q={query}&rpp=100";
				
				// Add the gzip Accept-Encoding header to the request
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setAcceptEncoding(Collections.singletonList(ContentCodingType.GZIP));
				
				// Perform the HTTP GET request
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(requestHeaders), String.class, "SpringSource");
				
				return response.getBody();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			refreshResults(result);
		}
		
	}
}