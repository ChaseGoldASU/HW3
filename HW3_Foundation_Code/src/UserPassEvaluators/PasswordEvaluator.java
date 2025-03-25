package UserPassEvaluators;

public class PasswordEvaluator {
	/**
	 * <p>
	 * Title: Directed Graph-translated Password Assessor.
	 * </p>
	 * 
	 * <p>
	 * Description: A demonstration of the mechanical translation of Directed Graph
	 * diagram into an executable Java program using the Password Evaluator Directed
	 * Graph. The code detailed design is based on a while loop with a cascade of if
	 * statements
	 * </p>
	 * 
	 * <p>
	 * Copyright: Lynn Robert Carter, Chase Goldberg © 2022
	 * </p>
	 * 
	 * @author Lynn Robert Carter and Chase Goldberg
	 * 
	 * @version 1.00 2025-01-24
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error
	 * message and a pointer to the character of the error will enhance the user
	 * experience.
	 * 
	 */

	public static String passwordErrorMessage = ""; // The error message text
	public static String passwordInput = ""; // The input being processed
	public static int passwordIndexofError = -1; // The index where the error was located (Unused, but makes compilation
													// errors if removed
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	public static boolean foundOtherChar = false;
	private static String inputLine = ""; // The input line
	private static char currentChar; // The current character in the line
	private static int currentCharNdx; // The index of the current character
	private static boolean running; // The flag that specifies if the FSM is
									// running

	/**********
	 * This private method display the input line and then on a line under it
	 * displays an up arrow at the point where an error should one be detected. This
	 * method is designed to be used to display the error message on the console
	 * terminal.
	 * 
	 * @param input          The input string
	 * @param currentCharNdx The location where an error was found
	 * @return Two lines, the entire input line followed by a line with an up arrow
	 */
	private static void displayInputState() {
		// Display the entire input line
		System.out.println(inputLine);
		System.out.println(inputLine.substring(0, currentCharNdx) + "?");
		System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + currentCharNdx
				+ "  |  The currentChar: \"" + currentChar + "\"");
	}

	/**********
	 * This method is a mechanical transformation of a Directed Graph diagram into a
	 * Java method.
	 * 
	 * @param input The input string for directed graph processing
	 * @return An output string that is empty if every things is okay or it will be
	 *         a string with a help description of the error follow by two lines
	 *         that shows the input line follow by a line with an up arrow at the
	 *         point where the error was found.
	 */
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph
		// simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0; // Initialize the IndexofError
		inputLine = input; // Save the reference to the input line as a global
		currentCharNdx = 0; // The index of the current character

		if (input.length() <= 0)
			return "The password is empty!";

		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0); // The current character from the above indexed position

		// Before entering state 0: Setting/resetting everything to false/0

		passwordInput = input; // Save a copy of the input
		foundUpperCase = false; // Reset the Boolean flag
		foundLowerCase = false; // Reset the Boolean flag
		foundNumericDigit = false; // Reset the Boolean flag
		foundSpecialChar = false; // Reset the Boolean flag
		foundNumericDigit = false; // Reset the Boolean flag
		foundLongEnough = false; // Reset the Boolean flag

		running = true; // Start the loop

		// State 0: Scanning each character to see if they check of any of the password
		// requirements
		while (running) {
			displayInputState();
			// The cascading if statement sequentially tries the current character against
			// all of the
			// valid transitions
			if (currentChar >= 'A' && currentChar <= 'Z') { // Case 1: Capital Letter
				System.out.println("Upper case letter found");
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') { // Case 2: Lower-case Letter
				System.out.println("Lower case letter found");
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') { // Case 3: Numeric Character
				System.out.println("Digit found");
				foundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) { // Case 4: "Special" Characters
				System.out.println("Special character found");
				foundSpecialChar = true;
			} else if (currentChar == 0) { // Debug code to make sure that if a null value gets checked somehow then the
											// code isn't broken
			} else { // Case 5: Other Characters
				passwordIndexofError = currentCharNdx; // This line helps set the red arrow towards the outlying
														// character, but the outlier doesn't fail the password
				foundOtherChar = true;
				break;
			}

			// Case 6:
			if (inputLine.length() >= 8) {
				System.out.println("At least 8 characters found");
				foundLongEnough = true;
			}

			// Go to the next character if there is one. This allows to loop back to case 1
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);

			//System.out.println();
		}

		// Compling Error message and returning it
		String errMessage = "";
		if (!foundUpperCase)
			if (errMessage == "") {
				errMessage += "No Upper case; ";
			} else {
				errMessage += "\nNo Upper case; ";
			}
		if (!foundLowerCase)
			if (errMessage == "") {
				errMessage += "No Lower case; ";
			} else {
				errMessage += "\nNo Lower case; ";
			}
		if (!foundNumericDigit)
			if (errMessage == "") {
				errMessage += "No Numeric digits; ";
			} else {
				errMessage += "\nNo Numeric digits; ";
			}
		if (!foundSpecialChar)
			if (errMessage == "") {
				errMessage += "No Special character; ";
			} else {
				errMessage += "\nNo Special character; ";
			}
		if (!foundLongEnough)
			if (errMessage == "") {
				errMessage += "Not Long Enough; ";
			} else {
				errMessage += "\nNot Long Enough; ";
			}
		if (!foundOtherChar)
			;

		if (errMessage == "")
			return "";

		System.out.println(errMessage);
		return errMessage + " ";

	}
}
