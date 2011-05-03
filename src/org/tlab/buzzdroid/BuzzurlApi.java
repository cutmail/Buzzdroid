package org.tlab.buzzdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import me.cutmail.buzzurl.Article;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.net.Uri;

public class BuzzurlApi {
	private static final String SCHEME = "https";
	private static final String AUTHORITY = "buzzurl.jp";
	private static final int PORT = 80;
	
	private static final AuthScope SCOPE = new AuthScope(AUTHORITY, PORT);
	private static final String USER_AGENT = "Buzzdroid";
	
	private final DefaultHttpClient mClient;
	private final UsernamePasswordCredentials mCredentials;
	
	public BuzzurlApi(UsernamePasswordCredentials credentials) {
		mClient = new DefaultHttpClient();
		mCredentials = credentials;
		updateUserAgent();
	}
	
	private void updateUserAgent() {
		HttpParams params = mClient.getParams();
		HttpProtocolParams.setUserAgent(params, USER_AGENT);
	}
	
	public boolean createPosts(/*String url, String title, String comment, boolean access*/Article article) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME);
		builder.authority(AUTHORITY);
		builder.appendEncodedPath("posts/add/v1");
		builder.appendQueryParameter("url", article.getUrl());
		builder.appendQueryParameter("title", article.getTitle());
		builder.appendQueryParameter("comment", article.getComment());
		String access_str = (article.isAccess() == false) ? "private" : ""; 
		builder.appendQueryParameter("access", access_str);
		Uri uri = builder.build();
		
		BufferedReader bufreader;
		HttpPost httpPost = new HttpPost(uri.toString());
		mClient.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()), mCredentials);
	
		try {
			HttpResponse response = mClient.execute(httpPost);
//			bufreader = new BufferedReader(new InputStreamReader(httpPost.getEntity().getContent()));
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return true;
			} else {
				return false;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
