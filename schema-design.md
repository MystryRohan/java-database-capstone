### Table: patients
- **id**: INT, Primary Key, AUTO_INCREMENT  
- **email**: VARCHAR(100), UNIQUE, NOT NULL  
- **full_name**: VARCHAR(100), NOT NULL  
- **phone**: VARCHAR(10), UNIQUE, NOT NULL  
- **gender**: ENUM('Male', 'Female', 'Other')  
- **date_of_birth**: DATE  
- **created_at**: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP  

> Each patient has a unique identity. Deleting a patient may require cascading deletes or archiving appointments.

---

### Table: doctors
- **id**: INT, Primary Key, AUTO_INCREMENT  
- **email**: VARCHAR(100), UNIQUE, NOT NULL  
- **full_name**: VARCHAR(100), NOT NULL  
- **specialization**: VARCHAR(100), NOT NULL  
- **phone**: VARCHAR(10), UNIQUE, NOT NULL  
- **available_from**: TIME  
- **available_to**: TIME  
- **created_at**: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP  

> Doctors manage their working hours, which help validate appointment times.

---

### Table: appointments
- **id**: INT, Primary Key, AUTO_INCREMENT  
- **doctor_id**: INT, Foreign Key → doctors(id), NOT NULL  
- **patient_id**: INT, Foreign Key → patients(id), NOT NULL  
- **appointment_time**: DATETIME, NOT NULL  
- **status**: ENUM('Scheduled', 'Completed', 'Cancelled'), DEFAULT 'Scheduled'  
- **created_at**: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP  

> A doctor cannot have overlapping appointments; a unique constraint can be enforced on (doctor_id, appointment_time).

---

### Table: admin
- **id**: INT, Primary Key, AUTO_INCREMENT  
- **username**: VARCHAR(50), UNIQUE, NOT NULL  
- **password**: VARCHAR(255), NOT NULL  
- **role**: ENUM('admin')  
- **created_at**: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP  

> Admin accounts are responsible for managing doctors and patients. Passwords should be encrypted.

---

## 🧾 MongoDB Collection Design

MongoDB is used to store unstructured or semi-structured data such as prescriptions, logs, and notes. These documents are often nested and vary in shape.

---

### Collection: prescriptions

```json
{
  "_id": "ObjectId('6651ae754f9')",
  "appointmentId": 42,
  "patientId": 101,
  "doctorId": 17,
  "dateIssued": "2024-06-03T10:15:00Z",
  "medications": [
    {
      "name": "Dolo",
      "dosage": "500mg",
      "frequency": "3 times/day",
      "duration": "7 days"
    },
    {
      "name": "Sudin Cold",
      "dosage": "200mg",
      "frequency": "2 times/day",
      "duration": "5 days"
    }
  ],
  "doctorNotes": "Avoid dairy with antibiotics. Return for follow-up in one week.",
  "pharmacy": {
    "name": "Good Health Pharmacy",
    "location": "Earth 616"
  },
  "tags": ["infection", "follow-up", "printed"]
}
