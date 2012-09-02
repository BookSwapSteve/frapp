package co.tomlee.frapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import co.tomlee.frapp.model.Post;

/**
 * This is the work horse of the application. Responsible for browsing the user's timeline,
 * handling input events relating to adding posts & replying to others, spinning up a PollThread,
 * etc. etc. etc.
 */
public class PostsActivity extends Activity implements OnScrollListener, OnMenuItemClickListener, OnItemClickListener, OnItemLongClickListener {
	// private static final String TAG = "PostsActivity";

	/**
	 * OAuth client ID from the app.net developer profile.
	 */
	private static final String OAUTH_CLIENT_ID = "<app-client-id>";
	
	/**
	 * OAuth redirect URI used to receive our access_token.
	 */
	private static final String OAUTH_REDIRECT_URI = "frapp://register";
	
	private String accessToken;
	private ListView postsListView;
	private PostsAdapter postAdapter;
	private PollThread pollThread;

	/**
	 * The maximum number of posts we'll load into memory.
	 * 
	 * XXX doesn't currently take posts added by the PollThread into account...
	 */
	private static final int MAX_POSTS = 300;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);
        
        postAdapter = new PostsAdapter(this, R.layout.postlistitem, R.layout.waitlistitem);
        postsListView = (ListView) findViewById(R.id.postsListView);
        postsListView.setAdapter(postAdapter);
        postsListView.setOnScrollListener(this);
        postsListView.setOnItemClickListener(this);
        postsListView.setOnItemLongClickListener(this);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_posts, menu);
        final MenuItem newPostItem = menu.findItem(R.id.new_post);
        newPostItem.setOnMenuItemClickListener(this);
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        final SharedPreferences prefs = getSharedPreferences(Pref.FILENAME, 0);
        accessToken = prefs.getString(Pref.PREF_ACCESS_TOKEN, null);
        
        if (accessToken != null) {
        	// Log.v(TAG, "Loading initial posts");
            new MorePostsTask(MorePostsTask.Type.BEFORE, postAdapter, true, accessToken).execute();
            
	        // Log.v(TAG, "Starting poll thread");
	        pollThread = new PollThread(this, postAdapter, accessToken);
	        pollThread.setActive(true);
	        pollThread.start();
        }
        else {
        	requestAuthorization();
        }
        
    	super.onResume();
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
    	postAdapter.setPosts(new ArrayList<Post>());
    	postAdapter.notifyDataSetChanged();
    	
    	if (pollThread != null) {
	    	// Log.v(TAG, "Asking poll thread to terminate");
	    	pollThread.setActive(false);
	    	pollThread.interrupt();
	    	try {
	    		pollThread.join(2000);
	    		// Log.v(TAG, "Poll thread stopped");
	    	}
	    	catch (InterruptedException e) {
	    		// Log.e(TAG, "Interrupted waiting for PollThread to finish");
	    	}
	    	finally {
	    		pollThread = null;
	    	}
    	}
    	
    	accessToken = null;
    	
    	super.onPause();
    }

    /*
     * (non-Javadoc)
     * @see android.view.MenuItem.OnMenuItemClickListener#onMenuItemClick(android.view.MenuItem)
     */
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item.getItemId() == R.id.new_post) {
			showPostEditor();
			
			return true;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		final boolean needMore = (firstVisibleItem + visibleItemCount >= totalItemCount) &&
									!postAdapter.getEndOfTime() &&
									postAdapter.getPosts().size() < MAX_POSTS;
		
		if (!postAdapter.isLoading() && needMore) {
			final MorePostsTask task = new MorePostsTask(MorePostsTask.Type.BEFORE, postAdapter, accessToken);
			task.execute();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {}
	
	/**
	 * Ask the user to authorize access to their data on the app.net web site.
	 */
    private void requestAuthorization() {
    	final String urlFormat =
    		"https://alpha.app.net/oauth/authenticate?client_id={0}&response_type=token" +
    				"&redirect_uri={1}&scope=stream%2cwrite_post%2cfollow%2cmessages";
    	try {
	    	final String url = MessageFormat.format(
	    		urlFormat, OAUTH_CLIENT_ID, URLEncoder.encode(OAUTH_REDIRECT_URI, "utf-8"));
	    	final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	    	startActivity(intent);
    	}
    	catch (UnsupportedEncodingException e) {
    		//
    		// Should never happen ...
    		//
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Shows the 'New Post' editor.
     */
	private void showPostEditor() {
		showPostEditor(null);
	}
	
	/**
	 * Shows the 'Reply to ...' editor.
	 * @param post
	 */
	private void showPostEditor(final Post post) {
		final EditText view = new EditText(this);
		view.setSingleLine(false);
		view.setLines(4);
		view.setMinLines(4);
		view.setMaxLines(4);
		
		new AlertDialog.Builder(this)
			.setTitle(post == null ? "New Post" : "Reply to @" + post.getAuthor())
			.setView(view)
			.setMessage(post == null ? null : post.getText())
			.setPositiveButton("Post", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					new AddPostTask(post != null ? post.getId() : null, postAdapter, accessToken).execute(view.getText().toString());
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
	}
	
	/**
	 * Displays the per-Post context menu. Typically triggered by a long click on a post.
	 */
	private void showPostContextMenu(final Post post) {
		final ArrayAdapter<PostAction> actions = new ArrayAdapter<PostAction>(this, android.R.layout.simple_list_item_1);
		for (PostActionType actionType : PostActionType.values()) {
			if (actionType.appearsInContextMenuFor(post)) {
				actions.add(new PostAction(actionType, post));
			}
		}
		new AlertDialog.Builder(this)
			.setAdapter(actions, new OnLongClickPostMenuListener(actions))
			.show();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> view, View parent, int pos, long id) {
		if (pos < postAdapter.getPosts().size()) {
			final Post post = postAdapter.getPosts().get(pos);
			showPostEditor(post);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> view, View parent, int pos, long id) {
		//
		// We may have one extra item in the PostAdapter for
		// our "loading" item, so check that `pos` is valid.
		//
		if (pos < postAdapter.getPosts().size()) {
			final Post post = postAdapter.getPosts().get(pos);
			showPostContextMenu(post);
			return true;
		}
		return false;
	}
	
	/**
	 * Handle item selection in the post context menu.
	 */
	private final class OnLongClickPostMenuListener implements OnClickListener {
		private final ArrayAdapter<PostAction> actions;
		
		public OnLongClickPostMenuListener(final ArrayAdapter<PostAction> actions) {
			this.actions = actions;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int pos) {
			final PostAction action = actions.getItem(pos);
			action.activate(dialog);
		}
	}
	
	/**
	 * Determine how to respond to a particular action wrt the post context menu.
	 */
	private class PostAction {
		private final PostActionType type;
		private final Post post;
		
		public PostAction(final PostActionType type, final Post post) {
			this.type = type;
			this.post = post;
		}
		
		public void activate(final DialogInterface dialog) {
			if (type == PostActionType.REPLY) {
				dialog.dismiss();
				showPostEditor(post);
			}
		}
		
		@Override
		public String toString() {
			return MessageFormat.format(type.getMessageFormat(), post.getAuthor());
		}
	}
	
	/**
	 * The types of actions that can be performed on a post.
	 */
	private enum PostActionType {
		REPLY("Reply to @{0}"),
		VIEW_PROFILE("View @{0}'s Profile [TODO]"),
		DELETE("Delete this Post [TODO]", new DeleteActionPredicate());
		
		private final String messageFormat;
		private final Predicate predicate;
		
		private PostActionType(final String messageFormat) {
			this(messageFormat, null);
		}
		
		private PostActionType(final String messageFormat, final Predicate predicate) {
			this.messageFormat = messageFormat;
			this.predicate = predicate;
		}
		
		public String getMessageFormat() {
			return messageFormat;
		}
		
		public boolean appearsInContextMenuFor(final Post post) {
			if (predicate != null) {
				return predicate.check(post);
			}
			else {
				return true;
			}
		}
		
		private static interface Predicate {
			boolean check(final Post post);
		}
		
		private static final class DeleteActionPredicate implements Predicate {
			@Override
			public boolean check(Post post) {
				//
				// TODO is `post` authored by the current user?
				//
				return false;
			}
		}
	}
}
