package com.project.back_end.models;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

@Entity
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne
  @NotNull(message = "doctor cannot be empty")
  private long doctor;

  @ManyToOne
  @NotNull(message = "patient cannot be empty")
  private long patient;

  @Future(message = "appointment time must be in the future")
  @NotNull(message = "appointment time cannot be empty")
  private LocalDateTime appointmentTime;

  @NotNull(message = "status cannot be empty")
  private int status;

  public Appointment() {
  };

  public void setAppointmentTime(LocalDateTime appointmentTime) {
    this.appointmentTime = appointmentTime;
  }

  public void setdoctor(long doctor) {
    this.doctor = doctor;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setPatient(long patient) {
    this.patient = patient;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public LocalDateTime getAppointmentTime() {
    return appointmentTime;
  }

  public long getDoctor() {
    return doctor;
  }

  public long getId() {
    return id;
  }

  public long getPatient() {
    return patient;
  }

  public int getStatus() {
    return status;
  }

  @Transient
  public LocalDateTime getEndTime() {
    return this.appointmentTime.plusHours(1);
  }

  @Transient
  public LocalDate getAppointmentDate() {
    return this.appointmentTime.toLocalDate();
  }

  @Transient
  public LocalTime getAppointmentTimeOnly() {
    return this.appointmentTime.toLocalTime();
  }

}
