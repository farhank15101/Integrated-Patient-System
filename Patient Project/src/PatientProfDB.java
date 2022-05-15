import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import com.med.MedCond;

public class PatientProfDB {

    private final String DBFileName; // Hold onto the name of the file backing this database.

    private PatientProf[] patientList = new PatientProf[5];
    private int next_insert_i = 0; // Index at which next patient is added
    private int next_access_i = 0; // Index at which next sequential access takes place

    /**
     * Constructs a patient profile database.
     * @param db_filename File which backs the database (may exist or not at database creation)
     */
    PatientProfDB(String db_filename) {
        DBFileName = db_filename;
    }

    /**
     * Insert new patient profile into the database.
     * @param profile Profile to be inserted
     */
    public void insertNewProfile(PatientProf profile) {
        // Expand patient array (in increments of 10 spaces) if it is almost full.
        if (next_insert_i >= patientList.length - 1)
            patientList = Arrays.copyOf(patientList, patientList.length + 10);

        // Add profile to array and increment next_insert_i
        patientList[next_insert_i++] = profile;
    }

    /**
     * Remove patient profile uniquely identified by adminID and lastName.
     * @param adminID Admin ID of the creator of the profile
     * @param lastName Last name of the patient represented by the profile
     * @return true if succeeded, false if failed (patient profile not found).
     */
    public boolean deleteProfile(String adminID, String lastName) {
        for (int i = 0; i < next_insert_i; i++) { // Stop looking once we reach the end of the array population

            // True if we found the patient profile
            if (patientList[i].getAdminID().equals(adminID) && patientList[i].getLastName().equals(lastName)) {

                // Shift profiles to right of removed patient one space to the left
                while (i < patientList.length - 1
                        && !(patientList[i] == null && patientList[i+1] == null)) { // Stop swapping once we reach the end of the array population
                    patientList[i] = patientList[i + 1];

                    i++;
                }

                next_insert_i--;
                return true;
            }

        }
        return false;
    }

    /**
     * Retrieve patient profile uniquely identified by adminID and lastName.
     * @param adminID Admin ID of the creator of the profile
     * @param lastName Last name of the patient represented by the profile
     * @return the patient profile or null if not found.
     */
    public PatientProf findProfile(String adminID, String lastName) {
        for (int i = 0; i < next_insert_i; i++) { // Stop looking once we reach the end of the array population
            if (patientList[i].getAdminID().equals(adminID) && patientList[i].getLastName().equals(lastName))
                return patientList[i];
        }

        return null;
    }

    /**
     * Retrieve the first profile in the database.
     * @return the first profile, or null if the database is empty
     */
    public PatientProf findFirstProfile() {
        if (next_insert_i == 0) return null;

        next_access_i = 1;
        return patientList[0];
    }

    /**
     * Retrieve the next profile in the database (after the last call to findNextProfile() or findFirstProfile()).
     * If the end of the database was reached, returns the first profile.
     * @return the next profile, or null if the database is empty
     */
    public PatientProf findNextProfile() {
        if (next_insert_i == 0) return null;

        // Return first profile if we've reached the end, otherwise the next (and increment next_access_i)
        return (next_access_i >= next_insert_i) ? findFirstProfile() : patientList[next_access_i++];
    }

    /**
     * Writes all patient profile information to the file name specified at initialization
     * (This erases any information prior stored in the file).
     */
    public void writeAllPatientProf() {
        String[] profiles = new String[next_insert_i];

        for (int i = 0; i < profiles.length; i++) {
            // Temp variables for patient profile
            PatientProf p = patientList[i];
            MedCond mc = p.getMedCondInfo();
            // Join all fields into one String separated by tabs, store in profiles String array.
            profiles[i] = String.join("\t", p.getAdminID(), p.getFirstName(), p.getLastName(), p.getAddress(),
                    p.getPhone(), String.valueOf(p.getCoPay()), p.getInsuType(), p.getPatientType(), mc.getMdContact(),
                    mc.getMdPhone(), mc.getAlgType(), mc.getIllType());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DBFileName))) {

            // Write each profile followed by a newline.
            for (String profile : profiles) {
                bw.write(profile);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error writing to file: " + DBFileName);
            e.printStackTrace();
        }
    }

    /**
     * Loads all patient profile information from the file name specified at initialization
     * (This erases the database instance in memory if it succeeds).
     */
    public void initializeDatabase() {
        try (BufferedReader br = new BufferedReader(new FileReader(DBFileName))) {
            ArrayList<PatientProf> profiles = new ArrayList<>(); // Temporarily hold loaded profiles here

            String line;
            while ((line = br.readLine()) != null && !line.isBlank()) {
                String[] items = line.split("\t"); // Split each patient data item at the tab delimiters.

                profiles.add(new PatientProf(items[0], items[1], items[2], items[3], items[4], Float.parseFloat(items[5]),
                        items[6], items[7], new MedCond(items[8], items[9], items[10], items[11])));
            }

            // Copy loaded profiles into database
            patientList = profiles.toArray(new PatientProf[0]);
            next_insert_i = profiles.size();

        } catch (IOException e) {
            System.out.println("Error reading from file: " + DBFileName);
            e.printStackTrace();
        }
    }

    /**
     * Test suite which initializes, modifies such that it ends up in the initial state, then saves back to the file.
     * Exercises all functionality of this class directly or indirectly.
     * @param args args[0] contains the database filename used for testing
     */
    public static void main(String[] args) {
        PatientProfDB db = new PatientProfDB(args[0]);
        db.printArrayDiag();

        db.initializeDatabase();
        db.printArrayDiag();

        db.insertNewProfile(new PatientProf("SomeAdmin", "FirstName", "LAST_NAME", "home_address",
                "123-456-7890", 30.0f, "insuranceType", "normal_patient_type",
                new MedCond("CONTACT", "PHONE-5678", "ALLERGIES", "ILLNESSES")));
        db.printArrayDiag();

        assert db.findProfile("Me", "Smith").getLastName().equals("Smith");

        boolean success = db.deleteProfile("SomeAdmin", "LAST_NAME");
        assert success;
        db.printArrayDiag();

        PatientProf firstNext = db.findNextProfile();
        PatientProf first = db.findFirstProfile();
        assert firstNext == first;

        int n = 1;
        while (db.findNextProfile() != first)
            n++;
        assert n == db.next_insert_i;

        PatientProf shouldStillBeFirst = db.findFirstProfile();
        assert shouldStillBeFirst == first;

        db.writeAllPatientProf();
    }

    /**
     * TESTING: Print next index and deep String representation of patient array
     */
    private void printArrayDiag() {
        System.out.printf("Next index: %d, array: %s%n", next_insert_i, Arrays.deepToString(patientList));
    }

}
