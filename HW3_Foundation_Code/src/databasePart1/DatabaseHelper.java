package databasePart1;

//importing SQL related files
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//JavaFX
import java.time.LocalDateTime;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

//Importing strings to create a random string of characters
//It was recommended to use java.security because it is somehow better at making safe randomized passwords
import java.security.SecureRandom;

//importing other files
import application.User;
import application.Rows;
import application.AdminHomePage;
import databaseHelperFunctions.*;
import qnaClasses.*;
import UserPassEvaluators.PasswordEvaluator;

/**
 * The DatabaseHelper class is responsible for managing the connection to the
 * database, performing operations such as user registration, login validation,
 * and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL_USER = "jdbc:h2:~/userDatabase";
	// Make second database for questions and answers
	static final String DB_URL_QNA = "jdbc:h2:~/qnaDatabase";
	// Make third database for private/direct messages
	static final String DB_URL_DM = "jdbc:h2:~/dmDatabase";

	// Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	// Username Database connection
	private Connection connectionUser = null;
	private Statement statementUser = null;
	// QNA Database Connection
	private Connection connectionQNA = null;
	private Statement statementQNA = null;
	// Private/Direct Messages Database Connection
	private Connection connectionDM = null;
	private Statement statementDM = null;

	// Create caches that temorarily store users, questions, answers, and replies
	public Map<Integer, User> userCache = new ConcurrentHashMap<>();
	public Map<String, Question> questionCache = new ConcurrentHashMap<>();

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			// Connect to users database
			connectionUser = DriverManager.getConnection(DB_URL_USER, USER, PASS);
			statementUser = connectionUser.createStatement();

			// Connect to Question and Answer Database
			connectionQNA = DriverManager.getConnection(DB_URL_QNA, USER, PASS);
			statementQNA = connectionQNA.createStatement();

			// Connect to Private/Direct Messages Database
			connectionDM = DriverManager.getConnection(DB_URL_DM, USER, PASS);
			statementDM = connectionDM.createStatement();

			// You can use this command to clear the database and restart from fresh.
			// statementUser.execute("DROP ALL OBJECTS");

			createTablesUser(); // Create the necessary tables for the user database if they don't exist
			createTableQNA(); // Create the necessary tables for the qna database if they don't exist
			createTableDM(); // Create the nessisary tables for the DM database if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTablesUser() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users (" +
				"id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
				"userName VARCHAR(255) UNIQUE, " +
				"password VARCHAR(255), " +
				"role VARCHAR(45))";
		statementUser.execute(userTable);

		// Create the one-time-user table
		String otpTable = "CREATE TABLE IF NOT EXISTS oneTimePasswords (" +
				"id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
				"userName VARCHAR(255), " +
				"otp VARCHAR(255), " +
				"role VARCHAR(45))";
		statementUser.execute(otpTable);

		// Create the invitation codes table
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes (" +
				"code VARCHAR(10) PRIMARY KEY, " +
				"isUsed BOOLEAN DEFAULT FALSE, " +
				"code_expire_date BIGINT)";
		statementUser.execute(invitationCodesTable);
	}

	private void createTableQNA() throws SQLException {
		// Note: Changed foreign key references from 'users' to 'cse360users'
		String questionTable = "CREATE TABLE IF NOT EXISTS questions (" + "isOpen BOOLEAN DEFAULT False,"
				+ "id BIGINT, " + "user_id INT, " + "likes INT, " + "views INT, " + "date_created TIMESTAMP, "
				+ "questionText TEXT)"; // Does not need its size to be set, a question can be more than 1000 characters
										// long
		statementQNA.execute(questionTable);

		String answerTable = "CREATE TABLE IF NOT EXISTS answers (" + "isRelevant BOOLEAN, " + "isOfficial BOOLEAN, "
				+ "id INT, " + "question_id INT, " + "user_id INT, " + "likes INT, " + "dislikes INT, "
				+ "date_created TIMESTAMP, " + "answerText TEXT)"; // Does not need its size to be set, an answer can be
																	// more than 1000 characters long
		statementQNA.execute(answerTable);

		String replyTable = "CREATE TABLE IF NOT EXISTS replies (" + "id INT, " + "user_id INT, " + "likes INT, "
		// the parent is what the reply is replying to. Reply can respond to both
		// questions and answers
				+ "parent_id INT, " + "parentType VARCHAR(8), " // Max 8 characters because only valid inputs are
																// question, answer, or reply
				+ "date_created TIMESTAMP, " + "replyText TEXT)";
		statementQNA.execute(replyTable);
	}

	private void createTableDM() throws SQLException {
		String privateMessage = "CREATE TABLE IF NOT EXISTS messages (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "user_id_receiver INT, " + "user_id_sender INT, " + "question_id INT, " + "date_created TIMESTAMP,"
				+ "message TEXT)";
		statementDM.execute(privateMessage);

	}

	// Check if the user database is empty
	public boolean isUserDatabaseEmpty() throws SQLException {
		String queryUser = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statementUser.executeQuery(queryUser);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Check if the questions database is empty
	public boolean isQuestionsDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM questions";
		ResultSet resultSet = statementQNA.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Check if the answers database is empty
	public boolean isAnswersDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM answers";
		ResultSet resultSet = statementQNA.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Check if the replies database is empty
	public boolean isRepliesDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM replies";
		ResultSet resultSet = statementQNA.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Check if the messages database is empty
	public boolean isDMDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM messages";
		ResultSet resultSet = statementQNA.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	public void cleanupExpiredAndUsedCodes() {
		String query = "DELETE FROM InvitationCodes WHERE isUsed = TRUE OR expiration_time < ?";

		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			int deletedRows = pstmt.executeUpdate();
			System.out.println("Cleanup complete. Removed " + deletedRows + "expired/used codes");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Clean up the databases
	public void clearDatabases() throws SQLException {
		try {
			statementUser.execute("DROP ALL OBJECTS");
			userCache.clear();
			statementQNA.execute("DROP ALL OBJECTS");
			questionCache.clear();
			statementDM.execute("DROP ALL OBJECTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		//Clear an individual database
	public void clearUserDatabase() throws SQLException {
		try {
			statementUser.execute("DROP ALL OBJECTS");
			userCache.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void clearQNADatabase() throws SQLException {
		try {
			statementQNA.execute("DROP ALL OBJECTS");
			questionCache.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void clearDMDatabase() throws SQLException {
		try {
			statementDM.execute("DROP ALL OBJECTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		// Close connection to userDatabase
		try {
			if (statementUser != null)
				statementUser.close();
		} catch (SQLException se2) {
			se2.printStackTrace();
		}
		try {
			if (connectionUser != null)
				connectionUser.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}

		// Close connection to qnaDatabase
		try {
			if (statementQNA != null) {
				statementQNA.close();
			}
		} catch (SQLException qe2) {
			qe2.printStackTrace();
		}
		try {
			if (connectionQNA != null)
				connectionQNA.close();
		} catch (SQLException qe) {
			qe.printStackTrace();
		}

		// Close connection to dmDatabase
		try {
			if (statementDM != null) {
				statementDM.close();
			}
		} catch (SQLException dme2) {
			dme2.printStackTrace();
		}
		try {
			if (connectionDM != null)
				connectionDM.close();
		} catch (SQLException dme) {
			dme.printStackTrace();
		}

		// Clear the user cache
		userCache.clear();
	}

	// Returns the current connections
	public Connection getConnectionUser() {
		return connectionUser;
	}

	public Connection getConnectionQNA() {
		return connectionQNA;
	}

	public Connection getConnectionDM() {
		return connectionDM;
	}

	// Invite Code Callers, calls functions in databaseHelperFunctions/GenCodes
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
		return GenCodes.generateInvitationCode(connectionUser);
	}

	public String getInviteCode() {
		return GenCodes.getInviteCode(connectionUser);
	}

	public boolean validateInvitationCode(String code) {
		return GenCodes.validateInvitationCode(connectionUser, code);
	}
	// End of Invite Codes

	// User Function Callers, calls functions in databaseHelperFunctions/AddUser
	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		AddUser.register(connectionUser, user);
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		return AddUser.login(connectionUser, user);
	}

	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
		return AddUser.doesUserExist(connectionUser, userName);
	}

	// Overloaded user exist chack that runs off of ID values instead of usernames
	public boolean doesUserExist(int userID) {
		return AddUser.doesUserExist(connectionUser, userID);
	}

	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
		return AddUser.getUserRole(connectionUser, userName);
	}

	public int getUserID(User user) {
		return AddUser.getUserID(connectionUser, user);
	}
	
	public int getUserID(String userName, String role) {
		return AddUser.getUserID(connectionUser, userName, role);
	}

	public int getUserID(String userName) {
		return AddUser.getUserID(connectionUser, userName);
	}

	public User getUserFromID(DatabaseHelper dbHelper, int id) {
		return AddUser.getUserFromID(dbHelper, connectionUser, id);
	}

	// Displays all of the Users in the Database
	public ObservableList<Rows> displayUsers() {
		return AddUser.displayUsers(connectionUser);
	}

	// Assigns roles to user
	public void assignUserRole(String userName, String role) throws SQLException {
		AddUser.assignUserRole(connectionUser, userName, role);
	}

	public void removeUserRole(DatabaseHelper dbHelper, String userName, String roleToRemove) throws SQLException {
		AddUser.removeUserRole(dbHelper, connectionUser, userName, roleToRemove);
	}

	// Deletes a user from the database
	public boolean deleteUser(String userName) {
		return AddUser.deleteUser(connectionUser, userName);
	}
	// End of user calls

	// One Time Password Callers, calls functions in databaseHelperFunctions/AddOTP
	// Makes the OTP
	public String generateOneTimePassword(DatabaseHelper dbHelper, String userName) {
		// Call this function in addOTP file
		return AddOTP.generateOTP(dbHelper, connectionUser, userName);
	}

	// Fetches the user's role while using an OTP
	public String getUserRoleOTP(DatabaseHelper dbHelper, String userName) {
		return AddOTP.getUserRoleOTP(dbHelper, connectionUser, userName);
	}

	// Login checker while using an OTP
	public Boolean loginOTP(DatabaseHelper dbHelper, User user) throws SQLException {
		return AddOTP.loginOTP(dbHelper, connectionUser, user);
	}

	// Returns most recent OTP for a user,
	public String getOTP(DatabaseHelper dbHelper, String user) {
		return AddOTP.getOTP(dbHelper, connectionUser, user);
	}

	// Displays all of the Users in the OTP Database
	public ObservableList<Rows> getUserOTP(DatabaseHelper dbHelper) {
		return AddOTP.getUserOTP(dbHelper, connectionUser);
	}

	// Removing all One Time Passwords for a user from database
	public void rmOTP(DatabaseHelper dbHelper, User user) {
		AddOTP.rmOTP(dbHelper, connectionUser, user);
	}
	// End of OTP functions

	// Question Function Callers, functions stored in
	// databaseHelperFunctions/AddQuestion
	public void newQuestion(DatabaseHelper dbHelper, Question question) {
		AddQuestion.newQuestion(dbHelper, connectionQNA, question);
	}

	public boolean updateQuestionText(DatabaseHelper dbHelper, int questionID, int userID, String question) {
		return AddQuestion.updateQuestionText(dbHelper, connectionQNA, questionID, question, userID);
	}

	public void updateQuestionLikes(DatabaseHelper dbHelper, int questionID, int likes, int userID) {
		AddQuestion.updateQuestionLikes(dbHelper, connectionQNA, questionID, likes, userID);
	}

	public void updateQuestionViews(DatabaseHelper dbHelper, int questionID, int views, int userID) {
		AddQuestion.updateQuestionViews(dbHelper, connectionQNA, questionID, views, userID);
	}

	public boolean markQuestionResolved(int userID, int questionID) throws SQLException {
		return AddQuestion.closeQuestion(connectionQNA, userID, questionID);
	}

	public boolean unmarkQuestionResolved(int userID, int questionID) throws SQLException {
		return AddQuestion.openQuestion(connectionQNA, userID, questionID);
	}

	public boolean isQuestionSolved(int userID, int questionID) {
		return AddQuestion.isOpen(connectionQNA, userID, questionID);
	}

	public String getQuestionText(int userID, int questionID) throws SQLException {
		return AddQuestion.getQuestionText(connectionQNA, userID, questionID);
	}

	public ObservableList<String> getUserQuestions(DatabaseHelper dbHelper, int userID) throws SQLException {
		return AddQuestion.getUserQuestions(dbHelper, connectionQNA, userID);
	}

	public int genQID(DatabaseHelper dbHelper, int userID) {
		return AddQuestion.genID(dbHelper, connectionQNA, userID);
	}

	public boolean deleteQuestion(DatabaseHelper dbHelper, int questionID, int userID) {
		return AddQuestion.deleteQuestion(dbHelper, connectionQNA, questionID, userID);
	}
	// End of Question functions

	// Answer Function Callers, functions stored in
	// databaseHelperFunctions/AddAnswer
	public void newAnswer(DatabaseHelper dbHelper, Answer answer) {
		AddAnswer.newAnswer(dbHelper, connectionQNA, answer);
	}

	public void updateAnswerText(DatabaseHelper dbHelper, int questionID, int answeID, String answer) {
		AddAnswer.updateAnswerText(dbHelper, connectionQNA, questionID, answeID, answer);
	}

	public void updateAnswerLikes(DatabaseHelper dbHelper, int questionID, int answerID, int likes, int dislikes) {
		AddAnswer.updateAnswerLikes(dbHelper, connectionQNA, questionID, answerID, likes, dislikes);
	}

	public void updateAnswerRelevant(DatabaseHelper dbHelper, int questionID, int answerID, boolean relevant) {
		AddAnswer.updateAnswerRelevant(dbHelper, connectionQNA, questionID, answerID, relevant);
	}

	public void updateAnswerOfficial(DatabaseHelper dbHelper, int questionID, int answerID, boolean official) {
		AddAnswer.updateAnswerOfficial(dbHelper, connectionQNA, questionID, answerID, official);
	}

	public int genAID(DatabaseHelper dbHelper, int userID, int questionID) {
		return AddAnswer.genID(dbHelper, connectionQNA, userID);
	}

	public boolean deleteAnswer(DatabaseHelper dbHelper, int answerID, int questionID) {
		return AddAnswer.deleteAnswer(dbHelper, connectionQNA, answerID, questionID);
	}
	// End of Answer functions

	// Reply function callers
	public void newReply(DatabaseHelper dbHelper, Reply reply) {
		AddReply.newReply(dbHelper, connectionQNA, reply);
	}

	public void updateReplyText(DatabaseHelper dbHelper, String parentType, int parentID, int replyID, String reply) {
		AddReply.updateReplyText(dbHelper, connectionQNA, parentType, parentID, replyID, reply);
	}

	public void updateReplyLikes(DatabaseHelper dbHelper, String parentType, int parentID, int replyID, int likes) {
		AddReply.updateReplyLikes(dbHelper, connectionQNA, parentType, parentID, replyID, likes);
	}

	public int genRID(DatabaseHelper dbHelper, String parentType, int parentID) {
		return AddReply.genID(dbHelper, connectionQNA, parentType, parentID);
	}

	public boolean deleteReply(DatabaseHelper dbHelper, String parentType, int parentID, int replyID) {
		return AddReply.deleteReply(dbHelper, connectionQNA, parentType, parentID, replyID);
	}
	// End of Reply functions

	// Message function callers
	public void addMessage(int senderID, int receiverID, int questionID, String message) throws SQLException {
		AddMessage.addMessage(connectionDM, senderID, receiverID, questionID, message);
	}

	public ObservableList<String> getMessages(int senderID, int receiverID) throws SQLException {
		return AddMessage.getMessages(connectionDM, connectionUser, senderID, receiverID);
	}

	public ObservableList<String> getAllChatMessages(int userID, int recieverID, int qID) throws SQLException {
		return AddMessage.getAllChatMessages(connectionDM, connectionUser, userID, userID, qID);
	}

	public ObservableList<String> getMessagesWhenSendingSelfMessage(DatabaseHelper dbHelper, int userID, int questionID)
			throws SQLException {
		return AddMessage.getMessagesWhenSendingSelfMessage(dbHelper, connectionDM, userID, questionID);
	}

	public ObservableList<String> getPrivatelyRepliedQuestionsByUser(int userID) throws SQLException {
		return AddMessage.getPrivatelyRepliedQuestionsByUser(connectionQNA, connectionUser, userID);
	}

	public ObservableList<String> getMessageSendersForQuestion(int questionID, int userID) throws SQLException {
		return AddMessage.getMessageSendersForQuestion(connectionDM, connectionUser, questionID, userID);
	}
	// End of Message functions
}
