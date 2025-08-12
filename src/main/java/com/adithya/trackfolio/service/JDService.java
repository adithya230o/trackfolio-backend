package com.adithya.trackfolio.service;

import com.adithya.trackfolio.entity.DriveSummary;
import com.adithya.trackfolio.entity.JD;
import com.adithya.trackfolio.repository.DriveRepository;
import com.adithya.trackfolio.repository.JDRepository;
import com.adithya.trackfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JDService {

    private final DriveRepository driveRepo;
    private final JDRepository jdRepo;
    private final UserRepository userRepo;

    /**
     * Retrieves the authenticated user's ID from the JWT context.
     */
    private Long getUserIdFromContext() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();
    }

    /**
     * validates if drive id belongs to the user
     */
    private void validateDriveOwnership(Long driveId) {
        Long userId = getUserIdFromContext();

        DriveSummary drive = driveRepo.findById(driveId)
                .orElseThrow(() -> {
                    log.warn("Drive not found for JD operation, id: {}", driveId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Drive not found");
                });

        if (!drive.getUserId().equals(userId)) {
            log.warn("Drive {} does not belong to user {}", driveId, userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to access this drive");
        }
    }

    /**
     * Saves or updates the JD text by extracting it from a PDF upload.
     * Validates drive ownership before processing.
     */
    @Transactional
    public String saveOrUpdateJDFromPdf(Long driveId, MultipartFile pdfFile) throws IOException {
        validateDriveOwnership(driveId);
        String extractedText = extractTextFromPdf(pdfFile);
        saveOrUpdateJDText(driveId, extractedText);
        return extractedText;
    }

    /**
     * Extracts raw text content from a PDF file using Apache PDFBox.
     */
    private String extractTextFromPdf(MultipartFile pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Saves or updates JD text from the text provided by user.
     * - Validates drive ownership before updating.
     */
    @Transactional
    public void saveOrUpdateJDText(Long driveId, String text) {
        validateDriveOwnership(driveId);

        Optional<JD> existing = jdRepo.findByDriveId(driveId);

        JD jd = existing.orElseGet(JD::new);
        jd.setDriveId(driveId);
        jd.setJdText(text);

        jdRepo.save(jd);
        log.info("Saved JD text for drive id {}", driveId);
    }

    /**
     * Retrieves the JD text for a given drive ID.
     * Validates drive ownership before fetching.
     * Returns Optional.empty() if no JD record exists for the drive.
     */
    public Optional<JD> getJDByDriveId(Long driveId) {
        validateDriveOwnership(driveId);
        return jdRepo.findByDriveId(driveId);
    }
}