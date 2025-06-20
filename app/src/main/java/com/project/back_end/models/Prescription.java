package com.project.back_end.models;

import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;

@Document(collection = "prescriptions")
public class Prescription {

	@Id
	private String id;

	@NonNull
	@Size(min = 3, max = 100)
	private String patientName;

	@NonNull
	private long appointmentId;

	@NonNull
	@Size(min = 3, max = 100)
	private String medication;

	@NonNull
	private String dosage;

	@Size(max = 200)
	private String doctorNotes;

	public Prescription() {
	}

	public Prescription(String patientName, String medication, String dosage, String doctorNotes, long appointmentId) {
		this.appointmentId = appointmentId;
		this.doctorNotes = doctorNotes;
		this.dosage = dosage;
		this.medication = medication;
		this.patientName = patientName;
	}

	public void setAppointmentId(long appointmentId) {
		this.appointmentId = appointmentId;
	}

	public void setDoctorNotes(String doctorNotes) {
		this.doctorNotes = doctorNotes;
	}

	public void setDosage(String dosage) {
		this.dosage = dosage;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMedication(String medication) {
		this.medication = medication;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public long getAppointmentId() {
		return appointmentId;
	}

	public String getDoctorNotes() {
		return doctorNotes;
	}

	public String getDosage() {
		return dosage;
	}

	public String getId() {
		return id;
	}

	public String getMedication() {
		return medication;
	}

	public String getPatientName() {
		return patientName;
	}

}
