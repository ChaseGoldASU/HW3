package databaseHelperFunctions;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import qnaClasses.Post;
import qnaClasses.PostList;
import qnaClasses.Question;

public class AddQuestion {
	public static void newQuestion(DatabaseHelper dbHelper, Connection connectionQNA, Question question) {
		String addQ = "INSERT INTO questions (" + "isOpen, id, user_id, likes, views, date_created, questionText) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(addQ)) {
			pstmt.setBoolean(1, question.getIsOpen());
			pstmt.setInt(2, question.getID());
			pstmt.setInt(3, question.getAuthorID());
			pstmt.setInt(4, question.getLikes());
			pstmt.setInt(5, question.getViews());
			pstmt.setTimestamp(6, Timestamp.valueOf(question.getDateCreated()));
			pstmt.setString(7, question.getContent());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbHelper.questionCache.put(generateKeyCache(question.getAuthorID(), question.getID()), question);
	}

	// Updates the question text saved to the database
	public static boolean updateQuestionText(DatabaseHelper dbHelper, Connection connectionQNA, int questionID,
			String question, int UserID) {
		String updateQuestion = "UPDATE questions SET questionText = ?, date_created = ? WHERE id = ? AND user_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateQuestion)) {
			pstmt.setString(1, "Edited: " + question);
			pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
			pstmt.setInt(3, questionID);
			pstmt.setInt(4, UserID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void updateQuestionLikes(DatabaseHelper dbHelper, Connection connectionQNA, int questionID, int likes,
			int UserID) {
		String updateLikes = "UPDATE questions SET likes = ? WHERE id = ? AND user_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateLikes)) {
			pstmt.setInt(1, likes);
			pstmt.setInt(2, questionID);
			pstmt.setInt(3, UserID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Question Likes updated successfully.");
			} else {
				System.out.println("No question found with the provided ID.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateQuestionViews(DatabaseHelper dbHelper, Connection connectionQNA, int questionID, int views,
			int UserID) {
		String updateViews = "UPDATE questions SET views = ? WHERE id = ? AND user_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateViews)) {
			pstmt.setInt(1, views);
			pstmt.setInt(2, questionID);
			pstmt.setInt(3, UserID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Question Views updated successfully.");
			} else {
				System.out.println("No question found with the provided ID.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Ruby Added Code
	// Changes question to resolved
	public static boolean closeQuestion(Connection connectionQNA, int userID, int questionID) throws SQLException {
		String query = "UPDATE questions SET resolved = TRUE WHERE id = ? AND user_id = ?";
		boolean returns = false;
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(query)) {
			pstmt.setInt(1, questionID);
			pstmt.setInt(2, userID);
			returns = pstmt.executeUpdate() > 0; // Returns true if the update was successful
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returns;
	}

	// Changes question to unresolved
	public static boolean openQuestion(Connection connectionQNA, int userID, int questionID) {
		String query = "UPDATE questions SET resolved = FALSE WHERE id = ? AND user_id = ?";
		boolean returns = false;
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(query)) {
			pstmt.setInt(1, questionID);
			pstmt.setInt(2, userID);
			returns = pstmt.executeUpdate() > 0; // Returns true if the update was successful
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returns;
	}

	// Gets is the question is resolved or not
	public static boolean isOpen(Connection connectionQNA, int userID, int questionID) {
		String query = "SELECT resolved FROM questions WHERE id = ? AND user_id = ?"; // Make sure the column is 'id'
																						// and not 'question_id'
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(query)) {
			pstmt.setInt(1, questionID);
			pstmt.setInt(2, userID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getBoolean("isOpen"); // Return true if resolved, false otherwise
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // Return false if no record found or query failed
	}

	public static String getQuestionText(Connection connectionQNA, int userID, int questionId) throws SQLException {
		String query = "SELECT questionText FROM questions WHERE id = ? AND user_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			pstmt.setInt(2, userID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("questionText");
			}
		}
		return "Question not found.";
	}

	public static ObservableList<String> getUserQuestions(DatabaseHelper dbHelper, Connection connectionQNA, int userId)
			throws SQLException {
		if (userId <= 0) {
			throw new IllegalArgumentException("Invalid userId: " + userId);
		}

		ObservableList<String> list = FXCollections.observableArrayList();
		String query = "SELECT questionText FROM questions WHERE id = ?";

		try (PreparedStatement pstmt = connectionQNA.prepareStatement(query)) {
			pstmt.setInt(1, userId); // Set the userId parameter

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String text = rs.getString("questionText");
				String username = dbHelper.getUserFromID(dbHelper, userId).getUserName();
				list.add("Question " + userId + " by " + username + ": " + text);
			}
		}
		return list;
	}

	public static int genID(DatabaseHelper dbHelper, Connection connectionQNA, int UserID) {
		String findOpenID = "Select id from questions WHERE UserID = ?";
		int id = -1;

		try (PreparedStatement pstmt = connectionQNA.prepareStatement(findOpenID)) {
			pstmt.setInt(1, UserID);
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

	public static boolean deleteQuestion(DatabaseHelper dbHelper, Connection connectionQNA, int questionID,
			int userID) {
		String delete = "DELETE FROM questions WHERE id = ? AND user_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(delete)) {
			pstmt.setInt(1, questionID);
			pstmt.setInt(2, userID);
			pstmt.executeUpdate();
			return true; // Return true if successful
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// All answers that responded to this question should also be deleted
		String deleteAnswers = "DELETE FROM answers WHERE question_id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(deleteAnswers)) {
			pstmt.setInt(1, questionID);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// All replies that responded to this question should also be deleted
		String deleteReplies = "DELETE FROM replies WHERE parent_id = ? AND parentType = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(deleteReplies)) {
			pstmt.setInt(1, questionID);
			pstmt.setString(2, "Question");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String generateKeyCache(int userID, int questionID) {
		return userID + ":" + questionID;
	}
}