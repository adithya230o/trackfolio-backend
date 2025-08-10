package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.NoteDTO;
import com.adithya.trackfolio.entity.DriveSummary;
import com.adithya.trackfolio.entity.Note;
import com.adithya.trackfolio.repository.DriveRepository;
import com.adithya.trackfolio.repository.NoteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing notes associated with a specific drive
 * Supports creation, update (via replace), and deletion operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final DriveRepository driveRepo;
    private final NoteRepository noteRepo;

    /**
     * Deletes any existing notes and saves the provided list of notes
     *
     * @param driveId  ID of the drive whose notes are to be replaced
     * @param noteDTOs List of note DTOs containing new or updated content
     */
    @Transactional
    protected void saveOrUpdateNotes(Long driveId, List<NoteDTO> noteDTOs) {

        // Remove all existing notes linked to this drive
        noteRepo.deleteByDriveId(driveId);
        log.info("Removed existing notes for drive {}", driveId);

        // Transform incoming DTOs into Note entities
        // Link them to the drive
        List<Note> notes = noteDTOs.stream().map(dto -> {
            Note note = new Note();
            note.setContent(dto.getContent());
            note.setCompleted(dto.isCompleted());

            // Maps notes table to driveSummary
            if (!driveRepo.existsById(driveId)) {
                log.warn("Drive with id {} not found", driveId);
                throw new EntityNotFoundException("Drive with id " + driveId + " not found");
            }
            DriveSummary drive = driveRepo.getReferenceById(driveId);
            note.setDrive(drive);

            return note;
        }).toList();

        noteRepo.saveAll(notes);
        log.info("Notes saved for drive {}", driveId);
    }

    /**
     * Retrieves all notes linked to the specified drive.
     * Used to populate detailed drive views with associated note data.
     *
     * @param driveId ID of the drive whose notes are to be fetched
     * @return List of note DTOs
     */
    public List<NoteDTO> getNotesByDriveId(Long driveId) {
        List<Note> notes = noteRepo.findByDriveId(driveId);
        return notes.stream().map(note -> NoteDTO.builder()
                .content(note.getContent())
                .completed(note.isCompleted())
                .build()
        ).toList();
    }
}