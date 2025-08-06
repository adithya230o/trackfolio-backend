package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.DriveRequestDTO;
import com.adithya.trackfolio.dto.DriveResponseDTO;
import com.adithya.trackfolio.entity.DriveSummary;
import com.adithya.trackfolio.repository.DriveRepository;
import com.adithya.trackfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            log.info("Drive saved successfully");
        } else {
            log.info("Creating a new drive...");
            DriveSummary newDrive = DriveSummary.builder()
                    .userId(userId)
                    .companyName(dto.getCompanyName())
                    .role(dto.getRole())
                    .driveDatetime(dto.getDriveDatetime())
                    .isOnCampus(dto.getIsOnCampus())
                    .build();

            driveRepo.save(newDrive);
            log.info("New Drive created successfully");
        }
    }

    /**
     * Deletes a drive belonging to the authenticated user.
     *
     * @param driveId ID of the drive to delete
     * @throws ResponseStatusException if drive is not found or unauthorized
     */
    public void deleteDriveById(Long driveId) {
        Long userId = getUserIdFromContext();  // Extracted from email in SecurityContext

        // check if drive exists
        DriveSummary drive = driveRepo.findById(driveId)
                .orElseThrow(() -> {
                    log.warn("Drive not found for (Delete drive)");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
                });

        // validate that the drive belongs to the current user
        if (!drive.getUserId().equals(userId)) {
            log.warn("Drive doesnt belong to the user for (Delete drive)");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this drive");
        }

        driveRepo.deleteById(driveId);
    }

    /**
     * Fetches a drive's details for the authenticated user.
     *
     * @param id ID of the drive to fetch
     * @return DTO containing drive details
     * @throws ResponseStatusException if drive is not found or unauthorized
     */
    public DriveResponseDTO getDriveById(Long id) {
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

        return toDto(drive);
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

        return driveRepo.findByUserIdAndDriveDatetimeBetween(userId, start, end).stream()
                .map(this::toDto)
                .toList();
    }
}