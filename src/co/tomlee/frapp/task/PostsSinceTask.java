package co.tomlee.frapp.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.tomlee.frapp.PostsAdapter;
import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.appnet.Stream;
import co.tomlee.frapp.model.Post;

public class PostsSinceTask extends AbstractPostsTask {
	private final AppNetClient client;
	private final String id;
	
	public PostsSinceTask(final Stream stream, final PostsAdapter postsAdapter, final AppNetClient client, final String id) {
		this(new HashMap<Stream, PostsAdapter>() {{ put(stream, postsAdapter); }}, client, id);
	}
	
	public PostsSinceTask(final Map<Stream, PostsAdapter> streamAdapters, final AppNetClient client, final String id) {
		super(streamAdapters);
		this.client = client;
		this.id = id;
	}

	@Override
	protected List<Post> getPosts(final Stream stream, final int batchSize) throws AppNetException {
			return client.getPostsSince(stream, id, batchSize);
	}
	
	@Override
	protected void addPosts(final PostsAdapter postsAdapter, final List<Post> posts) {
		postsAdapter.addNewerPosts(posts);
	}
}
