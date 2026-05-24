package clinic.view;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author Administrator
 */
public class PrescriptionsRecord {
    // =========================================================
    // BOOLEAN FLAG
    // Tracks if Update button is 1st click (load) or 2nd click (save)
    // =========================================================
    public boolean updateMode = false;
 
    // Reference to the main form
    private PrescriptionsFrame frame;
 
    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public PrescriptionsRecord(PrescriptionsFrame frame) {
        this.frame = frame;
    }
 
    // =========================================================
    // CLEAR FORM
    // Resets all fields and clears the table
    // =========================================================
    public void clearFormFields() {
        frame.txtPrescriptionID.setText("");
        frame.txtMedicine.setText("");
        frame.txtDosage.setText("");
        frame.txtFrequency.setText("");
        frame.txtInstructions.setText("");
        frame.cmbAppointment.setSelectedIndex(0);
        frame.txtPrescriptionID.setEditable(true);
        updateMode = false;
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
        model.setRowCount(0);
    }
 
    // =========================================================
    // LOAD APPOINTMENT IDs INTO COMBOBOX
    // Fetches all appointment IDs from appointments table
    // =========================================================
    public void loadAppointmentComboBox() {
         frame.cmbAppointment.removeAllItems();

    String sql = "SELECT a.appointment_id, CONCAT(p.first_name,' ',p.last_name) AS patient_name "
               + "FROM appointments a "
               + "JOIN patients p ON a.patient_id = p.patient_id";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            String item = rs.getString("appointment_id") + " - " + rs.getString("patient_name");
            frame.cmbAppointment.addItem(item);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(frame, "Error loading appointments:\n" + e.getMessage());
    }
    }
 
    // =========================================================
    // ADD ROW TO TABLE
    // Adds medicine details from fields into the table
    // =========================================================
    public void addRowToTable() {
        String medicine = frame.txtMedicine.getText().trim();
        String dosage = frame.txtDosage.getText().trim();
        String frequency = frame.txtFrequency.getText().trim();
        String instructions = frame.txtInstructions.getText().trim();
 
        if (medicine.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter medicine name!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
        model.addRow(new Object[]{medicine, dosage, frequency, instructions});
 
        // Clear medicine fields after adding row
        frame.txtMedicine.setText("");
        frame.txtDosage.setText("");
        frame.txtFrequency.setText("");
        frame.txtInstructions.setText("");
    }
 
    // =========================================================
    // REMOVE SELECTED ROW FROM TABLE
    // =========================================================
    public void removeRowFromTable() {
        int selectedRow = frame.tblPrescriptions.getSelectedRow();
 
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a row to remove!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
        model.removeRow(selectedRow);
    }
 
    // =========================================================
    // CREATE - SAVE ALL ROWS IN TABLE TO DATABASE
    // Called on 1st click of Update button (when updateMode = false)
    // =========================================================
    public void savePrescription() {
        String prescriptionID = frame.txtPrescriptionID.getText().trim();
       String appointmentID = frame.cmbAppointment.getSelectedItem() != null
        ? frame.cmbAppointment.getSelectedItem().toString().split(" - ")[0].trim() : "";
 
        if (prescriptionID.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter Prescription ID!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        if (appointmentID.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please select an Appointment!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
 
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Please add at least one medicine row!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        String sql = "INSERT INTO prescriptions (prescription_id, appointment_id, medicine, dosage, frequency, instructions) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            for (int i = 0; i < model.getRowCount(); i++) {
                // Add row suffix if multiple rows e.g. RX001-1, RX001-2
                String rowID = prescriptionID + (model.getRowCount() > 1 ? "-" + (i + 1) : "");
                pst.setString(1, rowID);
                pst.setString(2, appointmentID);
                pst.setString(3, model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString() : "");
                pst.setString(4, model.getValueAt(i, 1) != null ? model.getValueAt(i, 1).toString() : "");
                pst.setString(5, model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString() : "");
                pst.setString(6, model.getValueAt(i, 3) != null ? model.getValueAt(i, 3).toString() : "");
                pst.addBatch();
            }
 
            pst.executeBatch();
            JOptionPane.showMessageDialog(frame, "Prescription saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFormFields();
            loadAppointmentComboBox();
 
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(frame, "Prescription ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    // =========================================================
    // READ - LOAD BY PRESCRIPTION ID
    // Loads all rows for a given prescription ID into the table
    // Called on 1st click of Update button (when updateMode = false)
    // =========================================================
    public void loadPrescriptionByID() {
        String prescriptionID = frame.txtPrescriptionID.getText().trim();
 
        String sql = "SELECT * FROM prescriptions WHERE prescription_id = ? OR prescription_id LIKE ?";
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
        model.setRowCount(0);
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, prescriptionID);
            pst.setString(2, prescriptionID + "-%");
            ResultSet rs = pst.executeQuery();
 
            boolean found = false;
            while (rs.next()) {
                if (!found) {
                    // Set appointment combobox on first row
                    frame.cmbAppointment.setSelectedItem(rs.getString("appointment_id"));
                    frame.txtPrescriptionID.setEditable(false);
                    updateMode = true;
                    found = true;
                }
                model.addRow(new Object[]{
                    rs.getString("medicine"),
                    rs.getString("dosage"),
                    rs.getString("frequency"),
                    rs.getString("instructions")
                });
            }
 
            if (found) {
                JOptionPane.showMessageDialog(frame, "Prescription loaded!\nEdit rows then click Update again.");
            } else {
                JOptionPane.showMessageDialog(frame, "Prescription ID not found!");
                updateMode = false;
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // UPDATE - REPLACES ALL ROWS FOR THIS PRESCRIPTION ID
    // Called on 2nd click of Update button (when updateMode = true)
    // =========================================================
    public void updatePrescription() {
        String prescriptionID = frame.txtPrescriptionID.getText().trim();
        String appointmentID = frame.cmbAppointment.getSelectedItem() != null
        ? frame.cmbAppointment.getSelectedItem().toString().split(" - ")[0].trim() : "";
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
 
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Please add at least one medicine row!");
            return;
        }
 
        String deleteSql = "DELETE FROM prescriptions WHERE prescription_id = ? OR prescription_id LIKE ?";
        String insertSql = "INSERT INTO prescriptions (prescription_id, appointment_id, medicine, dosage, frequency, instructions) VALUES (?, ?, ?, ?, ?, ?)";
 
        try (Connection conn = DBConnection.getConnection()) {
 
            // Delete old rows first
            try (PreparedStatement delPst = conn.prepareStatement(deleteSql)) {
                delPst.setString(1, prescriptionID);
                delPst.setString(2, prescriptionID + "-%");
                delPst.executeUpdate();
            }
 
            // Re-insert updated rows
            try (PreparedStatement insPst = conn.prepareStatement(insertSql)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    String rowID = prescriptionID + (model.getRowCount() > 1 ? "-" + (i + 1) : "");
                    insPst.setString(1, rowID);
                    insPst.setString(2, appointmentID);
                    insPst.setString(3, model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString() : "");
                    insPst.setString(4, model.getValueAt(i, 1) != null ? model.getValueAt(i, 1).toString() : "");
                    insPst.setString(5, model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString() : "");
                    insPst.setString(6, model.getValueAt(i, 3) != null ? model.getValueAt(i, 3).toString() : "");
                    insPst.addBatch();
                }
                insPst.executeBatch();
            }
 
            JOptionPane.showMessageDialog(frame, "Prescription updated successfully!");
            clearFormFields();
            loadAppointmentComboBox();
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // DELETE - REMOVES ALL ROWS FOR THIS PRESCRIPTION ID
    // =========================================================
    public void deletePrescription() {
        String prescriptionID = frame.txtPrescriptionID.getText().trim();
 
        if (prescriptionID.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter Prescription ID first!");
            return;
        }
 
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete Prescription ID: " + prescriptionID + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
 
        if (confirm != JOptionPane.YES_OPTION) return;
 
        String sql = "DELETE FROM prescriptions WHERE prescription_id = ? OR prescription_id LIKE ?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, prescriptionID);
            pst.setString(2, prescriptionID + "-%");
            int result = pst.executeUpdate();
 
            if (result > 0) {
                JOptionPane.showMessageDialog(frame, "Prescription deleted successfully!");
                clearFormFields();
                loadAppointmentComboBox();
            } else {
                JOptionPane.showMessageDialog(frame, "Prescription ID not found!");
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
    
}
