package com.adithya.trackfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriveRequestDTO {
    private Boolean isUpdate;
    private Long driveId;
    private String companyName;
    private String role;
    private LocalDateTime driveDatetime;
    private Boolean isOnCampus;
    private List<NoteDTO> notes;
    private List<ChecklistDTO> checklists;
}
