package co.tomlee.frapp.task;

import java.util.List;
import java.util.Map;

import co.tomlee.frapp.PostsAdapter;
import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.appnet.Stream;
import co.tomlee.frapp.model.Post;

public final class RecentPostsTask extends AbstractPostsTask {
	private final AppNetClient client;
	
	public RecentPostsTask(final Map<Stream, PostsAdapter> streamAdapters, final AppNetClient client) {
		super(streamAdapters);
		this.client = client;
	}

	@Override
	protected List<Post> getPosts(final Stream stream, final int batchSize) throws AppNetException {
		return client.getRecentPosts(stream, batchSize);
	}

	@Override
	protected void addPosts(final PostsAdapter postsAdapter, final List<Post> posts) {
		postsAdapter.addNewerPosts(posts);
	}
}
