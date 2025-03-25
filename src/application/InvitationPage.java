package application;

import databasePart1.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InvitePage class represents the page where an admin can generate an
 * invitation code. The invitation code is displayed upon clicking a button.
 */

public class InvitationPage {

	/**
	 * Displays the Invite Page in the provided primary stage.
	 * 
	 * @param databaseHelper An instance of DatabaseHelper to handle database
	 *                       operations.
	 * @param primaryStage   The primary stage where the scene will be displayed.
	 * @param oldScene       Keeps the scene data of the previous scene to power a
	 *                       back button
	 */
	public void show(DatabaseHelper databaseHelper, Stage primaryStage, Scene oldScene) {
		VBox layout = new VBox();
		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

		// Label to display the title of the page
		Label userLabel = new Label("Invite ");
		userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		// Button to generate the invitation code
		Button showCodeButton = new Button("Generate Invitation Code");

		// Button to return to previous page
		Button backButton = new Button("Back");

		// Label to display the generated invitation code
		TextField inviteCodeLabel = new TextField("");
		inviteCodeLabel
				.setStyle("-fx-font-size: 14px; " + "-fx-font-style: italic; " + "-fx-background-color: transparent; "
						+ "-fx-border-width: 0; " + "-fx-text-fill: black; " + "-fx-alignment: center;");
		inviteCodeLabel.setEditable(false);

		showCodeButton.setOnAction(a -> {
			// Generate the invitation code using the databaseHelper and set it to the label
			String invitationCode = databaseHelper.generateInvitationCode();
			inviteCodeLabel.setText(invitationCode);
		});

		backButton.setOnAction(a -> {
			primaryStage.setScene(oldScene);
		});

		Button quitButton = new Button("Quit");
		quitButton.setOnAction(a -> {
			databaseHelper.closeConnection();
			Platform.exit(); // Exit the JavaFX application
		});

		layout.getChildren().addAll(userLabel, showCodeButton, inviteCodeLabel, backButton, quitButton);
		Scene inviteScene = new Scene(layout, 800, 400);

		// Set the scene to primary stage
		primaryStage.setScene(inviteScene);
		primaryStage.setTitle("Invite Page");

	}
}