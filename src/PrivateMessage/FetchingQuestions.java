package PrivateMessage;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import qnaClasses.Post;
import qnaClasses.PostList;
import qnaClasses.Question;
import databasePart1.DatabaseHelper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FetchingQuestions {

	private Scene questionScene;
	private int currentUserID;
	private DatabaseHelper databaseHelper;

	public FetchingQuestions(DatabaseHelper databaseHelper, int currentUserID) {
		this.currentUserID = currentUserID;
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage) {
		questionScene = createQscene(primaryStage);
		primaryStage.setScene(questionScene);
		primaryStage.setTitle("Your Messages");
		primaryStage.show();
	}

	private Scene createQscene(Stage primaryStage) {
		VBox qmPane = new VBox(10);
		qmPane.setStyle("-fx-padding: 20;");
		qmPane.setAlignment(Pos.CENTER);

		Label header = new Label("Your Questions & Messages");
		header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		TreeView<String> treeView = new TreeView<>();
		TreeItem<String> rootItem = new TreeItem<>("All Questions");
		rootItem.setExpanded(true);
		treeView.setRoot(rootItem);

		// Populate the tree with the user's questions
		refreshTree(rootItem);

		// Handle double-click to open the messaging view for the selected question
		treeView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
				if (selectedItem != null && selectedItem != rootItem) {
					int questionId = parseId(selectedItem.getValue());
					if (questionId != -1) {
						openMessageView(primaryStage, questionId);
					}
				}
			}
		});

		Button logoutButton = new Button("Logout");
		logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
		logoutButton.setOnAction(a -> primaryStage.close());

		qmPane.getChildren().addAll(header, treeView, logoutButton);
		return new Scene(qmPane, 600, 400);
	}

	/**
	 * Fetches all questions posted by the current user from the database and adds
	 * them as child nodes under the rootItem in the TreeView.
	 */
	private void refreshTree(TreeItem<String> rootItem) {
		rootItem.getChildren().clear();
		// Retrieve questions for the logged-in user
		List<Post> questions = new PostList(databaseHelper, currentUserID, -1, "", "", "", "Question").getList();
		for (Post question : questions) {
			// Each question might look like: "Question 12 by userName: [question text]"
			// We'll add it as-is to the tree
			TreeItem<String> questionItem = new TreeItem<>(((Question) question).toString(databaseHelper));
			rootItem.getChildren().add(questionItem);
		}
	}

	/**
	 * Opens the MessagingFrontEnd for the selected question ID.
	 */
	private void openMessageView(Stage primaryStage, int questionId) {
		try {
			// Retrieve all message senders (excluding the current user)
			ObservableList<String> senders = databaseHelper.getMessageSendersForQuestion(questionId, currentUserID);
			if (senders.isEmpty()) {
				showAlert("Error", "No messages from other users for this question.");
				return;
			}

			String chosen;
			if (senders.size() == 1) {
				chosen = senders.get(0);
			} else {
				ChoiceDialog<String> dialog = new ChoiceDialog<>(senders.get(0), senders);
				dialog.setTitle("Choose Chat Partner");
				dialog.setHeaderText("Select a user to chat with for this question:");
				dialog.setContentText("User:");
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					chosen = result.get();
				} else {
					return;
				}
			}

			// Extract the sender id from the chosen string (format: "userName (senderId)")
			int start = chosen.indexOf('(');
			int end = chosen.indexOf(')');
			int otherUserId = -1;
			if (start != -1 && end != -1) {
				String idStr = chosen.substring(start + 1, end);
				otherUserId = Integer.parseInt(idStr);
			}
			if (otherUserId == -1) {
				showAlert("Error", "Invalid user selected.");
				return;
			}

			// Open the messaging interface with the selected user
			MessagingFrontEnd messagePage = new MessagingFrontEnd(databaseHelper, currentUserID, otherUserId,
					questionId);
			messagePage.show(primaryStage);
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Error", "Could not retrieve messages.");
		}
	}

	/**
	 * Parses the second token of a string like: "Question 12 by someUser: question
	 * text" and returns 12 as the question ID.
	 */

	private int parseId(String text) {
		try {
			return Integer.parseInt(text.split(" ")[1]);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}