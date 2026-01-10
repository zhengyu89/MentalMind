package com.example.MentalMind.repository;

import com.example.MentalMind.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByIsActiveTrue();

    List<Resource> findByTypeAndIsActiveTrue(String type);

    List<Resource> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title);

    List<Resource> findByCategoryAndIsActiveTrue(String category);

    List<Resource> findByCategoryInAndIsActiveTrue(List<String> categories);
}