package com.adithya.trackfolio.repository;

import com.adithya.trackfolio.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    void deleteByDriveId(Long driveId);

    List<Note> findByDriveId(Long driveId);
}