package co.tomlee.frapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import co.tomlee.frapp.model.Post;

/**
 * ListView adapter with some smarts for displaying a spinner icon when busy.
 * 
 * XXX could do with a clean up.
 */
public final class PostsAdapter extends BaseAdapter {
	private final Context context;
	private final int itemLayoutId;
	private final int loadingLayoutId;
	private boolean endOfTime;
	private List<Post> posts = new ArrayList<Post>();
	
	private boolean loading;
	
	/**
	 * 
	 * @param context
	 * @param itemLayoutId
	 * @param loadingLayoutId
	 */
	public PostsAdapter(Context context, int itemLayoutId, int loadingLayoutId) {
		this.context = context;
		this.itemLayoutId = itemLayoutId;
		this.loadingLayoutId = loadingLayoutId;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		//
		// + 1 for the "loading" item that's always visible.
		//
		return posts.size() + 1;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		if (position < posts.size()) {
			return posts.get(position);
		}
		else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * Determine whether this instance has been marked as loading (i.e.
	 * we're in the middle of some sort of operation to modify the posts).
	 */
	public boolean isLoading() {
		return loading;
	}
	
	/**
	 * Indicate an operation is in progress that will affect the contents
	 * of the adapter.
	 */
	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	/**
	 * Have we seen the end of a user's timeline?
	 */
	public void setEndOfTime(boolean endOfTime) {
		this.endOfTime = endOfTime;
	}
	
	/**
	 * Have we seen the end of a user's timeline?
	 * 
	 * Useful to ensure we don't continually ask for more posts when
	 * we hit the end of a user's timeline.
	 */	
	public boolean getEndOfTime() {
		return endOfTime;
	}
	
	/**
	 * Get the set of posts.
	 */
	public List<Post> getPosts() {
		return posts;
	}
	
	/**
	 * Set posts.
	 */
	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}
	
	/**
	 * Add all the posts in the given collection at the start of the adapter.
	 */
	public void addNewerPosts(final Collection<Post> posts) {
		this.posts.addAll(0, posts);
	}
	
	/**
	 * Add all the posts in the given collection at the end of the adapter.
	 */
	public void addOlderPosts(final Collection<Post> posts) {
		this.posts.addAll(posts);
	}
	
	/**
	 * Get the ID of the newest post in this adapter.
	 */
	public String getNewestPostId() {
		if (posts.size() > 0) {
			return posts.get(0).getId();
		}
		return null;
	}
	
	/**
	 * Get the ID of the oldest post in this adapter.
	 */
	public String getOldestPostId() {
		if (posts.size() > 0) {
			return posts.get(posts.size()-1).getId();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		PostTag postTag = null;
		
		if (row == null) {
			final LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			if (position < posts.size()) {
				row = inflater.inflate(itemLayoutId, parent, false);
				
				postTag = new PostTag();
				postTag.textView = (TextView) row.findViewById(R.id.text);
				postTag.authorView = (TextView) row.findViewById(R.id.author);
				// postTag.imageView = (ImageView) row.findViewById(R.id.avatar);
				row.setTag(postTag);
			}
			else {
				//
				// TODO load this once, up front?
				//
				row = inflater.inflate(loadingLayoutId, parent, false);
			}
		}
		else {
			if (position < posts.size()) {
				postTag = (PostTag) row.getTag();
			}
		}
		
		if (postTag != null) {
			final Post post = posts.get(position);
			postTag.authorView.setText(post.getAuthor());
			postTag.textView.setText(post.getText());
		}
		
		return row;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.BaseAdapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		if (position < posts.size()) {
			return 0;
		}
		else {
			return 1;
		}
	}
	
	/**
	 * Handy for storing per-item data.
	 */
	private static final class PostTag {
		public TextView textView;
		public TextView authorView;
		// public ImageView imageView;
	}
}
