package com.adithya.trackfolio.repository;

import com.adithya.trackfolio.entity.JD;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JDRepository extends JpaRepository<JD, Long> {

    Optional<JD> findByDriveId(Long driveId);
}