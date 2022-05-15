import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;
import java.awt.*;
import java.awt.event.ActionListener;
import com.med.MedCond;

/**
 * @author Farhan Karim, Thomas Crosby
 */
public class PatientProfGUI {

    private PatientProfDB database; // The underlying in-memory database.

    private String adminID = "0"; // The most recently entered AdminID.

    private final JFrame mainMenu = new JFrame() { // Contains main menu.
        @Override
        public void dispose() { // Writes database to file and exits.
            database.writeAllPatientProf();
            System.exit(0);
        }
    };
    private final JFrame detailedProfileView = new JFrame(); // Displays all profile info for Create and Display options
    private final JFrame multiInfoPrompt = new JFrame(); // Prompt window for Delete, Find, and Update actions

    private final JRadioButton createProfileRB = new JRadioButton("Create Profile");
    private final JRadioButton deleteProfileRB = new JRadioButton("Delete Profile");
    private final JRadioButton updateProfileRB = new JRadioButton("Update Profile");
    private final JRadioButton findDisplayProfileRB = new JRadioButton("Find/Display Profile");
    private final JRadioButton displayAllProfileRB = new JRadioButton("Display All Profiles");
    private final JRadioButton[] menuItems = {
            createProfileRB,
            deleteProfileRB,
            updateProfileRB,
            findDisplayProfileRB,
            displayAllProfileRB};

    private final String[] attrNames = { // Interface label text for profile attributes.
            "Admin ID",
            "First Name",
            "Last Name",
            "Address",
            "Phone",
            "Co-Pay",
            "Insur. Type",
            "Patient Type",
            "Md Contact",
            "Md Phone",
            "Allergies",
            "Illnesses"};

    // Elements for profile view
    private final JLabel profileViewTitle = new JLabel();
    private final JTextField[] profileFields = new JTextField[attrNames.length];
    private final JButton profileViewButton = new JButton();

    // Elements for multi-input prompt view
    private final JPanel promptPanel = new JPanel();
    private final JLabel promptViewTitle = new JLabel();
    private final JTextField adminIDField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JComboBox<String> attrSelection = new JComboBox<>(attrNames);
    private final JLabel attrSelectLabel = new JLabel("Update Field:");
    private final JButton promptButton = new JButton();

    private static final class IllegalInputException extends Exception {
        int errorIndex; // The index of the field containing the error

        public int getErrorIndex() {
            return errorIndex;
        }
        public IllegalInputException(String message, int index) {
            super(String.format("Illegal input value \"%s\"", message));
            errorIndex = index;
        }
        public IllegalInputException(String message) {
            this(message, -1);
        }
    }

