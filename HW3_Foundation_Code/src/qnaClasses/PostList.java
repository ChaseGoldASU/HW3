package qnaClasses;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import qnaClasses.*;
import databasePart1.DatabaseHelper;

public class PostList {
	// Any of the following can be empty/null
	private final int searchUserID; // Only a specific user's questions can show up
	private final int pID; // Allows for searching within a parent post
	private final String pType; // Selects what type of parent this question will have
	private final String postType; // Selects the type of post you are looking for
	private final String searchKeyWord; // Searches for questions containing search as a substring
	private final String searchRole; // Only questions made by a specific role can appear
	private final Connection connectionQNA;
	private List<Post> List;

	// Basic constructor, makes everything empty and loads the entire QNA database
	public PostList(DatabaseHelper dbHelper, String sort, String priority) {
		this.searchUserID = -1; // Made -1 because there can be a user 0
		this.pID = -1;
		this.pType = "";
		this.postType = "";
		this.searchKeyWord = "";
		this.searchRole = "";
		this.connectionQNA = dbHelper.getConnectionQNA();

		this.List = new ArrayList<>();
		getAllPosts(dbHelper);

		// Sort the this.List based on the parameters in sortCountable and sortBoolean
		if (sort != null && !sort.isEmpty()) {
			sort(sort, dbHelper);
		}
		if (priority != null && !priority.equals("") && !priority.isEmpty()) {
			prioritize(priority);
		}
	}

	// Preloaded list, if you already have a list and you want to make a new list.
	// Doesn't search the database
	public PostList(DatabaseHelper dbHelper, List<Post> P, String sort, String priority) {
		this.searchUserID = -1; // Made -1 because there can be a user 0
		this.pID = -1;
		this.pType = "";
		this.postType = "";
		this.searchKeyWord = "";
		this.searchRole = "";
		this.connectionQNA = dbHelper.getConnectionQNA();

		this.List = P;

		// Sort the this.List based on the parameters in sortCountable and sortBoolean
		if (sort != null && !sort.equals("") && !sort.isEmpty()) {
			sort(sort, dbHelper);
		}
		if (priority != null && !priority.equals("") && !priority.isEmpty()) {
			prioritize(priority);
		}
	}

	// Build a list based on some parameters
	public PostList(DatabaseHelper dbHelper, int userID, int parentID, String parentType, String search, String role,
			String postType) {
		this.pID = parentID;
		this.pType = parentType;
		this.searchUserID = userID;
		this.postType = postType;
		this.searchKeyWord = search;
		this.searchRole = role;
		this.connectionQNA = dbHelper.getConnectionQNA();
		this.List = new ArrayList<>();

		populateList(dbHelper);
	}

	// Gets the list
	public List<Post> getList() {
		return this.List;
	}

