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
        "notes"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriveWithNotesResponseDTO {
    private Long id;
    private String companyName;
    private String role;
    private LocalDateTime driveDatetime;
    private boolean isOnCampus;

    private List<NoteDTO> notes;
}
