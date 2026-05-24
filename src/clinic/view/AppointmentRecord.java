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
public class AppointmentRecord {
     public boolean updateMode = false;
    private AppointmentFrame frame;
 
    public AppointmentRecord(AppointmentFrame frame) {
        this.frame = frame;
    }
 
    // =========================================================
    // TIME CONVERTER
    // Converts "10:10 AM" or "2:30 PM" to "10:10:00" or "14:30:00"
    // If already in 24hr format, returns as-is
    // =========================================================
    private String convertTo24Hour(String time) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("hh:mm a");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("HH:mm:ss");
            return outputFormat.format(inputFormat.parse(time));
        } catch (Exception e) {
            return time; // already in 24hr format, return as-is
        }
    }
 
    // =========================================================
    // CLEAR FORM
    // =========================================================
    public void clearFormFields() {
        frame.txtAppointmentID.setText("");
        frame.txtDate.setText("");
        frame.txtTime.setText("");
        frame.cmbPatient.setSelectedIndex(0);
        frame.cmbDoctor.setSelectedIndex(0);
        frame.cmbStatus.setSelectedIndex(0);
        frame.txtAppointmentID.setEditable(true);
    }
 
    // =========================================================
    // LOAD PATIENTS INTO COMBOBOX
    // =========================================================
    public void loadPatientComboBox() {
        frame.cmbPatient.removeAllItems();
 
        String sql = "SELECT patient_id, first_name, last_name FROM patients";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
 
            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                frame.cmbPatient.addItem(fullName);
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading patients:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // LOAD DOCTORS INTO COMBOBOX
    // =========================================================
    public void loadDoctorComboBox() {
        frame.cmbDoctor.removeAllItems();
 
        String sql = "SELECT doctor_id, doctor_name FROM doctors";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
 
            while (rs.next()) {
                frame.cmbDoctor.addItem(rs.getString("doctor_name"));
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading doctors:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // CREATE - INSERT
    // =========================================================
    public void createAppointment() {
        final String sql = "INSERT INTO appointments "
                + "(appointment_id, patient_id, doctor_id, appointment_date, appointment_time, status) "
                + "VALUES (?, "
                + "(SELECT patient_id FROM patients WHERE CONCAT(first_name,' ',last_name)=?), "
                + "(SELECT doctor_id FROM doctors WHERE doctor_name=?), "
                + "?, ?, ?)";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, frame.txtAppointmentID.getText().trim());
            pst.setString(2, frame.cmbPatient.getSelectedItem().toString());
            pst.setString(3, frame.cmbDoctor.getSelectedItem().toString());
            pst.setString(4, frame.txtDate.getText().trim());
            pst.setString(5, convertTo24Hour(frame.txtTime.getText().trim())); // FIXED
            pst.setString(6, frame.cmbStatus.getSelectedItem().toString());
 
            int result = pst.executeUpdate();
 
            if (result > 0) {
                JOptionPane.showMessageDialog(frame, "Appointment successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFormFields();
                frame.loadAppointmentTableData();
            }
 
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(frame, "Appointment ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    // =========================================================
    // READ - LOAD BY ID
    // =========================================================
    public void loadAppointmentByID() {
        String sql = "SELECT a.*, "
                + "CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                + "d.doctor_name "
                + "FROM appointments a "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                + "WHERE a.appointment_id=?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, frame.txtAppointmentID.getText().trim());
            ResultSet rs = pst.executeQuery();
 
            if (rs.next()) {
                frame.cmbPatient.setSelectedItem(rs.getString("patient_name"));
                frame.cmbDoctor.setSelectedItem(rs.getString("doctor_name"));
                frame.txtDate.setText(rs.getString("appointment_date"));
                frame.txtTime.setText(rs.getString("appointment_time"));
                frame.cmbStatus.setSelectedItem(rs.getString("status"));
 
                frame.txtAppointmentID.setEditable(false);
                updateMode = true;
 
                JOptionPane.showMessageDialog(frame, "Appointment loaded!\nEdit fields then click Update again.");
            } else {
                JOptionPane.showMessageDialog(frame, "Appointment ID not found!");
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // UPDATE
    // =========================================================
    public void updateAppointment() {
        String sql = "UPDATE appointments SET "
                + "patient_id=(SELECT patient_id FROM patients WHERE CONCAT(first_name,' ',last_name)=?), "
                + "doctor_id=(SELECT doctor_id FROM doctors WHERE doctor_name=?), "
                + "appointment_date=?, "
                + "appointment_time=?, "
                + "status=? "
                + "WHERE appointment_id=?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, frame.cmbPatient.getSelectedItem().toString());
            pst.setString(2, frame.cmbDoctor.getSelectedItem().toString());
            pst.setString(3, frame.txtDate.getText().trim());
            pst.setString(4, convertTo24Hour(frame.txtTime.getText().trim())); // FIXED
            pst.setString(5, frame.cmbStatus.getSelectedItem().toString());
            pst.setString(6, frame.txtAppointmentID.getText().trim());
 
            int result = pst.executeUpdate();
 
            if (result > 0) {
                JOptionPane.showMessageDialog(frame, "Appointment updated successfully!");
                clearFormFields();
                updateMode = false;
                frame.loadAppointmentTableData();
            } else {
                JOptionPane.showMessageDialog(frame, "Appointment ID not found!");
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // DELETE
    // =========================================================
    public void deleteAppointment() {
        String selectSql = "SELECT a.*, "
                + "CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                + "d.doctor_name "
                + "FROM appointments a "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                + "WHERE a.appointment_id=?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(selectSql)) {
 
            pst.setString(1, frame.txtAppointmentID.getText().trim());
            ResultSet rs = pst.executeQuery();
 
            if (rs.next()) {
                String info = "Appointment ID: " + rs.getString("appointment_id") + "\n"
                        + "Patient: " + rs.getString("patient_name") + "\n"
                        + "Doctor: " + rs.getString("doctor_name") + "\n"
                        + "Date: " + rs.getString("appointment_date") + "\n"
                        + "Time: " + rs.getString("appointment_time") + "\n"
                        + "Status: " + rs.getString("status") + "\n\n"
                        + "Are you sure you want to delete this appointment?";
 
                int confirm = JOptionPane.showConfirmDialog(frame, info, "Confirm Delete", JOptionPane.YES_NO_OPTION);
 
                if (confirm != JOptionPane.YES_OPTION) return;
 
                String deleteSql = "DELETE FROM appointments WHERE appointment_id=?";
 
                try (Connection conn2 = DBConnection.getConnection();
                     PreparedStatement pst2 = conn2.prepareStatement(deleteSql)) {
 
                    pst2.setString(1, frame.txtAppointmentID.getText().trim());
                    int result = pst2.executeUpdate();
 
                    if (result > 0) {
                        JOptionPane.showMessageDialog(frame, "Appointment deleted successfully!");
                        clearFormFields();
                        frame.loadAppointmentTableData();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Appointment ID not found!");
                    }
                }
 
            } else {
                JOptionPane.showMessageDialog(frame, "Appointment ID not found!");
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // SEARCH APPOINTMENT
    // =========================================================
    public void searchAppointment() {
        String filter = frame.cmbSearchFilter.getSelectedItem().toString();
        String keyword = frame.txtSearchInput.getText().trim();
 
        if (keyword.isEmpty()) {
            frame.loadAppointmentTableData();
            return;
        }
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblAppointmentRecords.getModel();
        model.setRowCount(0);
 
        String sql;
 
        if (filter.equals("Search by ID")) {
            sql = "SELECT a.appointment_id, "
                    + "CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                    + "d.doctor_name, a.appointment_date, a.appointment_time, a.status "
                    + "FROM appointments a "
                    + "JOIN patients p ON a.patient_id = p.patient_id "
                    + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                    + "WHERE a.appointment_id LIKE ?";
        } else if (filter.equals("Search by Date")) {
            sql = "SELECT a.appointment_id, "
                    + "CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                    + "d.doctor_name, a.appointment_date, a.appointment_time, a.status "
                    + "FROM appointments a "
                    + "JOIN patients p ON a.patient_id = p.patient_id "
                    + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                    + "WHERE a.appointment_date LIKE ?";
        } else {
            sql = "SELECT a.appointment_id, "
                    + "CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                    + "d.doctor_name, a.appointment_date, a.appointment_time, a.status "
                    + "FROM appointments a "
                    + "JOIN patients p ON a.patient_id = p.patient_id "
                    + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                    + "WHERE CONCAT(p.first_name,' ',p.last_name) LIKE ?";
        }
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
 
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("appointment_id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("status")
                });
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error searching:\n" + e.getMessage());
        }
    }
}
