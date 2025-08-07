package com.adithya.trackfolio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteDTO {
    private Long id;
    private String content;
    private boolean pinned;
    private boolean completed;
}
