import java.util.InputMismatchException;
import java.util.Scanner;
import com.med.MedCond;

public class PatientProfInterface {

    private static final Scanner scanner = new Scanner(System.in); // Single Scanner instance which handles user input.

    private static final String prompt = ">"; // Prompt appearance for all places where user input is requested.

    private final PatientProfDB database; // The underlying in-memory database.
    private String adminID; // Stores the adminID of the current user.

    /**
     * Constructs a PatientProfInterface. Calling getUserChoice() on the newly-created instance starts the interface.
     * @param dataFile database filename to pass on to the underlying database implementation
     */
    PatientProfInterface(String dataFile) {
        database = new PatientProfDB(dataFile);
    }

    /**
     * Deletes uniquely identified patient profile if possible, with status messages to indicate success or failure.
     */
    private void deletePatientProf() {
        if (database.deleteProfile(adminID, promptLastName()))
            System.out.println("Patient deleted.");
        else
            System.out.println("No matching patient found.");
    }

    /**
     * Secondary program loop to allow random-access editing of certain patient data.
     */
    private void updatePatientProf() {
        PatientProf profile = database.findProfile(adminID, promptLastName());

        if (profile == null) {
            System.out.println("No matching patient found.");
            return;
        }

        int input = displayMenu(String.format("(Modify Patient) Modifying: %s %s%n" +
                "Select an item to edit:%n" +
                "1: Address%n" +
                "2: Phone number%n" +
                "3: Insurance type%n" +
                "4: Copay%n" +
                "5: Patient type%n" +
                "6: Medical contact%n" +
                "7: Medical contact phone number%n" +
                "8: Allergy information%n" +
                "9: Illness information%n" +
                "0: Exit%n", profile.getFirstName(), profile.getLastName()), 9);

        // Exit if user entered 0
        if (input == 0) return;

        switch (input) {
            case 1: profile.updateAddress(promptInput("Enter new patient address.")); break;
            case 2: profile.updatePhone(promptInput("Enter new patient phone number.")); break;
            case 3: profile.updateInsuType(promptInput("Enter new patient insurance type.")); break;
            case 4: profile.updateCoPay(promptFloat("Enter new patient copay.")); break;
            case 5: profile.updatePatientType(promptInput("Enter new patient type.")); break;
            case 6: profile.getMedCondInfo().updateMdContact(promptInput("Enter new medical contact.")); break;
            case 7: profile.getMedCondInfo().updateMdPhone(promptInput("Enter new med contact phone number.")); break;
            case 8: profile.getMedCondInfo().updateAlgType(promptInput("Enter new allergy info.")); break;
            case 9: profile.getMedCondInfo().updateIllType(promptInput("Enter new illness info."));break;
        }
    }

    /**
     * Displays the provided patient profile.
     * @param p profile to display
     */
    private void displayPatientProf(PatientProf p) {
        if (p == null) {
            System.out.println("No matching patient found.");
        } else {
            MedCond mc = p.getMedCondInfo();
            System.out.printf("Admin ID: %s, First Name: %s, Last Name: %s, Address: %s, Phone: %s, CoPay: %.2f, " +
                            "Insurance type: %s, Patient Type: %s, Medical Contact: %s, Medical Contact Phone: %s, " +
                            "Allergy Type: %s, Illness Type: %s%n", p.getAdminID(), p.getFirstName(), p.getLastName(),
                    p.getAddress(), p.getPhone(), p.getCoPay(), p.getInsuType(), p.getPatientType(), mc.getMdContact(),
                    mc.getMdPhone(), mc.getAlgType(), mc.getIllType());
        }
    }

    /**
     * Displays all profiles belonging to the logged in admin user.
     */
    private void displayAllPatientProf() {
        PatientProf first = database.findFirstProfile(); // Reset sequential access counter to first item.
        if (first != null && first.getAdminID().equals(adminID))
            displayPatientProf(first); // Display the first profile now if it belongs to the current admin user.

        PatientProf next;
        while ((next = database.findNextProfile()) != first) // Stop looping when we wrap back to the first profile.
            if (next.getAdminID().equals(adminID))
                displayPatientProf(next); // Display profile only if it belongs to the current admin user.
    }

    /**
     * Loads data from db file.
     */
    private void initDB() {
        database.initializeDatabase();
    }

    /**
     * Saves in-memory database state to db file.
     */
    private void writeToDB() {
        database.writeAllPatientProf();
    }

    /**
     * Creates a new patient profile, prompting the user for each associated data field.
     * @return the newly-created patient profile
     */
    private PatientProf createNewPatientProf() {
        return new PatientProf(adminID, promptInput("Enter patient first name."), promptLastName(),
                promptInput("Enter patient address."), promptInput("Enter patient phone number."),
                promptFloat("Enter patient copay."), promptInput("Enter patient insurance type."),
                promptInput("Enter patient type."), createNewMedCond());
    }

