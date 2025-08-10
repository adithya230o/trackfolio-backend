package com.adithya.trackfolio.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({
        "id",
        "companyName",
        "role",
        "driveDatetime",
        "onCampus",
        "notes",
        "checklists"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriveDetailsResponseDTO {
    private Long id;
    private String companyName;
    private String role;
    private LocalDateTime driveDatetime;
    private boolean isOnCampus;

    private List<NoteDTO> notes;
    private List<ChecklistDTO> checklists;
}
