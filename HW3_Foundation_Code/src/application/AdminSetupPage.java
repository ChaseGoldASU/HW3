package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;

import UserPassEvaluators.PasswordEvaluator;
import UserPassEvaluators.UserNameRecognizer;
import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator
 * account. This is intended to be used by the first user to initialize the
 * system with admin credentials.
 */
public class AdminSetupPage {

	private final DatabaseHelper databaseHelper;

	public AdminSetupPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage) {
		// Input fields for userName and password
		TextField userNameField = new TextField();
		userNameField.setPromptText("Enter Admin userName");
		userNameField.setMaxWidth(250);

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter Password");
		passwordField.setMaxWidth(250);

		// Debug tool to ensure that the correct password is seen as correct
		Label sucessLabel = new Label();
		sucessLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");

		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

		Button setupButton = new Button("Setup");

		setupButton.setOnAction(a -> {
			// Retrieve user input
			String userName = userNameField.getText();
			String password = passwordField.getText();
			String usernameErrMessage = UserNameRecognizer.checkForValidUserName(userName);
			String passwordErrMessage = PasswordEvaluator.evaluatePassword(password);
			try {
				if (usernameErrMessage == "") {
					sucessLabel.setText("Username Valid");
					errorLabel.setText("");

					// check is password is valid
					if (passwordErrMessage == "") {
						sucessLabel.setText("Username Valid\nPasswordValid");
						errorLabel.setText("");

						// Create a new User object with admin role and register in the database
						User user = new User(userName, password, "admin");
						databaseHelper.register(user);
						System.out.println("Administrator setup completed.");

						// Navigate to the Welcome Login Page
						new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
					} else { // If password is invalid
						errorLabel.setText("This password is invalid because:\n" + passwordErrMessage);
					}
				} else { // If userName is invalid
					if (UserNameRecognizer.userNameRecognizerIndexofError <= -1)
						return; // Should never happen
					sucessLabel.setText("");
					errorLabel.setText("This username is invalid because:\n" + usernameErrMessage
							+ userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6");
				}
			} catch (SQLException e) {
				System.err.println("Database error: " + e.getMessage());
				e.printStackTrace();
			}
		});

		HBox passFail = new HBox(5);
		passFail.getChildren().addAll(errorLabel, sucessLabel);
		passFail.setStyle("-fx-padding: 10; -fx-alignment: center;");

		VBox layout = new VBox(10, userNameField, passwordField, setupButton, passFail);
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

		primaryStage.setScene(new Scene(layout, 800, 400));
		primaryStage.setTitle("Administrator Setup");
		primaryStage.show();
	}
}
