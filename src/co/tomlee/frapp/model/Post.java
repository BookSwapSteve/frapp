package co.tomlee.frapp.model;

/**
 * Partial model of an app.net post.
 */
public final class Post {
	private final String id;
	private final String author;
	private final String text;
	private final boolean deleted;
	
	public Post(final String id, final String author, final String text, final boolean deleted) {
		this.id = id;
		this.author = author;
		this.text = text;
		this.deleted = deleted;
	}
	
	public String getId() {
		return id;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
}
