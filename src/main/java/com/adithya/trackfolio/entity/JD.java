package com.adithya.trackfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "jd_details")
public class JD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drive_id", nullable = false, unique = true)
    private Long driveId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String jdText;
}