    /**
     * Creates a new MedCond data class, prompting the user for each associated data field.
     * @return the newly-created MedCond data class
     */
    private MedCond createNewMedCond() {
        return new MedCond(promptInput("Enter patient medical contact."),
                promptInput("Enter medical contact phone number."), promptInput("Enter patient allergy info."),
                promptInput("Enter patient illness info."));
    }

    private void findPatientProf() {
        displayPatientProf(database.findProfile(adminID, promptLastName()));
    }

    /**
     * The main program loop. Handles main menu interaction and calls other methods as requested.
     * This is the only non-static public method in this class; the interface should be accessed through this menu only.
     */
    public void getUserChoice() {
        while (true) {
            int input = displayMenu(String.format("(Main Menu) Logged in as: %s%n" +
                    "Welcome to the Integrated Patient System!%n%n" +
                    "Select an option from the menu below:%n" +
                    "1: Initialize database%n" +
                    "2: Enter a new patient profile%n" +
                    "3: Delete a patient%n" +
                    "4: Display patient information%n" +
                    "5: Modify patient information%n" +
                    "6: Display information for all patients%n" +
                    "7: Write database to file%n" +
                    "8: Change admin user%n" +
                    "0: Exit%n", adminID == null ? "<None>" : adminID), 8);

            // Exit if user entered 0
            if (input == 0) break;

            // Prompt for adminID if not already provided
            if (input != 8 && adminID == null)
                promptAdminID();

            switch (input) {
                case 1: initDB(); break;
                case 2: database.insertNewProfile(createNewPatientProf()); break;
                case 3: deletePatientProf(); break;
                case 4: findPatientProf(); break;
                case 5: updatePatientProf(); break;
                case 6: displayAllPatientProf(); break;
                case 7: writeToDB(); break;
                case 8: promptAdminID(); break;
            }
        }
    }

    /**
     * Shows the provided menu string and provides user interaction by means of typing an integer.
     * @param display Menu to display
     * @param num_options total number of menu options
     * @return valid integer selection entered by the user
     */
    private int displayMenu(String display, int num_options) {
        while (true) {
            System.out.printf("%n" + display + prompt); // Display the menu

            int input;
            try {
                input = scanner.nextInt();
                scanner.nextLine(); // Skip any additional input
            } catch (InputMismatchException e) {
                System.out.println("Please enter only a number.");
                scanner.nextLine(); // Skip any additional input
                continue;
            }

            if (input <= num_options) {
                return input;
            } else {
                // Display error message if the entered number is too large
                System.out.printf("Please enter a number listed on the menu (0-%d).%n", num_options);
            }
        }
    }

    /**
     * Prompts the user to enter their AdminID and updates the logged-in user accordingly.
     */
    private void promptAdminID() {
        adminID = promptInput("Enter your AdminID:");
    }

    /**
     * Reduces code repetition as this is a common call to promptInput().
     */
    private static String promptLastName() {
        return promptInput("Enter patient last name.");
    }

    /**
     * Prompts the user for input displaying the provided message.
     * @param message Message to display to the user
     * @return next line read by the scanner after the prompt which does not contain illegal characters
     */
    private static String promptInput(String message) {
        while (true) {
            System.out.printf("%s%n%s", message, prompt);
            String input = scanner.nextLine();
            if (checkInput(input))
                return input;
            else System.out.println("Invalid characters detected; please try again.");
        }
    }

    /**
     * Checks input for illegal characters used to encode database files.
     * @param input The input to check
     * @return true if allowed, false otherwise.
     */
    private static boolean checkInput(String input) {
        return !input.contains("\t");
    }

    /**
     * Prompts the user for input interpreted as a float displaying the provided message.
     * @param message Message to display to the user
     * @return next line read by the scanner after the prompt which can be interpreted as a float
     */
    private static float promptFloat(String message) {
        while (true) {
            System.out.printf("%s%n%s", message, prompt);
            try {
                float input = scanner.nextFloat();
                scanner.nextLine(); // Skip any additional input
                return input;
            } catch (InputMismatchException e) {
                System.out.println("Please enter only a number (decimal point allowed).");
                scanner.nextLine(); // Skip any additional input
            }
        }
    }

    /**
     * Starts the user interface after constructing the in-memory database session and passing it the db filename.
     * @param args args[0] contains the database filename to link with this session
     */
    public static void main(String[] args) {
        PatientProfInterface dbi = new PatientProfInterface(args[0]);
        dbi.getUserChoice();
    }

}

