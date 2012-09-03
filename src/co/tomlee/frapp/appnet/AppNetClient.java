package co.tomlee.frapp.appnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import co.tomlee.frapp.model.Post;

public class AppNetClient {
	private final String baseUrl;
	private final String accessToken;
	
	public AppNetClient(final String accessToken) {
		this("https://alpha-api.app.net", accessToken);
	}
	
	private AppNetClient(final String baseUrl, final String accessToken) {
		this.baseUrl = baseUrl;
		this.accessToken = accessToken;
	}
	
	public List<Post> getRecentPosts(final Stream stream, final int count) throws AppNetException {
		return getPosts(stream, null, null, count);
	}
	
	public List<Post> getPostsSince(final Stream stream, final String sinceId, final int count) throws AppNetException {
		return getPosts(stream, null, sinceId, count);
	}
	
	public List<Post> getPostsBefore(final Stream stream, final String beforeId, final int count) throws AppNetException {
		return getPosts(stream, beforeId, null, count);
	}
	
	private List<Post> getPosts(final Stream stream, final String beforeId, final String sinceId, final int count) throws AppNetException {
		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		if (beforeId != null) {
			params.add(new BasicNameValuePair("before_id", beforeId));
		}
		if (sinceId != null) {
			params.add(new BasicNameValuePair("since_id", sinceId));
		}
		if (count > 0) {
			params.add(new BasicNameValuePair("count", "" + count));
		}
		params.add(new BasicNameValuePair("include_deleted", "0"));
		
		final StringBuilder sb = new StringBuilder();
		sb.append(baseUrl).append(stream.getPath());
		if (params.size() > 0) {
			sb.append("?").append(URLEncodedUtils.format(params, "utf-8"));
		}
		
		try {
			final HttpGet get = new HttpGet(sb.toString());
			get.addHeader("Authorization", "Bearer " + accessToken);
			final HttpClient client = new DefaultHttpClient();
			HttpResponse resp = client.execute(get);
			return parsePosts(resp.getEntity().getContent());
		}
		catch (UnsupportedEncodingException e) {
			throw new AppNetException("Unsupported encoding", e);
		}
		catch (ClientProtocolException e) {
			throw new AppNetException("HTTP error", e);
		}
		catch (IOException e) {
			throw new AppNetException("I/O error", e);
		}
	}
	
	public Post addPost(final String replyTo, final String text) throws AppNetException {
		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("text", text));
		if (replyTo != null && !replyTo.equals("")) {
			params.add(new BasicNameValuePair("reply_to", replyTo));
		}
		
		try {
			final HttpPost post = new HttpPost(baseUrl + "/stream/0/posts");
			post.addHeader("Authorization", "Bearer " + accessToken);
			post.setEntity(new UrlEncodedFormEntity(params));
			final HttpClient client = new DefaultHttpClient();
			final HttpResponse resp = client.execute(post);
			return parsePost(resp.getEntity().getContent());
		}
		catch (UnsupportedEncodingException e) {
			throw new AppNetException("Unsupported encoding", e);
		}
		catch (ClientProtocolException e) {
			throw new AppNetException("HTTP error", e);
		}
		catch (IOException e) {
			throw new AppNetException("I/O error", e);
		}
	}
	
	private List<Post> parsePosts(final InputStream inputStream) throws IOException, AppNetException {
		final JSONTokener tokener = new JSONTokener(slurp(inputStream));
		try {
			//
			// TODO error checking!
			//
			final JSONArray arr = new JSONArray(tokener);
			final ArrayList<Post> posts = new ArrayList<Post>(arr.length());
			for (int i = 0; i < arr.length(); i++) {
				final JSONObject postJson = (JSONObject) arr.get(i);
				posts.add(parsePost(postJson));
			}
			return posts;
		}
		catch (JSONException e) {
			throw new AppNetException("Failed to parse JSON response", e);
		}
	}
	
	private Post parsePost(final InputStream inputStream) throws IOException, AppNetException {
		final JSONTokener tokener = new JSONTokener(slurp(inputStream));
		try {
			final JSONObject obj = new JSONObject(tokener);
			return parsePost(obj);
		}
		catch (JSONException e) {
			throw new AppNetException("Failed to parse JSON response", e);
		}
	}
	
	private Post parsePost(final JSONObject postJson) throws JSONException {
		final JSONObject userJson = postJson.getJSONObject("user");
		final String id = postJson.getString("id");
		final String author = userJson.getString("username");
		final String text = (postJson.has("text") ? postJson.getString("text") : "");
		
		//
		// XXX we currently exclude deleted posts explicitly in the request,
		//     but that may change ...
		//
		final boolean deleted;
		if (postJson.has("is_deleted")) {
			deleted = postJson.getBoolean("is_deleted");
		}
		else {
			deleted = false;
		}
		return new Post(id, author, text, deleted);
	}
	
	private static String slurp(final InputStream inputStream) throws IOException {
		//
		// APIv9: JsonReader isn't available :(
		//
		final StringBuilder sb = new StringBuilder();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			final char[] buf = new char[8192];
			for (;;) {
				final int nread = reader.read(buf);
				if (nread != -1) {
					sb.append(buf, 0, nread);
				}
				else {
					break;
				}
			}
			return sb.toString();
		}
		finally {
			reader.close();
		}
	}
}
