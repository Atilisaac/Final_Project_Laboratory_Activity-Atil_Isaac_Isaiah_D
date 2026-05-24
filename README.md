[README.md](https://github.com/user-attachments/files/28191471/README.md)
# Clinic Appointment and Patient Records System

A Java Swing desktop application for managing clinic operations including patient records, doctor information, appointments, prescriptions, and billing. Built with Java Swing and MySQL via JDBC.

---

## Project Information

| Field | Details |
|---|---|
| Course | Object-Oriented Programming |
| Activity | Final Project Laboratory Activity |
| Theme | Clinic Appointment and Patient Records System |
| Language | Java |
| GUI Framework | Java Swing |
| Database | MySQL |
| Database Access | JDBC |

---

## System Description

This system is designed for small clinic operations. It allows staff to manage patient records, schedule appointments with doctors, issue prescriptions, and track billing. The system provides a dashboard overview and dedicated modules for each major entity.

---

## Features

- **Login System** — secure access with username, password, and role selection
- **Dashboard** — displays total patients, doctors, appointments, and pending bills with a live appointments table
- **Patient Management** — full CRUD operations with search by ID or name
- **Doctor Management** — full CRUD operations
- **Appointment Management** — full CRUD with search by ID, date, or patient name; time auto-converts to 24-hour format
- **Prescription Management** — multi-row medicine entry per prescription linked to appointments
- **Billing Management** — full CRUD with search by ID or patient name; tracks payment status and due date

---

## Database Schema

The system uses a MySQL database named `clinic` with 5 related tables:

### Tables

**patients**
| Column | Type |
|---|---|
| patient_id | VARCHAR(20) PK |
| first_name | VARCHAR(50) |
| last_name | VARCHAR(50) |
| gender | VARCHAR(10) |
| birth_date | VARCHAR(20) |
| contact_number | VARCHAR(20) |
| address | VARCHAR(150) |

**doctors**
| Column | Type |
|---|---|
| doctor_id | VARCHAR(20) PK |
| doctor_name | VARCHAR(100) |
| specialization | VARCHAR(100) |
| contact_number | VARCHAR(20) |
| email | VARCHAR(100) |

**appointments**
| Column | Type |
|---|---|
| appointment_id | VARCHAR(20) PK |
| patient_id | VARCHAR(20) FK → patients |
| doctor_id | VARCHAR(20) FK → doctors |
| appointment_date | DATE |
| appointment_time | TIME |
| status | VARCHAR(30) |

**prescriptions**
| Column | Type |
|---|---|
| prescription_id | VARCHAR(20) PK |
| appointment_id | VARCHAR(20) FK → appointments |
| medicine | VARCHAR(100) |
| dosage | VARCHAR(100) |
| frequency | VARCHAR(100) |
| instructions | VARCHAR(200) |

**bills**
| Column | Type |
|---|---|
| bill_id | VARCHAR(20) PK |
| patient_id | VARCHAR(20) FK → patients |
| date | DATE |
| amount | DECIMAL(10,2) |
| payment_status | VARCHAR(45) |
| payment_date | DATE |

### Relationships

- `patients` → `appointments` (one-to-many)
- `doctors` → `appointments` (one-to-many)
- `appointments` → `prescriptions` (one-to-many)
- `patients` → `bills` (one-to-many)

---

## Project Structure

```
clinic.view/
│
├── LoginFrame.java          — Login screen
├── DashboardFrame.java      — Main dashboard with navigation and statistics
│
├── PatientsFrame.java       — Patient management GUI
├── Patient.java             — Patient CRUD and methods
│
├── DoctorFrame.java         — Doctor management GUI
├── Doctor.java              — Doctor CRUD and methods
│
├── AppointmentFrame.java    — Appointment management GUI
├── AppointmentRecord.java   — Appointment CRUD and methods
│
├── PrescriptionsFrame.java  — Prescription management GUI
├── PrescriptionsRecord.java — Prescription CRUD and methods
│
├── BillingFrame.java        — Billing management GUI
├── BillingRecord.java       — Billing CRUD and methods
│
├── DBConnection.java        — Database connection handler
│
└── clinic.view.picture/     — Image assets used in the GUI
```

---

## How to Run

### Prerequisites

- Java JDK 17 or higher
- MySQL 8.0 or higher
- NetBeans IDE (recommended) or any Java IDE
- MySQL Connector/J (JDBC driver)

### Steps

1. **Clone the repository**
   ```
   git clone https://github.com/yourusername/clinic-system.git
   ```

2. **Set up the database**
   - Open MySQL Workbench or any MySQL client
   - Run the provided `clinic.sql` script to create the database and tables
   ```sql
   SOURCE clinic.sql;
   ```

3. **Configure the database connection**
   - Open `DBConnection.java`
   - Update the connection details:
   ```java
   String url = "jdbc:mysql://localhost:3306/clinic";
   String user = "root";
   String password = "your_password";
   ```

4. **Add the JDBC driver**
   - In NetBeans: right-click project → Properties → Libraries → Add JAR
   - Add the `mysql-connector-j-x.x.x.jar` file

5. **Run the project**
   - Set `LoginFrame.java` as the main class
   - Press `F6` or click Run

### Default Login Credentials

| Field | Value |
|---|---|
| Username | Admin |
| Password | 123456789 |
| Role | Admin |

---

## Sample Test Data

**Patient**
- Patient ID: P001
- Name: Juan Dela Cruz
- Gender: Male
- Contact: 09171234567

**Doctor**
- Doctor ID: D001
- Name: Dr. Maria Santos
- Specialization: General Physician

**Appointment**
- Appointment ID: APT001
- Patient: P001
- Doctor: D001
- Date: 2026-05-24
- Time: 10:00:00
- Status: Confirmed

**Prescription**
- Prescription ID: RX001
- Appointment: APT001
- Medicine: Amoxicillin, 500mg, 3x a day

**Bill**
- Bill ID: BILL001
- Patient: P001
- Amount: 1500.00
- Status: Unpaid

---

## OOP Concepts Applied

- **Encapsulation** — all form fields are declared with proper access modifiers; CRUD logic is separated from GUI classes
- **Constructor use** — each Record class receives the Frame as a constructor parameter for field access
- **Method modularity** — each CRUD operation is its own method (createAppointment, loadAppointmentByID, updateAppointment, deleteAppointment)
- **Object interaction** — Frame classes communicate with Record classes through object references
- **Event-driven programming** — all buttons use ActionListener for user interaction

---

## Author

- **Name:** [Your Name]
- **Course:** Object-Oriented Programming
- **School:** [Your School]
- **Year:** 2026