    private final ActionListener createProfAction = actionEvent -> {
        try {
            database.insertNewProfile(new PatientProf(getStrF(0), getStrF(1), getStrF(2),
                    getStrF(3), getStrF(4), getFloatF(5), getStrF(6), getStrF(7),
                    new MedCond(getStrF(8), getStrF(9), getStrF(10), getStrF(11))));

            detailedProfileView.setVisible(false); // Close the window if insert operation was successful
        } catch (IllegalInputException e) {
            JOptionPane.showMessageDialog(detailedProfileView, e.getMessage()); // Show error message

            // Select all text in field containing error
            profileFields[e.getErrorIndex()].requestFocusInWindow();
            profileFields[e.getErrorIndex()].selectAll();
        }

    };
    private final ActionListener deleteAction = actionEvent ->
            JOptionPane.showMessageDialog(multiInfoPrompt,
                    database.deleteProfile(adminIDField.getText(), lastNameField.getText())
                            ? "Profile deleted." : "Profile not found.");
    private final ActionListener updateAction = actionEvent -> {
        PatientProf profile = database.findProfile(adminIDField.getText(), lastNameField.getText());
        if (profile == null) {
            JOptionPane.showMessageDialog(multiInfoPrompt, "Profile not found.");
            return;
        }

        try {
            switch (attrSelection.getSelectedIndex()) {
                case 3: profile.updateAddress(getUpdateString(attrSelection.getSelectedIndex(), profile.getAddress())); break;
                case 4: profile.updatePhone(getUpdateString(attrSelection.getSelectedIndex(), profile.getPhone())); break;
                case 5: profile.updateCoPay(getUpdateFloat(attrSelection.getSelectedIndex(), profile.getCoPay())); break;
                case 6: profile.updateInsuType(getUpdateString(attrSelection.getSelectedIndex(), profile.getInsuType())); break;
                case 7: profile.updatePatientType(getUpdateString(attrSelection.getSelectedIndex(), profile.getPatientType())); break;
                case 8: profile.getMedCondInfo().updateMdContact(getUpdateString(attrSelection.getSelectedIndex(), profile.getMedCondInfo().getMdContact())); break;
                case 9: profile.getMedCondInfo().updateMdPhone(getUpdateString(attrSelection.getSelectedIndex(), profile.getMedCondInfo().getMdPhone())); break;
                case 10: profile.getMedCondInfo().updateAlgType(getUpdateString(attrSelection.getSelectedIndex(), profile.getMedCondInfo().getAlgType())); break;
                case 11: profile.getMedCondInfo().updateIllType(getUpdateString(attrSelection.getSelectedIndex(), profile.getMedCondInfo().getIllType())); break;
                default: JOptionPane.showMessageDialog(multiInfoPrompt, "Can't edit that attribute!");
            }
        } catch (IllegalInputException e) {
            if (!e.getMessage().contains("null")) // Only show error messages from incorrect input, not cancelling
                JOptionPane.showMessageDialog(multiInfoPrompt, e.getMessage()); // Show error message
        }

    };
    private final ActionListener displayAction = actionEvent -> {
        PatientProf profile = database.findProfile(adminIDField.getText(), lastNameField.getText());
        if (profile == null)
            JOptionPane.showMessageDialog(multiInfoPrompt, "Profile not found.");
        else {
            displayProfile(profile);
            profileViewTitle.setText("Patient Profile");
            profileViewButton.setVisible(false);
            detailedProfileView.setVisible(true);
            for (JTextField field : profileFields)
                field.setEditable(false);
        }
    };
    private final ActionListener showNextProfAction = actionEvent -> {
        PatientProf current;
        while (true)
            if ((current = database.findNextProfile()).getAdminID().equals(adminID))
                break;

        displayProfile(current);
    };

    /**
     * Load any data from the provided database filename, then prepare (pre-load) interface elements.
     * @param dataFile database filename to pass on to the underlying database implementation
     */
    public PatientProfGUI(String dataFile) {
        database = new PatientProfDB(dataFile);
        database.initializeDatabase();

        preloadGUI();
    }

    /**
     * Pre-load GUI elements.
     */
    private void preloadGUI() {
        preloadMenu();
        preloadProfileView();
        preloadMultiInfoPrompt();
    }

