package co.tomlee.frapp.task;

import java.util.List;

import co.tomlee.frapp.PostsAdapter;
import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.appnet.Stream;
import co.tomlee.frapp.model.Post;

public final class RecentPostsTask extends AbstractPostsTask {
	private final AppNetClient client;
	
	public RecentPostsTask(final PostsAdapter postsAdapter, final AppNetClient client) {
		super(postsAdapter);
		this.client = client;
	}

	@Override
	protected List<Post> getPosts(final int batchSize) throws AppNetException {
		return client.getRecentPosts(Stream.MY_STREAM, batchSize);
	}

	@Override
	protected void addPosts(final PostsAdapter postsAdapter, final List<Post> posts) {
		postsAdapter.addNewerPosts(posts);
	}
}
