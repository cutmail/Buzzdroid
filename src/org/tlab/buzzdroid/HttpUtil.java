package org.tlab.buzzdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

class HttpUtil {

	public static String getTitle(String url) {

		HttpClient client = new DefaultHttpClient();
		String result = "";

		try {
			HttpResponse response = client.execute(new HttpGet(url));

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));

				while (true) {
					result = new String(reader.readLine());
					if (result.indexOf("title") != -1) {
						result = result.replace("<title>", "").replace(
								"</title>", "");
						inputStream.close();
						break;
					}
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }
}
