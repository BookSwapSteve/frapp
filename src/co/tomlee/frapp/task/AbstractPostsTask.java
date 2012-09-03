package co.tomlee.frapp.task;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import co.tomlee.frapp.PostsAdapter;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.model.Post;

/**
 * AsyncTask to load more posts into the UI.
 * 
 * This might be executed as part of a periodic update (i.e. recent posts),
 * or the user scrolling down to a point where we need to request more posts. 
 */
abstract class AbstractPostsTask extends AsyncTask<String, Void, List<Post>> {
	/**
	 * Each time this task is executed, we will return this many posts at most.
	 */
	private static final int DEFAULT_BATCH_SIZE = 30;
	
	private final PostsAdapter postsAdapter;
	
	private int batchSize = DEFAULT_BATCH_SIZE;
	
	public AbstractPostsTask(final PostsAdapter postsAdapter) {
		this.postsAdapter = postsAdapter;
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
		
		postsAdapter.setLoading(true);
		postsAdapter.notifyDataSetChanged();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected final List<Post> doInBackground(String... params) {
		try {
			return getPosts(batchSize);
		}
		catch (AppNetException e) {
			return new ArrayList<Post>();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected final void onPostExecute(List<Post> newPosts) {
		if (newPosts.size() > 0) {
			addPosts(postsAdapter, newPosts);
		}
		postsAdapter.setLoading(false);
		postsAdapter.notifyDataSetChanged();
	}
	
	protected abstract List<Post> getPosts(final int batchSize) throws AppNetException;
	protected abstract void addPosts(final PostsAdapter postsAdapter, final List<Post> posts);
}
