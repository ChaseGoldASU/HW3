package databaseHelperFunctions;

import java.security.SecureRandom;
import java.sql.*;

import UserPassEvaluators.PasswordEvaluator;
import application.User;
import application.Rows;
import databasePart1.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AddOTP {
	// To make the database a bit more readable, I moved the OTP functions to this
	// file

	// Creates an one time password that complies with the rules of the password
	public static String generateOTP(DatabaseHelper dbHelper, Connection connectionUser, String userName) {
		// Strings needed to save to database
		String query = "INSERT INTO oneTimePasswords (userName, otp, role) VALUES (?, ?, ?)";
		StringBuilder randomPassword = new StringBuilder();

		// Sets of accepted characters
		String upperChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lowerChar = "abcdefghijklmnopqrstuvwxyz";
		String digit = "1234567890";
		String specialChar = "~`!@#$%^&*()_-+{}[]|:,.?/";

		// All allowed characters
		String allChar = upperChar + lowerChar + digit + specialChar;

		// Secure Random Generator
		SecureRandom random = new SecureRandom();

		// String builder. Makes it easier to add and remove characters to a string
		StringBuilder password = new StringBuilder();

		// Fill the requirements for the password
		password.append(upperChar.charAt(random.nextInt(upperChar.length())));
		password.append(lowerChar.charAt(random.nextInt(lowerChar.length())));
		password.append(digit.charAt(random.nextInt(digit.length())));
		password.append(specialChar.charAt(random.nextInt(specialChar.length())));

		// Add random characters to get to at least 8 total characters
		int j = random.nextInt(8) + 4;
		for (int i = 0; i < j; i++) {
			password.append(allChar.charAt(random.nextInt(allChar.length())));
		}

		// Shuffle password to ensure randomPassword is randomly ordered
		while (password.length() > 0) {
			int index = random.nextInt(password.length());
			randomPassword.append(password.charAt(index));
			password.deleteCharAt(index);
		}

		// check if the password generation worked
		String checkRnd = PasswordEvaluator.evaluatePassword(randomPassword.toString());
		if (checkRnd == "") {
			// Insert saving to database
			System.out.println(randomPassword);
			try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
				pstmt.setString(1, userName);
				pstmt.setString(2, randomPassword.toString());
				pstmt.setString(3, dbHelper.getUserRole(userName));
				pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return "Random One-Time-Password generated, check with admin";
		} else {
			return "Password Generation Failed because generated password\n" + checkRnd;
		}
	}

	// Fetches the user's role while using an OTP
	public static String getUserRoleOTP(DatabaseHelper dbHelper, Connection connectionUser, String userName) {
		String query = "SELECT role FROM oneTimePasswords WHERE userName = ?";
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

	public static Boolean loginOTP(DatabaseHelper dbHelper, Connection connectionUser, User user) throws SQLException {
		String query = "SELECT * FROM oneTimePasswords WHERE userName = ? AND otp = ? AND role = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	// Returns most recent OTP for a user,
	public static String getOTP(DatabaseHelper dbHelper, Connection connectionUser, String user) {
		ObservableList<Rows> data = FXCollections.observableArrayList();
		String query = "SELECT otp FROM oneTimePasswords WHERE username = ? AND role = ?";
		String opt = "";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, user);
			pstmt.setString(2, getUserRoleOTP(dbHelper, connectionUser, user));
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				opt = rs.getString("otp");

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return opt;
	}

	// Displays all of the Users in the OTP Database
	public static ObservableList<Rows> getUserOTP(DatabaseHelper dbHelper, Connection connectionUser) {
		ObservableList<Rows> data = FXCollections.observableArrayList();
		String query = "SELECT username, role, otp FROM oneTimePasswords";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			int rowNumber = 1; // Starting row numbers from 1
			while (rs.next()) {
				String username = rs.getString("username");
				String role = rs.getString("role");
				String otp = rs.getString("otp");
				data.add(new Rows(String.valueOf(rowNumber), username, role, otp));
				rowNumber++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	// Removing all One Time Passwords for a user from database
	public static void rmOTP(DatabaseHelper dbHelper, Connection connectionUser, User user) {
		String query = "DELETE FROM oneTimePasswords WHERE userName = ? AND role = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getRole());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}