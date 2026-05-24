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
public class BillingRecord {
    // =========================================================
    // BOOLEAN FLAG
    // Tracks if Update button is 1st click (load) or 2nd click (save)
    // =========================================================
    public boolean updateMode = false;
 
    // Reference to the main form
    private BillingFrame frame;
 
    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public BillingRecord(BillingFrame frame) {
        this.frame = frame;
    }
 
    // =========================================================
    // CLEAR FORM
    // Resets all fields back to empty
    // =========================================================
    public void clearFormFields() {
        frame.txtBillID.setText("");
        frame.txtDate.setText("");
        frame.txtTotalAmount.setText("");
        frame.txtPaymentDate.setText("");
        frame.cmbPatient.setSelectedIndex(0);
        frame.cmbPaymentStatus.setSelectedIndex(0);
        frame.txtBillID.setEditable(true);
        updateMode = false;
    }
 
    // =========================================================
    // LOAD PATIENTS INTO COMBOBOX
    // Shows patient_id and full name
    // =========================================================
    public void loadPatientComboBox() {
        frame.cmbPatient.removeAllItems();
 
        String sql = "SELECT patient_id, CONCAT(first_name,' ',last_name) AS full_name FROM patients";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
 
            while (rs.next()) {
                String item = rs.getString("patient_id") + " - " + rs.getString("full_name");
                frame.cmbPatient.addItem(item);
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading patients:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // LOAD TABLE DATA
    // Loads all billing records into the table
    // =========================================================
    public void loadBillingTableData() {
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblBilling.getModel();
        model.setRowCount(0);
 
        String sql = "SELECT b.bill_id, "
                + "CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                + "b.date, b.amount, b.payment_status, b.payment_date "
                + "FROM bills b "
                + "JOIN patients p ON b.patient_id = p.patient_id";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
 
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("bill_id"),
                    rs.getString("patient_name"),
                    rs.getString("date"),
                    rs.getString("amount"),
                    rs.getString("payment_status"),
                    rs.getString("payment_date")
                });
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading table:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // CREATE - INSERT
    // Inserts new billing record into database
    // =========================================================
    public void createBill() {
        if (frame.cmbPatient.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(frame, "Please select a patient!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        String billID = frame.txtBillID.getText().trim();
        String patientID = frame.cmbPatient.getSelectedItem().toString().split(" - ")[0].trim();
        String date = frame.txtDate.getText().trim();
        String amount = frame.txtTotalAmount.getText().trim();
        String paymentStatus = frame.cmbPaymentStatus.getSelectedItem().toString();
        String paymentDate = frame.txtPaymentDate.getText().trim();
 
        if (billID.isEmpty() || amount.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill out all required fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
 
        String sql = "INSERT INTO bills (bill_id, patient_id, date, amount, payment_status, payment_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, billID);
            pst.setString(2, patientID);
            pst.setString(3, date);
            pst.setString(4, amount);
            pst.setString(5, paymentStatus);
            pst.setString(6, paymentDate.isEmpty() ? null : paymentDate);
 
            int result = pst.executeUpdate();
 
            if (result > 0) {
                JOptionPane.showMessageDialog(frame, "Bill saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFormFields();
                loadBillingTableData();
            }
 
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(frame, "Bill ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    // =========================================================
    // READ - LOAD BY BILL ID
    // Fetches bill info from DB and fills the form fields
    // Called on 1st click of Update button
    // =========================================================
    public void loadBillByID() {
        String billID = frame.txtBillID.getText().trim();
 
        String sql = "SELECT b.*, "
                + "CONCAT(p.patient_id,' - ',p.first_name,' ',p.last_name) AS patient_item "
                + "FROM bills b "
                + "JOIN patients p ON b.patient_id = p.patient_id "
                + "WHERE b.bill_id = ?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, billID);
            ResultSet rs = pst.executeQuery();
 
            if (rs.next()) {
                frame.cmbPatient.setSelectedItem(rs.getString("patient_item"));
                frame.txtDate.setText(rs.getString("date") != null ? rs.getString("date") : "");
                frame.txtTotalAmount.setText(rs.getString("amount"));
                frame.cmbPaymentStatus.setSelectedItem(rs.getString("payment_status"));
                frame.txtPaymentDate.setText(rs.getString("payment_date") != null ? rs.getString("payment_date") : "");
 
                frame.txtBillID.setEditable(false);
                updateMode = true;
 
                JOptionPane.showMessageDialog(frame, "Bill loaded!\nEdit fields then click Update again.");
            } else {
                JOptionPane.showMessageDialog(frame, "Bill ID not found!");
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
    public void updateBill() {
        if (frame.cmbPatient.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(frame, "Please select a patient!");
            return;
        }
 
        String billID = frame.txtBillID.getText().trim();
        String patientID = frame.cmbPatient.getSelectedItem().toString().split(" - ")[0].trim();
        String date = frame.txtDate.getText().trim();
        String amount = frame.txtTotalAmount.getText().trim();
        String paymentStatus = frame.cmbPaymentStatus.getSelectedItem().toString();
        String paymentDate = frame.txtPaymentDate.getText().trim();
 
        String sql = "UPDATE bills SET patient_id=?, date=?, amount=?, payment_status=?, payment_date=? "
                + "WHERE bill_id=?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, patientID);
            pst.setString(2, date.isEmpty() ? null : date);
            pst.setString(3, amount);
            pst.setString(4, paymentStatus);
            pst.setString(5, paymentDate.isEmpty() ? null : paymentDate);
            pst.setString(6, billID);
 
            int result = pst.executeUpdate();
 
            if (result > 0) {
                JOptionPane.showMessageDialog(frame, "Bill updated successfully!");
                clearFormFields();
                updateMode = false;
                loadBillingTableData();
            } else {
                JOptionPane.showMessageDialog(frame, "Bill ID not found!");
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // DELETE
    // Shows bill info then removes after confirmation
    // =========================================================
    public void deleteBill() {
        String billID = frame.txtBillID.getText().trim();
 
        if (billID.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter Bill ID first!");
            return;
        }
 
        String selectSql = "SELECT b.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name "
                + "FROM bills b "
                + "JOIN patients p ON b.patient_id = p.patient_id "
                + "WHERE b.bill_id = ?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(selectSql)) {
 
            pst.setString(1, billID);
            ResultSet rs = pst.executeQuery();
 
            if (rs.next()) {
                String info = "Bill ID: " + rs.getString("bill_id") + "\n"
                        + "Patient: " + rs.getString("patient_name") + "\n"
                        + "Date: " + rs.getString("date") + "\n"
                        + "Amount: " + rs.getString("amount") + "\n"
                        + "Status: " + rs.getString("payment_status") + "\n"
                        + "Payment Date: " + rs.getString("payment_date") + "\n\n"
                        + "Are you sure you want to delete this bill?";
 
                int confirm = JOptionPane.showConfirmDialog(frame, info, "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
 
                String deleteSql = "DELETE FROM bills WHERE bill_id = ?";
 
                try (Connection conn2 = DBConnection.getConnection();
                     PreparedStatement pst2 = conn2.prepareStatement(deleteSql)) {
 
                    pst2.setString(1, billID);
                    int result = pst2.executeUpdate();
 
                    if (result > 0) {
                        JOptionPane.showMessageDialog(frame, "Bill deleted successfully!");
                        clearFormFields();
                        loadBillingTableData();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Bill ID not found!");
                    }
                }
 
            } else {
                JOptionPane.showMessageDialog(frame, "Bill ID not found!");
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error:\n" + e.getMessage());
        }
    }
 
    // =========================================================
    // SEARCH
    // Filters table based on selected filter and keyword
    // =========================================================
    public void searchBill() {
        String filter = frame.cmbSearchFilter.getSelectedItem().toString();
        String keyword = frame.txtSearchInput.getText().trim();
 
        if (keyword.isEmpty()) {
            loadBillingTableData();
            return;
        }
 
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) frame.tblBilling.getModel();
        model.setRowCount(0);
 
        String sql;
 
        if (filter.equals("Search by ID")) {
            sql = "SELECT b.bill_id, CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                    + "b.date, b.amount, b.payment_status, b.payment_date "
                    + "FROM bills b "
                    + "JOIN patients p ON b.patient_id = p.patient_id "
                    + "WHERE b.bill_id LIKE ?";
        } else {
            // Search by Name
            sql = "SELECT b.bill_id, CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                    + "b.date, b.amount, b.payment_status, b.payment_date "
                    + "FROM bills b "
                    + "JOIN patients p ON b.patient_id = p.patient_id "
                    + "WHERE CONCAT(p.first_name,' ',p.last_name) LIKE ?";
        }
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
 
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
 
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("bill_id"),
                    rs.getString("patient_name"),
                    rs.getString("date"),
                    rs.getString("amount"),
                    rs.getString("payment_status"),
                    rs.getString("payment_date")
                });
            }
 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error searching:\n" + e.getMessage());
        }
    }
}
