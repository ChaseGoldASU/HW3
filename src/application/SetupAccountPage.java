package application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import UserPassEvaluators.PasswordEvaluator;
import UserPassEvaluators.UserNameRecognizer;
import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users. Users
 * provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {

	private final DatabaseHelper databaseHelper;

	// DatabaseHelper to handle database operations.
	public SetupAccountPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	/**
	 * Displays the Setup Account page in the provided stage.
	 * 
	 * @param primaryStage The primary stage where the scene will be displayed.
	 */
	public void show(Stage primaryStage) {
		// Input fields for userName, password, and invitation code
		TextField userNameField = new TextField();
		userNameField.setPromptText("Enter userName");
		userNameField.setMaxWidth(250);

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter Password");
		passwordField.setMaxWidth(250);

		TextField inviteCodeField = new TextField();
		inviteCodeField.setPromptText("Enter InvitationCode");
		inviteCodeField.setMaxWidth(250);

		// Debug tool to ensure that the correct password is seen as correct, can be
		// removed later
		Label sucessLabel = new Label();
		sucessLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");

		// Label to display error messages for invalid input or registration issues
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

		Button setupButton = new Button("Setup");

		setupButton.setOnAction(a -> {
			// Retrieve user input
			String userName = userNameField.getText();
			String password = passwordField.getText();
			String code = inviteCodeField.getText();
			String usernameErrMessage = UserNameRecognizer.checkForValidUserName(userName);
			String passwordErrMessage = PasswordEvaluator.evaluatePassword(password);

			try {
				// check if userName is valid
				if (usernameErrMessage == "") {
					// check is password is valid
					sucessLabel.setText("Username Valid");
					errorLabel.setText("");

					if (passwordErrMessage == "") {
						sucessLabel.setText("Username Valid\nPasswordValid");
						errorLabel.setText("");

						// Check if the user already exists
						if (!databaseHelper.doesUserExist(userName)) {

							// Validate the invitation code
							if (databaseHelper.validateInvitationCode(code)) {
								// debug validation
								sucessLabel.setText(sucessLabel.getText() + "Code Valid");
								errorLabel.setText("");

								// Create a new user and register them in the database
								User user = new User(userName, password, "user");
								databaseHelper.register(user);

								// Navigate to the user's welcome page
								if (user.getRole().equalsIgnoreCase("Admin")) {
									new AdminHomePage(databaseHelper).show(primaryStage);
								} else if (user.getRole().equalsIgnoreCase("Instructor")) {
									// new InstructorHomePage(databaseHelper).show(primaryStage);
								} else if (user.getRole().equalsIgnoreCase("Staff")) {
									// new StaffHomePage(databaseHelper).show(primaryStage);
								} else if (user.getRole().equalsIgnoreCase("Reviewer")) {
									// new ReviewerHomePage(databaseHelper).show(primaryStage);
								} else if (user.getRole().equalsIgnoreCase("Student")) {
									// new StudentHomePage(databaseHelper).show(primaryStage);
								} else if (user.getRole().equalsIgnoreCase("User")) {
									new UserHomePage(databaseHelper).show(primaryStage);
								} else {
									new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
								}

								// I think default should be student role, so it would become later
								// new StudentHomePage(databaseHelper).show(primaryStage, user);

							} else if (!databaseHelper.validateInvitationCode(code)) {
								errorLabel.setText("Code Expired/Invalid\nPlease enter a valid invitation code");
							}
						} else {
							errorLabel.setText("This useruserName is taken!!.. Please use another to setup an account");
						}
					} else { // This else statement is if the password is not valid
						errorLabel.setText("This password is invalid because:\n" + passwordErrMessage);
					}
				} else { // This else should be if userName is not valid
					if (UserNameRecognizer.userNameRecognizerIndexofError <= -1)
						return; // Should never happen
					usernameErrMessage = "This username is invalid because:\n" + usernameErrMessage
							+ userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6";
					errorLabel.setText(usernameErrMessage);
					System.out.print("This username is invalid because " + usernameErrMessage);
					// Display the input line so the user can see what was entered
					System.out.println("Inputted Username: " + userName);
					// Display the line up to the error and the display an up arrow
					System.out.println(
							userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6");
				}

			} catch (SQLException e) {
				System.err.println("Database error: " + e.getMessage());
				e.printStackTrace();
			}
		});

		Button quitButton = new Button("Quit");
		quitButton.setOnAction(a -> {
			databaseHelper.closeConnection();
			Platform.exit(); // Exit the JavaFX application
		});

		HBox passFail = new HBox(5);
		passFail.getChildren().addAll(errorLabel, sucessLabel);
		passFail.setStyle("-fx-padding: 10; -fx-alignment: center;");

		VBox layout = new VBox(10);
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
		layout.getChildren().addAll(userNameField, passwordField, inviteCodeField, setupButton, passFail, quitButton);

		primaryStage.setScene(new Scene(layout, 800, 400));
		primaryStage.setTitle("Account Setup");
		primaryStage.show();
	}
}
