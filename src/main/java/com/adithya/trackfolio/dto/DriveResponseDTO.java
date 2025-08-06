package com.adithya.trackfolio.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriveResponseDTO {
    private Long id;
    private String companyName;
    private String role;
    private LocalDateTime driveDatetime;
    private boolean isOnCampus;
}

