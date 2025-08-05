package com.adithya.trackfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "drive_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriveSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String companyName;
    private String role;

    private LocalDateTime driveDatetime;

    private boolean isOnCampus;
}
