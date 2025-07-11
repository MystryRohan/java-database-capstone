package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // - **findByEmail**:
    // - This method retrieves a Patient by their email address.
    // - Return type: Patient
    // - Parameters: String email
    public Patient findByEmail(String email);

    // - **findByEmailOrPhone**:
    // - This method retrieves a Patient by either their email or phone number,
    // allowing flexibility for the search.
    // - Return type: Patient
    // - Parameters: String email, String phone
    public Patient findByEmailOrPhone(String email, String phone);
}
