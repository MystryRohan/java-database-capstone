package com.project.back_end.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Appointment;

import jakarta.transaction.Transactional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	// - **findByDoctorIdAndAppointmentTimeBetween**:
	// - This method retrieves a list of appointments for a specific doctor within a
	// given time range.
	// - The doctor’s available times are eagerly fetched to avoid lazy loading.
	// - Return type: List<Appointment>
	// - Parameters: Long doctorId, LocalDateTime start, LocalDateTime end
	// - It uses a LEFT JOIN to fetch the doctor’s available times along with the
	// appointments.
	public List<Appointment> findByDoctorIdAndAppointmentTimeBetween(long doctorName, LocalDateTime start,
			LocalDateTime end);

	// -
	// **findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween**:
	// - This method retrieves appointments for a specific doctor and patient name
	// (ignoring case) within a given time range.
	// - It performs a LEFT JOIN to fetch both the doctor and patient details along
	// with the appointment times.
	// - Return type: List<Appointment>
	// - Parameters: Long doctorId, String patientName, LocalDateTime start,
	// LocalDateTime end
	public List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(long doctorId,
			String patientName, LocalDateTime start, LocalDateTime end);

	// - **deleteAllByDoctorId**:
	// - This method deletes all appointments associated with a particular doctor.
	// - It is marked as @Modifying and @Transactional, which makes it a
	// modification query, ensuring that the operation is executed within a
	// transaction.
	// - Return type: void
	// - Parameters: Long doctorId
	@Modifying
	@Transactional
	public void deleteAllByDoctorId(long doctorId);

	// - **findByPatientId**:
	// - This method retrieves all appointments for a specific patient.
	// - Return type: List<Appointment>
	// - Parameters: Long patientId
	public List<Appointment> findByPatientId(long patientId);

	// - **findByPatient_IdAndStatusOrderByAppointmentTimeAsc**:
	// - This method retrieves all appointments for a specific patient with a given
	// status, ordered by the appointment time.
	// - Return type: List<Appointment>
	// - Parameters: Long patientId, int status
	public List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(long patientId, int status);

	// - **filterByDoctorNameAndPatientId**:
	// - This method retrieves appointments based on a doctor’s name (using a LIKE
	// query) and the patient’s ID.
	// - Return type: List<Appointment>
	// - Parameters: String doctorName, Long patientId
	public List<Appointment> filterByDoctorNameAndPatientId(String doctorName, long patientId);

	// - **filterByDoctorNameAndPatientIdAndStatus**:
	// - This method retrieves appointments based on a doctor’s name (using a LIKE
	// query), patient’s ID, and a specific appointment status.
	// - Return type: List<Appointment>
	// - Parameters: String doctorName, Long patientId, int status
	public List<Appointment> filterByDoctorNameAndPatientIdAndStatus(String doctorName, long patientId, int status);

	// - **updateStatus**:
	// - This method updates the status of a specific appointment based on its ID.
	// - Return type: void
	// - Parameters: int status, long id
	public void updateStatus(int status, long id);

	// 3. @Modifying and @Transactional annotations:
	// - The @Modifying annotation is used to indicate that the method performs a
	// modification operation (like DELETE or UPDATE).
	// - The @Transactional annotation ensures that the modification is done within
	// a transaction, meaning that if any exception occurs, the changes will be
	// rolled back.

	// 4. @Repository annotation:
	// - The @Repository annotation marks this interface as a Spring Data JPA
	// repository.
	// - Spring Data JPA automatically implements this repository, providing the
	// necessary CRUD functionality and custom queries defined in the interface.

}
