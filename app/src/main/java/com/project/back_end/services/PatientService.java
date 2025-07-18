package com.project.back_end.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

import jakarta.transaction.Transactional;

@Service
public class PatientService {
    // 1. **Add @Service Annotation**:
    // - The `@Service` annotation is used to mark this class as a Spring service
    // component.
    // - It will be managed by Spring's container and used for business logic
    // related to patients and appointments.
    // - Instruction: Ensure that the `@Service` annotation is applied above the
    // class declaration.

    // 2. **Constructor Injection for Dependencies**:
    // - The `PatientService` class has dependencies on `PatientRepository`,
    // `AppointmentRepository`, and `TokenService`.
    // - These dependencies are injected via the constructor to maintain good
    // practices of dependency injection and testing.
    // - Instruction: Ensure constructor injection is used for all the required
    // dependencies.
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.tokenService = tokenService;
    }
    // 3. **createPatient Method**:
    // - Creates a new patient in the database. It saves the patient object using
    // the `PatientRepository`.
    // - If the patient is successfully saved, the method returns `1`; otherwise, it
    // logs the error and returns `0`.
    // - Instruction: Ensure that error handling is done properly and exceptions are
    // caught and logged appropriately.

    public int createPatient(Patient patient) {
        try {
            Patient result = patientRepository.findByEmail(patient.getEmail());
            if (result != null) {
                patientRepository.save(patient);
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    // 4. **getPatientAppointment Method**:
    // - Retrieves a list of appointments for a specific patient, based on their ID.
    // - The appointments are then converted into `AppointmentDTO` objects for
    // easier consumption by the API client.
    // - This method is marked as `@Transactional` to ensure database consistency
    // during the transaction.
    // - Instruction: Ensure that appointment data is properly converted into DTOs
    // and the method handles errors gracefully.

    @Transactional
    public List<AppointmentDTO> getPatientAppointment(Long patientId) {
        try {
            List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
            return appointments.stream().map(appts -> toAppointmentDTO(appts)).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    public AppointmentDTO toAppointmentDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getAppointmentId(),
                appointment.getDoctor().getDoctorId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getPatientId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus());
    }

    // 5. **filterByCondition Method**:
    // - Filters appointments for a patient based on the condition (e.g., "past" or
    // "future").
    // - Retrieves appointments with a specific status (0 for future, 1 for past)
    // for the patient.
    // - Converts the appointments into `AppointmentDTO` and returns them in the
    // response.
    // - Instruction: Ensure the method correctly handles "past" and "future"
    // conditions, and that invalid conditions are caught and returned as errors.

    public ResponseEntity<List<AppointmentDTO>> filterByCondition(String condition, Long patientId) {
        try {
            List<Appointment> appointments;
            if (condition.equals("past")) {
                appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, 1);
            } else if (condition.equals("future")) {
                appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, 0);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<AppointmentDTO> result = appointments.stream().map(appts -> toAppointmentDTO(appts))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    // 6. **filterByDoctor Method**:
    // - Filters appointments for a patient based on the doctor's name.
    // - It retrieves appointments where the doctor’s name matches the given value,
    // and the patient ID matches the provided ID.
    // - Instruction: Ensure that the method correctly filters by doctor's name and
    // patient ID and handles any errors or invalid cases.

    public List<AppointmentDTO> filterByDoctor(Long patientId, String doctorName) {
        try {
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(doctorName,
                    patientId);
            return appointments.stream().map(appts -> toAppointmentDTO(appts)).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    // 7. **filterByDoctorAndCondition Method**:
    // - Filters appointments based on both the doctor's name and the condition
    // (past or future) for a specific patient.
    // - This method combines filtering by doctor name and appointment status (past
    // or future).
    // - Converts the appointments into `AppointmentDTO` objects and returns them in
    // the response.
    // - Instruction: Ensure that the filter handles both doctor name and condition
    // properly, and catches errors for invalid input.

    public List<AppointmentDTO> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        try {
            List<Appointment> appointments;
            if (condition.equals("future")) {
                appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, 0);
            } else if (condition.equals("past")) {
                appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, 1);
            } else {
                return null;
            }
            return appointments.stream().map(appts -> toAppointmentDTO(appts)).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    // 8. **getPatientDetails Method**:
    // - Retrieves patient details using the `tokenService` to extract the patient's
    // email from the provided token.
    // - Once the email is extracted, it fetches the corresponding patient from the
    // `patientRepository`.
    // - It returns the patient's information in the response body.
    // - Instruction: Make sure that the token extraction process works correctly
    // and patient details are fetched properly based on the extracted email.

    public ResponseEntity<Patient> getPatientDetails(String token) {
        try {
            String patientEmail = tokenService.extractEmail(token);
            return new ResponseEntity<>(patientRepository.findByEmail(patientEmail), HttpStatus.OK);
        } catch (Exception e) {
            return null;
        }
    }

    // 9. **Handling Exceptions and Errors**:
    // - The service methods handle exceptions using try-catch blocks and log any
    // issues that occur. If an error occurs during database operations, the service
    // responds with appropriate HTTP status codes (e.g., `500 Internal Server
    // Error`).
    // - Instruction: Ensure that error handling is consistent across the service,
    // with proper logging and meaningful error messages returned to the client.

    // 10. **Use of DTOs (Data Transfer Objects)**:
    // - The service uses `AppointmentDTO` to transfer appointment-related data
    // between layers. This ensures that sensitive or unnecessary data (e.g.,
    // password or private patient information) is not exposed in the response.
    // - Instruction: Ensure that DTOs are used appropriately to limit the exposure
    // of internal data and only send the relevant fields to the client.

}
