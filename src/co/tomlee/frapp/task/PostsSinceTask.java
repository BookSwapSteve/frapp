package co.tomlee.frapp.task;

import java.util.Collections;
import java.util.List;

import co.tomlee.frapp.PostsAdapter;
import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.appnet.Stream;
import co.tomlee.frapp.model.Post;

public class PostsSinceTask extends AbstractPostsTask {
	private final AppNetClient client;
	private final String id;
	
	public PostsSinceTask(final PostsAdapter postAdapter, final AppNetClient client, final String id) {
		super(postAdapter);
		this.client = client;
		this.id = id;
	}

	@Override
	protected List<Post> getPosts(final int batchSize) throws AppNetException {
		return client.getPostsSince(Stream.MY_STREAM, id, batchSize);
	}
	
	@Override
	protected void addPosts(final PostsAdapter postsAdapter, final List<Post> posts) {
		Collections.reverse(posts);
		postsAdapter.addNewerPosts(posts);
	}
}
