package databaseHelperFunctions;

import java.sql.*;
import java.time.LocalDateTime;

import databasePart1.DatabaseHelper;
import qnaClasses.Reply;

public class AddReply {
	// Adds new reply object to the database
	public static void newReply(DatabaseHelper dbHelper, Connection connectionQNA, Reply reply) {
		String addR = "INSERT INTO replies (" + "id, user_id, likes, parent_id, parentType, date_created, replyText) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(addR)) {
			pstmt.setInt(1, reply.getID());
			pstmt.setInt(2, reply.getAuthorID());
			pstmt.setInt(3, reply.getLikes());
			pstmt.setInt(4, reply.getParentID());
			pstmt.setString(5, reply.getParentType());
			pstmt.setTimestamp(6, Timestamp.valueOf(reply.getDateCreated()));
			pstmt.setString(7, reply.getContent());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Update changes:
	// Update the reply's text
	public static void updateReplyText(DatabaseHelper dbHelper, Connection connectionQNA, String parentType,
			int parentID, int replyID, String reply) {
		String updateReply = "UPDATE replies SET replyText = ?, date_created = ? WHERE parentType = ? AND parent_id = ? AND id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateReply)) {
			pstmt.setString(1, reply);
			pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
			pstmt.setString(3, parentType);
			pstmt.setInt(4, parentID);
			pstmt.setInt(5, replyID);

			// Execute update and debug print
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Reply updated successfully.");
			} else {
				System.out.println("No Reply found with the provided ID.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Update Likes
	public static void updateReplyLikes(DatabaseHelper dbHelper, Connection connectionQNA, String parentType,
			int parentID, int replyID, int likes) {
		String updateLikes = "UPDATE replies SET likes = ? WHERE parentType = ? AND parent_id = ? AND id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(updateLikes)) {
			pstmt.setInt(1, likes);
			pstmt.setString(2, parentType);
			pstmt.setInt(3, parentID);
			pstmt.setInt(4, replyID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	// End of updates

	// Generates ID, looks for first free ID connected to the parent
	public static int genID(DatabaseHelper dbHelper, Connection connectionQNA, String parentType, int parentID) {
		String findOpenID = "Select id from replies WHERE parentType = ? AND parent_id = ?";
		int id = -1;

		try (PreparedStatement pstmt = connectionQNA.prepareStatement(findOpenID)) {
			pstmt.setString(1, parentType);
			pstmt.setInt(2, parentID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				if (rs.getInt("id") > id) {
					if (rs.getInt("id") > (id + 1)) {
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

	// Deletes a reply from the database
	public static boolean deleteReply(DatabaseHelper dbHelper, Connection connectionQNA, String parentType,
			int parentID, int replyID) {
		String delete = "DELETE FROM replies WHERE parentType = ? AND parent_id = ? AND id = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(delete)) {
			pstmt.setString(1, parentType);
			pstmt.setInt(2, parentID);
			pstmt.setInt(3, replyID);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// All replies that responded to this question should also be deleted
		String deleteReplies = "DELETE FROM replies WHERE parent_id = ? AND parentType = ?";
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(deleteReplies)) {
			pstmt.setInt(1, replyID);
			pstmt.setString(2, "Reply");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}