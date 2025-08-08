package com.adithya.trackfolio.repository;

import com.adithya.trackfolio.entity.DriveSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DriveRepository extends JpaRepository<DriveSummary, Long> {

    List<DriveSummary> findByUserIdAndDriveDatetimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<DriveSummary> findByUserIdAndDriveDatetimeAfter(Long userId, LocalDateTime now);

    List<DriveSummary> findByUserIdAndDriveDatetimeBefore(Long userId, LocalDateTime now);

    List<DriveSummary> findByUserIdAndCompanyName(Long id, String companyName);
}