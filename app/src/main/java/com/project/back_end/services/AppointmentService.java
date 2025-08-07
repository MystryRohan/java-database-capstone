package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import jakarta.transaction.Transactional;

@org.springframework.stereotype.Service
public class AppointmentService {
    // 1. **Add @Service Annotation**:
    // - To indicate that this class is a service layer class for handling business
    // logic.
    // - The `@Service` annotation should be added before the class declaration to
    // mark it as a Spring service component.
    // - Instruction: Add `@Service` above the class definition.

    // 2. **Constructor Injection for Dependencies**:
    // - The `AppointmentService` class requires several dependencies like
    // `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and
    // `DoctorRepository`.
    // - These dependencies should be injected through the constructor.
    // - Instruction: Ensure constructor injection is used for proper dependency
    // management in Spring.
    private final AppointmentRepository appointmentRepository;
    private final Service service;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository, Service service,
            PatientRepository patientRepository, DoctorRepository doctorRepository, TokenService tokenService,
            PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.patientService = patientService;
    }

    // 3. **Add @Transactional Annotation for Methods that Modify Database**:
    // - The methods that modify or update the database should be annotated with
    // `@Transactional` to ensure atomicity and consistency of the operations.
    // - Instruction: Add the `@Transactional` annotation above methods that
    // interact with the database, especially those modifying data.

    // 4. **Book Appointment Method**:
    // - Responsible for saving the new appointment to the database.
    // - If the save operation fails, it returns `0`; otherwise, it returns `1`.
    // - Instruction: Ensure that the method handles any exceptions and returns an
    // appropriate result code.
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 5. **Update Appointment Method**:
    // - This method is used to update an existing appointment based on its ID.
    // - It validates whether the patient ID matches, checks if the appointment is
    // available for updating, and ensures that the doctor is available at the
    // specified time.
    // - If the update is successful, it saves the appointment; otherwise, it
    // returns an appropriate error message.
    // - Instruction: Ensure proper validation and error handling is included for
    // appointment updates.
    @Transactional
    public ResponseEntity<String> updateAppointment(Appointment appointment) {
        Optional<Appointment> result = appointmentRepository.findById(appointment.getId());
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (result.get().getPatient().getId() != appointment.getPatient().getId()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        int slotAvailable = service.validateAppointment(appointment);
        if (slotAvailable == 1) {
            appointmentRepository.save(appointment);
            return new ResponseEntity<>("Appointment Updated", HttpStatus.OK);
        } else if (slotAvailable == -1) {
            return new ResponseEntity<>("Doctor Not Found", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Slot already booked", HttpStatus.CONFLICT);
    }
    // 6. **Cancel Appointment Method**:
    // - This method cancels an appointment by deleting it from the database.
    // - It ensures the patient who owns the appointment is trying to cancel it and
    // handles possible errors.
    // - Instruction: Make sure that the method checks for the patient ID match
    // before deleting the appointment.

    @Transactional
    public ResponseEntity<String> cancelAppointment(Long appointmentId, String token) {
        Patient result = patientRepository.findByEmail(tokenService.extractEmail(token));
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);

        try {
            if (appointment.isPresent() && appointment.get().getPatient().getId() == result.getId()) {
                appointmentRepository.delete(appointment.get());
                return new ResponseEntity<>("Appointment Cancelled", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid Id", HttpStatus.BAD_GATEWAY);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 7. **Get Appointments Method**:
    // - This method retrieves a list of appointments for a specific doctor on a
    // particular day, optionally filtered by the patient's name.
    // - It uses `@Transactional` to ensure that database operations are consistent
    // and handled in a single transaction.
    // - Instruction: Ensure the correct use of transaction boundaries, especially
    // when querying the database for appointments.

    @Transactional
    public ResponseEntity<List<AppointmentDTO>> getAppointments(String token, LocalDate date, String patientName) {
        Doctor doctor = doctorRepository.findByEmail(tokenService.extractEmail(token));
        if (doctor == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<Appointment> appointments;
        try {
            if (!patientName.equals(null)) {
                appointments = appointmentRepository
                        .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                doctor.getId(), patientName, dayStart, dayEnd);
            } else {
                appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(),
                        dayStart, dayEnd);
            }
            List<AppointmentDTO> result = appointments.stream().map(appts -> patientService.toAppointmentDTO(appts))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // 8. **Change Status Method**:
    // - This method updates the status of an appointment by changing its value in
    // the database.
    // - It should be annotated with `@Transactional` to ensure the operation is
    // executed in a single transaction.
    // - Instruction: Add `@Transactional` before this method to ensure atomicity
    // when updating appointment status.
    @Transactional
    public void updateStatus(Long appointmentId) {
        appointmentRepository.updateStatus(1, appointmentId);
    }

}
