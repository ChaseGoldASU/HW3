package qnaClasses;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;

public class Question extends Post {
	private boolean isOpen;
	private int views;
	private List<Post> answerList = new ArrayList<>();

	// Basic new question creation
	public Question(int authorID, String content, DatabaseHelper dbHelper) throws SQLException {
		super(authorID, content, "Question");
		this.isOpen = true;
		this.views = 0;

		if (dbHelper.isQuestionsDatabaseEmpty()) {
			super.id = 0;
		} else {
			super.id = dbHelper.genQID(dbHelper, authorID);
		}
	}

	// If we are loading a question from the database
	public Question(boolean isOpen, int id, int authorID, int likes, int views, String content, LocalDateTime date) {
		super(id, authorID, likes, content, date, "Question");
		this.isOpen = isOpen;
		this.views = views;
	}

	// views functions
	public int getViews() {
		return views;
	}

	public void incrementViews() {
		views++;
	}

	// AnswersList Functions
	public void addPost(Post post) {
		answerList.add(post);
	}

	public List<Post> getAnswersList() {
		return answerList;
	}
	// Loads all answers and questions replying to this post

	public Boolean getIsOpen() {
		return isOpen;
	}

	// Debugging method to get all information in a question
	@Override
	public String toString() {
		return "Question ID:  " + super.id + "\nIs Open?:     " + this.isOpen + "\nAuthorID:     " + super.authorID
				+ "\nLikes:        " + super.likes + "\nViews:        " + this.views + "\nDate Created: "
				+ super.date.toString() + "\nQuestion:     " + super.content;
	}

	@Override
	public String toString(DatabaseHelper dbHelper) {
		return "QuestionID " + super.id + "by AuthorName " + dbHelper.getUserFromID(dbHelper, this.authorID) + ", "
				+ super.authorID + "on " + super.date.toString() + ": " + super.content;
	}
}