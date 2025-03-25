package databaseHelperFunctions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AddMessage {
	// Method for adding a message to the database
	public static void addMessage(Connection connectionDM, int senderID, int receiverID, int questionId, String message)
			throws SQLException {
		String query = "INSERT INTO messages (user_id_sender, user_id_receiver, questionId, message, date_created) "
				+ "VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connectionDM.prepareStatement(query)) {
			pstmt.setInt(1, senderID);
			pstmt.setInt(2, receiverID);
			pstmt.setInt(3, questionId);
			pstmt.setString(4, message);
			pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
			pstmt.executeUpdate();
		}
	}

	public static ObservableList<String> getMessages(Connection connectionDM, Connection connectionUser, int senderID,
			int receiverID) throws SQLException {
		ObservableList<String> list = FXCollections.observableArrayList();
		String query = "SELECT user_id_sender, user_id_reciever, message FROM message"
				+ "WHERE (user_id_sender = ? AND user_id_receiver = ?)"
				+ "OR (user_id_sender = ? AND user_id_receiver = ?)";

		try (PreparedStatement pstmt = connectionDM.prepareStatement(query)) {
			pstmt.setInt(1, senderID);
			pstmt.setInt(2, receiverID);
			pstmt.setInt(3, receiverID);
			pstmt.setInt(4, senderID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int sender = rs.getInt("user_id_sender");
				String message = rs.getString("message");

				// Fetch the sender's username
				String userQuery = "SELECT userName FROM cse360users WHERE id = ?";
				try (PreparedStatement ustmt = connectionUser.prepareStatement(userQuery)) {
					ustmt.setInt(1, sender);
					ResultSet rsu = ustmt.executeQuery();
					if (rsu.next()) {
						list.add(rsu.getString("userName") + ": " + message);
					} else {
						list.add("[Unknown]: " + message);
					}
				}
			}
		}
		return list;
	}

	public static ObservableList<String> getAllChatMessages(Connection connectionDM, Connection connectionUser,
			int userId, int receiverId, int qId) throws SQLException {
		if (userId <= 0 || receiverId <= 0 || qId <= 0) {
			throw new IllegalArgumentException(
					"Invalid parameters: userId=" + userId + ", receiverId=" + receiverId + ", qId=" + qId);
		}

		ObservableList<String> list = FXCollections.observableArrayList();
		String query = "SELECT id, message, user_id_sender FROM messages "
				+ "WHERE (user_id_sender = ? AND user_id_receiver = ? AND questionId = ?) "
				+ "   OR (user_id_sender = ? AND user_id_receiver = ? AND questionId = ?) " + "ORDER BY id ASC";

		try (PreparedStatement pstmt = connectionDM.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, receiverId);
			pstmt.setInt(3, qId);
			pstmt.setInt(4, receiverId);
			pstmt.setInt(5, userId);
			pstmt.setInt(6, qId);

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				int messageId = rs.getInt("id");
				int senderID = rs.getInt("user_id_sender");
				String text = rs.getString("message");

				// get username from second database
				String userQuery = "SELECT userName FROM cse360users WHERE id = ?";
				try (PreparedStatement ustmt = connectionUser.prepareStatement(userQuery)) {
					ustmt.setInt(1, senderID);
					ResultSet rsu = ustmt.executeQuery();
					if (rsu.next()) {
						list.add("Message " + messageId + " from " + rsu.getString("userName") + ": " + text);
					} else {
						list.add("Message " + messageId + " from [UnKnown]: " + text);
					}
				}
			}
		}
		return list;
	}

	public static ObservableList<String> getMessagesWhenSendingSelfMessage(DatabaseHelper dbHelper,
			Connection connectionDM, int userID, int questionId) throws SQLException {
		ObservableList<String> list = FXCollections.observableArrayList();
		String query = "SELECT id, message, user_id_sender, user_id_receiver FROM messages WHERE question_id = ? ORDER BY id ASC";

		try (PreparedStatement pstmt = connectionDM.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				int messageId = rs.getInt("id");
				String text = rs.getString("message");
				int senderID = rs.getInt("user_id_sender");
				int receiverID = rs.getInt("user_id_receiver");

				// Get sender and reciever usernames from userdatabase
				String senderName = "";
				String receiverName = "";
				if (dbHelper.doesUserExist(senderID)) {
					senderName = dbHelper.getUserFromID(dbHelper, senderID).getUserName();
				} else {
					senderName = "[Unknown User]";
				}
				if (dbHelper.doesUserExist(receiverID)) {
					receiverName = dbHelper.getUserFromID(dbHelper, receiverID).getUserName();
				} else {
					receiverName = "[Unknown User]";
				}

				String formatted = "Message " + messageId + " from " + senderName + " to " + receiverName + ": " + text;
				list.add(formatted);
			}
		}
		return list;
	}

	public static ObservableList<String> getPrivatelyRepliedQuestionsByUser(Connection connectionQNA,
			Connection connectionUser, int userId) throws SQLException {
		ObservableList<String> list = FXCollections.observableArrayList();

		// Get the username
		String queryU = "SELECT userName FROM cse360users WHERE id = ?";
		String userName = "[Unknown User]";
		try (PreparedStatement ustmt = connectionUser.prepareStatement(queryU)) {
			ustmt.setInt(1, userId);
			ResultSet rsu = ustmt.executeQuery();
			if (rsu.next()) {
				userName = rsu.getString("userName");
			}
		}

		// get question information from questions database
		String queryQ = "SELECT id, questionText FROM questions WHERE user_id = ?";
		try (PreparedStatement qstmt = connectionQNA.prepareStatement(queryQ)) {
			qstmt.setInt(1, userId);
			ResultSet rsq = qstmt.executeQuery();
			while (rsq.next()) {
				int questionID = rsq.getInt("id");
				String questionText = rsq.getString("questionText");
				list.add("Question " + questionID + " by " + userName + ": " + questionText);
			}
		}
		return list;
	}

	public static ObservableList<String> getMessageSendersForQuestion(Connection connectionDM,
			Connection connectionUser, int questionId, int userId) throws SQLException {
		ObservableList<String> senders = FXCollections.observableArrayList();
		String query = "SELECT user_id_sender FROM messages WHERE question_id = ? AND user_id_sender = ?";
		try (PreparedStatement pstmt = connectionDM.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			pstmt.setInt(2, userId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				int senderID = rs.getInt("user_id_sender");
				String userName = "[Unknown User]";
				String queryU = "SELECT userName FROM cse360users WHERE id = ?";
				try (PreparedStatement ustmt = connectionUser.prepareStatement(queryU)) {
					ustmt.setInt(1, senderID);
					ResultSet rsu = ustmt.executeQuery();
					if (rsu.next()) {
						userName = rsu.getString("userName");
					}
				}
				senders.add(userName + " (" + senderID + ")");
			}
		}
		return senders;
	}
}