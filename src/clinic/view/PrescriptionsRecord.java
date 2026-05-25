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
   public boolean updateMode = false;
    private PrescriptionsFrame frame;
 
    public PrescriptionsRecord(PrescriptionsFrame frame) {
        this.frame = frame;
    }
 
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
 
    public void addRowToTable() {
        String prescriptionID = frame.txtPrescriptionID.getText().trim();
    String appointmentID = frame.cmbAppointment.getSelectedItem() != null
            ? frame.cmbAppointment.getSelectedItem().toString().split(" - ")[0].trim() : "";
    String medicine = frame.txtMedicine.getText().trim();
    String dosage = frame.txtDosage.getText().trim();
    String frequency = frame.txtFrequency.getText().trim();
    String instructions = frame.txtInstructions.getText().trim();

    if (prescriptionID.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Please enter Prescription ID first!", "Validation Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (appointmentID.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Please select an Appointment!", "Validation Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (medicine.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Please enter medicine name!", "Validation Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Count existing rows for this prescription to generate suffix
    int existingRows = frame.tblPrescriptions.getRowCount();
    String rowID = prescriptionID + (existingRows > 0 ? "-" + (existingRows + 1) : "");

    // Save directly to database
    String sql = "INSERT INTO prescriptions (prescription_id, appointment_id, medicine, dosage, frequency, instructions) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, rowID);
        pst.setString(2, appointmentID);
        pst.setString(3, medicine);
        pst.setString(4, dosage);
        pst.setString(5, frequency);
        pst.setString(6, instructions);

        int result = pst.executeUpdate();

        if (result > 0) {
            // Add to table display
            javax.swing.table.DefaultTableModel model =
                    (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
            model.addRow(new Object[]{medicine, dosage, frequency, instructions, rowID});

            // Clear medicine fields
            frame.txtMedicine.setText("");
            frame.txtDosage.setText("");
            frame.txtFrequency.setText("");
            frame.txtInstructions.setText("");
        }

    } catch (SQLException e) {
        if (e.getErrorCode() == 1062) {
            JOptionPane.showMessageDialog(frame, "Prescription ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    }
 
    public void removeRowFromTable() {
        String prescriptionID = frame.txtPrescriptionID.getText().trim();

    if (prescriptionID.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Enter Prescription ID first!");
        return;
    }

    // Find the row in DB first to show info
    String selectSql = "SELECT * FROM prescriptions WHERE prescription_id = ? OR prescription_id LIKE ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(selectSql)) {

        pst.setString(1, prescriptionID);
        pst.setString(2, prescriptionID + "-%");
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            String info = "Prescription ID: " + rs.getString("prescription_id") + "\n"
                    + "Medicine: " + rs.getString("medicine") + "\n"
                    + "Dosage: " + rs.getString("dosage") + "\n"
                    + "Frequency: " + rs.getString("frequency") + "\n"
                    + "Instructions: " + rs.getString("instructions") + "\n\n"
                    + "Are you sure you want to delete this prescription?";

            int confirm = JOptionPane.showConfirmDialog(frame, info, "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // Delete from DB
            String deleteSql = "DELETE FROM prescriptions WHERE prescription_id = ? OR prescription_id LIKE ?";

            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement pst2 = conn2.prepareStatement(deleteSql)) {

                pst2.setString(1, prescriptionID);
                pst2.setString(2, prescriptionID + "-%");
                int result = pst2.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(frame, "Prescription deleted successfully!");
                    clearFormFields();
                    loadAppointmentComboBox();
                    loadPrescriptionTableData();
                } else {
                    JOptionPane.showMessageDialog(frame, "Prescription ID not found!");
                }
            }

        } else {
            JOptionPane.showMessageDialog(frame, "Prescription ID not found!");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
    }
    }
 
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
            loadPrescriptionTableData();
 
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(frame, "Prescription ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    public void loadPrescriptionTableData() {
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
        model.setRowCount(0);
 
        String sql = "SELECT prescription_id, medicine, dosage, frequency, instructions FROM prescriptions";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
 
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("medicine"),
                    rs.getString("dosage"),
                    rs.getString("frequency"),
                    rs.getString("instructions"),
                    rs.getString("prescription_id")
                });
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading prescriptions:\n" + e.getMessage());
        }
    }
 
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
                // Fill combobox
                String apptID = rs.getString("appointment_id");
                for (int i = 0; i < frame.cmbAppointment.getItemCount(); i++) {
                    if (frame.cmbAppointment.getItemAt(i).toString().startsWith(apptID)) {
                        frame.cmbAppointment.setSelectedIndex(i);
                        break;
                    }
                }
                // Fill text fields with first row
                frame.txtMedicine.setText(rs.getString("medicine"));
                frame.txtDosage.setText(rs.getString("dosage"));
                frame.txtFrequency.setText(rs.getString("frequency"));
                frame.txtInstructions.setText(rs.getString("instructions"));

                frame.txtPrescriptionID.setEditable(false);
                updateMode = true;
                found = true;
            }
            // Fill table with all rows
            model.addRow(new Object[]{
                rs.getString("medicine"),
                rs.getString("dosage"),
                rs.getString("frequency"),
                rs.getString("instructions"),
                rs.getString("prescription_id")
            });
        }

        if (found) {
            JOptionPane.showMessageDialog(frame, "Prescription loaded!\nEdit fields then click Update again.");
        } else {
            JOptionPane.showMessageDialog(frame, "Prescription ID not found!");
            updateMode = false;
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
    }
    }
 
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
 
            try (PreparedStatement delPst = conn.prepareStatement(deleteSql)) {
                delPst.setString(1, prescriptionID);
                delPst.setString(2, prescriptionID + "-%");
                delPst.executeUpdate();
            }
 
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
            loadPrescriptionTableData();
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
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
                loadPrescriptionTableData();
            } else {
                JOptionPane.showMessageDialog(frame, "Prescription ID not found!");
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    public void searchPrescription() {
        String filter = frame.cmbpresfilter.getSelectedItem().toString();
        String keyword = frame.searchinput.getText().trim();
 
        if (keyword.isEmpty()) {
            loadPrescriptionTableData();
            return;
        }
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblPrescriptions.getModel();
        model.setRowCount(0);
 
        String sql;
 
        if (filter.equals("Search by ID")) {
            sql = "SELECT prescription_id, medicine, dosage, frequency, instructions "
                    + "FROM prescriptions WHERE prescription_id LIKE ?";
        } else if (filter.equals("Search by Medicine")) {
            sql = "SELECT prescription_id, medicine, dosage, frequency, instructions "
                    + "FROM prescriptions WHERE medicine LIKE ?";
        } else {
            sql = "SELECT prescription_id, medicine, dosage, frequency, instructions "
                    + "FROM prescriptions WHERE appointment_id LIKE ?";
        }
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
 
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("medicine"),
                    rs.getString("dosage"),
                    rs.getString("frequency"),
                    rs.getString("instructions"),
                    rs.getString("prescription_id")
                });
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error searching:\n" + e.getMessage());
        }
    }
    
}
