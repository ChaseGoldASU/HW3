package databaseHelperFunctions;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import application.Rows;
import application.User;
import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AddUser {
	//Create a hashmap for quick user getting through ID
	private static Map<Integer, User> userCache = new HashMap<>();
	
	// Registers a new user in the database.
	public static void register(Connection connectionUser, User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.executeUpdate();
		}
	}
	
	// Validates a user's login credentials.
	public static boolean login(Connection connectionUser, User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	public static boolean login(Connection connectionUser, String userName, String password) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public static boolean doesUserExist(Connection connectionUser, String userName) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {

			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				// If the count is greater than 0, the user exists
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // If an error occurs, assume user doesn't exist
	}
	
	public static boolean doesUserExist(Connection connectionUser, int userID) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE id = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {

			pstmt.setInt(1, userID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				// If the count is greater than 0, the user exists
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public static String getUserRole(Connection connectionUser, String userName) {
		String query = "SELECT role FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getString("role"); // Return the role if user exists
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null; // If no user exists or an error occurs
	}
	
	public static int getUserID(Connection connectionUser, User user) {
		String query = "SELECT id FROM cse360users WHERE userName = ? AND role = ?";
		int returns = -1;
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)){
			pstmt.setString(1,  user.getUserName());
			pstmt.setString(2, user.getRole());
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				returns = rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return returns;
	}
	
	public static int getUserID(Connection connectionUser, String userName, String role) {
		String query = "SELECT id FROM cse360users WHERE userName = ? AND role = ?";
		int returns = -1;
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)){
			pstmt.setString(1,  userName);
			pstmt.setString(2, role);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				returns = rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return returns;
	}
	
	public static int getUserID(Connection connectionUser, String userName) {
		String query = "SELECT id FROM cse360users WHERE userName = ? AND role = ?";
		int returns = -1;
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)){
			pstmt.setString(1,  userName);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				returns = rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return returns;
	}
	
	public static User getUserFromID(DatabaseHelper dbHelper, Connection connectionUser, int id) {
		//Check to see if the user is in the cache. If not, search the database
		User user = dbHelper.userCache.get(id);
		if (user != null) {
			return user;
		}
		
				
		String query = "Select * FROM cse360 WHERE id = ?";
		user = null;
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setInt(1,  id);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				user = new User(rs.getString("userName"), rs.getString("password"), rs.getString("role"));
				dbHelper.userCache.put(id, user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return user;
	}
	
	//Displays all of the Users in the Database
	public static ObservableList<Rows> displayUsers(Connection connectionUser) {
		ObservableList<Rows> data = FXCollections.observableArrayList();
		String query = "SELECT username, role FROM cse360users";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			int rowNumber = 1; //Starting row numbers from 1
			while(rs.next()) {
				String username = rs.getString("username");
				String role = rs.getString("role");
				data.add(new Rows(String.valueOf(rowNumber), username, role, ""));
				rowNumber++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return data;
	}
	
	//Assigns roles to user
	public static void assignUserRole(Connection connectionUser, String userName, String role) throws SQLException{
		//This is with the assumption we are adding roles to what is already there
		String query = "UPDATE cse360users SET role = CONCAT(role, ', ', ?) WHERE userName = ?";
	    try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
	        pstmt.setString(1, role);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    }
	}
	
	//Removes Role from user
	public static void removeUserRole(DatabaseHelper dbHelper, Connection connectionUser, String userName, String roleToRemove) throws SQLException{
		String query = "Update cse360users " +
				"SET role = TRIM(BOTH ', ' FROM REPLACE(CONCAT(', ', role, ', '), CONCAT(', ', ?, ', '), ', ')) " + 
				"WHERE userName = ?";
		int id = getUserID(connectionUser, userName, roleToRemove);
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)){
			pstmt.setString(1, roleToRemove);
			pstmt.setString(2, userName);
			pstmt.executeUpdate();
		}
		dbHelper.userCache.remove(id);
	}
	//Deletes a user from database
	public static boolean deleteUser(Connection connectionUser, String userName) {
		String query = "DELETE FROM cse360users WHERE username = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)){
			pstmt.setString(1,  userName);
			pstmt.executeUpdate();
			return true;	//Return true if successful
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; //Defaults to returning false if something goes wrong
	}
}