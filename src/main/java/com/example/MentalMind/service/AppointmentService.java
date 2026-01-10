package com.example.MentalMind.service;

import com.example.MentalMind.model.Appointment;
import com.example.MentalMind.model.User;
import com.example.MentalMind.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    // Kuala Lumpur timezone
    private static final ZoneId KUALA_LUMPUR_ZONE = ZoneId.of("Asia/Kuala_Lumpur");
    
    // Get current time in Kuala Lumpur timezone
    private LocalDateTime getNowInKualaLumpur() {
        return LocalDateTime.now(KUALA_LUMPUR_ZONE);
    }

    // Check if student has an active appointment at the same time (optionally excluding an appointment id)
    public boolean hasStudentConflict(User student, LocalDateTime dateTime, Long excludeAppointmentId) {
        if (excludeAppointmentId != null) {
            return appointmentRepository.existsActiveAtDateTimeExcluding(student, dateTime, excludeAppointmentId);
        }
        return appointmentRepository.existsActiveAtDateTime(student, dateTime);
    }

    // Create a new appointment request
    public Appointment createAppointment(User student, User counselor, LocalDateTime appointmentDateTime, String reason) {
        if (hasStudentConflict(student, appointmentDateTime, null)) {
            throw new IllegalStateException("Student already has an appointment at this time");
        }
        Appointment appointment = new Appointment(student, counselor, appointmentDateTime, reason);
        return appointmentRepository.save(appointment);
    }

    // Get all appointments for a student
    public List<Appointment> getStudentAppointments(User student) {
        return appointmentRepository.findByStudentOrderByAppointmentDateTimeAsc(student);
    }

    // Auto-complete APPROVED appointments that are in the past
    private void autoCompleteOverdueAppointments(List<Appointment> appointments) {
        LocalDateTime nowKL = getNowInKualaLumpur();
        for (Appointment appt : appointments) {
            if ("APPROVED".equals(appt.getStatus()) && appt.getAppointmentDateTime() != null && 
                appt.getAppointmentDateTime().isBefore(nowKL)) {
                appt.setStatus("COMPLETED");
                appt.setUpdatedAt(nowKL);
                appointmentRepository.save(appt);
            }
        }
    }
    
    // Auto-reject PENDING appointments that are in the past
    private void autoRejectOverduePendingAppointments(List<Appointment> appointments) {
        LocalDateTime nowKL = getNowInKualaLumpur();
        for (Appointment appt : appointments) {
            if ("PENDING".equals(appt.getStatus()) && appt.getAppointmentDateTime() != null && 
                appt.getAppointmentDateTime().isBefore(nowKL)) {
                appt.setStatus("REJECTED");
                appt.setRejectionReason("Automatically rejected - appointment time has passed");
                appt.setUpdatedAt(nowKL);
                appointmentRepository.save(appt);
            }
        }
    }

    // Get upcoming appointments for a student
    public List<Appointment> getStudentUpcomingAppointments(User student) {
        // Get ALL appointments (not just upcoming) to check for overdue APPROVED/PENDING ones
        List<Appointment> allAppointments = appointmentRepository.findByStudentOrderByAppointmentDateTimeAsc(student);
        
        // Auto-complete any APPROVED appointments that are in the past
        autoCompleteOverdueAppointments(allAppointments);
        // Auto-reject any PENDING appointments that are in the past
        autoRejectOverduePendingAppointments(allAppointments);
        
        // Return only future appointments
        LocalDateTime nowKL = getNowInKualaLumpur();
        return allAppointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().isAfter(nowKL))
                .toList();
    }

    // Get past appointments for a student
    public List<Appointment> getStudentPastAppointments(User student) {
        return appointmentRepository.findPastAppointments(student);
    }

    // Get all appointments for a counselor
    public List<Appointment> getCounselorAppointments(User counselor) {
        return appointmentRepository.findByCounselorOrderByAppointmentDateTimeAsc(counselor);
    }

    // Get upcoming appointments for a counselor
    public List<Appointment> getCounselorUpcomingAppointments(User counselor) {
        // Get ALL appointments (not just upcoming) to check for overdue APPROVED/PENDING ones
        List<Appointment> allAppointments = appointmentRepository.findByCounselorOrderByAppointmentDateTimeAsc(counselor);
        
        // Auto-complete any APPROVED appointments that are in the past
        autoCompleteOverdueAppointments(allAppointments);
        // Auto-reject any PENDING appointments that are in the past
        autoRejectOverduePendingAppointments(allAppointments);
        
        // Return only future appointments
        LocalDateTime nowKL = getNowInKualaLumpur();
        return allAppointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().isAfter(nowKL))
                .toList();
    }

    // Get pending appointments for a counselor
    public List<Appointment> getPendingAppointments(User counselor) {
        return appointmentRepository.findPendingAppointments(counselor);
    }

    // Get pending appointments for a counselor (alias)
    public List<Appointment> getCounselorPendingAppointments(User counselor) {
        return appointmentRepository.findPendingAppointments(counselor);
    }

    // Get today's appointments for a counselor
    public List<Appointment> getCounselorTodaysAppointments(User counselor) {
        List<Appointment> allAppointments = appointmentRepository.findByCounselorOrderByAppointmentDateTimeAsc(counselor);
        LocalDate today = LocalDate.now();
        // Auto-complete any overdue APPROVED appointments
        autoCompleteOverdueAppointments(allAppointments);
        // Auto-reject any overdue PENDING appointments
        autoRejectOverduePendingAppointments(allAppointments);
        return allAppointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null && 
                        a.getAppointmentDateTime().toLocalDate().equals(today) &&
                        "APPROVED".equals(a.getStatus()))
                .toList();
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

    // Get appointment by id and check if it needs auto-completion or auto-rejection
    public Optional<Appointment> getAppointmentById(Long id) {
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        
        // Auto-complete or auto-reject if needed
        if (apptOpt.isPresent()) {
            Appointment appt = apptOpt.get();
            List<Appointment> singleList = List.of(appt);
            autoCompleteOverdueAppointments(singleList);
            autoRejectOverduePendingAppointments(singleList);
            // Refresh from repository to get updated status
            apptOpt = appointmentRepository.findById(id);
        }
        
        return apptOpt;
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

    // Update appointment (for rescheduling)
    public Appointment updateAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    // Compute available slots for a counselor on a specific date
    // Returns a list of maps with keys: "time" (HH:mm) and "available" (boolean)
    public List<Map<String, Object>> getAvailableSlots(User counselor, LocalDate date) {
        List<Map<String, Object>> slots = new ArrayList<>();

        // Define fixed 45-minute slots starting each hour 09:00 through 17:00 (inclusive start times)
        LocalTime firstSlot = LocalTime.of(9, 0);
        LocalTime lastSlot = LocalTime.of(17, 0);

        // Collect counselor's appointments for the date (excluding REJECTED)
        List<Appointment> counselorAppts = appointmentRepository.findByCounselorOrderByAppointmentDateTimeAsc(counselor);
        List<LocalTime> occupied = new ArrayList<>();
        for (Appointment a : counselorAppts) {
            // Skip rejected appointments - they don't occupy the timeslot
            if ("REJECTED".equals(a.getStatus())) {
                continue;
            }
            if (a.getAppointmentDateTime() != null && a.getAppointmentDateTime().toLocalDate().equals(date)) {
                occupied.add(a.getAppointmentDateTime().toLocalTime().withSecond(0).withNano(0));
            }
        }

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime currentSlot = firstSlot;
        while (!currentSlot.isAfter(lastSlot)) {
            final LocalTime slotTime = currentSlot;
            boolean isOccupied = occupied.stream().anyMatch(t -> t.equals(slotTime));
            Map<String, Object> m = new HashMap<>();
            m.put("time", slotTime.format(tf));
            m.put("available", !isOccupied);
            slots.add(m);
            // Each appointment takes 45 minutes; we expose starts on the hour for consistency
            currentSlot = currentSlot.plusHours(1);
        }

        return slots;
    }

    // Delete appointment
    public void deleteAppointment(Long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }
}
