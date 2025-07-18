package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    // 1. Set Up the Controller Class:
    // - Annotate the class with `@RestController` to define it as a REST API
    // controller.
    // - Use `@RequestMapping("/appointments")` to set a base path for all
    // appointment-related endpoints.
    // - This centralizes all routes that deal with booking, updating, retrieving,
    // and canceling appointments.

    // 2. Autowire Dependencies:
    // - Inject `AppointmentService` for handling the business logic specific to
    // appointments.
    // - Inject the general `Service` class, which provides shared functionality
    // like token validation and appointment checks.
    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // 3. Define the `getAppointments` Method:
    // - Handles HTTP GET requests to fetch appointments based on date and patient
    // name.
    // - Takes the appointment date, patient name, and token as path variables.
    // - First validates the token for role `"doctor"` using the `Service`.
    // - If the token is valid, returns appointments for the given patient on the
    // specified date.
    // - If the token is invalid or expired, responds with the appropriate message
    // and status code.
    @GetMapping("/{date}/{patient}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable LocalDate date, String patient, String token) {
        ResponseEntity<String> result = service.validateToken(token, "doctor");
        Map<String, List<AppointmentDTO>> map = new HashMap<>();
        if (result.getBody().equals("Welcome")) {
            map.put("appointments", appointmentService.getAppointments(token, date, patient).getBody());
            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }
    }

    // 4. Define the `bookAppointment` Method:
    // - Handles HTTP POST requests to create a new appointment.
    // - Accepts a validated `Appointment` object in the request body and a token as
    // a path variable.
    // - Validates the token for the `"patient"` role.
    // - Uses service logic to validate the appointment data (e.g., check for doctor
    // availability and time conflicts).
    // - Returns success if booked, or appropriate error messages if the doctor ID
    // is invalid or the slot is already taken.
    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        String result = service.validateToken(token, "user").getBody();
        Map<String, Object> map = new HashMap<>();
        if (result.equals("Welcome")) {
            appointmentService.getAppointments(token, appointment.getAppointmentDate(), result);
            int res = appointmentService.bookAppointment(appointment);
            if (res == 1) {
                map.put("message", "booked");
                return new ResponseEntity<>(map, HttpStatus.OK);
            } else {
                map.put("message", "Failed");
                return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        map.put("message", "Invalid Credentials");
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
    // 5. Define the `updateAppointment` Method:
    // - Handles HTTP PUT requests to modify an existing appointment.
    // - Accepts a validated `Appointment` object and a token as input.
    // - Validates the token for `"patient"` role.
    // - Delegates the update logic to the `AppointmentService`.
    // - Returns an appropriate success or failure response based on the update
    // result.

    @PutMapping("/{token}")
    public ResponseEntity<String> updateAppointment(@PathVariable String token,
            @RequestBody @Valid Appointment appointment) {
        ResponseEntity<String> result = service.validateToken(token, "user");
        if (result.getBody().equals("Welcome")) {
            return appointmentService.updateAppointment(appointment);
        }
        return result;
    }

    // 6. Define the `cancelAppointment` Method:
    // - Handles HTTP DELETE requests to cancel a specific appointment.
    // - Accepts the appointment ID and a token as path variables.
    // - Validates the token for `"patient"` role to ensure the user is authorized
    // to cancel the appointment.
    // - Calls `AppointmentService` to handle the cancellation process and returns
    // the result.
    @DeleteMapping("/id/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable String token, Long id) {
        ResponseEntity<String> result = service.validateToken(token, "user");
        if (result.getBody().equals("Welcome")) {
            return appointmentService.cancelAppointment(id, token);
        }
        return result;
    }
}
