package databaseHelperFunctions;

import java.sql.*;
import java.time.LocalDateTime;

import databasePart1.DatabaseHelper;
import qnaClasses.Answer;

public class AddAnswer {
	// Add a new answer to the database
	public static void newAnswer(DatabaseHelper dbHelper, Connection connectionQNA, Answer answer) {
		String addA = "INSERT INTO answers ("
				+ "isRelevant, isOfficial, id, questionID, usersID, likes, dislikes, date_created, answerText) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(addA)) {
			pstmt.setBoolean(1, answer.getRelevant());
			pstmt.setBoolean(2, answer.getOfficial());
			pstmt.setInt(3, answer.getID());
			pstmt.setInt(4, answer.getQuestionID());
			pstmt.setInt(5, answer.getAuthorID());
			pstmt.setInt(6, answer.getLikes());
			pstmt.setInt(7, answer.getDislikes());
			pstmt.setTimestamp(8, Timestamp.valueOf(answer.getDateCreated()));
			pstmt.setString(9, answer.getContent());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Update changes:
	// Updates the answer's text
	public static void updateAnswerText(DatabaseHelper dbHelper, Connection connectionQNA, int questionID, int answerID,
			String answer) {
		String updateAnswer = "UPDATE answers SET answerText = ?, date_creted = ? WHERE id = ? AND question_id";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateAnswer)) {
			pstmt.setString(1, answer);
			pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
			pstmt.setInt(3, answerID);
			pstmt.setInt(4, questionID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Answer updated successfully.");
			} else {
				System.out.println("No Answer found with the provided ID.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Update the answer's likes and dislikes
	public static void updateAnswerLikes(DatabaseHelper dbHelper, Connection connectionQNA, int questionID,
			int answerID, int likes, int dislikes) {
		String updateLikes = "UPDATE answers SET likes = ?, dislikes = ? WHERE id = ? AND question_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateLikes)) {
			pstmt.setInt(1, likes);
			pstmt.setInt(2, dislikes);
			pstmt.setInt(3, answerID);
			pstmt.setInt(4, questionID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Answer likes updated successfully.");
			} else {
				System.out.println("No Answer found with the provided ID.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Update relevance
	public static void updateAnswerRelevant(DatabaseHelper dbHelper, Connection connectionQNA, int questionID,
			int answerID, boolean relevant) {
		String updateRelevence = "UPDATE answers SET isRelevant = ? WHERE id = ? AND question_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateRelevence)) {
			pstmt.setBoolean(1, relevant);
			pstmt.setInt(2, answerID);
			pstmt.setInt(3, questionID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Answer relevence updated successfully.");
			} else {
				System.out.println("No Answer found with the provided ID.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Update Official
	public static void updateAnswerOfficial(DatabaseHelper dbHelper, Connection connectionQNA, int questionID,
			int answerID, boolean official) {
		String updateRelevence = "UPDATE answers SET isOfficial = ? WHERE id = ? AND question_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateRelevence)) {
			pstmt.setBoolean(1, official);
			pstmt.setInt(2, answerID);
			pstmt.setInt(3, questionID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Answer official updated successfully.");
			} else {
				System.out.println("No Answer found with the provided ID.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	// End of Updates

	// Generates ID, looks for first free ID in all answers to a question
	public static int genID(DatabaseHelper dbHelper, Connection connectionQNA, int questionID) {
		String findOpenID = "Select id from answers WHERE question_id = ?";
		int id = -1;

		try (PreparedStatement pstmt = connectionQNA.prepareStatement(findOpenID)) {
			pstmt.setInt(1, questionID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				if (rs.getInt("id") > id) {
					if (rs.getInt("id") > id + 1) {
						return id + 1;
					}
					id = rs.getInt("id");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id + 1;
	}

	// Deletes an answer from the database
	public static boolean deleteAnswer(DatabaseHelper dbHelper, Connection connectionQNA, int answerID,
			int questionID) {
		String delete = "DELETE FROM answers WHERE id = ? AND question_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(delete)) {
			pstmt.setInt(1, answerID);
			pstmt.setInt(2, questionID);
			pstmt.executeUpdate();
			return true; // Return true if successful
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// All replies that responded to this question should also be deleted
		String deleteReplies = "DELETE FROM replies WHERE parent_id = ? AND parentType = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(deleteReplies)) {
			pstmt.setInt(1, answerID);
			pstmt.setString(2, "Answer");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}