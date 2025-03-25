package qnaClasses;

import java.time.LocalDateTime;

import databasePart1.DatabaseHelper;

public abstract class Post {
	protected int id;
	protected int authorID;
	protected int likes;
	protected String content;
	protected LocalDateTime date;
	protected String type;

	// For if we are making a new post
	public Post(int authorID, String content, String type) {
		this.id = -1;
		this.authorID = authorID;
		this.likes = 0;
		this.content = content;
		this.date = LocalDateTime.now();
		this.type = type;
	}

	// For if we are loading a post from the database
	public Post(int id, int authorID, int likes, String content, LocalDateTime date, String type) {
		this.id = id;
		this.authorID = authorID;
		this.likes = likes;
		this.content = content;
		this.date = date;
		this.type = type;
	}

	// id callers
	public int getID() {
		return id;
	}

	public int getAuthorID() {
		return authorID;
	}

	// likes callers
	public int getLikes() {
		return likes;
	}

	public void incrementLikes() {
		likes++;
	}

	// content callers
	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	// date created callers
	public LocalDateTime getDateCreated() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	// type caller
	public String getType() {
		return this.type;
	}

	public void setType() {
		// Fill in later with a method to change a data's type
	}

	@Override
	public String toString() {
		return "Post ID:   " + id + "\nPost Type: " + this.type + "\nAuthorID:     " + this.authorID
				+ "\nLikes:        " + this.likes + "\nDate Created: " + this.date.toString() + "\nContent:     "
				+ this.content;
	}

	// Display Text for the menus
	public String toString(DatabaseHelper dbHelper) {
		return "PostID " + this.id + "by AuthorName " + dbHelper.getUserFromID(dbHelper, this.authorID) + ", "
				+ this.authorID + "on " + this.date.toString() + ": " + this.content;
	}

}