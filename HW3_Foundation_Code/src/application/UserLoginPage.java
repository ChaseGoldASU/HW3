package application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.stage.Stage;

import java.sql.SQLException;

import UserPassEvaluators.PasswordEvaluator;
import UserPassEvaluators.UserNameRecognizer;
import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their
 * accounts. It validates the user's credentials and navigates to the
 * appropriate page upon successful login.
 */
public class UserLoginPage {

	private final DatabaseHelper databaseHelper;

	public UserLoginPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage) {
		// Input field for the user's userName, password
		TextField userNameField = new TextField();
		userNameField.setPromptText("Enter userName");
		userNameField.setMaxWidth(250);

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter Password");
		passwordField.setMaxWidth(250);

		// Label to display error messages
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

		Button useOTPButton = new Button("Use One-Time-Password");
		useOTPButton.setOnAction(a -> {
			String userName = userNameField.getText();
			String errUser = UserNameRecognizer.checkForValidUserName(userName);

			if (databaseHelper.doesUserExist(userName)) {
				if (errUser == "") {
					errorLabel.setText(databaseHelper.generateOneTimePassword(databaseHelper, userName));
					passwordField.setText("");
					passwordField.setPromptText("Enter One-Time-Password");
					errorLabel.setText("");
					useOTPButton.setBackground(new Background(new BackgroundFill(Color.CYAN, null, null)));

				} else {
					errorLabel.setText("This username is invalid because:\n" + errUser + "\n"
							+ userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6");
				}
			} else {
				errorLabel.setText("user account doesn't exists");
			}

		});

		Button loginButton = new Button("Login");

		loginButton.setOnAction(a -> {
			// Retrieve user inputs
			String userName = userNameField.getText();
			String password = passwordField.getText();
			String errUser = UserNameRecognizer.checkForValidUserName(userName);
			String errPassword = PasswordEvaluator.evaluatePassword(password);

			if (errUser == "") {
				errorLabel.setText("");

				// If we are using the normal password
				if (passwordField.getPromptText() == "Enter Password") {
					if (errPassword == "") {
						errorLabel.setText("");

						try {
							User user = new User(userName, password, "");
							WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);

							// Retrieve the user's role from the database using userName
							String role = databaseHelper.getUserRole(userName);

							if (role != null) {
								user.setRole(role);
								if (databaseHelper.login(user)) {
									if (role.equalsIgnoreCase("Admin")) {
										new AdminHomePage(databaseHelper).show(primaryStage);
									} else if (role.equalsIgnoreCase("Instructor")) {
										// new InstructorHomePage(databaseHelper).show(primaryStage);
									} else if (role.equalsIgnoreCase("Staff")) {
										// new StaffHomePage(databaseHelper).show(primaryStage);
									} else if (role.equalsIgnoreCase("Reviewer")) {
										// new ReviewerHomePage(databaseHelper).show(primaryStage);
									} else if (role.equalsIgnoreCase("Student")) {
										// new StudentHomePage(databaseHelper).show(primaryStage);
									} else if (role.equalsIgnoreCase("User")) {
										new UserHomePage(databaseHelper).show(primaryStage);
									} else {
										// welcomeLoginPage is the default if user is not any role or if they have
										// multiple roles
										welcomeLoginPage.show(primaryStage, user);
									}
								} else {
									// Display an error if the login fails
									errorLabel.setText("Error logging in");
								}
							} else {
								// Display an error if the account does not exist
								errorLabel.setText("user account doesn't exists");
							}

						} catch (SQLException e) {
							System.err.println("Database error: " + e.getMessage());
							e.printStackTrace();
						}
					} else { // password is invalid
						errorLabel.setText("This password is invalid because:\n" + errPassword);
					}
					// If we are using a one-time-password
				} else if (passwordField.getPromptText() == "Enter One-Time-Password") {
					try {
						User user = new User(userName, password, "");
						WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);

						// Retrieve the user's role from the database using userName
						String role = databaseHelper.getUserRoleOTP(databaseHelper, userName);

						if (role != null) {
							user.setRole(role);
							if (databaseHelper.loginOTP(databaseHelper, user)) {
								databaseHelper.rmOTP(databaseHelper, user);
								if (role.equalsIgnoreCase("Admin")) {
									new AdminHomePage(databaseHelper).show(primaryStage);
								} else if (role.equalsIgnoreCase("Instructor")) {
									// new InstructorHomePage(databaseHelper).show(primaryStage);
								} else if (role.equalsIgnoreCase("Staff")) {
									// new StaffHomePage(databaseHelper).show(primaryStage);
								} else if (role.equalsIgnoreCase("Reviewer")) {
									// new ReviewerHomePage(databaseHelper).show(primaryStage);
								} else if (role.equalsIgnoreCase("Student")) {
									// new StudentHomePage(databaseHelper).show(primaryStage);
								} else if (role.equalsIgnoreCase("User")) {
									new UserHomePage(databaseHelper).show(primaryStage);
								} else {
									// welcomeLoginPage is the default if user is not any role or if they have
									// multiple roles
									welcomeLoginPage.show(primaryStage, user);
								}
							} else {
								// Display an error if the login fails
								errorLabel.setText("Error logging in");
							}
						} else {
							// Display an error if the account does not exist
							errorLabel.setText("user account doesn't exists");
						}
						databaseHelper.rmOTP(databaseHelper, user);

					} catch (SQLException e) {
						System.err.println("Database error: " + e.getMessage());
						e.printStackTrace();
					}
				}

			} else { // userName is invalid
				errorLabel.setText("This username is invalid because:\n" + errUser
						+ userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6");
			}

		});

		Button quitButton = new Button("Quit");
		quitButton.setOnAction(a -> {
			databaseHelper.closeConnection();
			Platform.exit(); // Exit the JavaFX application
		});

		HBox entryButtons = new HBox(10);
		entryButtons.setStyle("-fx-padding: 20; -fx-alignment: center;");
		entryButtons.getChildren().addAll(loginButton, useOTPButton);

		VBox layout = new VBox(10);
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
		layout.getChildren().addAll(userNameField, passwordField, entryButtons, errorLabel, quitButton);

		primaryStage.setScene(new Scene(layout, 800, 400));
		primaryStage.setTitle("User Login");
		primaryStage.show();
	}
}
