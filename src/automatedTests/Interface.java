package automatedTests;

//imports
	//java Files
import java.util.Scanner;
	//Other files
import databasePart1.DatabaseHelper;


public class Interface{
	private static char input;
	private static Scanner keyboard = new Scanner(System.in);
	private static final String testOptions = ""
			+ "1. User Database test\n"
			+ "2. QNA Database Test\n"
			+ "3. DM Database Test\n"
			+ "4. Post Lists"
			+ "5. \n"
			+ "6. Quit";
	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	public static void main(String[]args) {
		//Greet user
		System.out.println("Welcome to the HW3 testbed\n" + 
				"Please enter your choice of test (Just enter the number)\n\n" +
				testOptions);
		
		//Start of loop
		while (true) {
			System.out.println("Please enter your choice of test (Just enter the number)\n" + testOptions);
			input = keyboard.next().charAt(0);	//Fetch user input
			
			switch(input) {
			case '1':
				System.out.println("Starting User Database Test");
				if (UserCreateTest.userTest()) {
					System.out.println("\nTesting finished\nAll tests run passed\nContinue? (y/n)\n");
					if (input == 'y') {
						System.out.println("Exiting program...");
						keyboard.close();	//Close the scanner when exiting
						return;
					} else if (input == 'n') {
						break;
					} else {
						System.out.println("Invalid Input, continueing");
					}
				}
				break;
			case '2':
				
			case '3':
				
			case '4':
				
			case '5':
				
			case '6':
				System.out.println("Exiting program...");
				keyboard.close();	//Close the scanner when exiting
				return;	//Exit the loop and program
			default:
					System.out.println("Invalid Selection");
					break;
			}
		}
	}
	
	private static void exit() {
		
	}
}
//Test DB Connection: I changed how dbHelper works, using helper functions in other files, so I need to make sure it is still working