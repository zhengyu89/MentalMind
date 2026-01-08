package com.example.MentalMind.repository;

import com.example.MentalMind.model.CounselorSettings;
import com.example.MentalMind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounselorSettingsRepository extends JpaRepository<CounselorSettings, Long> {

    Optional<CounselorSettings> findByCounselor(User counselor);

    Optional<CounselorSettings> findByCounselorId(Long counselorId);
}
