package co.tomlee.frapp;

import android.app.Activity;

/**
 * Periodically polls the app.net servers for new posts.
 */
class PollThread extends Thread {
	private final Activity activity;
	private final PostsAdapter postAdapter;
	private final String accessToken;
	private boolean active;
	
	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60 * ONE_SECOND;
	private static final int FIVE_MINUTES = 5 * ONE_MINUTE;
	
	/**
	 * Create the PollThread.
	 * 
	 * @param activity
	 * @param adapter
	 */
	public PollThread(final Activity activity, final PostsAdapter adapter, final String accessToken) {
		super("Frapp Poll Thread");
		this.activity = activity;
		this.postAdapter = adapter;
		this.accessToken = accessToken;
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
	public void start() {
		setActive(true);
		super.start();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
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
			
			try {
				Thread.sleep(FIVE_MINUTES);
			}
			catch (InterruptedException e) {
				//
				// TODO resume sleeping for remaining time if still active.
				//
			}
			
			//
			// If we were interrupted, we've probably been stopped.
			//
			// Don't ask for more posts if so.
			//
			boolean active;
			synchronized (this) {
				active = this.active;
			}
			if (active) {
				activity.runOnUiThread(r);
			}
		}
	}
	
	/**
	 * Executes a MorePostsTask on the main UI thread.
	 */
	private final class TaskRunnable implements Runnable {
		private MorePostsTask task = create();
		
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
		private MorePostsTask create() {
			return new MorePostsTask(MorePostsTask.Type.SINCE, postAdapter, accessToken);
		}
	}
}
