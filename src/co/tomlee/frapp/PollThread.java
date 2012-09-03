package co.tomlee.frapp;

import co.tomlee.frapp.appnet.AppNetClient;
import co.tomlee.frapp.appnet.Stream;
import co.tomlee.frapp.task.PostsSinceTask;
import android.app.Activity;

/**
 * Periodically polls the app.net servers for new posts.
 */
class PollThread extends Thread {
	private final Activity activity;
	private final Stream stream;
	private final PostsAdapter postsAdapter;
	private final AppNetClient client;
	private boolean active;
	
	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60 * ONE_SECOND;
	private static final int FIVE_MINUTES = 5 * ONE_MINUTE;
	
	/**
	 * Create the PollThread.
	 * 
	 * @param activity
	 * @param postsAdapter
	 */
	public PollThread(final Activity activity, final Stream stream, final PostsAdapter postsAdapter, final AppNetClient client) {
		super("Frapp Poll Thread");
		this.activity = activity;
		this.stream = stream;
		this.postsAdapter = postsAdapter;
		this.client = client;
	}
	
	/**
	 * Set to false to ask the thread to terminate.
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		synchronized (this) {
			this.active = active;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#start()
	 */
	@Override
	public void start() {
		setActive(true);
		super.start();
	}
	
	@Override
	public void interrupt() {
		setActive(false);
		super.interrupt();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		final TaskRunnable r = new TaskRunnable();
		
		for (;;) {
			//
			// If we've been asked to stop, comply.
			//
			synchronized (this) {
				if (!active) {
					r.cancel();
					break;
				}
			}
			
			activity.runOnUiThread(r);
			
			try {
				Thread.sleep(FIVE_MINUTES);
			}
			catch (InterruptedException e) {
				//
				// TODO resume sleeping for remaining time if still active.
				//
			}
		}
	}
	
	/**
	 * Executes a MorePostsTask on the main UI thread.
	 */
	private final class TaskRunnable implements Runnable {
		private PostsSinceTask task = create();
		
		/**
		 * Ask the task to cancel.
		 */
		public void cancel() {
			task.cancel(true);
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			task.cancel(true);
			task = create();
			task.execute();
		}
		
		/**
		 * Create a new MorePostsTask with the appropriate parameters to
		 * pull posts we haven't seen yet.
		 */
		private PostsSinceTask create() {
			return new PostsSinceTask(stream, postsAdapter, client, postsAdapter.getNewestPostId());
		}
	}
}
