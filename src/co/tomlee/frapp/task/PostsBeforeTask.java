package co.tomlee.frapp.task;

import java.util.List;

import co.tomlee.frapp.PostsAdapter;
import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.appnet.Stream;
import co.tomlee.frapp.model.Post;

public final class PostsBeforeTask extends AbstractPostsTask {
	private final AppNetClient client;
	private final String id;
	
	public PostsBeforeTask(final PostsAdapter postsAdapter, final AppNetClient client, final String id) {
		super(postsAdapter);
		this.client = client;
		this.id = id;
	}

	@Override
	protected List<Post> getPosts(final int batchSize) throws AppNetException {
		return client.getPostsBefore(Stream.MY_STREAM, id, batchSize);
	}

	@Override
	protected void addPosts(PostsAdapter postsAdapter, List<Post> posts) {
		if (posts.size() < getBatchSize()) {
			postsAdapter.setEndOfTime(true);
		}
		postsAdapter.addOlderPosts(posts);
	}
}
