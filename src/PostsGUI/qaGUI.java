package PostsGUI;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import databasePart1.*;
import qnaClasses.*;
import PrivateMessage.*;

public class qaGUI {

	// private backendQA back;
	private Scene qaScene;
	private int userID; // Store the logged-in user's id
	private DatabaseHelper databaseHelper;

	// Updated constructor
	public qaGUI(DatabaseHelper databaseHelper, int userID) {
		this.databaseHelper = databaseHelper;
		this.userID = userID;
	}

	public void show(Stage primaryStage) {
		qaScene = createQAScene(primaryStage);
		primaryStage.setScene(qaScene);
		primaryStage.setTitle("Q&A Application");
		primaryStage.show();
	}

	// --- Creates the Q&A Scene ---
	private Scene createQAScene(Stage primaryStage) {
		VBox qaPane = new VBox(10);
		qaPane.setStyle("-fx-padding: 20;");
		qaPane.setAlignment(Pos.CENTER);

		TreeView<String> treeView = new TreeView<>();
		TreeItem<String> rootItem = new TreeItem<>("Posts");
		rootItem.setExpanded(true);
		treeView.setRoot(rootItem);
		refreshTree(rootItem);

		treeView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
				if (selectedItem != null && selectedItem != rootItem) {
					String selectedText = selectedItem.getValue();
					String type = "";
					if (selectedText.startsWith("Question"))
						type = "Question";
					else if (selectedText.startsWith("Answer"))
						type = "Answer";
					else if (selectedText.startsWith("Reply"))
						type = "Reply";
					// else if (selectedText.startsWith("Message")) type = "message";

					int id = parseId(selectedText);
					try {
						showReplyDialog(id, type, primaryStage);
						refreshTree(rootItem);
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		MenuButton buttonSelection = new MenuButton("Choose what you want to do");

		// MenuItems for each option:
		MenuItem ReplyPrivately = new MenuItem("Reply Privately");
		MenuItem CheckMessagesQA = new MenuItem("Check Messages for your questions");
		MenuItem CheckRepliesQA = new MenuItem("Check replies to your QA pvt responses");
		MenuItem RefreshQA = new MenuItem("Refresh");

		// Add the MenuItems to the MenuButton:
		buttonSelection.getItems().addAll(ReplyPrivately, CheckMessagesQA, CheckRepliesQA, RefreshQA);

		// Define what happens when each option is selected:
		ReplyPrivately.setOnAction(e -> {
			// Get the currently selected item from the main treeView:
			TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem != rootItem && selectedItem.getValue().startsWith("Question")) {
				int questionId = parseId(selectedItem.getValue());
				int userID = parseUserID(selectedItem.getValue());
				MessagingFrontEnd pvtPage = new MessagingFrontEnd(databaseHelper, this.userID, userID, questionId);
				pvtPage.show(primaryStage);
			} else {
				showAlert("Error", "Please select a question to reply privately.");
			}
		});

		CheckMessagesQA.setOnAction(e -> {
			try {
				FetchingQuestions fetchPage = new FetchingQuestions(databaseHelper, userID);
				fetchPage.show(primaryStage);
			} catch (Exception ex) {
				ex.printStackTrace();
				showAlert("Error", "Failed to retrieve messages.");
			}
		});

		CheckRepliesQA.setOnAction(e -> {
			try {
				FetchingRepliesForQuestions fetchReplyPage = new FetchingRepliesForQuestions(databaseHelper, userID);
				fetchReplyPage.show(primaryStage);
			} catch (Exception ex) {
				ex.printStackTrace();
				showAlert("Error", "Failed to retrieve QA replies.");
			}
		});

		RefreshQA.setOnAction(e -> {
			refreshTree(rootItem);
		});

		// --- Post New Question ---
		Button postQuestionButton = new Button("Post New Question");
		postQuestionButton.setOnAction(e -> {
			if (userID == -1) {
				showAlert("Error", "No user logged in.");
				return;
			}
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("New Question");
			dialog.setHeaderText("Enter your question:");
			dialog.setContentText("Question:");
			dialog.showAndWait().ifPresent(text -> {
				try {
					Question question = new Question(userID, text, databaseHelper);
					int newQuestionId = question.getID();
					databaseHelper.newQuestion(databaseHelper, question);
					if (newQuestionId != -1) {
						showAlert("Success", "Question added with ID " + newQuestionId);
						refreshTree(rootItem);
					}
				} catch (SQLException ex) {

				}
			});
		});

		// --- Delete Selected Post ---
//        Button deleteButton = new Button("Delete Selected");
//        deleteButton.setOnAction(e -> {
//            
//        });

		// --- Edit Selected Question ---
		Button editButton = new Button("Edit Selected Question");
		editButton.setOnAction(e -> {
			TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem != rootItem && selectedItem.getValue().startsWith("Question")) {
				int id = parseId(selectedItem.getValue());
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Edit Question");
				dialog.setHeaderText("Enter new text for the question:");
				dialog.setContentText("New Question Text:");
				dialog.showAndWait().ifPresent(newText -> {
					if (newText.isEmpty()) {
						showAlert("Error", "New text cannot be empty.");
					} else {
						boolean success = databaseHelper.updateQuestionText(databaseHelper, id, userID, newText);
						if (success) {
							showAlert("Success", "Question updated successfully!");
							refreshTree(rootItem);
						} else {
							showAlert("Error", "Failed to update question.");
						}
					}
				});
			} else {
				showAlert("Error", "Please select a question to edit.");
			}
		});

		// --- Logout ---
		Button logoutButton = new Button("Logout");
		logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
		logoutButton.setOnAction(a -> primaryStage.close());

		qaPane.getChildren().addAll(new Label("Q&A Page"), treeView, postQuestionButton, editButton, logoutButton,
				buttonSelection);

		return new Scene(qaPane, 600, 400);
	}