    private void preloadMenu() {
        mainMenu.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel mainMenuPanel = new JPanel();
        mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.PAGE_AXIS));

        // Menu title
        JLabel menuTitle = new JLabel("Integrated Patient System");
        menuTitle.setFont(menuTitle.getFont().deriveFont(20f));
        menuTitle.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        mainMenuPanel.add(menuTitle);

        // Spacer between title and menu items
        mainMenuPanel.add(Box.createRigidArea(new DimensionUIResource(0, 25)));

        // Set up radio button behavior and add to interface
        ButtonGroup menuGroup = new ButtonGroup();
        for (JRadioButton radioButton : menuItems) {
            menuGroup.add(radioButton);
            radioButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            mainMenuPanel.add(radioButton);
        }

        // Spacer between menu items and select button
        mainMenuPanel.add(Box.createRigidArea(new DimensionUIResource(0, 25)));

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(actionEvent -> { // Perform action based on selected radio button
            if (createProfileRB.isSelected())
                createProfile();
            else if (deleteProfileRB.isSelected())
                deleteProfile();
            else if (updateProfileRB.isSelected())
                updateProfile();
            else if (findDisplayProfileRB.isSelected())
                showProfile();
            else if (displayAllProfileRB.isSelected())
                displayAllProfiles();
        });
        selectButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        mainMenuPanel.add(selectButton);

        mainMenu.setContentPane(mainMenuPanel);
        mainMenu.setSize(400, 400);
        mainMenu.setResizable(false);
    }
    private void preloadProfileView() {
        detailedProfileView.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.PAGE_AXIS));

        // Title
        profileViewTitle.setFont(profileViewTitle.getFont().deriveFont(20f));
        profileViewTitle.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        profilePanel.add(profileViewTitle);

        // Spacer between title and fields
        profilePanel.add(Box.createRigidArea(new DimensionUIResource(0, 25)));

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridLayout(attrNames.length, 2, 0, 5));
        // Set up each row with a label and text field
        int i = 0;
        for (String attrName : attrNames) {

            JLabel label = new JLabel(attrName + ":");
            label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            fieldPanel.add(label);

            profileFields[i] = new JTextField();
            profileFields[i].setAlignmentX(JComponent.RIGHT_ALIGNMENT);
            fieldPanel.add(profileFields[i]);

            i++;
        }
        profilePanel.add(fieldPanel);

        // Spacer between fields and button
        profilePanel.add(Box.createRigidArea(new DimensionUIResource(0, 25)));

        // Button
        profilePanel.add(profileViewButton);

        detailedProfileView.setContentPane(profilePanel);
        detailedProfileView.setSize(400, 500);
    }
    private void preloadMultiInfoPrompt() {
        multiInfoPrompt.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        // Title
        promptViewTitle.setFont(promptViewTitle.getFont().deriveFont(20f));
        promptViewTitle.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        mainPanel.add(promptViewTitle);

        // Spacer between title and prompts
        mainPanel.add(Box.createRigidArea(new DimensionUIResource(0, 25)));

        // Prompt labels and controls
        promptPanel.setLayout(new GridLayout(3, 2, 0, 5));

        JLabel adminLabel = new JLabel("Admin ID:");
        adminLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        promptPanel.add(adminLabel);
        adminIDField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        promptPanel.add(adminIDField);

        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        promptPanel.add(lastNameLabel);
        lastNameField.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        promptPanel.add(lastNameField);

        mainPanel.add(promptPanel);

        // Spacer between prompts and button
        mainPanel.add(Box.createRigidArea(new DimensionUIResource(0, 25)));

        mainPanel.add(promptButton);

        multiInfoPrompt.setContentPane(mainPanel);
        multiInfoPrompt.setSize(350, 225);
    }

    private void createProfile() {
        profileViewTitle.setText("Create Profile");

        for (JTextField textField : profileFields) {
            textField.setText("");
            textField.setEditable(true);
        }

        profileViewButton.removeActionListener(showNextProfAction);
        profileViewButton.removeActionListener(createProfAction);
        profileViewButton.addActionListener(createProfAction);
        profileViewButton.setText("Submit");
        profileViewButton.setVisible(true);

        detailedProfileView.setVisible(true);
    }

    /**
     * Check and retrieve String input from text field at specified index.
     * @param index the index of the text field to read input from
     * @return the String content of the text field, if legal
     * @throws IllegalInputException if call to checkInput() passing field content fails
     */
    private String getStrF(int index) throws IllegalInputException {
        String content = profileFields[index].getText();
        if (!checkInput(content))
            throw new IllegalInputException(content, index);
        else return content;
    }

    /**
     * Check and retrieve float input from text field at specified index.
     * @param index the index of the text field to read input from
     * @return the float content of the text field, if legal
     * @throws IllegalInputException if checkInput() fails, or if parsing a float value from field content fails
     */
    private float getFloatF(int index) throws IllegalInputException {
        String content = profileFields[index].getText();
        IllegalInputException exception = new IllegalInputException(content, index);

        if (!checkInput(content))
            throw exception;

        float result;
        try {
            result = Float.parseFloat(content);
        } catch (NumberFormatException e) {
            throw exception;
        }
        return result;
    }

    /**
     * Checks input for illegal characters used to encode database files and for being blank.
     * @param input The input to check
     * @return true if allowed, false otherwise.
     */
    private static boolean checkInput(String input) {
        return !input.contains("\t") && !input.isBlank();
    }

    private void deleteProfile() {
        promptViewTitle.setText("Delete Profile");
        adminIDField.setText("");
        lastNameField.setText("");

        promptPanel.remove(attrSelectLabel);
        promptPanel.remove(attrSelection);

        promptButton.removeActionListener(deleteAction);
        promptButton.removeActionListener(updateAction);
        promptButton.removeActionListener(displayAction);
        promptButton.addActionListener(deleteAction);
        promptButton.setText("Delete");

        multiInfoPrompt.setVisible(true);
    }

    private void updateProfile() {
        promptViewTitle.setText("Update Profile");
        adminIDField.setText("");
        lastNameField.setText("");
        attrSelection.setSelectedIndex(0);

        promptPanel.remove(attrSelectLabel);
        promptPanel.remove(attrSelection);
        promptPanel.add(attrSelectLabel);
        promptPanel.add(attrSelection);

        promptButton.removeActionListener(deleteAction);
        promptButton.removeActionListener(updateAction);
        promptButton.removeActionListener(displayAction);
        promptButton.addActionListener(updateAction);
        promptButton.setText("Find");

        multiInfoPrompt.setVisible(true);
    }

    private String getUpdateString(int attrIndex, String initialValue) throws IllegalInputException {
        String input = (String) JOptionPane.showInputDialog(multiInfoPrompt,
                String.format("Admin ID - %s%nLast Name - %s", adminIDField.getText(), lastNameField.getText()),
                "Update " + attrNames[attrIndex], JOptionPane.PLAIN_MESSAGE, null, null, initialValue);

        if (input == null || !checkInput(input))
            throw new IllegalInputException(input);
        else return input;
    }
    private float getUpdateFloat(int attrIndex, float initialValue) throws IllegalInputException {
        String input = (String) JOptionPane.showInputDialog(multiInfoPrompt,
                String.format("Admin ID - %s%nLast Name - %s", adminIDField.getText(), lastNameField.getText()),
                "Update " + attrNames[attrIndex], JOptionPane.PLAIN_MESSAGE, null, null, initialValue);

        if (input == null || !checkInput(input))
            throw new IllegalInputException(input);

        float result;
        try {
            result = Float.parseFloat(input);
        } catch (NumberFormatException e) {
            throw new IllegalInputException(input);
        }
        return result;
    }

    private void showProfile() {
        promptViewTitle.setText("Display Profile");
        adminIDField.setText("");
        lastNameField.setText("");

        promptPanel.remove(attrSelectLabel);
        promptPanel.remove(attrSelection);

        promptButton.removeActionListener(deleteAction);
        promptButton.removeActionListener(updateAction);
        promptButton.removeActionListener(displayAction);
        promptButton.addActionListener(displayAction);
        promptButton.setText("View");

        multiInfoPrompt.setVisible(true);
    }

    private void displayAllProfiles() {
        try {
            String adminIDInput;
            while (true)
                if (checkInput(adminIDInput = JOptionPane.showInputDialog("Enter AdminID"))) {
                    adminID = adminIDInput;
                    break;
                }
        } catch (NullPointerException e) {
            return;
        }

        profileViewTitle.setText("Patient Profile");

        for (JTextField textField : profileFields)
            textField.setEditable(false);

        profileViewButton.removeActionListener(createProfAction);
        profileViewButton.removeActionListener(showNextProfAction);
        profileViewButton.addActionListener(showNextProfAction);
        profileViewButton.setText("Next Profile");
        profileViewButton.setVisible(true);

        PatientProf firstProfile = database.findFirstProfile();
        if (firstProfile.getAdminID().equals(adminID))
            displayProfile(firstProfile);

        PatientProf nextProfile;
        while (true)
            if ((nextProfile = database.findNextProfile()).getAdminID().equals(adminID)) {
                displayProfile(nextProfile);
                break;
            } else if (nextProfile == firstProfile)
                return;

        detailedProfileView.setVisible(true);
    }

    private void displayProfile(PatientProf profile) {
        for (int i = 0; i < profileFields.length; i++)
            profileFields[i].setText(readProfAttr(profile, i));
    }

    private String readProfAttr(PatientProf profile, int index) {
        switch (index) {
            case 0: return profile.getAdminID();
            case 1: return profile.getFirstName();
            case 2: return profile.getLastName();
            case 3: return profile.getAddress();
            case 4: return profile.getPhone();
            case 5: return String.valueOf(profile.getCoPay());
            case 6: return profile.getInsuType();
            case 7: return profile.getPatientType();
            case 8: return profile.getMedCondInfo().getMdContact();
            case 9: return profile.getMedCondInfo().getMdPhone();
            case 10: return profile.getMedCondInfo().getAlgType();
            case 11: return profile.getMedCondInfo().getIllType();
            default: return "ERROR";
        }
    }

    public void showInterface() {
        mainMenu.setVisible(true);
    }

    /**
     * Start the graphical interface.
     * @param args args[0] contains the database filename to link with this session
     */
    public static void main(String[] args) {
        PatientProfGUI gui = new PatientProfGUI(args[0]);
        gui.showInterface();
    }

}