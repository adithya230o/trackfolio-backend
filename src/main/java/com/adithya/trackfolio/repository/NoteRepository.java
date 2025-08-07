package com.adithya.trackfolio.repository;

import com.adithya.trackfolio.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    void deleteByDriveId(Long driveId);
}