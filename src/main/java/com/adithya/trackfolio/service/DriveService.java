package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.DriveRequestDTO;
import com.adithya.trackfolio.entity.DriveSummary;
import com.adithya.trackfolio.repository.DriveRepository;
import com.adithya.trackfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        if (dto.getIsUpdate() != null && dto.getIsUpdate()) {
            DriveSummary existing = driveRepo.findById(dto.getDriveId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));

            if (!existing.getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this drive");
            }

            existing.setCompanyName(dto.getCompanyName());
            existing.setRole(dto.getRole());
            existing.setDriveDatetime(dto.getDriveDatetime());
            existing.setOnCampus(dto.getIsOnCampus());

            driveRepo.save(existing);
        } else {
            log.info("called next method");
            DriveSummary newDrive = DriveSummary.builder()
                    .userId(userId)
                    .companyName(dto.getCompanyName())
                    .role(dto.getRole())
                    .driveDatetime(dto.getDriveDatetime())
                    .isOnCampus(dto.getIsOnCampus())
                    .build();

            driveRepo.save(newDrive);
        }
    }

    /**
     * Deletes the drive summary
     *
     * @param driveId : the ID of the drive to be deleted
     */
    public void deleteDriveById(Long driveId) {
        Long userId = getUserIdFromContext();  // Extracted from email in SecurityContext

        // 1. Check if drive exists
        DriveSummary drive = driveRepo.findById(driveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found"));

        // 2. Validate that the drive belongs to the current user
        if (!drive.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this drive");
        }

        // 3. Delete the drive
        driveRepo.deleteById(driveId);
    }
}
