package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    // 1. Set Up the Controller Class:
    // - Annotate the class with `@RestController` to define it as a REST controller
    // that serves JSON responses.
    // - Use `@RequestMapping("${api.path}doctor")` to prefix all endpoints with a
    // configurable API path followed by "doctor".
    // - This class manages doctor-related functionalities such as registration,
    // login, updates, and availability.

    // 2. Autowire Dependencies:
    // - Inject `DoctorService` for handling the core logic related to doctors
    // (e.g., CRUD operations, authentication).
    // - Inject the shared `Service` class for general-purpose features like token
    // validation and filtering.
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private Service service;

    // 3. Define the `getDoctorAvailability` Method:
    // - Handles HTTP GET requests to check a specific doctor’s availability on a
    // given date.
    // - Requires `user` type, `doctorId`, `date`, and `token` as path variables.
    // - First validates the token against the user type.
    // - If the token is invalid, returns an error response; otherwise, returns the
    // availability status for the doctor.
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getDoctorAvailability(@PathVariable String user, Long doctorId, LocalDate date,
            String token) {
        ResponseEntity<String> result = service.validateToken(token, user);
        if (result.getBody().equals("Welcome")) {
            return new ResponseEntity<>(doctorService.getDoctorAvailability(doctorId, date), HttpStatus.OK);
        }
        return result;
    }

    // 4. Define the `getDoctor` Method:
    // - Handles HTTP GET requests to retrieve a list of all doctors.
    // - Returns the list within a response map under the key `"doctors"` with HTTP
    // 200 OK status.
    @GetMapping()
    public ResponseEntity<Map<String, List<Doctor>>> getDoctor() {
        Map<String, List<Doctor>> map = new HashMap<>();
        map.put("doctor", doctorService.getDoctors());
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    // 5. Define the `saveDoctor` Method:
    // - Handles HTTP POST requests to register a new doctor.
    // - Accepts a validated `Doctor` object in the request body and a token for
    // authorization.
    // - Validates the token for the `"admin"` role before proceeding.
    // - If the doctor already exists, returns a conflict response; otherwise, adds
    // the doctor and returns a success message.

    @PostMapping("/{token}")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        ResponseEntity<String> result = service.validateToken(token, "admin");
        if (result.getBody().equals("Welcome")) {
            int res = doctorService.saveDoctor(doctor);
            if (res == -1) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            } else if (res == 0) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<>("Saved Successfully", HttpStatus.OK);
            }

        }
        return result;
    }

    // 6. Define the `doctorLogin` Method:
    // - Handles HTTP POST requests for doctor login.
    // - Accepts a validated `Login` DTO containing credentials.
    // - Delegates authentication to the `DoctorService` and returns login status
    // and token information.

    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    // 7. Define the `updateDoctor` Method:
    // - Handles HTTP PUT requests to update an existing doctor's information.
    // - Accepts a validated `Doctor` object and a token for authorization.
    // - Token must belong to an `"admin"`.
    // - If the doctor exists, updates the record and returns success; otherwise,
    // returns not found or error messages.

    @PutMapping("/{token}")
    public ResponseEntity<?> putMethodName(@PathVariable String token, @RequestBody @Valid Doctor doctor) {
        ResponseEntity<String> result = service.validateToken(token, "admin");
        if (result.getBody().equals("Welcome")) {
            int res = doctorService.updateDoctor(doctor);
            if (res == -1) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (res == 0) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<>("Updated", HttpStatus.OK);
            }
        }
        return result;
    }

    // 8. Define the `deleteDoctor` Method:
    // - Handles HTTP DELETE requests to remove a doctor by ID.
    // - Requires both doctor ID and an admin token as path variables.
    // - If the doctor exists, deletes the record and returns a success message;
    // otherwise, responds with a not found or error message.
    @DeleteMapping("/{doctorId}/{token}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long doctorId, String token) {
        ResponseEntity<String> result = service.validateToken(token, "admin");
        if (result.getBody().equals("Welcome")) {
            int res = doctorService.deleteDoctor(doctorId);
            if (res == -1) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (res == 0) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return result;
    }

    // 9. Define the `filter` Method:
    // - Handles HTTP GET requests to filter doctors based on name, time, and
    // specialty.
    // - Accepts `name`, `time`, and `speciality` as path variables.
    // - Calls the shared `Service` to perform filtering logic and returns matching
    // doctors in the response.
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<?> filter(@RequestParam String name, String time, String speciality) {
        Map<String, List<Doctor>> map = new HashMap<>();
        map.put("doctors", doctorService.filterDoctorsByNameSpecilityandTime(name, speciality, time));
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

}