	// Gets all posts in the database
	private void getAllPosts(DatabaseHelper dbHelper) {
		String getterQ = "SELECT * FROM questions";
		String getterA = "SELECT * FROM answers";
		String getterR = "SELECT * FROM replies";

		// Get all questions and append the list
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(getterQ)) {
			ResultSet rs = pstmt.executeQuery();
			// While there is a question to be loaded, load it
			while (rs.next()) {
				// Build a question object
				boolean isOpen = rs.getBoolean("isOpen");
				int id = rs.getInt("id");
				int userID = rs.getInt("user_id");
				int likes = rs.getInt("likes");
				int views = rs.getInt("views");
				LocalDateTime date = rs.getTimestamp("date_created").toLocalDateTime();
				String questionText = rs.getString("questionText");
				Question question = new Question(isOpen, id, userID, likes, views, questionText, date);
				// Insert the question object into the list
				this.List.add(question);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Get all answers and append the list
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(getterA)) {
			ResultSet rs = pstmt.executeQuery();
			// While there is an answer to be loaded, load it
			while (rs.next()) {
				// Build an answer object
				boolean isRelevant = rs.getBoolean("isRelevent");
				boolean isOfficial = rs.getBoolean("isOfficial");
				int id = rs.getInt("id");
				int questionID = rs.getInt(("question_id"));
				int userID = rs.getInt("user_id");
				int likes = rs.getInt("likes");
				int dislikes = rs.getInt("dislikes");
				LocalDateTime date = rs.getTimestamp("date_created").toLocalDateTime();
				String answerText = rs.getString("answerText");
				Answer answer = new Answer(isRelevant, isOfficial, id, questionID, userID, likes, dislikes, answerText,
						date);
				// Insert the answer object into the list
				this.List.add(answer);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Get all replies and append the list
		try (PreparedStatement pstmt = connectionQNA.prepareStatement(getterR)) {
			ResultSet rs = pstmt.executeQuery();
			// While there is a reply to be loaded, load it
			while (rs.next()) {
				// Build a reply object
				int id = rs.getInt("id");
				int userID = rs.getInt("user_id");
				int likes = rs.getInt("likes");
				int parentID = rs.getInt("parent_id");
				String parentType = rs.getString("parentType");
				LocalDateTime date = rs.getTimestamp("date_created").toLocalDateTime();
				String replyText = rs.getString("replyText");
				Reply reply = new Reply(id, userID, likes, parentID, parentType, replyText, date);
				// Insert the reply object into the list
				this.List.add(reply);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Finds and fetches all posts by a specific user
	private void populateList(DatabaseHelper dbHelper) {

		if (!this.postType.equals("") && !this.postType.isEmpty()) {
			// If no post type is specified, we need to search all tables
			// Make search queries for each table
			String searchQueryQ = "SELECT * FROM questions";
			searchQueryQ = buildKeyUser(searchQueryQ + buildParentParameter());
			String searchQueryA = "SELECT * FROM answers";
			searchQueryA = buildKeyUser(searchQueryA + buildParentParameter());
			String searchQueryR = "SELECT * FROM replies";
			searchQueryR = buildKeyUser(searchQueryR + buildParentParameter());

			// Get Questions
			try (PreparedStatement pstmt = connectionQNA.prepareStatement(searchQueryQ)) {
				setParameters(pstmt);
				ResultSet rs = pstmt.executeQuery();
				processResults(dbHelper, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// Get Answers
			try (PreparedStatement pstmt = connectionQNA.prepareStatement(searchQueryA)) {
				setParameters(pstmt);
				ResultSet rs = pstmt.executeQuery();
				processResults(dbHelper, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// Get Replies
			try (PreparedStatement pstmt = connectionQNA.prepareStatement(searchQueryR)) {
				setParameters(pstmt);
				ResultSet rs = pstmt.executeQuery();
				processResults(dbHelper, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else {
			String searchQuery = "";
			searchQuery = buildKeyUser(getTableName() + buildParentParameter());

			try (PreparedStatement pstmt = connectionQNA.prepareStatement(searchQuery)) {
				setParameters(pstmt);
				ResultSet rs = pstmt.executeQuery();
				processResults(dbHelper, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// Helper function that makes the search queries
	// Creates the first part of the query, what database are we looking at. If this
	// is unspecified this part is skipped
	private String getTableName() {
		if (this.postType.equalsIgnoreCase("Question")) {
			return "SELECT * FROM questions";
		} else if (this.postType.equalsIgnoreCase("Answer")) {
			return "SELECT * FROM answers";
		} else if (this.postType.equalsIgnoreCase("Reply")) {
			return "SELECT * FROM replies";
		} else if (this.postType.equals("") && this.postType.isEmpty()) {
			throw new IllegalArgumentException("Invalid postType");
		}
		// If all checks fail, throw out an error
		throw new IllegalArgumentException("Invalid postType: " + this.postType);
	}

	// Builds the first search parameters, the parent paremeters
	private String buildParentParameter() {
		// String to be building the query portion
		// Check again if the parent parameters are properly filled
		// Check if pType is filled
		if (this.pType != null && !this.pType.equals("") && !this.pType.trim().isEmpty()) {
			// Check if qID is filled
			if (pID >= 0) {
				// If the parent is a question then we need a condition for if it is or isn't an
				// answer. If it isn't a question, then it can't be an answer
				if (this.pType.equalsIgnoreCase("Question")) {
					return " WHERE (question_id = ? OR (parent_id = ? AND parentType = ?))";
				}
				// Else return just the or portion
				return "WHERE (parent_id = ? AND parentType = ?)";
			} else {
				return " WHERE parentType = ?";
			}
		}
		return "";
	}

	// Builds the remaining parameters
	private String buildKeyUser(String query) {
		if (this.searchKeyWord.equals("") && (this.searchUserID <= 0)) {
			// If parameters are empty
			return query;
		}
		// Adding in the where/and before next parameter. This is because I call
		// buildParentParameter first so I don't know if WHERE was added or not
		if (!query.contains("WHERE")) {
			query += " WHERE";
		} else {
			query += " AND";
		}

		// Add in the parameters
		// If User is filled
		if (this.searchUserID > 0) {

			query += " user_id = ?";
		}
		// if keyword is filled
		if (!this.searchKeyWord.equals("")) {
			// Check again if user is filled in case we need to add another AND
			if (this.searchUserID > 0) {
				query += " AND";
			}
			query += " (questionText Like ? OR answerText LIKE ? OR replyText LIKE ?)";
		}
		return query;
	}

	// Helper method to set the query parameters based on userID and the search term
	private void setParameters(PreparedStatement pstmt) throws SQLException {
		int index = 1;
		// If there is a parent Type specified (Then the post is either an answer or a
		// reply
		if (((this.pType != null && !this.pType.equalsIgnoreCase("")) && !this.pType.trim().isEmpty())) {
			// If the pID is filled then it can still be a reply or a question, but if it
			// isn't specified then the post is a reply
			if (this.pID >= 0) {// check if the parent is a question object or not, Answers are only to
								// questions and replies are to answers or questions
				if (this.pType.equalsIgnoreCase("Question")) {
					// return " WHERE (question_id = ? OR (parent_id = ? AND parentType = ?))";
					pstmt.setInt(index++, this.pID);
				}
				// Else, must be a reply
				// return "WHERE (parent_id = ? AND parentType = ?)";
				pstmt.setInt(index++, this.pID);
				pstmt.setString(index++, pType);
			}
			// else, it must be a reply of unspecified parent id, so it will retun all
			// replies to this parentType
			// return " WHERE parentType = ?";
			pstmt.setString(index++, pType);
		}
		// else, it is not an answer or a reply, so it must be a question. Look for the
		// user ID
		else if (this.searchUserID > 0) {
			pstmt.setInt(index++, searchUserID);
		}

		// Now that we know whether it is a question, answer, or role, check if there is
		// a set keyword
		if (!((this.searchKeyWord != null && !this.searchKeyWord.equals("")) && !this.searchKeyWord.trim().isEmpty())) {
			pstmt.setString(index++, "%" + searchKeyWord + "%");
			pstmt.setString(index++, "%" + searchKeyWord + "%");
			pstmt.setString(index++, "%" + searchKeyWord + "%");
		}
	}

	// Helper method that extracts the question/answer/reply objects from the search
	// and appends them to the list
	// Made for mixed search, could work for specifed object search
	private void processResults(DatabaseHelper dbHelper, ResultSet rs) throws SQLException {
		while (rs.next()) {
			Integer parentID = rs.getObject("parent_id", Integer.class);
			Integer questionID = rs.getObject("question_id", Integer.class);

			if (questionID != null) {
				// then the gotten item is an answer
				// Build an answer class object and append it to the list
				// If the user role search is not set then everything passes the if statement.
				// If the role search is set then only
				// the ones that match or partially match the parameter get through the if
				// statement
				if ((this.searchRole == null && this.searchRole.equals("")) || this.searchRole.isEmpty()
						|| dbHelper.getUserRole(dbHelper.getUserFromID(dbHelper, rs.getInt("user_id")).getUserName())
								.toLowerCase().contains(this.searchRole.toLowerCase())) {
					// I don't have a way to easily get the user's role through just the ID, so I
					// have a bunch of functions reading eachother's output to get the userID
					// Build Answer
					boolean isRelevant = rs.getBoolean("isRelevant");
					boolean official = rs.getBoolean("isOfficial");
					int id = rs.getInt("id");
					// Integer questionID
					int userID = rs.getInt("user_id");
					int likes = rs.getInt("likes");
					int dislikes = rs.getInt("dislikes");
					LocalDateTime date = rs.getTimestamp("date_created").toLocalDateTime();
					String answerText = rs.getString("answerText");
					Answer answer = new Answer(isRelevant, official, id, questionID, userID, likes, dislikes,
							answerText, date);

					// Add answer to list
					this.List.add(answer);
				}
			} else if (parentID != null) {
				// then the gotten item is a reply
				// Build a reply class object and append it to the list
				// If the user role search is not set then everything passes the if statement.
				// If the role search is set then only
				// the ones that match or partially match the parameter get through the if
				// statement
				if ((this.searchRole == null && this.searchRole.equals("")) || this.searchRole.isEmpty()
						|| dbHelper.getUserRole(dbHelper.getUserFromID(dbHelper, rs.getInt("user_id")).getUserName())
								.toLowerCase().contains(this.searchRole.toLowerCase())) {
					// Repeat of (questionID != null)'s search role checker
					// Build Reply
					int id = rs.getInt("id");
					int userID = rs.getInt("user_id");
					int likes = rs.getInt("likes");
					// Integer parentID
					LocalDateTime date = rs.getTimestamp("date_created").toLocalDateTime();
					String parentType = rs.getString("parentType");
					String replyText = rs.getString("replyText");
					Reply reply = new Reply(id, userID, likes, parentID, parentType, replyText, date);

					// Add reply to list
					this.List.add(reply);
				}
			} else {
				// if it isn't an answer or a reply then it must be a question
				// Build a question class object and append it to the list
				// If the user role search is not set then everything passes the if statement.
				// If the role search is set then only
				// the ones that match or partially match the parameter get through the if
				// statement
				if ((this.searchRole == null && this.searchRole.equals("")) || this.searchRole.isEmpty()
						|| dbHelper.getUserRole(dbHelper.getUserFromID(dbHelper, rs.getInt("user_id")).getUserName())
								.toLowerCase().contains(this.searchRole.toLowerCase())) {
					// Repeat of the function again
					// Build answer
					boolean open = rs.getBoolean("isOpen");
					int id = rs.getInt("id");
					int userID = rs.getInt("user_id");
					int likes = rs.getInt("likes");
					int views = rs.getInt("views");
					LocalDateTime date = rs.getTimestamp("date_created").toLocalDateTime();
					String questionText = rs.getString("questionText");
					Question question = new Question(open, id, userID, likes, views, questionText, date);

					// Add question to the list
					this.List.add(question);
				}
			}
		}
	}

	private void sort(String sort, DatabaseHelper dbHelper) {
		/*
		 * Sort option includes: most/least recently made, (getDateCreated) most/least
		 * likes, (if Answer then getLikes - getDislikes, else getLikes) author id in
		 * incremental order and decremental order, (getAuthorID) author
		 * alphabetically/reverse alphabetically, (Gets username through dbHelper
		 * function getUserFromID) Post content alphabetically/reverse alphabetically,
		 * (getContent) post id in incremental order and decremental order, (getID)
		 * Most/Least controversal/diverse opinionated, (if Answer then |getLikes -
		 * getDislikes|, else getLikes. 0 likes and dislikes sent to bottom) and default
		 * say problem to console
		 */
		switch (sort.toLowerCase()) {
		// Sort by the oldest of the post's dates
		case "oldest":
			this.List.sort(Comparator.comparing(Post::getDateCreated));
			break;
		// Sort by the newest of the post's dates
		case "newest":
			this.List.sort(Comparator.comparing(Post::getDateCreated).reversed());
			break;
		case "most_liked":
			// Because Answer has likes and dislikes, we need to check if each object is an
			// answer or not when sorting this
			this.List.sort((post1, post2) -> { // Get two posts at a time and compares them. This setup allows for
												// getting and changing values as needed
				// Handle post1
				int score1 = post1.getLikes(); // Defaults to just getting likes for regular posts
				if (post1 instanceof Answer) {
					// If this post is an answer object, subtracts dislikes from likes to get the
					// general feeling of the score
					score1 -= ((Answer) post1).getDislikes();
				}

				// Handle post2
				int score2 = post2.getLikes();// Defaults to just getting likes for regular posts
				if (post2 instanceof Answer) {
					// If this post is an answer object, subtracts dislikes from likes to get the
					// general feeling of the score
					score2 -= ((Answer) post2).getDislikes();
				}

				// compare the scores. Since we are getting the most likes, we input the scores
				// in reverse order of the post order
				return Integer.compare(score2, score1);
			});
			break;
		case "least_liked":
			// Because Answer has likes and dislikes, we need to check if each object is an
			// answer or not when sorting this
			this.List.sort((post1, post2) -> { // Get two posts at a time and compares them. This setup allows for
												// getting and changing values as needed
				// Handle post1
				int score1 = post1.getLikes(); // Defaults to just getting likes for regular posts
				if (post1 instanceof Answer) {
					// If this post is an answer object, subtracts dislikes from likes to get the
					// general feeling of the score
					score1 -= ((Answer) post1).getDislikes();
				}

				// Handle post2
				int score2 = post2.getLikes();// Defaults to just getting likes for regular posts
				if (post2 instanceof Answer) {
					// If this post is an answer object, subtracts dislikes from likes to get the
					// general feeling of the score
					score2 -= ((Answer) post2).getDislikes();
				}

				// compare the scores. Since we are getting the least likes, we input the scores
				// in the same order as the post order
				return Integer.compare(score1, score2);
			});
			break;
		case "userid_greatest_first":
			this.List.sort(Comparator.comparing(Post::getAuthorID).reversed());
			break;
		case "userid_least_first":
			this.List.sort(Comparator.comparing(Post::getAuthorID));
			break;
		case "user_alphabetically":
			// We are using sort((post1, post2) -> {}) again because we need to run a
			// external function to get the usernames
			this.List.sort((post1, post2) -> {
				// Get usernames from the database using the AuthorIDs
				String name1 = dbHelper.getUserFromID(dbHelper, post1.getAuthorID()).getUserName();
				String name2 = dbHelper.getUserFromID(dbHelper, post2.getAuthorID()).getUserName();

				// Compare the usernames alphabetically. Same order as post1 and post2 to get
				// a-z order returned
				return name1.compareTo(name2);
			});
			break;
		case "user_reverse_alphabetically":
			// We are using sort((post1, post2) -> {}) again because we need to run a
			// external function to get the usernames
			this.List.sort((post1, post2) -> {
				// Get usernames from the database using the AuthorIDs
				String name1 = dbHelper.getUserFromID(dbHelper, post1.getAuthorID()).getUserName();
				String name2 = dbHelper.getUserFromID(dbHelper, post2.getAuthorID()).getUserName();

				// Compare the usernames alphabetically. Reverse order to post1 and post2 to get
				// z-a order returned
				return name2.compareTo(name1);
			});
			break;
		case "alphabetically":
			this.List.sort(Comparator.comparing(Post::getContent));
			break;
		case "reverse_alphabetically":
			this.List.sort(Comparator.comparing(Post::getContent).reversed());
			break;
		case "post_id":
			this.List.sort(Comparator.comparing(Post::getID));
			break;
		case "reverse_post_id":
			this.List.sort(Comparator.comparing(Post::getID).reversed());
			break;
		case "most_controversial": // Sort by the greatest difference between likes and dislikes. Similar to
									// sorting by likes, but not exactly the same
			// Note, I mean most diverse opinion
			this.List.sort((post1, post2) -> {
				// Get Controversy scores
				// Load in individual likes and dislikes
				int likes1 = post1.getLikes();
				int dislikes1 = (post1 instanceof Answer) ? ((Answer) post1).getDislikes() : 0;
				int likes2 = post2.getLikes();
				int dislikes2 = (post2 instanceof Answer) ? ((Answer) post2).getDislikes() : 0;
				// Get the controversies of each post
				int score1 = Math.abs(likes1 - dislikes1);
				int score2 = Math.abs(likes2 - dislikes2);
				// Get the total engagement of each post
				int total1 = likes1 + dislikes1;
				int total2 = likes2 + dislikes2;

				// Push posts with o engagement to the bottom
				if (total1 == 0 && total2 > 0)
					return 1;
				if (total1 > 0 && total2 == 0)
					return -1;
				if (total1 == 0 && total2 == 0)
					return 0;

				// Sort by controversy (lower |likes - dislikes| is more controversial
				if (score1 != score2)
					return Integer.compare(score1, score2);

				// If controversy is the same, sort by total engagement (higher first)
				return Integer.compare(total2, total1);
			});
			break;
		case "least_contraversial":// Sort by the greatest difference between likes and dislikes. Similar to
									// sorting by likes, but not exactly the same
			// Note, I mean least diverse opinion
			this.List.sort((post1, post2) -> {
				// Get Controversy scores
				// Load in individual likes and dislikes
				int likes1 = post1.getLikes();
				int dislikes1 = (post1 instanceof Answer) ? ((Answer) post1).getDislikes() : 0;
				int likes2 = post2.getLikes();
				int dislikes2 = (post2 instanceof Answer) ? ((Answer) post2).getDislikes() : 0;
				// Get the controversies of each post
				int score1 = Math.abs(likes1 - dislikes1);
				int score2 = Math.abs(likes2 - dislikes2);
				// Get the total engagement of each post
				int total1 = likes1 + dislikes1;
				int total2 = likes2 + dislikes2;

				// Push posts with o engagement to the bottom
				if (total1 == 0 && total2 > 0)
					return -1;
				if (total1 > 0 && total2 == 0)
					return 1;
				if (total1 == 0 && total2 == 0)
					return 0;

				// Sort by controversy (lower |likes - dislikes| is more controversial
				if (score1 != score2)
					return Integer.compare(score2, score1);

				// If controversy is the same, sort by total engagement (lower first)
				return Integer.compare(total1, total2);
			});
			break;
		default: // Default case for unrecognized sort types. By default it outputs to the
					// console then breaks
			// Either use a default sort option, or log an error
			System.out.println("Invalid sort type: " + sort);
			break;
		}
	}

	// sorts the lists by booleans
	private void prioritize(String priority) {
		switch (priority.toLowerCase()) {
		case "relevence_true":
			// Answer Only, need to check first if these are answer objects
			this.List.sort((post1, post2) -> {
				// Handle post1 (If the post is isRelevant, it will be assigned the number 1,
				// else it will be assigned a 0)
				int score1 = 0; // Default to 0/false for non-Answer posts. non-Answer posts can't be isRelevant
				if (post1 instanceof Answer) {
					score1 = ((Answer) post1).getRelevant() ? 1 : 0; // If post1 is relevant, score1 = 1
				}

				// Handle post2
				int score2 = 0; // Default to 0/false for non-answer posts.
				if (post2 instanceof Answer) {
					score2 = ((Answer) post2).getRelevant() ? 1 : 0; // If post2 is relavent, score2 = 1
				}

				// Compare the relevance scores
				return Integer.compare(score2, score1); // Sort by relevance (true first)
			});
			break;
		case "relevence_false":
			// Answer Only, need to check first if these are answer objects
			this.List.sort((post1, post2) -> {
				// Handle post1 (If the post is relevant, it will be assigned the number 1, else
				// it will be assigned a 0)
				int score1 = 0; // Default to 0/false for non-Answer posts. non-Answer posts can't be relevant
				if (post1 instanceof Answer) {
					score1 = ((Answer) post1).getRelevant() ? 1 : 0; // If post1 is relevant, score1 = 1
				}

				// Handle post2
				int score2 = 0; // Default to 0/false for non-answer posts.
				if (post2 instanceof Answer) {
					score2 = ((Answer) post2).getRelevant() ? 1 : 0; // If post2 is relevant, score2 = 1
				}

				// Compare the relevence scores
				return Integer.compare(score1, score2); // Sort by relevance (false first)
			});
			break;
		case "official_true":
			// Answer Only, need to check first if these are answer objects
			this.List.sort((post1, post2) -> {
				// Handle post1 (If the post is official, it will be assigned the number 1, else
				// it will be assigned a 0)
				int score1 = 0; // Default to 0/false for non-Answer posts. non-Answer posts can't be official
				if (post1 instanceof Answer) {
					score1 = ((Answer) post1).getOfficial() ? 1 : 0; // If post1 is official, score1 = 1
				}

				// Handle post2
				int score2 = 0; // Default to 0/false for non-answer posts.
				if (post2 instanceof Answer) {
					score2 = ((Answer) post2).getOfficial() ? 1 : 0; // If post2 is official, score2 = 1
				}

				// Compare the official scores
				return Integer.compare(score2, score1); // Sort by official (true first)
			});
			break;
		case "official_false":
			// Answer Only, need to check first if these are answer objects
			this.List.sort((post1, post2) -> {
				// Handle post1 (If the post is official, it will be assigned the number 1, else
				// it will be assigned a 0)
				int score1 = 0; // Default to 0/false for non-Answer posts. non-Answer posts can't be official
				if (post1 instanceof Answer) {
					score1 = ((Answer) post1).getOfficial() ? 1 : 0; // If post1 is official, score1 = 1
				}

				// Handle post2
				int score2 = 0; // Default to 0/false for non-answer posts.
				if (post2 instanceof Answer) {
					score2 = ((Answer) post2).getOfficial() ? 1 : 0; // If post2 is official, score2 = 1
				}

				// Compare the official scores
				return Integer.compare(score1, score2); // Sort by official (false first)
			});
			break;
		case "open_true":
			// Question Only, need to check if these are answer objects
			this.List.sort((post1, post2) -> {
				// Handle post1 (if the post is open, it will be assigned the number 1, else it
				// will be assigned a 0)
				int score1 = 0; // Default to 0/false for non-Question posts. non-question posts aren't open or
								// closed
				if (post1 instanceof Question) {
					score1 = ((Question) post1).getIsOpen() ? 1 : 0; // If post1 is open, score1 = 1
				}

				// Handle post2
				int score2 = 0; // Default to 0/false for non-question posts
				if (post2 instanceof Question) {
					score2 = ((Question) post2).getIsOpen() ? 1 : 0; // If post1 is open, score1 = 1
				}

				// compare the Open scores
				return Integer.compare(score2, score1); // Sort by open (true first)
			});
			break;
		case "open_false":
			// Question Only, need to check if these are answer objects
			this.List.sort((post1, post2) -> {
				// Handle post1 (if the post is open, it will be assigned the number 1, else it
				// will be assigned a 0)
				int score1 = 0; // Default to 0/false for non-Question posts. non-question posts aren't open or
								// closed
				if (post1 instanceof Question) {
					score1 = ((Question) post1).getIsOpen() ? 1 : 0; // If post1 is open, score1 = 1
				}

				// Handle post2
				int score2 = 0; // Default to 0/false for non-question posts
				if (post2 instanceof Question) {
					score2 = ((Question) post2).getIsOpen() ? 1 : 0; // If post1 is open, score1 = 1
				}

				// compare the Open scores
				return Integer.compare(score1, score2); // Sort by open (false first)
			});
			break;
		default: // If the given thing matches no caases, say so in the console and do no sorting
			System.out.println("Invalid sort type: " + priority);
			break;
		}
	}
}