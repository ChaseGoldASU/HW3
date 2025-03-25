package qnaClasses;

import java.sql.SQLException;
import java.time.LocalDateTime;

import databasePart1.DatabaseHelper;

public class Reply extends Post {
	// The parent in this case is the object you are replying to
	private String parentType;
	private int parentID;

	// For if we are making a new reply object
	public Reply(int authorID, int parentID, String parentType, String content, DatabaseHelper dbHelper)
			throws SQLException {
		super(authorID, content, "Reply");
		// Parent Types include "Question", "Answer", and "Reply"
		this.parentID = parentID;
		this.parentType = parentType;

		if (dbHelper.isRepliesDatabaseEmpty()) {
			super.id = 0;
		} else {
			super.id = dbHelper.genRID(dbHelper, parentType, parentID);
		}
	}

	// For if we are loading an existing reply from the database
	public Reply(int id, int authorID, int likes, int parentID, String parentType, String content, LocalDateTime date) {
		super(id, authorID, likes, content, date, "Reply");
		this.parentType = parentType;
		this.parentID = parentID;
	}

	public String getParentType() {
		return parentType;
	}

	public int getParentID() {
		return parentID;
	}

	// Debugging method to get all information in a reply
	@Override
	public String toString() {

		return "Question ID:  " + super.id + "\nParent Type:  " + this.parentType + "\nParentID:     " + this.parentID
				+ "\nAuthorID:     " + super.authorID + "\nLikes:        " + super.likes + "\nDate Created: "
				+ super.date.toString() + "\nQuestion:     " + super.content;
	}

	@Override
	public String toString(DatabaseHelper dbHelper) {
		return "ReplyID " + super.id + ", replying to " + this.parentType + " - " + this.parentID + ", by AuthorName "
				+ dbHelper.getUserFromID(dbHelper, super.authorID) + " (ID " + super.authorID + ") " + "on "
				+ super.date.toString() + ": " + super.content;
	}
}