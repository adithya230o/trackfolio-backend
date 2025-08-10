package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.ChecklistDTO;
import com.adithya.trackfolio.entity.Checklist;
import com.adithya.trackfolio.entity.DriveSummary;
import com.adithya.trackfolio.repository.ChecklistRepository;
import com.adithya.trackfolio.repository.DriveRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing checklist associated with a specific drive
 * Supports creation, update (via replace), and deletion operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChecklistService {

    private final DriveRepository driveRepo;
    private final ChecklistRepository checklistRepo;

    /**
     * Deletes any existing checklist and saves the provided list of checklist
     *
     * @param driveId      ID of the drive whose checklist are to be replaced
     * @param checklistDTO List of checklist DTOs containing new or updated content
     */
    @Transactional
    protected void saveOrUpdateChecklists(Long driveId, List<ChecklistDTO> checklistDTO) {

        // Remove all existing checklists linked to this drive
        checklistRepo.deleteByDriveId(driveId);
        log.info("Removed existing checklists for drive {}", driveId);

        // Transform incoming DTOs into checklist entities
        // Link them to the drive
        List<Checklist> checklists = checklistDTO.stream().map(dto -> {
            Checklist checklistObj = new Checklist();
            checklistObj.setContent(dto.getContent());
            checklistObj.setCompleted(dto.isCompleted());

            // Maps checklist table to driveSummary
            if (!driveRepo.existsById(driveId)) {
                log.warn("Drive with id {} not found", driveId);
                throw new EntityNotFoundException("Drive with id " + driveId + " not found");
            }
            DriveSummary drive = driveRepo.getReferenceById(driveId);
            checklistObj.setDrive(drive);

            return checklistObj;
        }).toList();

        checklistRepo.saveAll(checklists);
        log.info("Checklists saved for drive {}", driveId);
    }

    /**
     * Retrieves all checklist linked to the specified drive.
     * Used to populate detailed drive views with associated checklist data.
     *
     * @param driveId ID of the drive whose checklists are to be fetched
     * @return List of checklist DTOs
     */
    public List<ChecklistDTO> getChecklistsByDriveId(Long driveId) {

        //list (entities) -> checklistDTO -> list of DTOs
        List<Checklist> checklists = checklistRepo.findByDriveId(driveId);
        return checklists.stream().map(checklistObj -> ChecklistDTO.builder()
                .content(checklistObj.getContent())
                .completed(checklistObj.isCompleted())
                .build()
        ).toList();
    }
}