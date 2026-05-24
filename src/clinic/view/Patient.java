package clinic.view;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.sql.ResultSet;
/**
 *
 * @author Administrator
 */
public class Patient {
    
    // Tracks if Update button is on 1st click (load) or 2nd click (save)
    public boolean updateMode = false;
    
    // Reference to the main form so we can access its text fields
    private PatientsFrame frame;

    // Constructor - receives the main form
    public Patient(PatientsFrame frame) {
        this.frame = frame;
    }

    // =========================================================
    // CLEAR FORM
    // Resets all fields back to empty after any operation
    // =========================================================
    public void clearFormFields() {
        frame.txtPatientID.setText("");
        frame.txtFirstName.setText("");
        frame.txtLastName.setText("");
        frame.txtBirthDate.setText("");
        frame.txtContactNo.setText("");
        frame.txtAddress.setText("");
        frame.txtEmail.setText("");
        frame.cmbGender.setSelectedIndex(0);
        frame.txtPatientID.setEditable(true); // unlock Patient ID field
    }

    // =========================================================
    // CREATE PATIENT
    // Inserts a new patient record into the database
    // =========================================================
    public void createPatient() {
        final String sql = "INSERT INTO patients "
                + "(patient_id, first_name, last_name, gender, birth_date, contact_number, address) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            // Set values from form fields
            pst.setString(1, frame.txtPatientID.getText().trim());
            pst.setString(2, frame.txtFirstName.getText().trim());
            pst.setString(3, frame.txtLastName.getText().trim());
            pst.setString(4, frame.cmbGender.getSelectedItem().toString());
            pst.setString(5, frame.txtBirthDate.getText().trim());
            pst.setString(6, frame.txtContactNo.getText().trim());
            pst.setString(7, frame.txtAddress.getText().trim());

            int result = pst.executeUpdate();

            if (result > 0) {
                // Success - show message and clear the form
                JOptionPane.showMessageDialog(frame, "Patient successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFormFields();
            }

        } catch (SQLException e) {
            // Check if error is duplicate Patient ID
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(frame, "Patient ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================
    // LOAD PATIENT BY ID
    // Fetches patient info from DB and fills the form fields
    // Called on 1st click of Update button
    // =========================================================
    public void loadPatientByID() {
        String sql = "SELECT * FROM patients WHERE patient_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            // Search using the Patient ID entered in the form
            pst.setString(1, frame.txtPatientID.getText().trim());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Fill all fields with data from database
                frame.txtFirstName.setText(rs.getString("first_name"));
                frame.txtLastName.setText(rs.getString("last_name"));
                frame.cmbGender.setSelectedItem(rs.getString("gender"));
                frame.txtBirthDate.setText(rs.getString("birth_date"));
                frame.txtContactNo.setText(rs.getString("contact_number"));
                frame.txtAddress.setText(rs.getString("address"));

                // Lock Patient ID so user cannot change it
                frame.txtPatientID.setEditable(false);
                
                // Switch to save mode for 2nd click
                updateMode = true;

                JOptionPane.showMessageDialog(frame, "Patient loaded!\nEdit fields then click Update again.");
            } else {
                JOptionPane.showMessageDialog(frame, "Patient ID not found!");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }

    // =========================================================
    // UPDATE PATIENT
    // Saves edited fields back to database
    // Called on 2nd click of Update button
    // =========================================================
    public void updatePatient() {
        String sql = "UPDATE patients SET "
                + "first_name=?, last_name=?, gender=?, "
                + "birth_date=?, contact_number=?, address=? "
                + "WHERE patient_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            // Set updated values from form fields
            pst.setString(1, frame.txtFirstName.getText().trim());
            pst.setString(2, frame.txtLastName.getText().trim());
            pst.setString(3, frame.cmbGender.getSelectedItem().toString());
            pst.setString(4, frame.txtBirthDate.getText().trim());
            pst.setString(5, frame.txtContactNo.getText().trim());
            pst.setString(6, frame.txtAddress.getText().trim());
            pst.setString(7, frame.txtPatientID.getText().trim()); // WHERE condition

            int result = pst.executeUpdate();

            if (result > 0) {
                // Success - clear form and reset update mode
                JOptionPane.showMessageDialog(frame, "Patient updated successfully!");
                clearFormFields();
                updateMode = false;
                 frame.loadPatientTableData(); // ← ADD THIS LINE
            } else {
                JOptionPane.showMessageDialog(frame, "Patient ID not found!");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }

    // =========================================================
    // DELETE PATIENT
    // Removes patient record from database after confirmation
    // =========================================================
    public void deletePatient() {
        
    // First check if patient exists and show their info
    String selectSql = "SELECT * FROM patients WHERE patient_id=?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(selectSql)) {

        pst.setString(1, frame.txtPatientID.getText().trim());
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {

            // Show patient info before confirming delete
            String info = "Patient ID: " + rs.getString("patient_id") + "\n"
                    + "Name: " + rs.getString("first_name") + " " + rs.getString("last_name") + "\n"
                    + "Gender: " + rs.getString("gender") + "\n"
                    + "Birth Date: " + rs.getString("birth_date") + "\n"
                    + "Contact No: " + rs.getString("contact_number") + "\n"
                    + "Address: " + rs.getString("address") + "\n\n"
                    + "Are you sure you want to delete this patient?";

            // Ask user to confirm before deleting
            int confirm = JOptionPane.showConfirmDialog(frame, info, "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            // Proceed with delete
            String deleteSql = "DELETE FROM patients WHERE patient_id=?";

            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement pst2 = conn2.prepareStatement(deleteSql)) {

                pst2.setString(1, frame.txtPatientID.getText().trim());
                int result = pst2.executeUpdate();

                if (result > 0) {
                    // Success - clear form and refresh table
                    JOptionPane.showMessageDialog(frame, "Patient deleted successfully!");
                    clearFormFields();
                    frame.loadPatientTableData();
                } else {
                    JOptionPane.showMessageDialog(frame, "Patient ID not found!");
                }
            }

        } else {
            JOptionPane.showMessageDialog(frame, "Patient ID not found!");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
    }
}
    }
