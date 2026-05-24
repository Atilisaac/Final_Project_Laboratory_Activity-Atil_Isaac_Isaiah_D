/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clinic.view;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author Administrator
 */
public class Doctor {
    // =========================================================
    // BOOLEAN FLAG
    // Tracks if Update button is on 1st click (load) or 2nd click (save)
    // =========================================================
    public boolean updateMode = false;

    // Reference to the main form to access its text fields
    private DoctorFrame frame;

    // =========================================================
    // CONSTRUCTOR
    // Receives DoctorFrame so we can access its fields
    // =========================================================
    public Doctor(DoctorFrame frame) {
        this.frame = frame;
    }

    // =========================================================
    // CLEAR FORM METHOD
    // Resets all fields back to empty after any operation
    // =========================================================
    public void clearFormFields() {
        frame.txtDoctorID.setText("");
        frame.txtDoctorName.setText("");
        frame.txtSpecialization.setText("");
        frame.txtContactNo.setText("");
        frame.txtEmail.setText("");
        frame.txtDoctorID.setEditable(true); // unlock Doctor ID field
    }

    // =========================================================
    // CREATE - INSERT
    // Inserts a new doctor record into the database
    // =========================================================
    public void createDoctor() {
        final String sql = "INSERT INTO doctors "
                + "(doctor_id, doctor_name, specialization, contact_number, email) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            // Set values from form fields
            pst.setString(1, frame.txtDoctorID.getText().trim());
            pst.setString(2, frame.txtDoctorName.getText().trim());
            pst.setString(3, frame.txtSpecialization.getText().trim());
            pst.setString(4, frame.txtContactNo.getText().trim());
            pst.setString(5, frame.txtEmail.getText().trim());

            int result = pst.executeUpdate();

            if (result > 0) {
                // Success - show message and clear form
                JOptionPane.showMessageDialog(frame, "Doctor successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFormFields();
                frame.loadDoctorTableData();
            }

        } catch (SQLException e) {
            // Check if error is duplicate Doctor ID
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(frame, "Doctor ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================
    // READ - LOAD BY ID
    // Fetches doctor info from DB and fills the form fields
    // Called on 1st click of Update button
    // =========================================================
    public void loadDoctorByID() {
        String sql = "SELECT * FROM doctors WHERE doctor_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            // Search using Doctor ID entered in the form
            pst.setString(1, frame.txtDoctorID.getText().trim());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Fill all fields with data from database
                frame.txtDoctorName.setText(rs.getString("doctor_name"));
                frame.txtSpecialization.setText(rs.getString("specialization"));
                frame.txtContactNo.setText(rs.getString("contact_number"));
                frame.txtEmail.setText(rs.getString("email"));

                // Lock Doctor ID so user cannot change it
                frame.txtDoctorID.setEditable(false);

                // Switch to save mode for 2nd click
                updateMode = true;

                JOptionPane.showMessageDialog(frame, "Doctor loaded!\nEdit fields then click Update again.");
            } else {
                JOptionPane.showMessageDialog(frame, "Doctor ID not found!");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }

    // =========================================================
    // UPDATE
    // Saves edited fields back to database
    // Called on 2nd click of Update button
    // =========================================================
    public void updateDoctor() {
        String sql = "UPDATE doctors SET "
                + "doctor_name=?, specialization=?, "
                + "contact_number=?, email=? "
                + "WHERE doctor_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            // Set updated values from form fields
            pst.setString(1, frame.txtDoctorName.getText().trim());
            pst.setString(2, frame.txtSpecialization.getText().trim());
            pst.setString(3, frame.txtContactNo.getText().trim());
            pst.setString(4, frame.txtEmail.getText().trim());
            pst.setString(5, frame.txtDoctorID.getText().trim()); // WHERE condition

            int result = pst.executeUpdate();

            if (result > 0) {
                // Success - clear form and reset update mode
                JOptionPane.showMessageDialog(frame, "Doctor updated successfully!");
                clearFormFields();
                updateMode = false;
                frame.loadDoctorTableData();
            } else {
                JOptionPane.showMessageDialog(frame, "Doctor ID not found!");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }

    // =========================================================
    // DELETE
    // Shows doctor info first then removes record after confirmation
    // =========================================================
    public void deleteDoctor() {

        // First fetch and show doctor info before confirming
        String selectSql = "SELECT * FROM doctors WHERE doctor_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(selectSql)) {

            pst.setString(1, frame.txtDoctorID.getText().trim());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                // Show doctor info before confirming delete
                String info = "Doctor ID: " + rs.getString("doctor_id") + "\n"
                        + "Name: " + rs.getString("doctor_name") + "\n"
                        + "Specialization: " + rs.getString("specialization") + "\n"
                        + "Contact No: " + rs.getString("contact_number") + "\n"
                        + "Email: " + rs.getString("email") + "\n\n"
                        + "Are you sure you want to delete this doctor?";

                // Ask user to confirm before deleting
                int confirm = JOptionPane.showConfirmDialog(frame, info, "Confirm Delete", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) return;

                // Proceed with delete
                String deleteSql = "DELETE FROM doctors WHERE doctor_id=?";

                try (Connection conn2 = DBConnection.getConnection();
                     PreparedStatement pst2 = conn2.prepareStatement(deleteSql)) {

                    pst2.setString(1, frame.txtDoctorID.getText().trim());
                    int result = pst2.executeUpdate();

                    if (result > 0) {
                        // Success - clear form and refresh table
                        JOptionPane.showMessageDialog(frame, "Doctor deleted successfully!");
                        clearFormFields();
                        frame.loadDoctorTableData();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Doctor ID not found!");
                    }
                }

            } else {
                JOptionPane.showMessageDialog(frame, "Doctor ID not found!");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
}
