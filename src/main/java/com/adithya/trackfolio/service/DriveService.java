package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.*;
import com.adithya.trackfolio.entity.DriveSummary;
import com.adithya.trackfolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Handles the service logic for dDriveSummary operations such as creation, update, and deletion.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepo;
    private final UserRepository userRepo;
    private final NoteService noteService;
    private final ChecklistService checklistService;
    private final NoteRepository noteRepository;
    private final ChecklistRepository checklistRepository;
    private final JDRepository jdRepository;

    /**
     * Retrieves the authenticated user's ID from the JWT context.
     *
     * @return the user ID associated with the authenticated email
     * @throws ResponseStatusException if the user is not found
     */
    private Long getUserIdFromContext() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();
    }

    /**
     * Creates new drive if isUpdate is false
     * Updates old drive details if isUpdate is true
     *
     * @param dto : Drive details
     */
    public void saveDrive(DriveRequestDTO dto) {

        Long userId = getUserIdFromContext();

        //update drive details
        if (dto.getIsUpdate() != null && dto.getIsUpdate()) {

            // check if drive exists
            DriveSummary existing = driveRepo.findById(dto.getDriveId())
                    .orElseThrow(() -> {
                        log.warn("Drive not found for (Save drive)");
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
                    });

            // validate that the drive belongs to the current user
            if (!existing.getUserId().equals(userId)) {
                log.warn("Drive doesnt belong to the user for (Save drive)");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this drive");
            }

            existing.setCompanyName(dto.getCompanyName());
            existing.setRole(dto.getRole());
            existing.setDriveDatetime(dto.getDriveDatetime());
            existing.setOnCampus(dto.getIsOnCampus());

            driveRepo.save(existing);
            noteService.saveOrUpdateNotes(existing.getId(), dto.getNotes());
            log.info("Drive updated with notes");
            checklistService.saveOrUpdateChecklists(existing.getId(), dto.getChecklists());
            log.info("Drive updated with checklists");
        } else {
            log.info("Creating a new drive...");
            DriveSummary newDrive = DriveSummary.builder()
                    .userId(userId)
                    .companyName(dto.getCompanyName())
                    .role(dto.getRole())
                    .driveDatetime(dto.getDriveDatetime())
                    .isOnCampus(dto.getIsOnCampus())
                    .build();

            DriveSummary savedDrive = driveRepo.save(newDrive);
            noteService.saveOrUpdateNotes(savedDrive.getId(), dto.getNotes());
            checklistService.saveOrUpdateChecklists(savedDrive.getId(), dto.getChecklists());
            log.info("New drive created with notes and checklists");
        }
    }

    /**
     * Deletes a drive belonging to the authenticated user.
     *
     * @param driveId ID of the drive to delete
     * @throws ResponseStatusException if drive is not found or unauthorized
     */
    @Transactional
    public void deleteDriveById(Long driveId) {
        Long userId = getUserIdFromContext();  // Extracted from email in SecurityContext

        // check if drive exists
        DriveSummary drive = driveRepo.findById(driveId)
                .orElseThrow(() -> {
                    log.warn("Drive not found for (Delete drive) : ", driveId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
                });

        // validate that the drive belongs to the current user
        if (!drive.getUserId().equals(userId)) {
            log.warn("Drive doesnt belong to the user for (Delete drive)");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this drive");
        }

        // Delete JD
        jdRepository.findByDriveId(driveId).ifPresent(jd -> jdRepository.delete(jd));

        // Delete notes
        noteRepository.deleteByDriveId(driveId);

        // Delete checklist items
        checklistRepository.deleteByDriveId(driveId);

        // Delete drive summary
        driveRepo.deleteById(driveId);
        log.info("Drive deleted");
    }

    /**
     * Fetches a drive's details for the authenticated user.
     *
     * @param id ID of the drive to fetch
     * @return DTO containing drive details
     * @throws ResponseStatusException if drive is not found or unauthorized
     */
    public DriveDetailsResponseDTO getDriveDetailsById(Long id) {
        Long userId = getUserIdFromContext();

        DriveSummary drive = driveRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Drive not found for (get drive by Id)");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
                });

        if (!drive.getUserId().equals(userId)) {
            log.warn("Drive doesnt belong to the user for (get drive by Id)");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to view this drive");
        }

        List<NoteDTO> notes = noteService.getNotesByDriveId(id);
        List<ChecklistDTO> checklists = checklistService.getChecklistsByDriveId(id);

        log.info("Returning the drive based on drive id");

        return DriveDetailsResponseDTO.builder()
                .id(drive.getId())
                .companyName(drive.getCompanyName())
                .role(drive.getRole())
                .driveDatetime(drive.getDriveDatetime())
                .isOnCampus(drive.isOnCampus())
                .notes(notes)
                .checklists(checklists)
                .build();
    }

    // Converts DriveSummary entity to response DTO
    private DriveResponseDTO toDto(DriveSummary d) {
        return DriveResponseDTO.builder()
                .id(d.getId())
                .companyName(d.getCompanyName())
                .role(d.getRole())
                .driveDatetime(d.getDriveDatetime())
                .isOnCampus(d.isOnCampus())
                .build();
    }

    /**
     * Fetches the details of all the drives on a given date
     *
     * @param date : date for which the drives have to be fetched
     * @return : A list of DTOs containing drive details. Return empty list if there are no drives on that date
     */
    public List<DriveResponseDTO> getDrivesForDate(LocalDate date) {
        Long userId = getUserIdFromContext();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        log.info("Returning the list of drives scheduled on {}", date);

        return driveRepo.findByUserIdAndDriveDatetimeBetween(userId, start, end).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Fetches all drives for the current user based on the specified type.
     *
     * @param type : "completed" to fetch drives that have already occurred,
     *             "nextup" to fet drives scheduled for today and tomorrow
     *             "upcoming" to fetch drives scheduled for future dates.
     * @return : A list of DTOs containing drive details.
     * @throws ResponseStatusException if the type is invalid.
     */
    public List<DriveResponseDTO> getDrivesByType(String type) {
        Long userId = getUserIdFromContext();
        LocalDateTime now = LocalDateTime.now();
        List<DriveSummary> drives;

        switch (type.toLowerCase()) {
            case "nextup" -> {
                LocalDateTime start = LocalDate.now().atStartOfDay();
                LocalDateTime end = LocalDate.now().plusDays(2).atStartOfDay(); // includes today and tomorrow
                drives = driveRepo.findByUserIdAndDriveDatetimeBetween(userId, start, end);
            }
            case "upcoming" -> {
                LocalDateTime afterTomorrow = LocalDate.now().plusDays(2).atStartOfDay();
                drives = driveRepo.findByUserIdAndDriveDatetimeAfter(userId, afterTomorrow);
            }
            case "completed" -> {
                LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
                drives = driveRepo.findByUserIdAndDriveDatetimeBefore(userId, startOfToday);
            }
            default -> {
                log.warn("Invalid drive type '{}' requested by user {}", type, userId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type must be 'nextup', 'upcoming' or 'completed'");
            }
        }

        log.info("Returning the list of drives filtered on the type : {}", type);

        return drives.stream()
                .sorted(Comparator.comparing(DriveSummary::getDriveDatetime))
                .map(this::toDto)
                .toList();
    }

    /**
     * Fetches the drives by the name of the Company hiring.
     *
     * @param companyName : Name of the Company hiring, for which the drives have to be fetched
     * @return : List DTOs containing drive details
     */
    public List<DriveResponseDTO> getDrivesByName(String companyName) {
        Long userId = getUserIdFromContext();

        List<DriveSummary> drives = driveRepo.findByUserIdAndCompanyName(userId, companyName);

        log.info("Returning the list of drives filtered on the company name : {}", companyName);

        return drives.stream()
                .sorted(Comparator.comparing(DriveSummary::getDriveDatetime))
                .map(this::toDto)
                .toList();
    }
}