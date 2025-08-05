package com.adithya.trackfolio.controller;

import com.adithya.trackfolio.dto.DriveRequestDTO;
import com.adithya.trackfolio.service.DriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles endpoints for operations on drive summaries
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/drives")
public class DriveController {

    private final DriveService service;

    @PostMapping("/save")
    public void saveDrive(@RequestBody DriveRequestDTO dto) {
        log.info("called save");
        service.saveDrive(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDrive(@PathVariable Long id) {
        service.deleteDriveById(id);
        return ResponseEntity.ok("Drive deleted successfully");
    }
}
