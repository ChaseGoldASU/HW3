package automatedTests;

import java.security.SecureRandom;
//imports
	//java functions
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

	//javaFX functions
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import application.User;
import application.Rows;
import UserPassEvaluators.*;
import databasePart1.DatabaseHelper;
import qnaClasses.Post;
//runs all addUser functions to be sure they work
public class UserCreateTest{
	//Connecting to the databases
	final static DatabaseHelper db = new DatabaseHelper();
	
	//All tests run from here
	public static boolean userTest() {
		boolean passes = false;
		try {
			db.connectToDatabase();
			
			userTableTest();
			
			//OTP
			//Invitation Codes
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return passes;
	}
	
	private static boolean userTableTest() throws SQLException{
		boolean startEmpty = db.isUserDatabaseEmpty();
		boolean pass = true;
		boolean returns = true;
		//Make Users
		String[] roles = {"User", "Student", "Instructor", "Staff", "Reviewer"};
		SecureRandom random = new SecureRandom();
		int id = -1;
		List<User> Users = new ArrayList<>();
		Users.add(new User(generateUsername(), generatePassword(), roles[0]));
		Users.add(new User(generateUsername(), generatePassword(), roles[1]));
		Users.add(new User(generateUsername(), generatePassword(), roles[2]));
		Users.add(new User(generateUsername(), generatePassword(), roles[3]));
		Users.add(new User(generateUsername(), generatePassword(), roles[4]));
		
		int j = random.nextInt(4);
		for (int i = 0; i < j; i++) {
			Users.add(new User(generateUsername(), generatePassword(), roles[random.nextInt(5)]));
		}
		
		//Register new users
		for (User user: Users) {
			db.register(user);
		}
		
		//In one long loop after the register
		for (User user: Users) {
			//Run the login check
			pass = db.login(user);
			if (!pass) {
				System.out.println("Login failed for " + user.getUserName());
				returns = false;
			}

			//does user exist through username
			pass = db.doesUserExist(user.getUserName());
			if (!pass) {
				System.out.println(user.getUserName() + " does not exist");
				returns = false;
			}
			
			//get user role
			pass = (user.getRole() == db.getUserRole(user.getUserName()));
			if (!pass) {
				System.out.println(user.getUserName() + "'s role failed to save");
				returns = false;
			}
			
			//get user id User
			if (id == -1) {
				id = db.getUserID(user);
			} else {
				int temp = db.getUserID(user);
				pass = (id < temp);
				id = temp;
				
				if (!pass) {
					System.out.println(user.getUserName() + "'s id generation failed");
					returns = false;
				}
			}
			
			//get user id username and role
			{
				int temp = db.getUserID(user.getUserName(), user.getRole());
				pass = (id < temp);
				if (!pass) {
					System.out.println(user.getUserName() + "'s user and role based id fetch failed");
					returns = false;
				}
			}
			
			//get user id username
			{
				int temp = db.getUserID(user.getRole());
				pass = (id < temp);
				if (!pass) {
					System.out.println(user.getUserName() + "'s user and based id fetch failed");
					returns = false;
				}
			}
			
			//get username from id
			User tempUser = db.getUserFromID(db, id);
			pass = (tempUser.equals(user));
	
			//assign user roles
			db.assignUserRole(user.getUserName(), "User");
			pass = ((user.getRole() + ", User").equals(db.getUserRole(user.getUserName())));
			if (!pass) {
				System.out.println(user.getUserName() + " append roles failed");
				returns = false;
			}
			
			//remove user role
			db.removeUserRole(db, user.getUserName(), "User");
			pass = (user.getRole().replaceAll(",?Role", "") == db.getUserRole(user.getUserName()));
			if (!pass) {
				System.out.println(user.getUserName() + " append roles failed");
				returns = false;
			}
		}
		
		//display users (Has to be run on a list that started empty list)
		if (startEmpty) {
			ObservableList<Rows> data = FXCollections.observableArrayList();
			int rowNumber = 1;
			for (User user: Users) {
				data.add(new Rows(String.valueOf(rowNumber), user.getUserName(), user.getRole(), ""));
				rowNumber++;
			}
			
			pass = (data.equals(db.displayUsers()));
		} else {
			System.out.println("display test unrunable, user table didn't start empty");
		}
		

		
		//delete user
		for (User user: Users) {
			pass = db.deleteUser(user.getUserName());
			if (!pass) {
				System.out.println("Failed to delete " + user.getUserName());
				returns = false;
			}
		}
		
		return returns;
	}
	
	//private static boolean OTPTableTest() {
	
	//private static boolean CodeTableTest() {
	
	private static String generateUsername() {
		
		//Sets of accepted characters
			//I forget if there are any restrictions on the special characters, so they won't be used for the test
		String upperChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lowerChar = "abcdefghijklmnopqrstuvwxyz";
		String digit = "1234567890";
		String allChar = upperChar + lowerChar + digit;
		
		// Secure Random Generator
		SecureRandom random = new SecureRandom();
		
		// String builder. Makes it easier to add and remove characters to a string
		StringBuilder username = new StringBuilder();
		
		//Fill the username with random characters
		int j = random.nextInt(12) + 4;
		for (int i = 0; i < j; i++) {
			username.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		
		if (UserNameRecognizer.checkForValidUserName(username.toString()) == "") {
			System.out.println("Password generation failed, retrying... " + username.toString());
			return generateUsername();
		}
		return username.toString();
	}
	private static String generatePassword() {
		//returned password
	    StringBuilder randomPassword = new StringBuilder();
		
		// Sets of accepted characters
		String upperChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lowerChar = "abcdefghijklmnopqrstuvwxyz";
		String digit = "1234567890";
		String specialChar = "~`!@#$%^&*()_-+{}[]|:,.?/";

		// All allowed characters
		String allChar = upperChar + lowerChar + digit + specialChar;

		// Secure Random Generator
		SecureRandom random = new SecureRandom();

		// String builder. Makes it easier to add and remove characters to a string
		StringBuilder password = new StringBuilder();

		// Fill the requirements for the password
		char upper = upperChar.charAt(random.nextInt(upperChar.length()));
		char lower = lowerChar.charAt(random.nextInt(lowerChar.length()));
		char num = digit.charAt(random.nextInt(digit.length()));
		char special = specialChar.charAt(random.nextInt(specialChar.length()));
		
		password.append(upper);
		password.append(lower);
		password.append(num);
		password.append(special);

		// Add random characters to get to at least 8 total characters
		int j = random.nextInt(10) + 4;
		for (int i = 0; i < j; i++) {
			password.append(allChar.charAt(random.nextInt(allChar.length())));
		}

		// Shuffle password to ensure randomPassword is randomly ordered
		while (password.length() > 0) {
			int index = random.nextInt(password.length());
			randomPassword.append(password.charAt(index));
			password.deleteCharAt(index);
		}
		
		//Check against passwordEvaluator
		String checkRnd = PasswordEvaluator.evaluatePassword(randomPassword.toString());
		if (!checkRnd.equals("")) {
			System.out.println("Password generation failed, retrying... " + checkRnd);
			//return generatePassword();
		}
		return randomPassword.toString();
	}
}