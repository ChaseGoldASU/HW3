package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import application.Rows;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import UserPassEvaluators.UserNameRecognizer;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;

/**
 * AdminPage class represents the user interface for the admin user. This page
 * displays a simple welcome message for the admin.
 */

public class AdminHomePage {

	/**
	 * Displays the admin page in the provided primary stage.
	 * 
	 * @param primaryStage The primary stage where the scene will be displayed.
	 */

	private final DatabaseHelper databaseHelper;

	public AdminHomePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage) {
		/*
		 * All layout components: VBox layout Label adminLabel Button logoutButton
		 * Region spacer Label assignRoleLabel TextField usernameField
		 * ListView<CheckBox> roleListView CheckBox[5] chkRoles Button assignRoleButton
		 * Button removeRoleButton Button inviteButton Label listInvitesLabel Button
		 * listInvitesButton TableView<Rows> table TableColumn<Rows, String>
		 * rowNumberColumn TableColumn<Rows, String> userNameColumn TableColumn<Rows,
		 * String> roleColumn TableColumn<Rows, String> otpColumn Button otpButton
		 * Button seeAllUsers Button deleteUser
		 */

		VBox layout = new VBox();
		Scene adminScene = new Scene(layout, 800, 400);

		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

		// label to display the welcome message for the admin
		Label adminLabel = new Label("Hello, Admin!");
		adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		// RUBY's CODE
		// logoutButton returns user to homepage. Can be reused in other user pages
		Button logoutButton = new Button("Logout"); // adding logout feature
		logoutButton.setStyle("-fx-font-size: 14px;  -fx-padding: 10;");
		logoutButton.setOnAction(a -> {
			// redirect admin to the SetupLogin page w/ database helper
			SetupLoginSelectionPage setupLoginPage = new SetupLoginSelectionPage(databaseHelper);
			setupLoginPage.show(primaryStage);
		});

		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		Label assignRoleLabel = new Label("Assign Role to User");
		assignRoleLabel.setStyle("-fx-font-size: 14px;  -fx-font-weight: bold;");

		// input box to get username
		TextField usernameField = new TextField();
		usernameField.setPromptText("Enter username: ");
		usernameField.setMaxWidth(250); // adjust acording to scale of panel

		// Combo box to select roles for each user
		ListView<CheckBox> roleListView = new ListView<>();
		CheckBox[] chkRoles = { new CheckBox("Admin"), new CheckBox("Student"), new CheckBox("Instructor"),
				new CheckBox("Staff"), new CheckBox("Reviewer") };

		roleListView.getItems().addAll(chkRoles);

		// button to assign selected roles to entered user
		Button assignRoleButton = new Button("Assign Role/s");
		assignRoleButton.setOnAction(e -> {
			// Get username, validate username, act on checked checkboxes
			String userName = usernameField.getText();
			String errUsername = UserNameRecognizer.checkForValidUserName(userName); // This calls the function made to
																						// check if a username is valid
																						// for this program
			StringBuilder selectedRoles = new StringBuilder(); // Get Selected Roles

			// Check Validity of entered username against the rules of what is/isn't a
			// username and the database
			if (!errUsername.isEmpty()) {
				Alert usernameInvalid = new Alert(Alert.AlertType.ERROR);
				usernameInvalid.setTitle("Username Invalid");
				usernameInvalid.setContentText("This username is invalid because:\n" + errUsername + "\n"
						+ userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6");
				usernameInvalid.showAndWait();
				return;
			} else if (!databaseHelper.doesUserExist(userName)) {
				Alert userNotFound = new Alert(Alert.AlertType.ERROR);
				userNotFound.setTitle("User does not exist!");
				userNotFound.setContentText("The user \"" + userName + "\" , cannot be found in database");
				userNotFound.showAndWait();
				return;
			}

			// In a for loop, check all checkboxes and add their text if they are checked
			for (CheckBox chkCurrent : chkRoles) {
				if (chkCurrent.isSelected()) {
					selectedRoles.append(chkCurrent.getText()).append(", ");
				}
			}

			if (selectedRoles.length() > 0) {
				selectedRoles.setLength(selectedRoles.length() - 2); // Removes the last ", " so there is no more text
																		// than nessisary saved

				// Save new role to the user
				try {
					databaseHelper.assignUserRole(userName, selectedRoles.toString());
				} catch (SQLException err) {
					err.printStackTrace();
				}

				// Success Alert
				Alert assignmentSuccess = new Alert(Alert.AlertType.INFORMATION);
				assignmentSuccess.setTitle("Successfully Added");
				assignmentSuccess.setContentText("Roles" + selectedRoles + "assigned to " + userName);
				assignmentSuccess.showAndWait();
			} else {
				Alert noRole = new Alert(Alert.AlertType.WARNING);
				noRole.setTitle("No Roles");
				noRole.setContentText("Error: No role selected");
				noRole.showAndWait();
			}
		});

		Button removeRoleButton = new Button("Remove role/s");
		removeRoleButton.setOnAction(e -> {
			// get and check username
			String userName = usernameField.getText();
			String errUsername = UserNameRecognizer.checkForValidUserName(userName);
			StringBuilder selectedRoles = new StringBuilder();

			// Check Validity of entered username against the rules of what is/isn't a
			// username and the database
			if (!errUsername.isEmpty()) {
				Alert usernameInvalid = new Alert(Alert.AlertType.ERROR);
				usernameInvalid.setTitle("Username Invalid");
				usernameInvalid.setContentText("This username is invalid because:\n" + errUsername + "\n"
						+ userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6");
				usernameInvalid.showAndWait();
				return;
			} else if (!databaseHelper.doesUserExist(userName)) {
				Alert userNotFound = new Alert(Alert.AlertType.ERROR);
				userNotFound.setTitle("User does not exist!");
				userNotFound.setContentText("The user \"" + userName + "\" , cannot be found in database");
				userNotFound.showAndWait();
				return;
			}

			// Remove checked roles from user using for loop
			for (CheckBox chkCurrent : chkRoles) {
				if (chkCurrent.isSelected() && !chkCurrent.getText().equalsIgnoreCase("Admin")) {
					try {
						databaseHelper.removeUserRole(databaseHelper, userName, chkCurrent.getText());
					} catch (SQLException err) {
						err.printStackTrace();
					}
					selectedRoles.append(chkCurrent.getText()).append(", ");
				}
			}

			// Success Alert
			if (selectedRoles.length() > 0) {
				selectedRoles.setLength(selectedRoles.length() - 2);
				Alert assignmentSuccess = new Alert(Alert.AlertType.INFORMATION);
				assignmentSuccess.setTitle("Successfully Added");
				assignmentSuccess.setContentText("Roles" + selectedRoles + "removed from " + userName);
				assignmentSuccess.showAndWait();
			} else {
				Alert removalFailure = new Alert(Alert.AlertType.INFORMATION);
				removalFailure.setTitle("Removal Failed");
				removalFailure.setContentText("There were no roles selected");
				removalFailure.showAndWait();
			}

		});

		// invite Button which will display the invite page
		Button inviteButton = new Button("Invite Code");
		inviteButton.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
		inviteButton.setOnAction(e -> {
			new InvitationPage().show(databaseHelper, primaryStage, adminScene);
		});

		// Show Invite Button (listInvitesLabel was turned into a textbox so that the
		// text can be highlighted and copied to clipboard)
		TextField listInvitesLabel = new TextField();
		listInvitesLabel.setStyle(
				"-fx-font-size: 14px;  -fx-font-weight: bold; -fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: black;");
		listInvitesLabel.setEditable(false);
		listInvitesLabel.setFocusTraversable(false);
		Button listInvitesButton = new Button("Show InviteCodes");
		listInvitesButton.setOnAction(e -> {
			listInvitesLabel.setText(databaseHelper.getInviteCode());
		});

		// Brandon's table code, modified by Chase
		// Making the table:
		// Making the table view
		TableView<Rows> table = new TableView<>(); // Overall Table
		table.setPrefHeight(300);
		table.setPrefWidth(600);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setVisible(false); // By default this is invisible. It is shown if otpButton or seeAllUsers are hit

		// Define the columns (top row has predefined words, later rows are not
		// predefined
		TableColumn<Rows, String> rowNumberColumn = new TableColumn<>("Row #");
		TableColumn<Rows, String> userNameColumn = new TableColumn<>("Username");
		TableColumn<Rows, String> roleColumn = new TableColumn<>("Role");
		TableColumn<Rows, String> otpColumn = new TableColumn<>("One-Time-Password");

		// Boolean to keep track of if
		AtomicBoolean populated = new AtomicBoolean(false);

		// Bind columns to properties in Rows
		rowNumberColumn.setCellValueFactory(cellData -> cellData.getValue().rowNumber);
		userNameColumn.setCellValueFactory(cellData -> cellData.getValue().userName);
		roleColumn.setCellValueFactory(cellData -> cellData.getValue().role);
		otpColumn.setCellValueFactory(cellData -> cellData.getValue().otp);

		// By default, otpColumn is invisible. Vidibility is changed by Button OTP
		otpColumn.setVisible(false);

		// Add columns to table
		table.getColumns().addAll(rowNumberColumn, userNameColumn, roleColumn, otpColumn);
		// Add columns to table

		Button otpButton = new Button("List OTPs");
		otpButton.setOnAction(e -> {
			// Make table visible if it isn't
			if (!table.isVisible())
				table.setVisible(true);

			// Set size of each column
			rowNumberColumn.setPrefWidth(0.1 * table.getWidth()); // 15% of avalible table space
			userNameColumn.setPrefWidth(0.3 * table.getWidth()); // 42.5%
			roleColumn.setPrefWidth(0.3 * table.getWidth()); // 42.5%
			otpColumn.setPrefWidth(0.3 * table.getWidth()); // Since this column is visible, it gets porportionate size
			// Ensure OTP Column is visible
			otpColumn.setVisible(true);

			// Populate OTPs for users
			if (populated.get()) {
				ObservableList<Rows> userData = table.getItems();
				for (Rows row : userData) {
					if (row.getOTP() == null) {
						row.setOTP(databaseHelper.getOTP(databaseHelper, row.getUserName()));
					}
				}
				table.refresh();
			} else {
				ObservableList<Rows> userData = databaseHelper.getUserOTP(databaseHelper);
				table.setItems(userData);
				populated.set(true);
			}

		});

		// Button to see all users in the systems
		Button seeAllUsers = new Button("See All Users");
		seeAllUsers.setOnAction(e -> {
			// Make table visible if it isn't
			if (!table.isVisible())
				table.setVisible(true);

			// Ensure OTP column is invisible because this function doesn't change that
			otpColumn.setVisible(false);

			// Set size of each column
			rowNumberColumn.setPrefWidth(0.15 * table.getWidth()); // 15% of avalible table space
			userNameColumn.setPrefWidth(0.425 * table.getWidth()); // 42.5%
			roleColumn.setPrefWidth(0.425 * table.getWidth()); // 42.5%
			otpColumn.setPrefWidth(0 * table.getWidth()); // Since this column is invisible, it gets no size

			if (populated.get()) { // If the table has been populated, clear it and repopulate it
				table.getItems().clear();
				populated.set(false);
			} // Otherwise just populate normally

			// Populate table with all users
			ObservableList<Rows> userData = databaseHelper.displayUsers();
			table.setItems(userData);
			table.refresh();
			populated.set(true); // Mark table as populated
		});

		// Admin button to delete the user
		Button deleteUser = new Button("Delete User");
		deleteUser.setOnAction(e -> {
			// First verify the username, Then call the deleteUser in databaseHelper, then
			// display the output
			String userName = usernameField.getText();
			String errUser = UserNameRecognizer.checkForValidUserName(userName);
			if (!errUser.isEmpty()) {
				Alert usernameInvalid = new Alert(Alert.AlertType.ERROR);
				usernameInvalid.setTitle("Username Invalid");
				usernameInvalid.setContentText("This username is invalid because:\n" + errUser + "\n"
						+ userName.substring(0, UserNameRecognizer.userNameRecognizerIndexofError) + "\u21E6");
				usernameInvalid.showAndWait();
			} else {
				if (!databaseHelper.getUserRole(userName).contains("Admin")) {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Confirmation");
					alert.setHeaderText("Are you sure you want to delete this user?");
					alert.setContentText("Choose an Option");

					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						boolean deleteUserResult = databaseHelper.deleteUser(userName); // int is too big if the result
																						// is a -1, a 1, or other.
					}
				} else {
					Alert adminFound = new Alert(Alert.AlertType.ERROR);
					adminFound.setTitle("Can't Delete Admin");
					adminFound.setContentText("You can't delete a user with the role of admin");
					adminFound.showAndWait();
				}

			}
		});

		Button quitButton = new Button("Quit");
		quitButton.setOnAction(a -> {
			databaseHelper.closeConnection();
			Platform.exit(); // Exit the JavaFX application
		});

		// Load all components into the layout
		layout.getChildren().addAll(adminLabel, usernameField, deleteUser, table, seeAllUsers, inviteButton,
				listInvitesLabel, listInvitesButton, assignRoleLabel, roleListView, assignRoleButton, removeRoleButton,
				spacer, logoutButton, otpButton, quitButton);
		// Set the scene to primary stage
		primaryStage.setScene(adminScene);
		primaryStage.setTitle("Admin Page");
	}
}