	// --- Refresh the TreeView ---
	private void refreshTree(TreeItem<String> rootItem) {
		rootItem.getChildren().clear();
		try {
			// Fetch all questions - PostList(databaseObject, userID (0 means unspecified),
			// parentID (-1 means no single parent specified), parentType ("" means no
			// parent), search, role, postType
			List<Post> questions = new PostList(databaseHelper, userID, -1, "", "", "", "Question").getList();

			// Load all answers and replies and load them into hash tables. This makes less
			// database calls which are slower than a hash table lookup
			// Make hash tables, key format is ParentType-PostID
			Map<Integer, Post> answersToQuestion = new HashMap<>();
			// Key = ParentType-ParentID
			Map<String, Post> allReplies = new HashMap<>();
			// Load all replies to the questions
			// Load all replies and the parents they match
			for (Post rep : new PostList(databaseHelper, 0, -1, "Question", "", "", "Reply").getList()) {
				allReplies.put(((Reply) rep).getParentType() + "-" + ((Reply) rep).getParentID(), rep);
			}
			// Load all answers that match the question into the hash
			for (Post ans : new PostList(databaseHelper, 0, -1, "", "", "", "Answer").getList()) {
				answersToQuestion.put(((Answer) ans).getQuestionID(), ans);
			}

			// Iterate through all questions and add them to the tree
			for (Post q : questions) {
				int questionID = q.getID();
				TreeItem<String> questionItem = new TreeItem<>(q.toString(databaseHelper));

				// Add replies to the questionItem
				if (allReplies.containsKey("Question-" + questionID)) {
					TreeItem<String> replyItem = new TreeItem<>(
							allReplies.get("Question-" + questionID).toString(databaseHelper));

					// Add all replies to currently loaded replies
					int replyID = allReplies.get("Question-" + questionID).getID();
					if (allReplies.containsKey("Reply-" + replyID)) {
						TreeItem<String> replyItem2 = new TreeItem<>(
								allReplies.get("Reply-" + replyID).toString(databaseHelper));
						replyItem.getChildren().add(replyItem2);
					}

				}

				// Add answers to the questionItem
				if (answersToQuestion.containsKey(questionID)) {
					TreeItem<String> answerItem = new TreeItem<>(
							answersToQuestion.get(questionID).toString(databaseHelper));

					// Add All replies to the answer
					int answerID = answersToQuestion.get(questionID).getID();
					if (allReplies.containsKey("Answer-" + answerID)) {
						TreeItem<String> replyItems = new TreeItem<>(
								allReplies.get("Answer-" + answerID).toString(databaseHelper));

						// Add all replies to currently loaded answer replies
						int replyID = allReplies.get("Question-" + answerID).getID();
						if (allReplies.containsKey("Reply-" + replyID)) {
							TreeItem<String> replyItem2 = new TreeItem<>(
									allReplies.get("Reply-" + replyID).toString(databaseHelper));
							replyItems.getChildren().add(replyItem2);
						}
						answerItem.getChildren().add(replyItems);
					}
					questionItem.getChildren().add(answerItem);
				}
				rootItem.getChildren().add(questionItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int parseId(String text) {
		try {
			return Integer.parseInt(text.split(" ")[1]);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private int parseUserID(String text) {
		try {
			return Integer.parseInt(text.split("by AuthorName ")[1].split(",")[0].trim());
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private void showReplyDialog(int id, String type, Stage primaryStage) throws SQLException {
		ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Answer", "Answer", "Reply");
		choiceDialog.setTitle("Choose Action");
		choiceDialog.setHeaderText("Choose your action for this " + type + ":");
		choiceDialog.setContentText("Select:");
		choiceDialog.showAndWait().ifPresent(choice -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle(choice);
			dialog.setHeaderText("Enter your " + choice + " for the " + type + ":");
			dialog.setContentText("Text:");
			dialog.showAndWait().ifPresent(text -> {
				if (choice.equals("Answer")) {
					try {
						Answer answer = new Answer(userID, id, text, databaseHelper);
						int newAnswerId = answer.getID();
						databaseHelper.newAnswer(databaseHelper, answer);
						if (newAnswerId != -1)
							showAlert("Success", "Answer added with ID " + newAnswerId);
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				} else if (choice.equals("Reply")) {
					try {
						Reply reply = new Reply(userID, id, type, text, databaseHelper);
						int newReplyId = reply.getID();
						databaseHelper.newReply(databaseHelper, reply);
						if (newReplyId != -1)
							showAlert("Success", "Reply added with ID " + newReplyId);
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
			});
		});
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
