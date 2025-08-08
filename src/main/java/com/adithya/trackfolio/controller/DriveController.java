package com.adithya.trackfolio.controller;

import com.adithya.trackfolio.dto.DriveRequestDTO;
import com.adithya.trackfolio.dto.DriveResponseDTO;
import com.adithya.trackfolio.dto.DriveWithNotesResponseDTO;
import com.adithya.trackfolio.service.DriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
        service.saveDrive(dto);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> deleteDrive(@PathVariable Long id) {
        service.deleteDriveById(id);
        return ResponseEntity.ok("Drive deleted successfully");
    }

    @GetMapping("/fetch/{id}")
    public DriveWithNotesResponseDTO getDrive(@PathVariable Long id) {
        return service.getDriveById(id);
    }

    @GetMapping("/date")
    public List<DriveResponseDTO> getDrivesByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getDrivesForDate(date);
    }

    @GetMapping("/type")
    public List<DriveResponseDTO> getDrivesByType(@RequestParam("type") String value) {
        return service.getDrivesByType(value);
    }

    @GetMapping("/find/{companyName}")
    public List<DriveResponseDTO> getDrivesByName(@PathVariable String companyName) {
        return service.getDrivesByName(companyName);
    }
}