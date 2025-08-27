package com.adithya.trackfolio.controller;

import com.adithya.trackfolio.dto.*;
import com.adithya.trackfolio.entity.JD;
import com.adithya.trackfolio.service.AuthService;
import com.adithya.trackfolio.service.DriveService;
import com.adithya.trackfolio.service.JDService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Handles endpoints for operations on drive summaries
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/drives")
public class DriveController {

    private final DriveService driveService;
    private final JDService jdService;
    private final AuthService authService;

    @PostMapping("/save")
    public ResponseEntity<Void> saveDrive(@RequestBody DriveRequestDTO dto) {
        driveService.saveDrive(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> deleteDrive(@PathVariable Long id) {
        driveService.deleteDriveById(id);
        return ResponseEntity.ok("Drive deleted successfully");
    }

    @GetMapping("/fetch/{id}")
    public DriveDetailsResponseDTO getDrive(@PathVariable Long id) {
        return driveService.getDriveDetailsById(id);
    }

    @GetMapping("/date")
    public List<DriveResponseDTO> getDrivesByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return driveService.getDrivesForDate(date);
    }

    @GetMapping("/type")
    public List<DriveResponseDTO> getDrivesByType(@RequestParam("type") String value) {
        return driveService.getDrivesByType(value);
    }

    @GetMapping("/find/{companyName}")
    public List<DriveResponseDTO> getDrivesByName(@PathVariable String companyName) {
        return driveService.getDrivesByName(companyName);
    }

    /**
     * Uploads a Job Description PDF, extracts its text, and stores it for the specified drive.
     *
     * @param driveId ID of the drive to associate the JD with
     * @param file    PDF file containing the JD
     * @return the extracted JD text
     */
    @PostMapping("/pdf/{driveId}")
    public ResponseEntity<JDTextResponseDto> uploadJDFromPdf(
            @PathVariable Long driveId,
            @RequestParam("file") MultipartFile file) {
        try {
            String extractedText = jdService.saveOrUpdateJDFromPdf(driveId, file);
            return ResponseEntity.ok(new JDTextResponseDto(extractedText));
        } catch (IOException e) {
            log.error("Failed to extract text from PDF for drive {}", driveId, e);
            return ResponseEntity.status(500)
                    .body(new JDTextResponseDto("Failed to process PDF"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new JDTextResponseDto(e.getMessage()));
        }
    }

    /**
     * Saves plain-text JD content for the specified drive.
     *
     * @param driveId ID of the drive to associate the JD with
     * @param request JD content
     * @return stored JD text
     */
    @PostMapping("/text/{driveId}")
    public ResponseEntity<JDTextResponseDto> saveJDText(
            @PathVariable Long driveId,
            @RequestBody JDTextRequestDto request) {
        try {
            jdService.saveOrUpdateJDText(driveId, request.getJdText());
            return ResponseEntity.ok(new JDTextResponseDto(request.getJdText()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new JDTextResponseDto(e.getMessage()));
        }
    }

    /**
     * Retrieves plain-text JD content for the specified drive.
     *
     * @param driveId ID of the drive to retrieve the JD for
     * @return stored JD text
     */
    @GetMapping("/text/{driveId}")
    public ResponseEntity<JDTextResponseDto> getJDText(@PathVariable Long driveId) {
        try {
            Optional<JD> jdOpt = jdService.getJDByDriveId(driveId);
            if (jdOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new JDTextResponseDto(null));
            }
            return ResponseEntity.ok(new JDTextResponseDto(jdOpt.get().getJdText()));
        } catch (Exception e) {
            log.error("Error retrieving JD text for driveId {}: {}", driveId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new JDTextResponseDto(null));
        }
    }

    /**
     * Endpoint to delete a user account
     *
     * @return Response entity on a successful operation
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount() {
        authService.deleteAccount();
        return ResponseEntity.ok().build();
    }
}