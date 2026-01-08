package com.example.MentalMind.repository;

import com.example.MentalMind.model.StudentSettings;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentSettingsRepository extends JpaRepository<StudentSettings, Long> {

    Optional<StudentSettings> findByStudent(User student);

    Optional<StudentSettings> findByStudentId(Long studentId);
}
