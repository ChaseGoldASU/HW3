package databaseHelperFunctions;

import java.sql.*;
import java.security.SecureRandom;

public class GenCodes {
	// Generates a new invitation code and inserts it into the database.
	public static String generateInvitationCode(Connection connectionUser) {
		// Generate 4-character long code through secureRandom
		// Define characters we can use
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_-+=<>?/|:;,.~";
		StringBuilder code = new StringBuilder();
		SecureRandom random = new SecureRandom();
		// Generate 4 character code
		for (int i = 0; i < 4; i++) {
			code.append(characters.charAt(random.nextInt(characters.length())));
		}

		long expiredCodeDate = System.currentTimeMillis() + 259200000;
		expiredCodeDate = expiredCodeDate / (1000);
		String query = "INSERT INTO InvitationCodes (code, code_expire_date) VALUES (?, ?)";

		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, code.toString());
			pstmt.setLong(2, expiredCodeDate);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Code Generation Failed: " + e;
		}

		return code.toString();
	}

	// Marks the invitation code as used in the database.
	private static void markInvitationCodeAsUsed(Connection connectionUser, String code) {
		String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Validates an invitation code to check if it is unused.
	public static boolean validateInvitationCode(Connection connectionUser, String code) {
		String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			pstmt.setString(1, code);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				// Mark the code as used
				markInvitationCodeAsUsed(connectionUser, code);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// returns all invite codes
	public static String getInviteCode(Connection connectionUser) {
		String query = "SELECT * FROM InvitationCodes WHERE isUsed = FALSE";
		StringBuilder codes = new StringBuilder();
		try (PreparedStatement pstmt = connectionUser.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String currentCode = rs.getString("code");
				if (validateInvitationCode(connectionUser, currentCode)) {
					if (!codes.isEmpty()) {
						codes.append("\n");
					}
					codes.append("Code: " + currentCode);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return codes.toString();
	}
}

/*
 * Expiration functions made by chase, not being used, but I didn't want to get
 * rid of them completely //Create the invitation codes table String
 * invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes (" +
 * "code VARCHAR(10) PRIMARY KEY, " + "isUsed BOOLEAN DEFAULT FALSE," +
 * "expiration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
 * statement.execute(invitationCodesTable); //function to return all currently
 * valid expiration codes public String getInviteCode() { String query =
 * "SELECT code, expiration_time FROM InvitationCodes WHERE isUseed = FALSE";
 * StringBuilder codes = new StringBuilder();
 * 
 * try (PreparedStatement pstmt = connection.prepareStatement(query)) {
 * ResultSet rs = pstmt.executeQuery();
 * 
 * while (rs.next()) { if (!codes.isEmpty()) { codes.append("\n"); }
 * codes.append("Code: " + rs.getString("code") + " | Expiration: " +
 * rs.getTimestamp("expiration_time")); } }catch (SQLException e) {
 * e.printStackTrace(); System.out.println("Failed to list all invited codes");
 * } return codes.toString(); }
 * 
 * //Generates a new invitation code and inserts it into the database. public
 * String generateInvitationCode() { String code =
 * UUID.randomUUID().toString().substring(0, 4); // Generate a random
 * 4-character code LocalDateTime expireyDate =
 * LocalDateTime.now().plusMinutes(30);//Calculate Expiration Time String query
 * = "INSERT INTO InvitationCodes (code, expiration_time) VALUES (?, ?)";
 * 
 * try (PreparedStatement pstmt = connection.prepareStatement(query)) {
 * pstmt.setString(1, code); pstmt.setTimestamp(2,
 * Timestamp.valueOf(expireyDate));//Convert Local Time to a Timestame
 * pstmt.executeUpdate(); } catch (SQLException e) { e.printStackTrace(); }
 * 
 * return code; }
 * 
 * //Marks the invitation code as used in the database. private void
 * markInvitationCodeAsUsed(String code) { String query =
 * "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?"; try
 * (PreparedStatement pstmt = connection.prepareStatement(query)) {
 * pstmt.setString(1, code); pstmt.executeUpdate(); } catch (SQLException e) {
 * e.printStackTrace(); } }
 * 
 * //Validates an invitation code to check if it is unused. public char
 * validateInvitationCode(String code) { String query =
 * "SELECT expiration_time FROM InvitationCodes WHERE code = ? AND isUsed = FALSE"
 * ; //Validation code should check if the code has expired try
 * (PreparedStatement pstmt = connection.prepareStatement(query)) {
 * pstmt.setString(1, code); ResultSet rs = pstmt.executeQuery(); if (rs.next())
 * { //Check if the code has expired yet LocalDateTime expireyDate =
 * rs.getTimestamp("expiration_time").toLocalDateTime(); LocalDateTime
 * currentTime = LocalDateTime.now(); if(currentTime.isBefore(expireyDate)) { //
 * Mark the code as used and return respective return statement
 * markInvitationCodeAsUsed(code);
 * 
 * //code exists and is unexpired. return t for true return 't'; } else { //if
 * get here, code has expired
 * 
 * //code exists but is expired. return e for expired return 'e'; } } } catch
 * (SQLException e) { e.printStackTrace(); }
 * 
 * //code doesn't exist. return f for false return 'f'; }
 */