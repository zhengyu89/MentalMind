package com.example.MentalMind.repository;

import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find all appointments for a student
    List<Appointment> findByStudentOrderByAppointmentDateTimeDesc(User student);

    // Find upcoming appointments for a student
    @Query("SELECT a FROM Appointment a WHERE a.student = :student AND a.appointmentDateTime > CURRENT_TIMESTAMP ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findUpcomingAppointments(@Param("student") User student);

    // Find past appointments for a student
    @Query("SELECT a FROM Appointment a WHERE a.student = :student AND a.appointmentDateTime < CURRENT_TIMESTAMP ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findPastAppointments(@Param("student") User student);

    // Find all appointments for a counselor
    List<Appointment> findByCounselorOrderByAppointmentDateTimeDesc(User counselor);

    // Find upcoming appointments for a counselor
    @Query("SELECT a FROM Appointment a WHERE a.counselor = :counselor AND a.appointmentDateTime > CURRENT_TIMESTAMP ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findCounselorUpcomingAppointments(@Param("counselor") User counselor);

    // Find appointments by status
    List<Appointment> findByStatusOrderByCreatedAtDesc(String status);

    // Find pending appointments for a counselor
    @Query("SELECT a FROM Appointment a WHERE a.counselor = :counselor AND a.status = 'PENDING' ORDER BY a.createdAt DESC")
    List<Appointment> findPendingAppointments(@Param("counselor") User counselor);
}
