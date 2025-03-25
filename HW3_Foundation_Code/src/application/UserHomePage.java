package application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.stage.Stage;
import databasePart1.*;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {

	private final DatabaseHelper databaseHelper;

	public UserHomePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage) {

		VBox layout = new VBox();
		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

		// Label to display Hello user
		Label userLabel = new Label("Hello, User!");
		userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		// RUBY'S CODE
		Button logoutButton = new Button("Logout"); // adding logout feature
		logoutButton.setStyle("-fx-font-size: 14px;  -fx-padding: 10;");

		logoutButton.setOnAction(a -> {
			// redirect user to the SetupLogin page w/ database helper
			SetupLoginSelectionPage setupLoginPage = new SetupLoginSelectionPage(databaseHelper);
			setupLoginPage.show(primaryStage);
		});

		Button quitButton = new Button("Quit");
		quitButton.setOnAction(a -> {
			databaseHelper.closeConnection();
			Platform.exit(); // Exit the JavaFX application
		});

		layout.getChildren().addAll(userLabel, logoutButton, quitButton);
		Scene userScene = new Scene(layout, 800, 400);

		// Set the scene to primary stage
		primaryStage.setScene(userScene);
		primaryStage.setTitle("User Page");
	}
}