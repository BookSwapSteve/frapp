package co.tomlee.frapp;

import android.os.AsyncTask;
import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.AppNetException;
import co.tomlee.frapp.model.Post;

/**
 * AsyncTask that will add a post to the current user's stream.
 * 
 * Invoked to add a new post or reply.
 */
class AddPostTask extends AsyncTask<String, Void, Post> {
	private final String replyTo;
	private final PostsAdapter postAdapter;
	private final String accessToken;
	
	/**
	 * Constructor for new posts.
	 * 
	 * @param postAdapter
	 * @param accessToken
	 */
	public AddPostTask(final PostsAdapter postAdapter, final String accessToken) {
		this(null, postAdapter, accessToken);
	}
	
	/**
	 * Constructor for replies.
	 * 
	 * @param replyTo
	 * @param postAdapter
	 */
	public AddPostTask(final String replyTo, final PostsAdapter postAdapter, final String accessToken) {
		this.replyTo = replyTo;
		this.postAdapter = postAdapter;
		this.accessToken = accessToken;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		postAdapter.setLoading(true);
		
		// TODO show 'please wait'?
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Post doInBackground(String... params) {
		try {
			return (new AppNetClient(accessToken)).addPost(replyTo, params[0]);
		}
		catch (AppNetException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Post post) {
		if (post != null) {
			postAdapter.getPosts().add(0, post);
			postAdapter.notifyDataSetChanged();
		}
		super.onPostExecute(post);
	}
}
