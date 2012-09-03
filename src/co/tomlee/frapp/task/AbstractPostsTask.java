package co.tomlee.frapp.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import co.tomlee.frapp.PostsAdapter;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.appnet.Stream;
import co.tomlee.frapp.model.Post;

/**
 * AsyncTask to load more posts into the UI.
 * 
 * This might be executed as part of a periodic update (i.e. recent posts),
 * or the user scrolling down to a point where we need to request more posts. 
 */
abstract class AbstractPostsTask extends AsyncTask<String, Void, Map<Stream, List<Post>>> {
	/**
	 * Each time this task is executed, we will return this many posts at most.
	 */
	private static final int DEFAULT_BATCH_SIZE = 30;
	
	private final Map<Stream, PostsAdapter> streamAdapters;
	
	private int batchSize = DEFAULT_BATCH_SIZE;
	
	public AbstractPostsTask(final Map<Stream, PostsAdapter> streamAdapters) {
		this.streamAdapters = streamAdapters;
	}
	
	public final void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}
	
	public final int getBatchSize() {
		return batchSize;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected final void onPreExecute() {
		super.onPreExecute();
		
		for (Map.Entry<Stream, PostsAdapter> entry : streamAdapters.entrySet()) {
			entry.getValue().setLoading(true);
			entry.getValue().notifyDataSetChanged();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected final Map<Stream, List<Post>> doInBackground(String... params) {
		try {
			final HashMap<Stream, List<Post>> map = new HashMap<Stream, List<Post>>();
			for (Map.Entry<Stream, PostsAdapter> entry : streamAdapters.entrySet()) {
				map.put(entry.getKey(), getPosts(entry.getKey(), batchSize));
			}
			return map;
		}
		catch (AppNetException e) {
			return new HashMap<Stream, List<Post>>();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected final void onPostExecute(Map<Stream, List<Post>> newPosts) {
		for (Map.Entry<Stream, PostsAdapter> entry : streamAdapters.entrySet()) {
			final List<Post> posts = newPosts.get(entry.getKey());
			if (posts != null && posts.size() > 0) {
				addPosts(entry.getValue(), posts);
			}
			entry.getValue().setLoading(false);
			entry.getValue().notifyDataSetChanged();
		}
	}
	
	protected abstract List<Post> getPosts(final Stream stream, final int batchSize) throws AppNetException;
	protected abstract void addPosts(final PostsAdapter postsAdapter, final List<Post> posts);
}
