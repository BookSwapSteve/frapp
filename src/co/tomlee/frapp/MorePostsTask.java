package co.tomlee.frapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.model.Post;

import android.os.AsyncTask;

/**
 * AsyncTask to load more posts into the UI.
 * 
 * This might be executed as part of a periodic update (i.e. recent posts),
 * or the user scrolling down to a point where we need to request more posts. 
 */
class MorePostsTask extends AsyncTask<String, Void, List<Post>> {
	/**
	 * Each time this task is executed, we will return this many posts at most.
	 */
	private static final int POSTS_PER_REQUEST = 30;
	
	private final Type type;
	private final PostsAdapter postAdapter;
	private final boolean forceRequest;
	private String id;
	private final String accessToken;
	
	/**
	 * Optionally force this request even in the event we don't have a valid before or after ID.
	 * 
	 * XXX This should probably just be a separate MorePostsTask.Type.
	 * 
	 * @param type
	 * @param postAdapter
	 * @param forceRequest
	 */
	public MorePostsTask(final Type type, final PostsAdapter postAdapter, final boolean forceRequest, final String accessToken) {
		this.type = type;
		this.postAdapter = postAdapter;
		this.forceRequest = forceRequest;
		this.accessToken = accessToken;
	}
	
	/**
	 * By default, we won't force requests.
	 * 
	 * @param type
	 * @param postAdapter
	 */
	public MorePostsTask(final Type type, final PostsAdapter postAdapter, final String accessToken) {
		this(type, postAdapter, false, accessToken);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		postAdapter.setLoading(true);
		postAdapter.notifyDataSetChanged();
		
		final List<Post> posts = postAdapter.getPosts();
		final int index = (type == Type.BEFORE) ? posts.size()-1 : 0;
		if (posts.size() > 0) {
			id = posts.get(index).getId();
		}
		else {
			id = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected List<Post> doInBackground(String... params) {
		if (id != null || forceRequest) {
			final AppNetClient client = new AppNetClient(accessToken);
			try {
				if (type == Type.BEFORE) {
					return client.getPostsBefore(id, POSTS_PER_REQUEST);
				}
				else {
					return client.getPostsSince(id, POSTS_PER_REQUEST);
				}
			}
			catch (AppNetException e) {
				return new ArrayList<Post>();
			}
		}
		return new ArrayList<Post>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(List<Post> newPosts) {
		if (type == Type.BEFORE && newPosts.size() < POSTS_PER_REQUEST) {
			postAdapter.setEndOfTime(true);
		}
		if (newPosts.size() > 0) {
			ArrayList<Post> posts = new ArrayList<Post>(postAdapter.getPosts());
			
			if (type == Type.BEFORE) { 
				for (Post post : newPosts) {
					if (!post.isDeleted()) {
						posts.add(post);
					}
				}
			}
			else {
				Collections.reverse(newPosts);
				for (Post post : newPosts) {
					if (!post.isDeleted()) {
						posts.add(0, post);
					}
				}
			}
			
			postAdapter.setPosts(posts);
		}
		postAdapter.setLoading(false);
		postAdapter.notifyDataSetChanged();
	}
	
	/**
	 * before_id / since_id
	 */
	public static enum Type {
		BEFORE,
		SINCE;
	}
}
