package com.adithya.trackfolio.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JDTextResponseDto {
    private String jdText;

    public JDTextResponseDto(String jdText) {
        this.jdText = jdText;
    }
}