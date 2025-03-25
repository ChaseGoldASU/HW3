package qnaClasses;

import java.sql.SQLException;
import java.time.LocalDateTime;

import databasePart1.DatabaseHelper;

public class Answer extends Post {
	private boolean isRelevant;
	private boolean isOfficial;
	private int dislikes;
	private int questionID;

	// For if we are making a new answer
	public Answer(int authorID, int questionID, String content, DatabaseHelper dbHelper) throws SQLException {
		super(authorID, content, "Answer");
		this.isRelevant = true;
		this.isOfficial = false;
		this.dislikes = 0;
		this.questionID = questionID;

		if (dbHelper.isAnswersDatabaseEmpty()) {
			super.id = 0;
		} else {
			super.id = dbHelper.genAID(dbHelper, authorID, questionID);
		}
	}

	// For if we are loading an answer from the database
	public Answer(boolean isRelevant, boolean isOfficial, int id, int questionID, int authorID, int likes, int dislikes,
			String content, LocalDateTime timestamp) {
		super(id, authorID, likes, content, timestamp, "Answer");
		this.isRelevant = isRelevant;
		this.isOfficial = isOfficial;
		this.dislikes = dislikes;
		this.questionID = questionID;
	}

	// Dislikes Modifier
	public int getDislikes() {
		return dislikes;
	}

	public void incrementDislikes() {
		dislikes++;
	}

	// QuestionID modifier
	public int getQuestionID() {
		return questionID;
	}

	// isRelevant Modifier
	public void changeRelevant(boolean isRelevant) {
		this.isRelevant = isRelevant;
	}

	public boolean getRelevant() {
		return isRelevant;
	}

	// isOfficial Modifier
	public void changeOfficial(boolean isOfficial) {
		this.isOfficial = isOfficial;
	}

	public boolean getOfficial() {
		return isOfficial;
	}
	// Official

	// Debugging method to get all information in an answer
	@Override
	public String toString() {
		return "Answer ID:    " + super.id + "\nQuestion ID   " + this.questionID + "\nIs Relevant?: " + this.isRelevant
				+ "\nIs Official?: " + this.isOfficial + "\nAuthorID:     " + super.authorID + "\nLikes:        "
				+ super.likes + "\nDislikes:     " + this.dislikes + "\nDate Created: " + super.date.toString()
				+ "\nAnswer:       " + super.content;
	}

	@Override
	public String toString(DatabaseHelper dbHelper) {
		return "QuestionID " + super.id + ", replying to " + this.questionID + ", by AuthorName "
				+ dbHelper.getUserFromID(dbHelper, super.authorID) + " (ID " + super.authorID + ") " + "on "
				+ super.date.toString() + ": " + super.content;
	}

}