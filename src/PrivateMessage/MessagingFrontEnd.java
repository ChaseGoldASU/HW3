package PrivateMessage;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.sql.SQLException;

public class MessagingFrontEnd {

	private PrivateMessageBackend pmb;
	private Scene pvtscene;
	private int userID;
	private int otherUserID;
	private int questionID; // Store the related question ID
	private DatabaseHelper databaseHelper;

	public MessagingFrontEnd(DatabaseHelper databaseHelper, int userID, int otherUserID, int questionID) {
		pmb = new PrivateMessageBackend(databaseHelper);
		this.userID = userID;
		this.databaseHelper = databaseHelper;
		this.otherUserID = otherUserID;
		this.questionID = questionID;
		pmb.setuserID(userID);
	}

	public void show(Stage primaryStage) {
		pvtscene = createpvtscene(primaryStage);
		primaryStage.setScene(pvtscene);
		primaryStage.setTitle("Private Messaging");
		primaryStage.show();
	}

	private Scene createpvtscene(Stage primaryStage) {
		VBox pvtPane = new VBox(10);
		pvtPane.setStyle("-fx-padding: 20;");
		pvtPane.setAlignment(Pos.CENTER);

		// Fetch and display the question at the top
		String questionText = getQuestionText();
		Label questionLabel = new Label("Question: " + questionText);
		questionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		TreeView<String> treeView = new TreeView<>();
		TreeItem<String> rootItem = new TreeItem<>("Messages");
		rootItem.setExpanded(true);
		treeView.setRoot(rootItem);
		refreshTree(rootItem);

		Button newConvoThread = new Button("Post New Message");
		newConvoThread.setOnAction(e -> {
			if (userID == -1) {
				showAlert("Error", "No user logged in.");
				return;
			}
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("New Message");
			dialog.setHeaderText("Enter your Message:");
			dialog.setContentText("Message:");
			dialog.showAndWait().ifPresent(text -> {
				int messageId = pmb.sendMessage(userID, otherUserID, questionID, text);
				if (messageId != -1) {
					showAlert("Success", "Message added with ID " + messageId);
					refreshTree(rootItem);
				}
			});
		});

		// --- Logout ---
		Button logoutButton = new Button("Logout");
		logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
		logoutButton.setOnAction(a -> primaryStage.close());

		pvtPane.getChildren().addAll(questionLabel, new Label("Messaging Page"), treeView, newConvoThread,
				logoutButton);

		return new Scene(pvtPane, 600, 400);
	}

	private void refreshTree(TreeItem<String> rootItem) {
		rootItem.getChildren().clear();
		try {
			ObservableList<String> messages;

			// If the user is the same as the question's owner,
			// show *all* messages for the question.
			if (userID == otherUserID) {
				messages = databaseHelper.getMessagesWhenSendingSelfMessage(databaseHelper, userID, questionID);
			} else {
				// Otherwise, show only the "private" A↔B conversation
				messages = databaseHelper.getAllChatMessages(userID, otherUserID, questionID);
			}

			for (String msg : messages) {
				TreeItem<String> messageItem = new TreeItem<>(msg);
				rootItem.getChildren().add(messageItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getQuestionText() {
		try {
			return databaseHelper.getQuestionText(userID, questionID);
		} catch (SQLException e) {
			e.printStackTrace();
			return "Error fetching question.";
		}
	}

	private int parseId(String text) {
		try {
			String[] parts = text.split(" ");
			String idStr = parts[1];
			return Integer.parseInt(idStr);
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
