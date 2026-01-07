package com.example.MentalMind.service;

import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Create a new appointment request
    public Appointment createAppointment(User student, User counselor, LocalDateTime appointmentDateTime, String reason) {
        Appointment appointment = new Appointment(student, counselor, appointmentDateTime, reason);
        return appointmentRepository.save(appointment);
    }

    // Get all appointments for a student
    public List<Appointment> getStudentAppointments(User student) {
        return appointmentRepository.findByStudentOrderByAppointmentDateTimeDesc(student);
    }

    // Get upcoming appointments for a student
    public List<Appointment> getStudentUpcomingAppointments(User student) {
        return appointmentRepository.findUpcomingAppointments(student);
    }

    // Get past appointments for a student
    public List<Appointment> getStudentPastAppointments(User student) {
        return appointmentRepository.findPastAppointments(student);
    }

    // Get all appointments for a counselor
    public List<Appointment> getCounselorAppointments(User counselor) {
        return appointmentRepository.findByCounselorOrderByAppointmentDateTimeDesc(counselor);
    }

    // Get upcoming appointments for a counselor
    public List<Appointment> getCounselorUpcomingAppointments(User counselor) {
        return appointmentRepository.findCounselorUpcomingAppointments(counselor);
    }

    // Get pending appointments for a counselor
    public List<Appointment> getPendingAppointments(User counselor) {
        return appointmentRepository.findPendingAppointments(counselor);
    }

    // Approve an appointment
    public Appointment approveAppointment(Long appointmentId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus("APPROVED");
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }
        return null;
    }

    // Reject an appointment
    public Appointment rejectAppointment(Long appointmentId, String reason) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus("REJECTED");
            appointment.setRejectionReason(reason);
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }
        return null;
    }

    // Complete an appointment
    public Appointment completeAppointment(Long appointmentId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus("COMPLETED");
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }
        return null;
    }

    // Cancel an appointment
    public Appointment cancelAppointment(Long appointmentId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus("CANCELLED");
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }
        return null;
    }

    // Get appointment by id
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    // Update appointment status
    public Appointment updateStatus(Long appointmentId, String status) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus(status);
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }
        return null;
    }
}
