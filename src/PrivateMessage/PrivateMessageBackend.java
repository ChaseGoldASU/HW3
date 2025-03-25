package PrivateMessage;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.collections.ObservableList;

/**
 * 
 * The Private Message Backend class is responsible for managing the function in
 * regards to private messaging, performing operations such as user
 * registration, login validation, and handling invitation codes.
 */
public class PrivateMessageBackend {

	private DatabaseHelper databaseHelper;
	private int currentUserId;
	private int otherUserId;

	public PrivateMessageBackend(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
		try {
			databaseHelper.connectToDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setCurrentUserId(int currentUserId) {
		this.currentUserId = currentUserId;

	}

	public void setOtherUserId(int otherUserId) {
		this.otherUserId = otherUserId;
	}

	// Update the database to contain the message along with the sender and sendee
	// id
	// returns a 0 if successful, -1 otherwise
	public int sendMessage(int senderId, int receiverId, int questionId, String message) {
		try {
			databaseHelper.addMessage(senderId, receiverId, questionId, message);
			return 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void setuserID(int id) {
		this.currentUserId = id;
	}

}