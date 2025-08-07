package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

import jakarta.transaction.Transactional;

@Service
public class DoctorService {

    // 1. **Add @Service Annotation**:
    // - This class should be annotated with `@Service` to indicate that it is a
    // service layer class.
    // - The `@Service` annotation marks this class as a Spring-managed bean for
    // business logic.
    // - Instruction: Add `@Service` above the class declaration.

    // 2. **Constructor Injection for Dependencies**:
    // - The `DoctorService` class depends on `DoctorRepository`,
    // `AppointmentRepository`, and `TokenService`.
    // - These dependencies should be injected via the constructor for proper
    // dependency management.
    // - Instruction: Ensure constructor injection is used for injecting
    // dependencies into the service.
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }
    // 3. **Add @Transactional Annotation for Methods that Modify or Fetch Database
    // Data**:
    // - Methods like `getDoctorAvailability`, `getDoctors`, `findDoctorByName`,
    // `filterDoctorsBy*` should be annotated with `@Transactional`.
    // - The `@Transactional` annotation ensures that database operations are
    // consistent and wrapped in a single transaction.
    // - Instruction: Add the `@Transactional` annotation above the methods that
    // perform database operations or queries.

    // 4. **getDoctorAvailability Method**:
    // - Retrieves the available time slots for a specific doctor on a particular
    // date and filters out already booked slots.
    // - The method fetches all appointments for the doctor on the given date and
    // calculates the availability by comparing against booked slots.
    // - Instruction: Ensure that the time slots are properly formatted and the
    // available slots are correctly filtered.

    private String formatSlot(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:MM");
        return start.format(formatter) + "-" + end.format(formatter);
    }

    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            return List.of("Invalid Doctor ID: " + doctorId);
        }
        List<String> availableTimes = doctor.get().getAvailableTimes().stream().map(Object::toString)
                .collect(Collectors.toList());

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId,
                startOfDay, endOfDay);

        Set<String> bookedSlots = appointments.stream().map(appt -> {
            return formatSlot(appt.getAppointmentTime(), appt.getEndTime());
        }).collect(Collectors.toSet());
        return availableTimes.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    // 5. **saveDoctor Method**:
    // - Used to save a new doctor record in the database after checking if a doctor
    // with the same email already exists.
    // - If a doctor with the same email is found, it returns `-1` to indicate
    // conflict; `1` for success, and `0` for internal errors.
    // - Instruction: Ensure that the method correctly handles conflicts and
    // exceptions when saving a doctor.
    public int saveDoctor(Doctor newDoctor) {
        Doctor result = doctorRepository.findByEmail(newDoctor.getEmail());
        if (result != null) {
            return -1;
        }
        try {
            doctorRepository.save(newDoctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 6. **updateDoctor Method**:
    // - Updates an existing doctor's details in the database. If the doctor doesn't
    // exist, it returns `-1`.
    // - Instruction: Make sure that the doctor exists before attempting to save the
    // updated record and handle any errors properly.
    public int updateDoctor(Doctor doctor) {
        Doctor result = doctorRepository.findByEmail(doctor.getEmail());
        if (result == null) {
            return -1;
        }
        try {
            result.setName(doctor.getName());
            result.setEmail(doctor.getEmail());
            result.setPhone(doctor.getPhone());
            result.setSpecialty(doctor.getSpecialty());
            doctorRepository.save(result);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    // 7. **getDoctors Method**:
    // - Fetches all doctors from the database. It is marked with `@Transactional`
    // to ensure that the collection is properly loaded.
    // - Instruction: Ensure that the collection is eagerly loaded, especially if
    // dealing with lazy-loaded relationships (e.g., available times).

    @Transactional
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        // lazy to eager
        doctors.forEach(doctor -> doctor.getAvailableTimes().size());
        return doctors;
    }

    // 8. **deleteDoctor Method**:
    // - Deletes a doctor from the system along with all appointments associated
    // with that doctor.
    // - It first checks if the doctor exists. If not, it returns `-1`; otherwise,
    // it deletes the doctor and their appointments.
    // - Instruction: Ensure the doctor and their appointments are deleted properly,
    // with error handling for internal issues.

    public int deleteDoctor(Long id) {
        Optional<Doctor> result = doctorRepository.findById(id);
        if (result.isEmpty()) {
            return -1;
        }
        try {
            appointmentRepository.deleteAllByDoctorId(result.get().getId());
            doctorRepository.deleteById(result.get().getId());
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 9. **validateDoctor Method**:
    // - Validates a doctor's login by checking if the email and password match an
    // existing doctor record.
    // - It generates a token for the doctor if the login is successful, otherwise
    // returns an error message.
    // - Instruction: Make sure to handle invalid login attempts and password
    // mismatches properly with error responses.

    public ResponseEntity<String> validateDoctor(Login login) {
        try {
            Doctor result = doctorRepository.findByEmail(login.getEmail());
            if (result == null) {
                return new ResponseEntity<>("Invalid Email or Password", HttpStatus.UNAUTHORIZED);
            }
            if (result.getPassword().equals(login.getPassword())) {
                String token = tokenService.generateToken(login.getEmail());
                return new ResponseEntity<>(token, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid Email or Password", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 10. **findDoctorByName Method**:
    // - Finds doctors based on partial name matching and returns the list of
    // doctors with their available times.
    // - This method is annotated with `@Transactional` to ensure that the database
    // query and data retrieval are properly managed within a transaction.
    // - Instruction: Ensure that available times are eagerly loaded for the
    // doctors.

    @Transactional
    public List<Doctor> findDoctorByName(String name) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors.forEach(doctor -> doctor.getAvailableTimes().size());
        return doctors;
    }

    // 11. **filterDoctorsByNameSpecilityandTime Method**:
    // - Filters doctors based on their name, specialty, and availability during a
    // specific time (AM/PM).
    // - The method fetches doctors matching the name and specialty criteria, then
    // filters them based on their availability during the specified time period.
    // - Instruction: Ensure proper filtering based on both the name and specialty
    // as well as the specified time period.
    @Transactional
    public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return filterDoctorByTime(doctors, amOrPm);
    }

    // 12. **filterDoctorByTime Method**:
    // - Filters a list of doctors based on whether their available times match the
    // specified time period (AM/PM).
    // - This method processes a list of doctors and their available times to return
    // those that fit the time criteria.
    // - Instruction: Ensure that the time filtering logic correctly handles both AM
    // and PM time slots and edge cases.
    @Transactional
    public List<Doctor> filterDoctorByTime(String name, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findAll();
        return filterDoctorByTime(doctors, amOrPm);
    }

    // 13. **filterDoctorByNameAndTime Method**:
    // - Filters doctors based on their name and the specified time period (AM/PM).
    // - Fetches doctors based on partial name matching and filters the results to
    // include only those available during the specified time period.
    // - Instruction: Ensure that the method correctly filters doctors based on the
    // given name and time of day (AM/PM).
    @Transactional
    public List<Doctor> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        return filterDoctorByTime(doctors, amOrPm);
    }

    // 14. **filterDoctorByNameAndSpecility Method**:
    // - Filters doctors by name and specialty.
    // - It ensures that the resulting list of doctors matches both the name
    // (case-insensitive) and the specified specialty.
    // - Instruction: Ensure that both name and specialty are considered when
    // filtering doctors.
    @Transactional
    public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
    }

    // 15. **filterDoctorByTimeAndSpecility Method**:
    // - Filters doctors based on their specialty and availability during a specific
    // time period (AM/PM).
    // - Fetches doctors based on the specified specialty and filters them based on
    // their available time slots for AM/PM.
    // - Instruction: Ensure the time filtering is accurately applied based on the
    // given specialty and time period (AM/PM).
    @Transactional
    public List<Doctor> filterDoctorByTimeAndSpecility(String amOrPm, String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return filterDoctorByTime(doctors, amOrPm);
    }

    // 16. **filterDoctorBySpecility Method**:
    // - Filters doctors based on their specialty.
    // - This method fetches all doctors matching the specified specialty and
    // returns them.
    // - Instruction: Make sure the filtering logic works for case-insensitive
    // specialty matching.
    @Transactional
    public List<Doctor> filterDoctorBySpecility(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }

    // 17. **filterDoctorsByTime Method**:
    // - Filters all doctors based on their availability during a specific time
    // period (AM/PM).
    // - The method checks all doctors' available times and returns those available
    // during the specified time period.
    // - Instruction: Ensure proper filtering logic to handle AM/PM time periods.
    @Transactional
    public List<Doctor> filterDoctorsByTime(String amOrPm) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        return filtered;
    }

    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        return doctors.stream()
                .filter(doctor -> {
                    if (amOrPm == null || amOrPm.isBlank())
                        return true;

                    boolean isAM = amOrPm.equalsIgnoreCase("am");
                    return doctor.getAvailableTimes().stream().anyMatch(slot -> {
                        try {
                            String startHourStr = slot.split("-")[0].split(":")[0]; // e.g. "09"
                            int hour = Integer.parseInt(startHourStr);
                            return isAM ? hour < 12 : hour >= 12;
                        } catch (Exception e) {
                            return false; // Skip invalid time slots
                        }
                    });
                })
                .collect(Collectors.toList());
    }

}
