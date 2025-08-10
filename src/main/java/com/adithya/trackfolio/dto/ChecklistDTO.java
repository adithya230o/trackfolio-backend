package com.adithya.trackfolio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistDTO {
    private Long id;
    private String content;
    private boolean completed;
}
