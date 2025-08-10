package com.adithya.trackfolio.repository;

import com.adithya.trackfolio.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    void deleteByDriveId(Long driveId);

    List<Checklist> findByDriveId(Long driveId);
}