package PrivateMessage;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.sql.SQLException;

public class FetchingRepliesForQuestions {

	private Scene questionScene;
	private int currentUserID;
	private DatabaseHelper databaseHelper;

	public FetchingRepliesForQuestions(DatabaseHelper databaseHelper, int currentUserID) {
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
					int otherUserID = databaseHelper.getUserID(parseUser(selectedItem.getValue()));
					if (questionId != -1) {
						openMessageView(primaryStage, otherUserID, questionId);
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
		try {
			// Instead of getUserQuestions, call our new method:
			ObservableList<String> repliedQuestions = databaseHelper.getPrivatelyRepliedQuestionsByUser(currentUserID);

			for (String question : repliedQuestions) {
				// question will look like: "Question 12 by userX: question text"
				TreeItem<String> questionItem = new TreeItem<>(question);
				rootItem.getChildren().add(questionItem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Error", "Failed to retrieve questions you have replied to.");
		}
	}

	/**
	 * Opens the MessagingFrontEnd for the selected question ID.
	 */
	private void openMessageView(Stage primaryStage, int otherUserID, int questionId) {
		// If you (currentUserID) == questionOwner (otherUserId),
		// your refreshTree(...) will fetch everything for that question.
		MessagingFrontEnd messagePage = new MessagingFrontEnd(databaseHelper, currentUserID, otherUserID, questionId);
		messagePage.show(primaryStage);
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

	private String parseUser(String text) {
		try {
			return text.split("by ")[1].split(":")[0].trim();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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