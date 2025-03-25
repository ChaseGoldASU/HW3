package automatedTests;

import java.sql.*;

//imports

//this class makes quick connection and disconnection objects for the automated tests
public class connectToDatabase {
	//Database Connection
	private char DB;
	
	private Connection connection = null;
	private Statement statement = null;
	
	public connectToDatabase(char DB) {
		this.DB = DB;
		try {
			Class.forName("org.h2.Driver");
			System.out.println("Connecting to database...");
			//connect to database
			if (DB == 'U') {
				connection = DriverManager.getConnection("jdbc:h2:~/userDatabase", "sa", "");
			} else if (DB == 'Q') {
				connection = DriverManager.getConnection("jdbc:h2:~/qnaDatabase", "sa", "");
			} else if (DB == 'D') {
				connection = DriverManager.getConnection("jdbc:h2:~/dmDatabase", "sa", "");
			}
			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
	
	public void createTables() throws SQLException{
		if (DB == 'U') {
			createUser();
		} else if (DB == 'Q') {
			createQNA();
		} else if (DB == 'D') {
			createDM();
		}
	}
	
	private void createUser() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users (" +
				"id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
				"userName VARCHAR(255) UNIQUE, " +
				"password VARCHAR(255), " +
				"role VARCHAR(45))";
		statement.execute(userTable);

		// Create the one-time-user table
		String otpTable = "CREATE TABLE IF NOT EXISTS oneTimePasswords (" +
				"id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
				"userName VARCHAR(255), " +
				"otp VARCHAR(255), " +
				"role VARCHAR(45))";
		statement.execute(otpTable);

		// Create the invitation codes table
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes (" +
				"code VARCHAR(10) PRIMARY KEY, " +
				"isUsed BOOLEAN DEFAULT FALSE, " +
				"code_expire_date BIGINT)";
		statement.execute(invitationCodesTable);
	}

	private void createQNA() throws SQLException {
		// Note: Changed foreign key references from 'users' to 'cse360users'
		String questionTable = "CREATE TABLE IF NOT EXISTS questions (" + "isOpen BOOLEAN DEFAULT False,"
				+ "id BIGINT, " + "user_id INT, " + "likes INT, " + "views INT, " + "date_created TIMESTAMP, "
				+ "questionText TEXT)"; // Does not need its size to be set, a question can be more than 1000 characters
										// long
		statement.execute(questionTable);

		String answerTable = "CREATE TABLE IF NOT EXISTS answers (" + "isRelevant BOOLEAN, " + "isOfficial BOOLEAN, "
				+ "id INT, " + "question_id INT, " + "user_id INT, " + "likes INT, " + "dislikes INT, "
				+ "date_created TIMESTAMP, " + "answerText TEXT)"; // Does not need its size to be set, an answer can be
																	// more than 1000 characters long
		statement.execute(answerTable);

		String replyTable = "CREATE TABLE IF NOT EXISTS replies (" + "id INT, " + "user_id INT, " + "likes INT, "
		// the parent is what the reply is replying to. Reply can respond to both
		// questions and answers
				+ "parent_id INT, " + "parentType VARCHAR(8), " // Max 8 characters because only valid inputs are
																// question, answer, or reply
				+ "date_created TIMESTAMP, " + "replyText TEXT)";
		statement.execute(replyTable);
	}

	private void createDM() throws SQLException {
		String privateMessage = "CREATE TABLE IF NOT EXISTS messages (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "user_id_receiver INT, " + "user_id_sender INT, " + "question_id INT, " + "date_created TIMESTAMP,"
				+ "message TEXT)";
		statement.execute(privateMessage);

	}
	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		boolean empty = false;
		DatabaseMetaData dbm = connection.getMetaData();
		try (ResultSet rs = dbm.getTables(null, null, "%", new String[] {"TABLE"})){
			while (rs.next()) {
				if (!isTableEmpty(rs.getString("TABLE_NAME"))) {
					empty = false;
					break;
				}
			}
		}
		
		if (!empty) {
			System.out.print(dbm.getDatabaseProductName() + " is empty");
		} else {
			System.out.print(dbm.getDatabaseProductName() + " is filled");
		}
		return empty;
	}
	
	private boolean isTableEmpty(String tableName) throws SQLException{
		String query = "SELECT COUNT(*) FROM " + tableName;
		boolean empty = false;
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {
			if (rs.next()) {
				if (rs.getInt(1) == 0) {
					System.out.println("Table " + tableName + " is empty");
					empty = true;
				}
			}
		}
		return empty;
	}
	
	// Clean up the databases
	public void clearDatabase() throws SQLException {
		try {
			statement.execute("DROP ALL OBJECTS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Closes the database connection and statement.
	public void closeConnection() {
		// Close connection to userDatabase
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException se2) {
			se2.printStackTrace();
		}
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}
}